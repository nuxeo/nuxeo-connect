/*
 * (C) Copyright 2006-2015 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.connector.ConnectServerError;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.data.DownloadingPackage;
import org.nuxeo.connect.downloads.ConnectDownloadManager;
import org.nuxeo.connect.packages.dependencies.CUDFHelper;
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
import org.nuxeo.connect.update.PackageException;
import org.nuxeo.connect.update.PackageType;
import org.nuxeo.connect.update.PackageUpdateService;
import org.nuxeo.connect.update.PackageVisibility;
import org.nuxeo.connect.update.Version;
import org.nuxeo.connect.update.VersionRange;
import org.nuxeo.connect.update.task.Task;

/**
 * Nuxeo Component that implements {@link PackageManager}
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
@SuppressWarnings("deprecation")
public class PackageManagerImpl implements PackageManager {

    protected static final Log log = LogFactory.getLog(PackageManagerImpl.class);

    protected List<PackageSource> localSources = new ArrayList<>();

    protected List<PackageSource> remoteSources = new ArrayList<>();

    protected List<String> sourcesNames = new ArrayList<>();

    protected Map<String, DownloadablePackage> cachedPackageList = null;

    protected DependencyResolver resolver;

    @Override
    public List<PackageSource> getAllSources() {
        List<PackageSource> allSources = new ArrayList<>();
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
    @Override
    public void setResolver(String resolverType) {
        if (P2CUDF_DEPENDENCY_RESOLVER.equals(resolverType)) {
            resolver = new P2CUDFDependencyResolver(this);
        } else if (LEGACY_DEPENDENCY_RESOLVER.equals(resolverType)) {
            resolver = new LegacyDependencyResolver(this);
        } else {
            log.warn("Resolver " + resolverType + "is not supported - fallback on default resolver "
                    + DEFAULT_DEPENDENCY_RESOLVER);
            resolver = new P2CUDFDependencyResolver(this);
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

    /**
     * Merge packages, keeping only greater versions
     */
    protected List<DownloadablePackage> doMergePackages(List<PackageSource> sources, PackageType type,
            String targetPlatform) {
        List<DownloadablePackage> allPackages = getAllPackages(sources, type, targetPlatform);
        Map<String, Map<String, DownloadablePackage>> packagesByIdAndTargetPlatform = new HashMap<>();
        for (DownloadablePackage pkg : allPackages) {
            String[] targetPlatforms = targetPlatform != null ? new String[] { targetPlatform }
                    : pkg.getTargetPlatforms();
            if (targetPlatforms == null) { // if the package doesn't specify any target platform
                targetPlatforms = new String[] { null };
            }
            for (String tp : targetPlatforms) {
                Map<String, DownloadablePackage> packagesById = packagesByIdAndTargetPlatform.get(tp);
                if (packagesById == null) {
                    packagesById = new HashMap<>();
                    packagesByIdAndTargetPlatform.put(tp, packagesById);
                }
                String key = pkg.getId();
                if (packagesById.containsKey(key)) {
                    if (pkg.getVersion().greaterThan(packagesById.get(key).getVersion())) {
                        packagesById.put(key, pkg);
                    }
                } else {
                    packagesById.put(key, pkg);
                }
            }
        }
        List<DownloadablePackage> result = new ArrayList<>();
        for (Map<String, DownloadablePackage> packagesById : packagesByIdAndTargetPlatform.values()) {
            for (DownloadablePackage pkg : packagesById.values()) {
                if (!result.contains(pkg)) {
                    result.add(pkg);
                }
            }
        }
        Collections.sort(result, new PackageComparator());
        return result;
    }

    /**
     * @since 1.4
     * @return All downloadable packages from given sources filtered on type if not null
     */
    protected List<DownloadablePackage> getAllPackages(List<PackageSource> sources, PackageType type) {
        return getAllPackages(sources, type, null);
    }

    /**
     * @since 1.4
     * @return All downloadable packages from given sources, optionally filtered on type and/or target platform if not
     *         null
     */
    protected List<DownloadablePackage> getAllPackages(List<PackageSource> sources, PackageType type,
            String targetPlatform) {
        Map<String, DownloadablePackage> packagesById = getAllPackagesByID(sources, type, targetPlatform);
        return new ArrayList<>(packagesById.values());
    }

    /**
     * @since 1.4
     * @return a Map of all packages from given sources filtered on type if not null
     */
    protected Map<String, DownloadablePackage> getAllPackagesByID(List<PackageSource> sources, PackageType type) {
        return getAllPackagesByID(sources, type, null);
    }

    /**
     * @since 1.4
     * @return a Map of all packages from given sources, optionally filtered on type and/or target platform if not null
     */
    protected Map<String, DownloadablePackage> getAllPackagesByID(List<PackageSource> sources, PackageType type,
            String targetPlatform) {
        Map<String, DownloadablePackage> packagesById = new HashMap<>();
        for (PackageSource source : sources) {
            List<DownloadablePackage> packages = null;
            if (type == null) {
                packages = source.listPackages();
            } else {
                packages = source.listPackages(type);
            }
            for (DownloadablePackage pkg : packages) {
                if (TargetPlatformFilterHelper.isCompatibleWithTargetPlatform(pkg, targetPlatform)) {
                    packagesById.put(pkg.getId(), pkg);
                }
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
     * @return a Map of all packages from given sources filtered on type if not null
     */
    protected Map<String, List<DownloadablePackage>> getAllPackagesByName(List<PackageSource> sources, PackageType type) {
        Map<String, List<DownloadablePackage>> packagesByName = new HashMap<>();
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
                    pkgsForName = new ArrayList<>();
                    packagesByName.put(pkg.getName(), pkgsForName);
                } else {
                    pkgsForName = packagesByName.get(pkg.getName());
                }
                pkgsForName.add(pkg);
            }
        }
        return packagesByName;
    }

    @Override
    public List<DownloadablePackage> findRemotePackages(String packageName) {
        List<DownloadablePackage> pkgs = new ArrayList<>();
        for (PackageSource source : remoteSources) {
            pkgs.addAll(source.listPackagesByName(packageName));
        }
        return pkgs;
    }

    @Override
    public List<DownloadablePackage> findLocalPackages(String packageName) {
        List<DownloadablePackage> pkgs = new ArrayList<>();
        for (PackageSource source : localSources) {
            for (DownloadablePackage pkg : source.listPackages()) {
                if (pkg.getName().equals(packageName)) {
                    pkgs.add(pkg);
                }
            }
        }
        return pkgs;
    }

    @Override
    public List<Version> findLocalPackageVersions(String packageName) {
        List<Version> versions = new ArrayList<>();
        for (PackageSource source : localSources) {
            for (DownloadablePackage pkg : source.listPackages()) {
                if (pkg.getName().equals(packageName)) {
                    versions.add(pkg.getVersion());
                }
            }
        }
        return versions;
    }

    @Override
    public List<Version> findLocalPackageInstalledVersions(String packageName) {
        List<Version> versions = new ArrayList<>();
        for (PackageSource source : localSources) {
            for (DownloadablePackage pkg : source.listPackages()) {
                if (pkg.getName().equals(packageName) && pkg.getPackageState().isInstalled()) {
                    versions.add(pkg.getVersion());
                }
            }
        }
        return versions;
    }

    @Override
    public DownloadablePackage findPackageById(String packageId) {
        DownloadablePackage pkg = findPackageById(packageId, localSources);
        if (pkg == null) {
            pkg = findPackageById(packageId, remoteSources);
        }
        return pkg;
    }

    @Override
    public DownloadablePackage findRemotePackageById(String packageId) {
        return findPackageById(packageId, remoteSources);
    }

    @Override
    public DownloadablePackage findLocalPackageById(String packageId) {
        return findPackageById(packageId, localSources);
    }

    /**
     * @since 1.4
     * @param packageId Package ID to look for in {@code sources}
     * @param sources
     * @return The package searched by ID or null if not found.
     */
    protected DownloadablePackage findPackageById(String packageId, List<PackageSource> sources) {
        for (PackageSource source : sources) {
            DownloadablePackage pkg = source.getPackageById(packageId);
            if (pkg != null) {
                return pkg;
            }
        }
        return null;
    }

    @Override
    public List<Version> getPreferedVersions(String pkgName) {
        List<Version> versions = new ArrayList<>();
        List<Version> installedVersions = new ArrayList<>();
        List<Version> localVersions = new ArrayList<>();
        List<Version> remoteVersions = new ArrayList<>();
        for (PackageSource source : localSources) {
            for (DownloadablePackage pkg : source.listPackagesByName(pkgName)) {
                if (pkg.getPackageState().isInstalled()) {
                    installedVersions.add(pkg.getVersion());
                } else {
                    localVersions.add(pkg.getVersion());
                }
            }
        }
        for (PackageSource source : remoteSources) {
            for (DownloadablePackage pkg : source.listPackagesByName(pkgName)) {
                remoteVersions.add(pkg.getVersion());
            }
        }
        Collections.sort(localVersions);
        Collections.sort(remoteVersions);
        versions.addAll(installedVersions);
        versions.addAll(localVersions);
        versions.addAll(remoteVersions);
        return versions;
    }

    @Override
    public List<Version> getAvailableVersion(String pkgName, VersionRange range, String targetPlatform) {
        List<Version> versions = new ArrayList<>();
        for (PackageSource source : getAllSources()) {
            for (DownloadablePackage pkg : source.listPackagesByName(pkgName)) {
                if (range.matchVersion(pkg.getVersion())
                        && TargetPlatformFilterHelper.isCompatibleWithTargetPlatform(pkg, targetPlatform)) {
                    if (!versions.contains(pkg.getVersion())) {
                        versions.add(pkg.getVersion());
                    }
                }
            }
        }
        return versions;
    }

    @Override
    public List<DownloadablePackage> listPackages() {
        return listPackages(null, null);
    }

    @Override
    public List<DownloadablePackage> listPackages(String targetPlatform) {
        return listPackages(null, targetPlatform);
    }

    @Override
    public List<DownloadablePackage> listPackages(PackageType type) {
        return listPackages(type, null);
    }

    @Override
    public List<DownloadablePackage> listPackages(PackageType pkgType, String targetPlatform) {
        return doMergePackages(getAllSources(), pkgType, targetPlatform);
    }

    @Override
    public List<DownloadablePackage> searchPackages(String searchExpr) {
        return null;
    }

    @Override
    public void registerSource(PackageSource source, boolean local) {
        String name = source.getName();
        if (!sourcesNames.contains(name)) {
            if (local) {
                localSources.add(source);
            } else {
                remoteSources.add(source);
            }
        } else {
            log.warn("Already registered a package source named " + name);
        }
    }

    @Override
    public List<DownloadablePackage> listInstalledPackages() {
        List<DownloadablePackage> res = new ArrayList<>();
        for (PackageSource source : localSources) {
            for (DownloadablePackage pkg : source.listPackages()) {
                if (pkg.getPackageState().isInstalled()) {
                    res.add(pkg);
                }
            }
        }
        Collections.sort(res, new PackageComparator());
        return res;
    }

    @Override
    public List<DownloadablePackage> listRemotePackages() {
        return listRemotePackages(null, null);
    }

    @Override
    public List<DownloadablePackage> listRemotePackages(PackageType pkgType) {
        return listRemotePackages(pkgType, null);
    }

    @Override
    public List<DownloadablePackage> listRemotePackages(PackageType pkgType, String targetPlatform) {
        return doMergePackages(remoteSources, pkgType, targetPlatform);
    }

    @Override
    public List<DownloadablePackage> listLocalPackages() {
        return listLocalPackages(null);
    }

    @Override
    public List<DownloadablePackage> listLocalPackages(PackageType type) {
        List<DownloadablePackage> result = new ArrayList<>();
        List<String> pkgIds = new ArrayList<>();
        for (PackageSource source : localSources) {
            List<DownloadablePackage> pkgs = source.listPackages(type);
            for (DownloadablePackage pkg : pkgs) {
                if (!pkgIds.contains(pkg.getId())) {
                    pkgIds.add(pkg.getId());
                    result.add(pkg);
                }
            }
        }
        Collections.sort(result, new PackageComparator());
        return result;
    }

    @Override
    public List<DownloadablePackage> listUpdatePackages() {
        return listUpdatePackages(null, null);
    }

    @Override
    public List<DownloadablePackage> listUpdatePackages(PackageType type) {
        return listUpdatePackages(type, null);
    }

    @Override
    public List<DownloadablePackage> listUpdatePackages(String targetPlatform) {
        return listUpdatePackages(null, targetPlatform);
    }

    @Override
    public List<DownloadablePackage> listUpdatePackages(PackageType type, String targetPlatform) {
        List<DownloadablePackage> localPackages = getAllPackages(localSources, type);
        List<DownloadablePackage> availablePackages = doMergePackages(getAllSources(), type, targetPlatform);
        List<DownloadablePackage> toUpdate = new ArrayList<>();
        List<String> toUpdateIds = new ArrayList<>();

        // take all the available packages that correspond to an upgrade of an
        // active local package and match the target platform
        for (DownloadablePackage pkg : localPackages) {
            // Ignore upgrade check for unused packages
            if (!pkg.getPackageState().isInstalled()) {
                continue;
            }
            for (DownloadablePackage availablePackage : availablePackages) {
                if (availablePackage.getName().equals(pkg.getName())) {
                    if (availablePackage.getVersion().isUpgradeFor(pkg.getVersion())) {
                        toUpdate.add(availablePackage);
                        toUpdateIds.add(availablePackage.getId());
                    }
                    break;
                }
            }
        }

        if (type == null || type == PackageType.HOT_FIX) {
            // force addition of hot-fixes
            List<DownloadablePackage> hotFixes = doMergePackages(getAllSources(), PackageType.HOT_FIX, targetPlatform);
            for (DownloadablePackage pkg : hotFixes) {
                // check it is not already in update list
                if (!toUpdateIds.contains(pkg.getId())) {
                    // check if package is not already in local
                    boolean alreadyInLocal = false;
                    for (DownloadablePackage lpkg : localPackages) {
                        if (lpkg.getName().equals(pkg.getName())) {
                            if (lpkg.getVersion().greaterOrEqualThan(pkg.getVersion())) {
                                if (lpkg.getPackageState().isInstalled()) {
                                    alreadyInLocal = true;
                                }
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

        // Remove duplicates
        Set<DownloadablePackage> updateSet = new LinkedHashSet<>(toUpdate);
        toUpdate = new ArrayList<>(updateSet);
        Collections.sort(toUpdate, new PackageComparator());
        return toUpdate;
    }

    @Override
    public List<DownloadablePackage> listPrivatePackages(PackageType pkgType, String targetPlatform) {
        List<DownloadablePackage> allPackages = getAllPackages(getAllSources(), pkgType, targetPlatform);
        Collections.sort(allPackages, new PackageComparator());
        List<DownloadablePackage> allPrivatePackages = new ArrayList<>();
        for (DownloadablePackage downloadablePackage : allPackages) {
            if (downloadablePackage.getVisibility() == PackageVisibility.PRIVATE) {
                allPrivatePackages.add(downloadablePackage);
            }
        }
        return allPrivatePackages;
    }

    @Override
    public List<DownloadablePackage> listPrivatePackages(String targetPlatform) {
        return listPrivatePackages(null, targetPlatform);
    }

    @Override
    public DownloadingPackage download(String packageId) throws ConnectServerError {
        ConnectRegistrationService crs = NuxeoConnectClient.getConnectRegistrationService();
        return crs.getConnector().getDownload(packageId);
    }

    @Override
    public List<DownloadingPackage> download(List<String> packageIds) throws ConnectServerError {
        List<DownloadingPackage> downloadings = new ArrayList<>();
        for (String packageId : packageIds) {
            DownloadingPackage download = download(packageId);
            if (download != null) {
                downloadings.add(download);
            } else {
                log.error("Download failed for " + packageId);
            }
        }
        return downloadings;
    }

    @Override
    public void install(String packageId, Map<String, String> params) throws PackageException {
        PackageUpdateService pus = NuxeoConnectClient.getPackageUpdateService();
        LocalPackage pkg = pus.getPackage(packageId);
        Task installationTask = pkg.getInstallTask();
        installationTask.validate();
        installationTask.run(params);
    }

    @Override
    public void install(List<String> packageIds, Map<String, String> params) throws PackageException {
        for (String packageId : packageIds) {
            install(packageId, params);
        }
    }

    protected void invalidateCache() {
        cachedPackageList = null;
    }

    protected Map<String, DownloadablePackage> getCachedPackageList() {
        if (cachedPackageList == null) {
            cachedPackageList = new HashMap<>();
        }
        for (DownloadablePackage pkg : listPackages()) {
            cachedPackageList.put(pkg.getId(), pkg);
        }
        return cachedPackageList;
    }

    protected DownloadablePackage getPkgInList(List<DownloadablePackage> pkgs, String pkgId) {
        for (DownloadablePackage pkg : pkgs) {
            if (pkgId.equals(pkg.getId())) {
                return pkg;
            }
        }
        return null;
    }

    @Override
    public DownloadablePackage getLocalPackage(String pkgId) {
        List<DownloadablePackage> pkgs = listLocalPackages();
        return getPkgInList(pkgs, pkgId);
    }

    @Override
    public DownloadablePackage getRemotePackage(String pkgId) {
        List<DownloadablePackage> pkgs = listRemotePackages();
        return getPkgInList(pkgs, pkgId);
    }

    @Override
    public DownloadablePackage getPackage(String pkgId) {
        // Merge is an issue for P2CUDFDependencyResolver
        List<DownloadablePackage> pkgs = listAllPackages();
        DownloadablePackage pkg = getPkgInList(pkgs, pkgId);
        if (pkg == null) {
            List<DownloadablePackage> studioPkgs = listAllStudioRemotePackages();
            pkg = getPkgInList(studioPkgs, pkgId);
        }
        return pkg;
    }

    @Deprecated
    @Override
    public List<DownloadablePackage> listRemoteOrLocalPackages() {
        return listRemoteOrLocalPackages(null, null);
    }

    @Deprecated
    @Override
    public List<DownloadablePackage> listRemoteOrLocalPackages(PackageType pkgType) {
        return listRemoteOrLocalPackages(pkgType, null);
    }

    @Override
    public List<DownloadablePackage> listRemoteOrLocalPackages(String targetPlatform) {
        return listRemoteOrLocalPackages(null, targetPlatform);
    }

    @Override
    public List<DownloadablePackage> listRemoteOrLocalPackages(PackageType pkgType, String targetPlatform) {
        List<DownloadablePackage> result = new ArrayList<>();
        List<DownloadablePackage> all = listPackages(pkgType, targetPlatform);
        List<DownloadablePackage> remotes = listRemotePackages(pkgType, targetPlatform);
        // Return only packages which are available on remote sources
        for (DownloadablePackage pkg : all) {
            for (DownloadablePackage remote : remotes) {
                if (remote.getId().equals(pkg.getId())) {
                    result.add(pkg);
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public List<DownloadablePackage> listAllStudioRemoteOrLocalPackages() {
        List<DownloadablePackage> remote = listRemoteAssociatedStudioPackages();
        List<DownloadablePackage> local = listLocalPackages(PackageType.STUDIO);
        List<DownloadablePackage> result = new ArrayList<>();
        result.addAll(local);
        REMOTE: for (DownloadablePackage rpkg : remote) {
            for (DownloadablePackage lpkg : local) {
                if (lpkg.getId().equals(rpkg.getId())) {
                    continue REMOTE;
                }
            }
            result.add(rpkg);
        }
        Collections.sort(result, new PackageComparator());
        return result;
    }

    @Deprecated
    @Override
    public List<DownloadablePackage> listOnlyRemotePackages() {
        return listOnlyRemotePackages(null, null);
    }

    @Deprecated
    @Override
    public List<DownloadablePackage> listOnlyRemotePackages(PackageType pkgType) {
        return listOnlyRemotePackages(pkgType, null);
    }

    @Override
    public List<DownloadablePackage> listOnlyRemotePackages(String targetPlatform) {
        return listOnlyRemotePackages(null, targetPlatform);
    }

    @Override
    public List<DownloadablePackage> listOnlyRemotePackages(PackageType pkgType, String targetPlatform) {
        List<DownloadablePackage> result = listRemotePackages(pkgType, targetPlatform);
        List<DownloadablePackage> local = listLocalPackages(pkgType);
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

    @Override
    public List<DownloadablePackage> listAllStudioRemotePackages() {
        return listRemotePackages(PackageType.STUDIO);
    }

    @Override
    public List<DownloadablePackage> listRemoteAssociatedStudioPackages() {
        List<DownloadablePackage> result = new ArrayList<>();
        List<String> pkgIds = new ArrayList<>();
        for (PackageSource source : remoteSources) {
            List<DownloadablePackage> pkgs = source.listStudioPackages();
            for (DownloadablePackage pkg : pkgs) {
                if (!pkgIds.contains(pkg.getId())) {
                    pkgIds.add(pkg.getId());
                    result.add(pkg);
                }
            }
        }
        return result;
    }

    @Override
    public void flushCache() {
        for (PackageSource source : getAllSources()) {
            source.flushCache();
        }
    }

    @Override
    public DependencyResolution resolveDependencies(String pkgId, String targetPlatform) {
        try {
            DependencyResolution resolution = resolver.resolve(pkgId, targetPlatform);
            log.debug(beforeAfterResolutionToString(resolution));
            return resolution;
        } catch (DependencyException e) {
            return new DependencyResolution(e);
        }
    }

    /**
     * @return Packages list before and after given resolution
     * @since 1.4.13
     */
    public String beforeAfterResolutionToString(DependencyResolution resolution) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nBefore: " + listInstalledPackages());
        List<String> after = new ArrayList<>();
        after.addAll(resolution.getUnchangedPackageIds());
        after.addAll(resolution.getInstallPackageIds());
        Collections.sort(after);
        sb.append("\nAfter:  " + after);
        return sb.toString();
    }

    /**
     * @since 1.4
     * @see PackageManager#resolveDependencies(List, List, List, String, boolean)
     */
    @Override
    public DependencyResolution resolveDependencies(List<String> pkgInstall, List<String> pkgRemove,
            List<String> pkgUpgrade, String targetPlatform) {
        return resolveDependencies(pkgInstall, pkgRemove, pkgUpgrade, targetPlatform, CUDFHelper.defaultAllowSNAPSHOT,
                true);
    }

    @Override
    public DependencyResolution resolveDependencies(List<String> pkgInstall, List<String> pkgRemove,
            List<String> pkgUpgrade, String targetPlatform, boolean allowSNAPSHOT) {
        return resolveDependencies(pkgInstall, pkgRemove, pkgUpgrade, targetPlatform, allowSNAPSHOT, true);
    }

    @Override
    public DependencyResolution resolveDependencies(List<String> pkgInstall, List<String> pkgRemove,
            List<String> pkgUpgrade, String targetPlatform, boolean allowSNAPSHOT, boolean doKeep) {
        try {
            DependencyResolution resolution = resolver.resolve(pkgInstall, pkgRemove, pkgUpgrade, targetPlatform,
                    allowSNAPSHOT, doKeep);

            log.debug(beforeAfterResolutionToString(resolution));
            return resolution;
        } catch (DependencyException e) {
            return new DependencyResolution(e);
        }
    }

    @Override
    public List<DownloadablePackage> getUninstallDependencies(Package pkg, String targetPlatform) {
        List<DownloadablePackage> packagesToUninstall = new ArrayList<>();
        List<String> removes = new ArrayList<>();
        removes.add(pkg.getName());
        DependencyResolution resolution = resolveDependencies(null, removes, null, targetPlatform);
        if (!resolution.isFailed() && !resolution.isEmpty()) {
            List<String> idsToRemove = resolution.getOrderedPackageIdsToRemove();
            idsToRemove.remove(pkg.getId());
            for (String pkgIdToRemove : idsToRemove) {
                DownloadablePackage localPackage = findPackageById(pkgIdToRemove, localSources);
                if (localPackage != null) {
                    packagesToUninstall.add(localPackage);
                } else {
                    log.error("Missing local package to remove: " + pkgIdToRemove);
                }
            }
        }
        return packagesToUninstall;
    }

    @Deprecated
    @Override
    public List<DownloadablePackage> getUninstallDependencies(Package pkg) {
        // This impl is clearly not very sharp
        List<String> pkgNamesToRemove = new ArrayList<>();
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
        List<DownloadablePackage> packagesToUninstall = new ArrayList<>();
        for (String pkgName : pkgNamesToRemove) {
            for (Version v : findLocalPackageInstalledVersions(pkgName)) {
                DownloadablePackage p = getLocalPackage(pkgName + "-" + v.toString());
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
        return isInstalled(pkg.getId());
    }

    @Override
    public boolean isInstalled(String pkgId) {
        DownloadablePackage pkg = getLocalPackage(pkgId);
        return (pkg != null && pkg.getPackageState().isInstalled());
    }

    @Override
    public void order(DependencyResolution res) throws DependencyException {
        Map<String, DownloadablePackage> allPackagesByID = getAllPackagesByID();
        synchronized (res) {
            if (!res.isSorted()) {
                res.sort(this);
            }
            List<String> installOrder = res.getOrderedPackageIdsToInstall();
            orderByDependencies(allPackagesByID, installOrder);
            List<String> removeOrder = res.getOrderedPackageIdsToRemove();
            orderByDependencies(allPackagesByID, removeOrder);
            Collections.reverse(removeOrder);
        }
    }

    private void orderByDependencies(Map<String, DownloadablePackage> allPackagesByID, List<String> orderedList)
            throws DependencyException {
        Map<String, Package> orderedMap = Collections.synchronizedMap(new LinkedHashMap<String, Package>());
        boolean hasChanged = true;
        Set<String> missingDeps = new HashSet<>();
        while (!orderedList.isEmpty() && hasChanged) {
            hasChanged = false;
            for (String id : orderedList) {
                DownloadablePackage pkg = allPackagesByID.get(id);
                if (pkg.getDependencies().length == 0) {
                    // Add pkg to orderedMap if it has no dependencies
                    orderedMap.put(id, pkg);
                    hasChanged = true;
                } else {
                    // Add to orderedMap if all its dependencies are satisfied
                    boolean allSatisfied = true;
                    for (PackageDependency pkgDep : pkg.getDependencies()) {
                        // is pkgDep satisfied in orderedMap?
                        boolean satisfied = false;
                        for (Package orderedPkg : orderedMap.values()) {
                            if (matchDependency(pkgDep, orderedPkg)) {
                                satisfied = true;
                                missingDeps.remove(pkgDep);
                                break;
                            }
                        }
                        // else, is pkgDep satisfied in already installed pkgs?
                        if (!satisfied) {
                            for (Version version : findLocalPackageInstalledVersions(pkgDep.getName())) {
                                if (pkgDep.getVersionRange().matchVersion(version)) {
                                    satisfied = true;
                                    missingDeps.remove(pkgDep);
                                    break;
                                }
                            }
                        }
                        if (!satisfied) { // couldn't satisfy pkgDep
                            allSatisfied = false;
                            missingDeps.add(pkgDep.toString());
                            break;
                        }
                    }
                    if (allSatisfied) {
                        orderedMap.put(id, pkg);
                        hasChanged = true;
                    }
                }
            }
            orderedList.removeAll(orderedMap.keySet());
        }
        if (!orderedList.isEmpty()) {
            throw new DependencyException(String.format("Couldn't order %s missing %s.", orderedList, missingDeps));
        }
        orderedList.addAll(orderedMap.keySet());
    }

    private boolean matchDependency(PackageDependency pkgDep, Package pkg) {
        boolean match = pkgDep.getName().equals(pkg.getName())
                && pkgDep.getVersionRange().matchVersion(pkg.getVersion());
        if (!match && pkg.getProvides() != null) { // Look at provides
            for (PackageDependency provide : pkg.getProvides()) {
                if (pkgDep.getName().equals(provide.getName())
                        && pkgDep.getVersionRange().matchVersionRange(provide.getVersionRange())) {
                    match = true;
                    break;
                }
            }
        }
        return match;
    }

    @Override
    public void cancelDownload(String pkgId) {
        ConnectDownloadManager cdm = NuxeoConnectClient.getDownloadManager();
        cdm.removeDownloadingPackage(pkgId);
    }

    @Override
    public List<? extends Package> sort(List<? extends Package> pkgs) {
        Collections.sort(pkgs, new PackageComparator());
        return pkgs;
    }

    @Override
    public String getNonCompliant(List<String> packages, String targetPlatform) throws PackageException {
        for (String pkg : packages) {
            if (!matchesPlatform(pkg, targetPlatform)) {
                return pkg;
            }
        }
        return null;
    }

    @Override
    public List<String> getNonCompliantList(List<String> packages, String targetPlatform) throws PackageException {
        List<String> nonCompliant = new ArrayList<>();
        for (String pkg : packages) {
            if (!matchesPlatform(pkg, targetPlatform)) {
                nonCompliant.add(pkg);
            }
        }
        return nonCompliant;
    }

    @Override
    public boolean matchesPlatform(String requestPkgStr, String targetPlatform) throws PackageException {
        Map<String, DownloadablePackage> allPackagesByID = getAllPackagesByID();
        // Try ID match first
        if (allPackagesByID.containsKey(requestPkgStr)) {
            return TargetPlatformFilterHelper.isCompatibleWithTargetPlatform(allPackagesByID.get(requestPkgStr),
                    targetPlatform);
        }
        // Fallback on name match
        List<DownloadablePackage> allPackagesForName = getAllPackagesByName().get(requestPkgStr);
        if (allPackagesForName == null) {
            throw new PackageException("Package not found: " + requestPkgStr);
        }
        for (DownloadablePackage pkg : allPackagesForName) {
            if (requestPkgStr.equals(pkg.getName())
                    && TargetPlatformFilterHelper.isCompatibleWithTargetPlatform(pkg, targetPlatform)) {
                return true;
            }
        }
        return false;
    }

}
