/*
 * (C) Copyright 2006-2017 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *     Yannis JULIENNE
 *     Ronan DANIELLOU <rdaniellou@nuxeo.com>
 *
 */

package org.nuxeo.connect.packages;

import java.util.List;
import java.util.Map;

import org.nuxeo.connect.connector.ConnectServerError;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.data.DownloadingPackage;
import org.nuxeo.connect.packages.dependencies.DependencyException;
import org.nuxeo.connect.packages.dependencies.DependencyResolution;
import org.nuxeo.connect.packages.dependencies.DependencyResolver;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.PackageException;
import org.nuxeo.connect.update.PackageType;
import org.nuxeo.connect.update.PackageUpdateService;
import org.nuxeo.connect.update.Version;
import org.nuxeo.connect.update.VersionRange;

/**
 * Service interface that wraps all {@link PackageSource} to provide an unified view The main purpose of this interface
 * is to provide listing methods that return the most up to date version of packages for given filters
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public interface PackageManager extends BasePackageManager {

    /**
     * @since 1.4
     * @deprecated Use {@link #P2CUDF_DEPENDENCY_RESOLVER}
     */
    @Deprecated
    public static final String LEGACY_DEPENDENCY_RESOLVER = "legacy";

    /**
     * @since 1.4
     */
    public static final String P2CUDF_DEPENDENCY_RESOLVER = "p2cudf";

    /**
     * @since 1.4
     */
    public static final String DEFAULT_DEPENDENCY_RESOLVER = P2CUDF_DEPENDENCY_RESOLVER;

    /**
     * Returns most recent version of {@link DownloadablePackage} from all sources.
     */
    List<DownloadablePackage> listPackages();

    /**
     * Returns most recent version of {@link DownloadablePackage} from all sources for given {@code targetPlatform}
     *
     * @since 1.4
     */
    List<DownloadablePackage> listPackages(String targetPlatform);

    /**
     * Returns most recent version of {@link DownloadablePackage} from all sources for a given {@link PackageType}.
     */
    List<DownloadablePackage> listPackages(PackageType type);

    /**
     * Returns most recent version of {@link DownloadablePackage} from all sources for given {@link PackageType} and
     * {@code targetPlatform}
     *
     * @param pkgType
     * @param targetPlatform
     * @since 1.4
     */
    List<DownloadablePackage> listPackages(PackageType pkgType, String targetPlatform);

    /**
     * Search for packages. (Currently not implemented)
     */
    List<DownloadablePackage> searchPackages(String searchExpr);

    /**
     * Lists most recent version of {@link DownloadablePackage} locally present. Local packages are not merged/filtered
     * on latest versions
     */
    List<DownloadablePackage> listLocalPackages();

    /**
     * Lists most recent version of {@link DownloadablePackage} locally present for a given {@link PackageType}. Local
     * packages are not merged/filtered on latest versions
     */
    List<DownloadablePackage> listLocalPackages(PackageType type);

    /**
     * Lists most recent version of {@link DownloadablePackage} locally present for a given {@link PackageType} and/or
     * target platform. Local packages are not merged/filtered on latest versions
     *
     * @param pkgType
     * @param targetPlatform
     * @since TODO
     */
    List<DownloadablePackage> listLocalPackages(PackageType type, String targetPlatform);

    /**
     * Lists most recent version of {@link DownloadablePackage} available on connect server.
     */
    List<DownloadablePackage> listRemotePackages();

    /**
     * Lists most recent version of {@link DownloadablePackage} available on connect server for a given
     * {@link PackageType}.
     */
    List<DownloadablePackage> listRemotePackages(PackageType type);

    /**
     * Lists most recent version of {@link DownloadablePackage} available on connect server for a given
     * {@link PackageType} and {@code targetPlatform}
     *
     * @param pkgType
     * @param targetPlatform
     * @since 1.4
     */
    List<DownloadablePackage> listRemotePackages(PackageType pkgType, String targetPlatform);

    /**
     * Lists all {@link DownloadablePackage} that are locally present and that can be updated by a higher version
     * available on connect server.
     */
    List<DownloadablePackage> listUpdatePackages();

    /**
     * Lists all {@link DownloadablePackage} that are locally present and that can be updated by a higher version
     * available on connect server for a given {@link PackageType}. Also list all available hotfixes for the current
     * targetPlatform if {@link PackageType} is null or is explicitly {@value PackageType#HOT_FIX}.
     */
    List<DownloadablePackage> listUpdatePackages(PackageType type);

    /**
     * Lists all {@link DownloadablePackage} that are locally present and that can be updated by a higher version
     * available on connect server for a given target platform. Also list all available hotfixes for the given
     * targetPlatform.
     */
    List<DownloadablePackage> listUpdatePackages(String targetPlatform);

    /**
     * Lists all {@link DownloadablePackage} that are locally present and that can be updated by a higher version
     * available on connect server for a given {@link PackageType} and target platform. Also list all available hotfixes
     * for the given targetPlatform if {@link PackageType} is null or is explicitly {@value PackageType#HOT_FIX}.
     */
    List<DownloadablePackage> listUpdatePackages(PackageType type, String targetPlatform);

    /**
     * Lists most recent version of {@link DownloadablePackage} available only on the connect server (ie no local
     * version).
     *
     * @deprecated Since 1.4. Use {@link #listOnlyRemotePackages(String)} instead.
     */
    @Deprecated
    List<DownloadablePackage> listOnlyRemotePackages();

    /**
     * Lists most recent version of {@link DownloadablePackage} available only on the connect server (ie no local
     * version).
     *
     * @param targetPlatform
     * @since 1.4
     */
    List<DownloadablePackage> listOnlyRemotePackages(String targetPlatform);

    /**
     * Lists most recent version of {@link DownloadablePackage} available only on the connect server (ie no local
     * version) for a given {@link PackageType}.
     *
     * @deprecated Since 1.4. Use {@link #listOnlyRemotePackages(PackageType, String)} instead.
     */
    @Deprecated
    List<DownloadablePackage> listOnlyRemotePackages(PackageType type);

    /**
     * Lists most recent version of {@link DownloadablePackage} available only on the connect server (ie no local
     * version) for a given {@link PackageType}.
     *
     * @param pkgType {@link PackageType}
     * @param targetPlatform
     * @since 1.4
     */
    List<DownloadablePackage> listOnlyRemotePackages(PackageType pkgType, String targetPlatform);

    /**
     * Lists all versions of the studio packages the user has access to. This was used to return only the Studio
     * package(s) associated with the server registration. Now that includes the Studio packages the user is contributor
     * on. Thus the deprecation to encourage explicit use of {@link #listRemotePackages(PackageType)} or
     * {@link #listRemoteAssociatedStudioPackages()}.
     *
     * @deprecated Since 1.4.19. Use instead {@link #listRemotePackages(PackageType)} with {@link PackageType#STUDIO} or
     *             {@link #listRemoteAssociatedStudioPackages()}
     */
    @Deprecated
    List<DownloadablePackage> listAllStudioRemotePackages();

    /**
     * @return All remote versions of the Studio package associated with the server registration.
     * @since 1.4.19
     */
    List<DownloadablePackage> listRemoteAssociatedStudioPackages();

    /**
     * @return All remote versions of the Studio package associated with the server registration.
     * @since TODO
     */
    List<DownloadablePackage> listRemoteAssociatedStudioPackages(String targetPlatform);

    /**
     * Lists all versions of the studio packages associated to user account in remote and potentially overridden by a
     * local package.
     */
    List<DownloadablePackage> listAllStudioRemoteOrLocalPackages();

    /**
     * Lists all versions of the studio packages associated to user account in remote and potentially overridden by a
     * local package.
     * @since TODO
     */
    List<DownloadablePackage> listAllStudioRemoteOrLocalPackages(String targetPlatform);

    /**
     * Lists packages available in remote and potentially overridden by a local package.
     *
     * @deprecated Since 1.4. Use {@link #listRemoteOrLocalPackages(String)} instead.
     */
    @Deprecated
    List<DownloadablePackage> listRemoteOrLocalPackages();

    /**
     * Lists packages available in remote and potentially overridden by a local package.
     *
     * @param targetPlatform
     * @since 1.4
     */
    List<DownloadablePackage> listRemoteOrLocalPackages(String targetPlatform);

    /**
     * Lists packages availab.e in remote and potentially overridden by a local package.
     *
     * @deprecated Since 1.4. Use {@link #listRemoteOrLocalPackages(PackageType, String)} instead.
     */
    @Deprecated
    List<DownloadablePackage> listRemoteOrLocalPackages(PackageType type);

    /**
     * Lists most recent version of {@link DownloadablePackage} available on connect server for a given
     * {@link PackageType}.
     *
     * @param pkgType {@link PackageType}
     * @param targetPlatform
     * @since 1.4
     */
    List<DownloadablePackage> listRemoteOrLocalPackages(PackageType pkgType, String targetPlatform);

    /**
     * Lists all local or remote private packages, filtered on package type if {@code pkgType} is not null.
     *
     * @param pkgType {@link PackageType}
     * @param targetPlatform
     * @since 1.4
     */
    List<DownloadablePackage> listPrivatePackages(PackageType pkgType, String targetPlatform);

    /**
     * @since 1.4
     * @see #listPrivatePackages(PackageType, String)
     */
    List<DownloadablePackage> listPrivatePackages(String targetPlatform);

    /**
     * List all installed packages names (without version), filtered on package type if {@code pkgType} is not null.
     *
     * @since 1.4.26
     */
    List<String> listInstalledPackagesNames(PackageType pkgType);

    /**
     * List available hotfixes names (without version), already installed or not, for the given targetPlatform.
     *
     * @param allowSNAPSHOT whether to return hotfixes names with only a snapshot version available.
     * @since 1.4.26
     */
    List<String> listHotfixesNames(String targetPlatform, boolean allowSNAPSHOT);

    /**
     * Register a new {@link PackageSource}
     *
     * @param source
     * @param local
     */
    void registerSource(PackageSource source, boolean local);

    /**
     * Get the Download descriptor for a given package id
     *
     * @param packageId
     */
    DownloadingPackage download(String packageId) throws ConnectServerError;

    /**
     * Get the Download descriptors for a given list of package ids
     *
     * @param packageIds
     */
    List<DownloadingPackage> download(List<String> packageIds) throws ConnectServerError;

    /**
     * Start installation process via {@link PackageUpdateService}
     *
     * @param packageId Identifier of the {@link Package} to install
     * @param params Installation parameters (as collected via Wizard's form)
     */
    void install(String packageId, Map<String, String> params) throws PackageException;

    /**
     * Serial installation of several packages
     *
     * @param packageIds List of identifiers of the {@link Package}s to install
     * @param params Installation parameters (as collected via Wizard's form)
     */
    void install(List<String> packageIds, Map<String, String> params) throws PackageException;

    /**
     * Flushes the caches used on remote {@link PackageSource}
     */
    void flushCache();

    /**
     * Choose the resolver implementation
     *
     * @param resolverType the {@link DependencyResolver} to use
     */
    void setResolver(String resolverType);

    /**
     * Try to resolve dependencies of a given {@link Package}
     *
     * @param pkgId
     * @param targetPlatform (String representing the target platform or null
     * @deprecated since 1.4.26 use {@link #resolveDependencies(List, List, List, String)} instead
     */
    @Deprecated
    DependencyResolution resolveDependencies(String pkgId, String targetPlatform);

    /**
     * @param pkgInstall
     * @param pkgRemove
     * @param pkgUpgrade
     * @param targetPlatform
     * @since 1.4
     */
    DependencyResolution resolveDependencies(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform);

    /**
     * Returns the packages uninstalled if the given {@link Package} is removed
     *
     * @param pkg the {@link Package} that is being uninstalled
     * @return List of all {@link DownloadablePackage} that must be uninstalled too
     * @deprecated Since 1.4. Use {@link #getUninstallDependencies(Package, String)} instead.
     */
    @Deprecated
    List<DownloadablePackage> getUninstallDependencies(Package pkg);

    /**
     * @param pkg the {@link Package} that is being uninstalled
     * @param targetPlatform If null, the constraint on target platform is relaxed.
     * @return List of all {@link DownloadablePackage} that must be uninstalled too
     * @since 1.4
     */
    List<DownloadablePackage> getUninstallDependencies(Package pkg, String targetPlatform);

    /**
     * @return all the packages, in all versions, properly managing classifiers
     */
    List<DownloadablePackage> listAllPackages();

    /**
     * @return all the packages, in all versions, properly managing classifiers
     * @since TODO
     */
    List<DownloadablePackage> listAllPackages(String targetPlatform);

    /**
     * @since 1.4
     */
    boolean isInstalled(Package pkg);

    /**
     * @since 1.4
     * @return a Map of all packages by ID
     */
    Map<String, DownloadablePackage> getAllPackagesByID();

    /**
     * @since TODO
     * @return a Map of all packages by ID
     */
    Map<String, DownloadablePackage> getAllPackagesByID(String targetPlatform);

    /**
     * @since 1.4
     * @return a Map of all packages by Name
     */
    Map<String, List<DownloadablePackage>> getAllPackagesByName();

    /**
     * @since TODO
     * @return a Map of all packages by Name
     */
    Map<String, List<DownloadablePackage>> getAllPackagesByName(String targetPlatform);

    /**
     * Return the available {@link Version} for a given {@link Package} name. Versions are sorted in the "preferred
     * order":
     * <ul>
     * <li>already installed version (means no upgrade and no download)</li>
     * <li>already downloaded version (means no download)</li>
     * <li>remote versions sorted by version number (higher comes last)</li>
     * </ul>
     *
     * @param pkgName
     * @since 1.4
     */
    List<Version> getPreferedVersions(String pkgName);

    /**
     * Returns all remote {@link Package} versions for a given name
     *
     * @param packageName
     * @since 1.4
     */
    List<DownloadablePackage> findRemotePackages(String packageName);

    /**
     * Returns all remote {@link Package} versions for a given name
     *
     * @param packageName
     * @since TODO
     */
    List<DownloadablePackage> findRemotePackages(String packageName, String targetPlatform);

    /**
     * Find a {@link Package} by it's id (will find masked versions on the contrary of {@link PackageManager} getPackage
     *
     * @param packageId
     * @since 1.4
     */
    DownloadablePackage findPackageById(String packageId);

    /**
     * Returns all local {@link Package} versions for a given name
     *
     * @param packageName
     * @since 1.4
     */
    List<Version> findLocalPackageVersions(String packageName);

    /**
     * Returns all local {@link Package} installed versions for a given name
     *
     * @param packageName
     * @since 1.4
     */
    List<Version> findLocalPackageInstalledVersions(String packageName);

    /**
     * Returns all {@link Package} versions for a given name and {@link VersionRange}
     *
     * @param pkgName
     * @param range
     * @param targetPlatform
     * @since 1.4
     */
    List<Version> getAvailableVersion(String pkgName, VersionRange range, String targetPlatform);

    /**
     * Order dependencies to install and to remove.
     *
     * @param res DependencyResolution to be ordered
     * @throws DependencyException
     * @since 1.4
     */
    void order(DependencyResolution res) throws DependencyException;

    /**
     * @since 1.4
     */
    void cancelDownload(String pkgId);

    /**
     * @param pkgs the packages to sort
     * @return The packages sorted by type, name, version
     * @since 1.4
     */
    List<? extends Package> sort(List<? extends Package> pkgs);

    /**
     * @param packages
     * @param targetPlatform The target platform to be compliant with.
     * @return First non compliant package found. Null if none.
     * @throws PackageException
     * @since 1.4
     */
    @Deprecated
    String getNonCompliant(List<String> packages, String targetPlatform) throws PackageException;

    /**
     * @param packages
     * @param targetPlatform The target platform to be compliant with.
     * @return List of non compliant packages. Empty list if none.
     * @throws PackageException
     * @since 1.4.17
     */
    List<String> getNonCompliantList(List<String> packages, String targetPlatform) throws PackageException;

    /**
     * @param requestPkgStr
     * @param targetPlatform
     * @return true if {@code requestPkgStr} is compliant with {@code targetPlatform}
     * @throws PackageException
     */
    boolean matchesPlatform(String requestPkgStr, String targetPlatform) throws PackageException;

    /**
     * @since 1.4.11
     */
    DownloadablePackage findRemotePackageById(String packageId);

    /**
     * @since 1.4.11
     */
    DownloadablePackage findLocalPackageById(String packageId);

    /**
     * @since 1.4.11
     */
    DownloadablePackage getRemotePackage(String pkgId);

    /**
     * @since 1.4.11
     */
    DownloadablePackage getLocalPackage(String pkgId);

    /**
     * @since 1.4.11
     */
    boolean isInstalled(String pkgId);

    /**
     * @since 1.4.13
     */
    List<DownloadablePackage> findLocalPackages(String packageName);

    /**
     * @param allowSNAPSHOT Whether to allow SNAPSHOT versions or not. Even if not allowed, SNAPSHOT versions of a given
     *            package will be included if a SNAPSHOT version of that package is already installed. There may be
     *            other acceptance cases.
     * @since 1.4.13
     */
    DependencyResolution resolveDependencies(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform, boolean allowSNAPSHOT);

    /**
     * @param allowSNAPSHOT Whether to allow SNAPSHOT versions or not. Even if not allowed, SNAPSHOT versions of a given
     *            package will be included if a SNAPSHOT version of that package is already installed. There may be
     *            other acceptance cases.
     * @param doKeep Whether to keep the installed versions in the resolution.
     * @since 1.4.14
     */
    DependencyResolution resolveDependencies(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform, boolean allowSNAPSHOT, boolean doKeep);

    /**
     * @param allowSNAPSHOT Whether to allow SNAPSHOT versions or not. Even if not allowed, SNAPSHOT versions of a given
     *            package will be included if a SNAPSHOT version of that package is already installed. There may be
     *            other acceptance cases.
     * @param doKeep Whether to keep the installed versions in the resolution.
     * @param isSubResolution if true, do not check for optional dependencies on installed packages
     * @since 1.4.27
     */
    DependencyResolution resolveDependencies(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform, boolean allowSNAPSHOT, boolean doKeep, boolean isSubResolution);

    /**
     * @since public since 1.4.21, was protected before that
     */
    List<PackageSource> getAllSources();

    /**
     * Scan all installed packages unresolved optional dependencies and if some of them are being installed in the given
     * {@link DependencyResolution}, mark them for reinstallation. Also scan all installed packages resolved optional
     * dependencies and if some of them are being uninstalled in the given {@link DependencyResolution}, mark them for
     * reinstallation.
     *
     * @param res DependencyResolution listing packages to be installed
     * @since 1.4.26
     */
    void checkOptionalDependenciesOnInstalledPackages(DependencyResolution res);

}
