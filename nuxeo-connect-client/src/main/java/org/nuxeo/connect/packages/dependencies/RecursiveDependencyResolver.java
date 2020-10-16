/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 */
package org.nuxeo.connect.packages.dependencies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.PackageManager;
import org.nuxeo.connect.platform.PlatformId;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.PackageVersionRange;
import org.nuxeo.connect.update.Version;

/**
 * This is the "heart" of this dumb resolution system For each possible {@link DependencySet} it checks if it matches
 * the constraints. If yes it verifies that installation can be done without breaking already installed packages. The
 * update checks is for now very limited because it does not re-run the complete resolution system.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class RecursiveDependencyResolver {

    protected String packageId;

    protected PlatformId targetPlatform;

    protected PackageManager pm;

    protected boolean resolved = false;

    protected DependencyResolution resolution;

    protected List<DependencyResolution> fallBacks = new ArrayList<DependencyResolution>();

    protected Map<String, List<Version>> deps = new HashMap<String, List<Version>>();

    protected List<String> orderedPackages = new ArrayList<String>();

    protected List<DownloadablePackage> installedPackages;

    public RecursiveDependencyResolver(String packageId, PackageManager pm, PlatformId targetPlatform) {
        this.packageId = packageId;
        this.pm = pm;
        this.targetPlatform = targetPlatform;
    }

    public void sort() {
        for (String pkgName : deps.keySet()) {
            List<Version> versions = deps.get(pkgName);

            List<Version> orderedVersion = pm.getPreferedVersions(pkgName);
            Collections.reverse(orderedVersion);
            for (Version v : orderedVersion) {
                if (versions.contains(v)) {
                    versions.remove(v);
                    versions.add(0, v);
                }
            }
        }
    }

    public DependencyResolution tryResolve() {
        resolved = false;
        for (String pkgName : deps.keySet()) {
            for (Version v : deps.get(pkgName)) {
                DependencySet set = new DependencySet(deps.keySet());
                if (!resolved) {
                    buildDependencySet(set, pkgName, v);
                }
            }
        }
        if (resolved) {
            return resolution;
        } else {
            if (fallBacks.size() > 0) {
                DependencyResolution fbRes = fallBacks.get(0); // XXX choose the
                                                               // best one
                fbRes.markAsSuccess();
                return fbRes;
            }
            return null;
        }
    }

    protected void buildDependencySet(DependencySet set, String packageName, Version v) {
        set.set(packageName, v);
        if (!resolved) {
            if (!set.isComplete()) {
                String pkgName = set.getNextPackageName();
                for (Version v2 : deps.get(pkgName)) {
                    buildDependencySet(set.clone(), pkgName, v2);
                }
            } else {
                resolution = resolve(set);
                if (resolution.isValidated()) {
                    // we now have a working solution
                    // but we need to check if it requires an update of the
                    // already installed packages
                    UpdateCheckResult updateRes = checkForUpdates(set);
                    if (!updateRes.requireUpdate) {
                        resolved = true;
                    } else {
                        if (!updateRes.isUpdatePossible()) {
                            resolution.markAsFailed(
                                    "Update impossible for " + updateRes.getLastUpdateImpossiblePkgName());
                            for (DownloadablePackage pkg : updateRes.getPackagesToRemove()) {
                                resolution.markPackageForRemoval(pkg.getName(), pkg.getVersion());
                            }
                            fallBacks.add(resolution);
                        } else {
                            if (updateRes.isTransparentUpdate()) {
                                for (DownloadablePackage pkg : updateRes.getPackagesToAdd()) {
                                    resolution.addPackage(pkg.getName(), pkg.getVersion());
                                }
                                resolution.sort(pm);
                                resolved = true;
                            } else {
                                // XXXX
                            }
                        }
                    }
                }
            }
        }
    }

    protected List<DownloadablePackage> getInstalledPackages() {
        if (installedPackages == null) {
            installedPackages = pm.listInstalledPackages();
        }
        return installedPackages;
    }

    /**
     * check if one of the dependency chosen implies to upgrade a locally installed package
     */
    protected UpdateCheckResult checkForUpdates(DependencySet set) {

        UpdateCheckResult result = new UpdateCheckResult();

        Map<String, List<DownloadablePackage>> toUpdatePackageNames = new HashMap<String, List<DownloadablePackage>>();

        for (DownloadablePackage pkg : getInstalledPackages()) {
            for (PackageDependency dep : pkg.getDependencies()) {
                if (set.getTargetVersion(dep.getName()) != null) {
                    if (!dep.getVersionRange().matchVersion(set.getTargetVersion(dep.getName()))) {
                        // oops : we need to upgrade
                        result.setRequireUpdate(true);
                        // first check if this is possible
                        List<DownloadablePackage> possibleUpdates = toUpdatePackageNames.get(pkg.getName());
                        if (possibleUpdates == null) {
                            List<DownloadablePackage> unfiltredPossibleUpdates = pm.findRemotePackages(pkg.getName());
                            possibleUpdates = new ArrayList<DownloadablePackage>();
                            for (DownloadablePackage pup : unfiltredPossibleUpdates) {
                                if (TargetPlatformFilterHelper.isCompatibleWithTargetPlatform(pup, targetPlatform)) {
                                    possibleUpdates.add(pup);
                                }
                            }
                        }
                        // find possible candidate versions
                        List<DownloadablePackage> filtredPossibleUpdates = new ArrayList<DownloadablePackage>();
                        for (DownloadablePackage pupdate : possibleUpdates) {
                            for (PackageDependency newDep : pupdate.getDependencies()) {
                                if (newDep.getName().equals(dep.getName())) {
                                    if (newDep.getVersionRange().matchVersion(set.getTargetVersion(dep.getName()))) {
                                        filtredPossibleUpdates.add(pupdate);
                                    }
                                }
                            }
                        }

                        if (filtredPossibleUpdates.size() == 0) {
                            result.setUpdatePossible(pkg.getName(), false);
                            result.addPackageToRemove(pkg);
                        } else {
                            result.setUpdatePossible(pkg.getName(), true);
                            toUpdatePackageNames.put(pkg.getName(), filtredPossibleUpdates);
                        }
                    }
                }
            }
        }

        if (toUpdatePackageNames.size() == 0) {
            return result;
        }

        if (!result.isUpdatePossible()) {
            return result;
        }

        // try to solve package upgrade
        List<DownloadablePackage> choosenPackgesToUpdate = new ArrayList<DownloadablePackage>();
        Map<String, PackageVersionRange> dependencyUpdates = new HashMap<String, PackageVersionRange>();
        // run through all packages to update to choose right version and gather
        // deps
        for (String pkgName : toUpdatePackageNames.keySet()) {
            Map<String, PackageVersionRange> oneDependencyUpdatesOptimal = new HashMap<String, PackageVersionRange>();
            // see what versions are available
            for (DownloadablePackage pkg : toUpdatePackageNames.get(pkgName)) {
                Map<String, PackageVersionRange> oneDependencyUpdates = new HashMap<String, PackageVersionRange>();
                // check dependencies
                for (PackageDependency dep : pkg.getDependencies()) {
                    if (set.getTargetVersion(dep.getName()) == null) {
                        // this version needs a new dep
                        oneDependencyUpdates.put(dep.getName(), dep.getVersionRange());
                    } else {
                        if (!dep.getVersionRange().matchVersion(set.getTargetVersion(dep.getName()))) {
                            // this version needs to update an existing dep ...
                            oneDependencyUpdates.put(dep.getName(), dep.getVersionRange());
                        } else {
                            // no need to update : all good
                        }
                    }
                }
                if (oneDependencyUpdates.size() <= oneDependencyUpdatesOptimal.size()) {
                    oneDependencyUpdatesOptimal = oneDependencyUpdates;
                    choosenPackgesToUpdate.add(pkg);
                }
            }
            dependencyUpdates.putAll(oneDependencyUpdatesOptimal);
        }

        if (dependencyUpdates.size() == 0) {
            // magic ! : we can directly update
            result.setTransparentUpdate();
            for (DownloadablePackage pkg : choosenPackgesToUpdate) {
                result.addPackage(pkg);
            }
        } else {
            // XXX TODO rerun resolution
        }

        return result;
    }

    public void addDep(String pkgName, List<Version> versions) {
        if (!deps.containsKey(pkgName)) {
            deps.put(pkgName, versions);
        } else {
            List<Version> existingVersions = deps.get(pkgName);
            for (Version v : versions) {
                if (!existingVersions.contains(v)) {
                    existingVersions.add(v);
                }
            }
        }
        orderedPackages.remove(pkgName);
        orderedPackages.add(pkgName);
    }

    protected DependencyResolution resolve(DependencySet depSet) {
        return resolve(packageId, depSet);
    }

    /**
     * check if the Dependency set satisfies all contracts
     */
    protected DependencyResolution resolve(String pkgId, DependencySet depSet) {
        DependencyResolution res = new DependencyResolution();
        Package pkg = pm.getPackage(pkgId);
        recurseResolve(pkg, res, depSet);
        if (!res.isFailed()) {
            res.markAsSuccess();
        }
        return res;
    }

    protected void recurseResolve(Package pkg, DependencyResolution res, DependencySet depSet) {
        res.addPackage(pkg.getName(), pkg.getVersion());
        for (PackageDependency dep : pkg.getDependencies()) {
            Version targetVersion = depSet.getTargetVersion(dep.getName());
            if (!dep.getVersionRange().matchVersion(targetVersion)) {
                res.markAsFailed(dep.toString() + " doesn't match " + targetVersion);
                return;
            } else {
                if (!res.addPackage(dep.getName(), targetVersion)) {
                    return;
                }
            }
            Package subPkg = pm.findPackageById(dep.getName() + "-" + targetVersion.toString());
            recurseResolve(subPkg, res, depSet);
        }
    }

    public int getMaxPossibilities() {
        int res = 1;
        for (String pkgName : deps.keySet()) {
            res = res * deps.get(pkgName).size();
        }
        return res;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (String pkgName : deps.keySet()) {
            sb.append(pkgName);
            sb.append(" : ");
            for (Version v : deps.get(pkgName)) {
                sb.append(v.toString());
                sb.append(", ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public List<String> getOrderedPackages() {
        return orderedPackages;
    }

}
