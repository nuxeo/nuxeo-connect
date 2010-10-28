package org.nuxeo.connect.packages.dependencies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.PackageManagerImpl;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.Version;
import org.nuxeo.connect.update.VersionRange;

public class DependencyChoicesResolver {

    protected String packageId;

    protected String targetPlatform;

    protected PackageManagerImpl pm;

    protected boolean resolved=false;

    protected DependencyResolution resolution;

    protected List<DependencyResolution> fallBacks= new ArrayList<DependencyResolution>();

    protected Map<String, List<Version>> deps = new HashMap<String, List<Version>>();

    protected List<DownloadablePackage> installedPackages;

    public DependencyChoicesResolver(String packageId, PackageManagerImpl pm, String targetPlatform) {
        this.packageId=packageId;
        this.pm=pm;
        this.targetPlatform=targetPlatform;
    }

    public void sort(PackageManagerImpl pm) {
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
        resolved=false;
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
            if (fallBacks.size()>0) {
                DependencyResolution fbRes = fallBacks.get(0); // XXX choose the best one
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
                    // but we need to check if it requires an update of the already installed packages
                    UpdateCheckResult updateRes = checkForUpdates(set);
                    if (!updateRes.requireUpdate) {
                        resolved=true;
                    } else {
                        if (!updateRes.isUpdatePossible()) {
                            resolution.markAsFailed();
                            for (DownloadablePackage pkg : updateRes.getPackagesToRemove()) {
                                resolution.markPackageForRemoval(pkg.getName(), pkg.getVersion());
                            }
                            fallBacks.add(resolution);
                        } else {
                            if (updateRes.isTransparentUpdate()) {
                                for(DownloadablePackage pkg : updateRes.getPackagesToAdd()) {
                                    resolution.addPackage(pkg.getName(), pkg.getVersion());
                                }
                                resolution.sort(pm);
                                resolved=true;
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
        if (installedPackages==null) {
            installedPackages = pm.listInstalledPackages();
        }
        return installedPackages;
    }

    // check if one of the dependecy choosen imply to upgrade a local installed package
    protected UpdateCheckResult checkForUpdates(DependencySet set) {

        UpdateCheckResult result = new UpdateCheckResult();

        Map<String, List<DownloadablePackage>> toUpdatePackageNames = new HashMap<String, List<DownloadablePackage>>();

        for (DownloadablePackage pkg : getInstalledPackages()) {
            for (PackageDependency dep : pkg.getDependencies()) {
                if (set.getTargetVersion(dep.getName())!=null) {
                    if (!dep.getVersionRange().matchVersion(set.getTargetVersion(dep.getName()))) {
                        // oops : we need to upgrade
                        result.setRequireUpdate(true);
                        // first check if this is possible
                        List<DownloadablePackage> possibleUpdates = toUpdatePackageNames.get(pkg.getName());
                        if (possibleUpdates==null) {
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

                        if (filtredPossibleUpdates.size()==0) {
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

        if (toUpdatePackageNames.size()==0) {
            return result;
        }

        if (!result.isUpdatePossible()) {
            return result;
        }

        // try to solve package upgrade
        List<DownloadablePackage> choosenPackgesToUpdate= new ArrayList<DownloadablePackage>();
        Map<String, VersionRange> dependencyUpdates = new HashMap<String, VersionRange>();
        // run through all packages to update to choose right version and gather deps
        for (String pkgName : toUpdatePackageNames.keySet()) {
            Map<String, VersionRange> oneDependencyUpdatesOptimal=new HashMap<String, VersionRange>();
            // see what versions are availables
            for (DownloadablePackage pkg : toUpdatePackageNames.get(pkgName)) {
                Map<String, VersionRange> oneDependencyUpdates=new HashMap<String, VersionRange>();
                // check dependencies
                for (PackageDependency dep : pkg.getDependencies()) {
                    if (set.getTargetVersion(dep.getName())==null) {
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
                if (oneDependencyUpdates.size()<=oneDependencyUpdatesOptimal.size()) {
                    oneDependencyUpdatesOptimal=oneDependencyUpdates;
                    choosenPackgesToUpdate.add(pkg);
                }
            }
            dependencyUpdates.putAll(oneDependencyUpdatesOptimal);
        }

        if (dependencyUpdates.size()==0) {
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
    }


    protected DependencyResolution resolve(DependencySet depSet) {
        return resolve(packageId, depSet);
    }

    // check if the Dependency set satifies all contracts
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
        for (PackageDependency dep : pkg.getDependencies()) {
            Version targetVersion = depSet.getTargetVersion(dep.getName());
            if (!dep.getVersionRange().matchVersion(targetVersion)) {
                res.markAsFailed();
                return;
            }
            else {
                if (!res.addPackage(dep.getName(), targetVersion)) {
                    return;
                }
            }
            Package subPkg = pm.findPackageById(dep.getName()+"-"+ targetVersion.toString());
            recurseResolve(subPkg, res, depSet);
        }
    }


    public int getNaxPossibilities() {
        int res = 1;
        for (String pkgName : deps.keySet()) {
            res = res * deps.get(pkgName).size();
        }
        return res;
    }

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
}
