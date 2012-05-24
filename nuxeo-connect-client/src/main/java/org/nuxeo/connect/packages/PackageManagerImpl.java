/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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

package org.nuxeo.connect.packages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.data.DownloadingPackage;
import org.nuxeo.connect.packages.dependencies.DependencyException;
import org.nuxeo.connect.packages.dependencies.DependencyResolution;
import org.nuxeo.connect.packages.dependencies.DependencyResolver;
import org.nuxeo.connect.packages.dependencies.LegacyDependencyResolver;
import org.nuxeo.connect.packages.dependencies.P2CUDFDependencyResolver;
import org.nuxeo.connect.packages.dependencies.TargetPlatformFilterHelper;
import org.nuxeo.connect.registration.ConnectRegistrationService;
import org.nuxeo.connect.update.LocalPackage;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.PackageState;
import org.nuxeo.connect.update.PackageType;
import org.nuxeo.connect.update.PackageUpdateService;
import org.nuxeo.connect.update.Version;
import org.nuxeo.connect.update.VersionRange;
import org.nuxeo.connect.update.task.Task;

/**
 *
 * Nuxeo Component that implements {@link PackageManager}
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class PackageManagerImpl implements InternalPackageManager {

    protected static final Log log = LogFactory.getLog(PackageManagerImpl.class);

    protected List<PackageSource> localSources = new ArrayList<PackageSource>();

    protected List<PackageSource> remoteSources = new ArrayList<PackageSource>();

    protected List<String> sourcesNames = new ArrayList<String>();

    protected Map<String, DownloadablePackage> cachedPackageList = null;

    protected DependencyResolver resolver;

    protected List<PackageSource> getAllSources() {
        List<PackageSource> allSources = new ArrayList<PackageSource>();
        allSources.addAll(remoteSources);
        allSources.addAll(localSources);
        return allSources;
    }

    public PackageManagerImpl() {
        registerSource(new RemotePackageSource(), false);
        registerSource(new DownloadingPackageSource(), true);
        registerSource(new LocalPackageSource(), true);
        setResolver(DEFAULT_DEPENDENCY_RESOLVER);
    }

    /**
     * @since 1.4
     */
    public void setResolver(String resolverType) {
        if (P2CUDF_DEPENDENCY_RESOLVER.equals(resolverType)) {
            resolver = new P2CUDFDependencyResolver(this);
        } else if (LEGACY_DEPENDENCY_RESOLVER.equals(resolverType)) {
            resolver = new LegacyDependencyResolver(this);
        } else {
            log.warn("Resolver " + resolverType
                    + "is not supported - fallback on default resolver "
                    + DEFAULT_DEPENDENCY_RESOLVER);
            resolver = new LegacyDependencyResolver(this);
        }
    }

    public void resetSources() {
        localSources.clear();
        remoteSources.clear();
        sourcesNames.clear();
        if (cachedPackageList != null) {
            cachedPackageList.clear();
        }
    }

    protected List<DownloadablePackage> doMergePackages(
            List<PackageSource> sources, PackageType type) {
        List<DownloadablePackage> allPackages = getAllPackages(sources, type);
        Map<String, DownloadablePackage> packagesByName = new HashMap<String, DownloadablePackage>();
        for (DownloadablePackage pkg : allPackages) {
            String name = pkg.getName();
            if (packagesByName.containsKey(name)) {
                DownloadablePackage other = packagesByName.get(name);
                if (pkg.getVersion().greaterThan(other.getVersion())) {
                    packagesByName.put(name, pkg);
                }
            } else {
                packagesByName.put(name, pkg);
            }
        }
        return new ArrayList<DownloadablePackage>(packagesByName.values());
    }

    /**
     * @since 1.4
     * @return All downloadable packages from given sources filtered on type if
     *         not null
     */
    protected List<DownloadablePackage> getAllPackages(
            List<PackageSource> sources, PackageType type) {
        Map<String, DownloadablePackage> packagesById = getAllPackagesByID(
                sources, type);
        return new ArrayList<DownloadablePackage>(packagesById.values());
    }

    /**
     * @since 1.4
     * @return a Map of all packages from given sources filtered on type if not
     *         null
     */
    protected Map<String, DownloadablePackage> getAllPackagesByID(
            List<PackageSource> sources, PackageType type) {
        Map<String, DownloadablePackage> packagesById = new HashMap<String, DownloadablePackage>();
        for (PackageSource source : sources) {
            List<DownloadablePackage> packages = null;
            if (type == null) {
                packages = source.listPackages();
            } else {
                packages = source.listPackages(type);
            }
            for (DownloadablePackage pkg : packages) {
                packagesById.put(pkg.getId(), pkg);
            }
        }
        return packagesById;
    }

    @Override
    public Map<String, DownloadablePackage> getAllPackagesByID() {
        return getAllPackagesByID(getAllSources(), null);
    }

    @Override
    public Map<String, List<DownloadablePackage>> getAllPackagesByName() {
        return getAllPackagesByName(getAllSources(), null);
    }

    /**
     * @since 1.4
     * @return a Map of all packages from given sources filtered on type if not
     *         null
     */
    protected Map<String, List<DownloadablePackage>> getAllPackagesByName(
            List<PackageSource> sources, PackageType type) {
        Map<String, List<DownloadablePackage>> packagesByName = new HashMap<String, List<DownloadablePackage>>();
        for (PackageSource source : sources) {
            List<DownloadablePackage> packages = null;
            if (type == null) {
                packages = source.listPackages();
            } else {
                packages = source.listPackages(type);
            }
            for (DownloadablePackage pkg : packages) {
                List<DownloadablePackage> pkgsForName;
                if (!packagesByName.containsKey(pkg.getName())) {
                    pkgsForName = new ArrayList<DownloadablePackage>();
                    packagesByName.put(pkg.getName(), pkgsForName);
                } else {
                    pkgsForName = packagesByName.get(pkg.getName());
                }
                pkgsForName.add(pkg);
            }
        }
        return packagesByName;
    }

    public List<DownloadablePackage> findRemotePackages(String packageName) {
        List<DownloadablePackage> pkgs = new ArrayList<DownloadablePackage>();
        for (PackageSource source : remoteSources) {
            for (DownloadablePackage pkg : source.listPackages()) {
                if (pkg.getName().equals(packageName)) {
                    pkgs.add(pkg);
                }
            }
        }
        return pkgs;
    }

    public List<Version> findLocalPackageVersions(String packageName) {
        List<Version> versions = new ArrayList<Version>();
        for (PackageSource source : localSources) {
            for (DownloadablePackage pkg : source.listPackages()) {
                if (pkg.getName().equals(packageName)) {
                    versions.add(pkg.getVersion());
                }
            }
        }
        return versions;
    }

    public List<Version> findLocalPackageInstalledVersions(String packageName) {
        List<Version> versions = new ArrayList<Version>();
        for (PackageSource source : localSources) {
            for (DownloadablePackage pkg : source.listPackages()) {
                if (pkg.getName().equals(packageName)
                        && pkg.getState() >= PackageState.INSTALLING) {
                    versions.add(pkg.getVersion());
                }
            }
        }
        return versions;
    }

    public DownloadablePackage findPackageById(String packageId) {
        for (PackageSource source : localSources) {
            for (DownloadablePackage pkg : source.listPackages()) {
                if (pkg.getId().equals(packageId)) {
                    return pkg;
                }
            }
        }
        for (PackageSource source : remoteSources) {
            for (DownloadablePackage pkg : source.listPackages()) {
                if (pkg.getId().equals(packageId)) {
                    return pkg;
                }
            }
        }
        return null;
    }

    public List<Version> getPreferedVersions(String pkgName) {
        List<Version> versions = new ArrayList<Version>();
        List<Version> installedVersions = new ArrayList<Version>();
        List<Version> localVersions = new ArrayList<Version>();
        List<Version> remoteVersions = new ArrayList<Version>();

        for (PackageSource source : localSources) {
            for (DownloadablePackage pkg : source.listPackages()) {
                if (pkg.getName().equals(pkgName)) {
                    if (pkg.getState() == PackageState.INSTALLED) {
                        installedVersions.add(pkg.getVersion());
                    } else {
                        localVersions.add(pkg.getVersion());
                    }
                }
            }
        }
        for (PackageSource source : remoteSources) {
            for (DownloadablePackage pkg : source.listPackages()) {
                if (pkg.getName().equals(pkgName)) {
                    remoteVersions.add(pkg.getVersion());
                }
            }
        }

        Collections.sort(localVersions);
        Collections.sort(remoteVersions);

        versions.addAll(installedVersions);
        versions.addAll(localVersions);
        versions.addAll(remoteVersions);

        return versions;
    }

    public List<Version> getAvailableVersion(String pkgName,
            VersionRange range, String targetPlatform) {
        List<Version> versions = new ArrayList<Version>();
        for (PackageSource source : getAllSources()) {
            for (DownloadablePackage pkg : source.listPackages()) {
                if (pkg.getName().equals(pkgName)
                        && range.matchVersion(pkg.getVersion())
                        && TargetPlatformFilterHelper.isCompatibleWithTargetPlatform(
                                pkg, targetPlatform)) {
                    if (!versions.contains(pkg.getVersion())) {
                        versions.add(pkg.getVersion());
                    }
                }
            }
        }
        return versions;
    }

    /**
     * @return All packages merged by name, keeping the greater versions
     */
    public List<DownloadablePackage> listPackages() {
        return doMergePackages(getAllSources(), null);
    }

    public List<DownloadablePackage> listPackages(PackageType type) {
        return doMergePackages(getAllSources(), type);
    }

    public List<DownloadablePackage> searchPackages(String searchExpr) {
        // TODO Auto-generated method stub
        return null;
    }

    public void registerSource(PackageSource source, boolean local) {
        String name = source.getName();
        if (!sourcesNames.contains(name)) {
            if (local) {
                localSources.add(source);
            } else {
                remoteSources.add(source);
            }
        }
    }

    public List<DownloadablePackage> listInstalledPackages() {
        List<DownloadablePackage> res = new ArrayList<DownloadablePackage>();
        for (PackageSource source : localSources) {
            for (DownloadablePackage pkg : source.listPackages()) {
                if (pkg.getState() >= PackageState.INSTALLING) {
                    res.add(pkg);
                }
            }
        }
        Collections.sort(res, new VersionPackageComparator());
        return res;
    }

    public List<DownloadablePackage> listRemotePackages() {
        return doMergePackages(remoteSources, null);
    }

    public List<DownloadablePackage> listRemotePackages(PackageType type) {
        List<DownloadablePackage> result = doMergePackages(remoteSources, type);
        Collections.sort(result, new VersionPackageComparator());
        return result;
    }

    public List<DownloadablePackage> listLocalPackages() {
        return listLocalPackages(null);
    }

    /**
     * for local package we don't merge / filter on latest versions
     */
    public List<DownloadablePackage> listLocalPackages(PackageType type) {
        List<DownloadablePackage> result = new ArrayList<DownloadablePackage>();
        List<String> pkgIds = new ArrayList<String>();
        for (PackageSource source : localSources) {
            List<DownloadablePackage> pkgs = null;
            if (type == null) {
                pkgs = source.listPackages();
            } else {
                pkgs = source.listPackages(type);
            }
            for (DownloadablePackage pkg : pkgs) {
                if (!pkgIds.contains(pkg.getId())) {
                    pkgIds.add(pkg.getId());
                    result.add(pkg);
                }
            }
        }

        Collections.sort(result, new VersionPackageComparator());
        return result;
    }

    public List<DownloadablePackage> listUpdatePackages() {
        return listUpdatePackages(null);
    }

    public List<DownloadablePackage> listUpdatePackages(PackageType type) {
        List<DownloadablePackage> localPackages = doMergePackages(localSources,
                type);
        List<DownloadablePackage> remotePackages = listRemotePackages(type);
        List<DownloadablePackage> toUpdate = new ArrayList<DownloadablePackage>();
        List<String> toUpdateIds = new ArrayList<String>();

        // take all the remote packages that correspond to an upgrade of a local
        // package
        for (DownloadablePackage pkg : localPackages) {
            for (DownloadablePackage remotePkg : remotePackages) {
                if (remotePkg.getName().equals(pkg.getName())) {
                    if (remotePkg.getVersion() != null) {
                        if (remotePkg.getVersion().greaterThan(pkg.getVersion())) {
                            toUpdate.add(remotePkg);
                            toUpdateIds.add(remotePkg.getId());
                        } else if (remotePkg.getVersion().equals(
                                pkg.getVersion())) {
                            // also list update in progress
                            if (pkg.getState() == PackageState.DOWNLOADING
                                    || pkg.getState() == PackageState.DOWNLOADED
                                    || pkg.getState() == PackageState.INSTALLING) {
                                toUpdate.add(pkg);
                                toUpdateIds.add(pkg.getId());
                            }
                        }
                    } else {
                        log.warn("Package " + remotePkg.getId()
                                + " has a null version");
                    }
                    break;
                }
            }
        }

        if (type == null || type == PackageType.HOT_FIX) {
            // force addition of hot-fixes
            List<DownloadablePackage> hotFixes = listRemotePackages(PackageType.HOT_FIX);
            for (DownloadablePackage pkg : hotFixes) {
                // check it is not already in update list
                if (!toUpdateIds.contains(pkg.getId())) {
                    // check if package is not already in local
                    boolean alreadyInLocal = false;
                    for (DownloadablePackage lpkg : localPackages) {
                        if (lpkg.getName().equals(pkg.getName())) {
                            if (lpkg.getVersion().greaterOrEqualThan(
                                    pkg.getVersion())) {
                                alreadyInLocal = true;
                            }
                            break;
                        }
                    }
                    if (!alreadyInLocal) {
                        toUpdate.add(0, pkg);
                    }
                }
            }
        }

        Collections.sort(toUpdate, new VersionPackageComparator());
        return toUpdate;
    }

    public DownloadingPackage download(String packageId) throws Exception {
        ConnectRegistrationService crs = NuxeoConnectClient.getConnectRegistrationService();
        return crs.getConnector().getDownload(packageId);
    }

    public List<DownloadingPackage> download(List<String> packageIds)
            throws Exception {
        List<DownloadingPackage> downloadings = new ArrayList<DownloadingPackage>();
        for (String packageId : packageIds) {
            downloadings.add(download(packageId));
        }
        return downloadings;
    }

    public void install(String packageId, Map<String, String> params)
            throws Exception {
        PackageUpdateService pus = NuxeoConnectClient.getPackageUpdateService();
        if (pus == null) {
            if (!NuxeoConnectClient.isTestModeSet()) {
                log.error("Can not locate PackageUpdateService, exiting");
            }
            return;
        }
        LocalPackage pkg = pus.getPackage(packageId);

        Task installationTask = pkg.getInstallTask();
        installationTask.validate();
        installationTask.run(params);
    }

    public void install(List<String> packageIds, Map<String, String> params)
            throws Exception {
        for (String packageId : packageIds) {
            install(packageId, params);
        }
    }

    protected void invalidateCache() {
        cachedPackageList = null;
    }

    protected Map<String, DownloadablePackage> getCachedPackageList() {
        if (cachedPackageList == null) {
            cachedPackageList = new HashMap<String, DownloadablePackage>();
        }
        for (DownloadablePackage pkg : listPackages()) {
            cachedPackageList.put(pkg.getId(), pkg);
        }
        return cachedPackageList;
    }

    protected DownloadablePackage getPkgInList(List<DownloadablePackage> pkgs,
            String pkgId) {
        for (DownloadablePackage pkg : pkgs) {
            if (pkgId.equals(pkg.getId())) {
                return pkg;
            }
        }
        return null;
    }

    public DownloadablePackage getLocalPackage(String pkgId) {
        List<DownloadablePackage> pkgs = listLocalPackages();
        return getPkgInList(pkgs, pkgId);
    }

    public DownloadablePackage getRemotePackage(String pkgId) {
        List<DownloadablePackage> pkgs = listRemotePackages();
        return getPkgInList(pkgs, pkgId);
    }

    public DownloadablePackage resolvePackage(String pkgId) {
        // get
        return null;
    }

    public DownloadablePackage getPackage(String pkgId) {
        // Merge is an issue for P2CUDFDependencyResolver, try with
        // listAllPackages() instead of listPackages()
        // List<DownloadablePackage> pkgs = listPackages();
        List<DownloadablePackage> pkgs = listAllPackages();
        DownloadablePackage pkg = getPkgInList(pkgs, pkgId);
        if (pkg == null) {
            List<DownloadablePackage> studioPkgs = listAllStudioRemotePackages();
            pkg = getPkgInList(studioPkgs, pkgId);
        }
        return pkg;
    }

    public List<DownloadablePackage> listRemoteOrLocalPackages() {
        return listRemoteOrLocalPackages(null);
    }

    public List<DownloadablePackage> listRemoteOrLocalPackages(PackageType type) {

        List<DownloadablePackage> result = new ArrayList<DownloadablePackage>();
        List<DownloadablePackage> all = listPackages(type);
        List<DownloadablePackage> remotes = listRemotePackages(type);

        for (DownloadablePackage pkg : all) {
            for (DownloadablePackage remote : remotes) {
                if (remote.getName().equals(pkg.getName())) {
                    result.add(pkg);
                    break;
                }
            }
        }
        return result;
    }

    public List<DownloadablePackage> listAllStudioRemoteOrLocalPackages() {
        List<DownloadablePackage> remote = listAllStudioRemotePackages();
        List<DownloadablePackage> local = listLocalPackages(PackageType.STUDIO);
        List<DownloadablePackage> result = new ArrayList<DownloadablePackage>();

        for (DownloadablePackage pkg : remote) {
            boolean found = false;
            for (DownloadablePackage lpkg : local) {
                if (lpkg.getId().equals(pkg.getId())) {
                    result.add(lpkg);
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.add(pkg);
            }
        }
        return result;
    }

    public List<DownloadablePackage> listOnlyRemotePackages() {
        return listOnlyRemotePackages(null);
    }

    public List<DownloadablePackage> listOnlyRemotePackages(PackageType type) {
        List<DownloadablePackage> result = listRemotePackages(type);
        List<DownloadablePackage> local = listLocalPackages(type);

        for (DownloadablePackage pkg : local) {
            for (DownloadablePackage remote : result) {
                if (remote.getName().equals(pkg.getName())) {
                    result.remove(remote);
                    break;
                }
            }
        }
        return result;
    }

    public List<DownloadablePackage> listAllStudioRemotePackages() {
        List<DownloadablePackage> result = new ArrayList<DownloadablePackage>();
        for (PackageSource source : remoteSources) {
            List<DownloadablePackage> packages = source.listPackages(PackageType.STUDIO);
            result.addAll(packages);
        }
        return result;
    }

    public void flushCache() {
        for (PackageSource source : getAllSources()) {
            source.flushCache();
        }
    }

    @Override
    public DependencyResolution resolveDependencies(String pkgId,
            String targetPlatform) {
        try {
            return resolver.resolve(pkgId, targetPlatform);
        } catch (DependencyException e) {
            return new DependencyResolution(e);
        }
    }

    /**
     * @since 1.4
     * @see PackageManager#resolveDependencies(List, List, List, String)
     */
    @Override
    public DependencyResolution resolveDependencies(List<String> pkgInstall,
            List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform) {
        try {
            return resolver.resolve(pkgInstall, pkgRemove, pkgUpgrade,
                    targetPlatform);
        } catch (DependencyException e) {
            return new DependencyResolution(e);
        }
    }

    @Override
    public List<DownloadablePackage> getUninstallDependencies(Package pkg) {
        // This impl is clearly not very sharp
        List<String> pkgNamesToRemove = new ArrayList<String>();
        List<DownloadablePackage> installedPackages = listInstalledPackages();
        int nbImpactedPackages = 0;
        pkgNamesToRemove.add(pkg.getName());
        while (pkgNamesToRemove.size() > nbImpactedPackages) {
            nbImpactedPackages = pkgNamesToRemove.size();
            for (DownloadablePackage p : installedPackages) {
                if (!pkgNamesToRemove.contains(p.getName())) {
                    for (PackageDependency dep : p.getDependencies()) {
                        if (pkgNamesToRemove.contains(dep.getName())) {
                            pkgNamesToRemove.add(p.getName());
                            break;
                        }
                    }
                }
            }
        }

        pkgNamesToRemove.remove(pkg.getName());
        List<DownloadablePackage> packagesToUninstall = new ArrayList<DownloadablePackage>();
        for (String pkgName : pkgNamesToRemove) {
            for (Version v : findLocalPackageInstalledVersions(pkgName)) {
                DownloadablePackage p = getLocalPackage(pkgName + "-"
                        + v.toString());
                packagesToUninstall.add(p);
            }

        }

        return packagesToUninstall;
    }

    @Override
    public List<DownloadablePackage> listAllPackages() {
        return getAllPackages(getAllSources(), null);
    }

    @Override
    public boolean isInstalled(Package pkg) {
        PackageUpdateService pus = NuxeoConnectClient.getPackageUpdateService();
        if (pus == null) {
            if (!NuxeoConnectClient.isTestModeSet()) {
                log.error("Can not locate PackageUpdateService, set package as not installed.");
            }
            return false;
        }
        return pus.isStarted(pkg.getId());
    }

}
