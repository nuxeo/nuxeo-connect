/*
 * (C) Copyright 2006-2013 Nuxeo SA (http://nuxeo.com/) and contributors.
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

import java.util.List;
import java.util.Map;

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
 * Service interface that wraps all {@link PackageSource} to provide an unified
 * view The main purpose of this interface is to provide listing methods that
 * return the most up to date version of packages for given filters
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public interface PackageManager extends BasePackageManager {

    /**
     * @since 1.4
     */
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
     * Returns most recent version of {@link DownloadablePackage} from all
     * sources.
     */
    List<DownloadablePackage> listPackages();

    /**
     * Returns most recent version of {@link DownloadablePackage} from all
     * sources for given {@code targetPlatform}
     *
     * @since 1.4
     */
    List<DownloadablePackage> listPackages(String targetPlatform);

    /**
     * Returns most recent version of {@link DownloadablePackage} from all
     * sources for a given {@link PackageType}.
     */
    List<DownloadablePackage> listPackages(PackageType type);

    /**
     * Returns most recent version of {@link DownloadablePackage} from all
     * sources for given {@link PackageType} and {@code targetPlatform}
     *
     * @param pkgType
     * @param targetPlatform
     * @since 1.4
     */
    List<DownloadablePackage> listPackages(PackageType pkgType,
            String targetPlatform);

    /**
     * Search for packages. (Currently not implemented)
     */
    List<DownloadablePackage> searchPackages(String searchExpr);

    /**
     * Lists most recent version of {@link DownloadablePackage} locally present.
     * Local packages are not merged/filtered on latest versions
     */
    List<DownloadablePackage> listLocalPackages();

    /**
     * Lists most recent version of {@link DownloadablePackage} locally present
     * for a given {@link PackageType}.
     * Local packages are not merged/filtered on latest versions
     */
    List<DownloadablePackage> listLocalPackages(PackageType type);

    /**
     * Lists most recent version of {@link DownloadablePackage} available on
     * connect server.
     */
    List<DownloadablePackage> listRemotePackages();

    /**
     * Lists most recent version of {@link DownloadablePackage} available on
     * connect server for a given {@link PackageType}.
     */
    List<DownloadablePackage> listRemotePackages(PackageType type);

    /**
     * Lists most recent version of {@link DownloadablePackage} available on
     * connect server for a given {@link PackageType} and {@code targetPlatform}
     *
     * @param pkgType
     * @param targetPlatform
     * @since 1.4
     */
    List<DownloadablePackage> listRemotePackages(PackageType pkgType,
            String targetPlatform);

    /**
     * Lists all {@link DownloadablePackage} that are locally present and that
     * can be updated by a higher version available on connect server.
     */
    List<DownloadablePackage> listUpdatePackages();

    /**
     * Lists all {@link DownloadablePackage} that are locally present and that
     * can be updated by a higher version available on connect server for a
     * given {@link PackageType}.
     */
    List<DownloadablePackage> listUpdatePackages(PackageType type);

    /**
     * Lists all {@link DownloadablePackage} that are locally present and that
     * can be updated by a higher version available on connect server for a
     * given {@link PackageType} and target platform.
     */
    List<DownloadablePackage> listUpdatePackages(String targetPlatform);

    /**
     * Lists all {@link DownloadablePackage} that are locally present and that
     * can be updated by a higher version available on connect server for a
     * given target platform.
     */
    List<DownloadablePackage> listUpdatePackages(PackageType type,
            String targetPlatform);

    /**
     * Lists most recent version of {@link DownloadablePackage} available only
     * on the connect server (ie no local version).
     *
     * @deprecated Since 1.4. Use {@link #listOnlyRemotePackages(String)}
     *             instead.
     */
    @Deprecated
    List<DownloadablePackage> listOnlyRemotePackages();

    /**
     * Lists most recent version of {@link DownloadablePackage} available only
     * on the connect server (ie no local version).
     *
     * @param targetPlatform
     * @since 1.4
     */
    List<DownloadablePackage> listOnlyRemotePackages(String targetPlatform);

    /**
     * Lists most recent version of {@link DownloadablePackage} available only
     * on the connect server (ie no local version) for a given
     * {@link PackageType}.
     *
     * @deprecated Since 1.4. Use
     *             {@link #listOnlyRemotePackages(PackageType, String)} instead.
     */
    @Deprecated
    List<DownloadablePackage> listOnlyRemotePackages(PackageType type);

    /**
     * Lists most recent version of {@link DownloadablePackage} available only
     * on the connect server (ie no local version) for a given
     * {@link PackageType}.
     *
     * @param pkgType {@link PackageType}
     * @param targetPlatform
     * @since 1.4
     */
    List<DownloadablePackage> listOnlyRemotePackages(PackageType pkgType,
            String targetPlatform);

    /**
     * Lists all versions of the studio packages associated to user account.
     */
    List<DownloadablePackage> listAllStudioRemotePackages();

    /**
     * Lists all versions of the studio packages associated to user account in
     * remote and potentially overridden by a local package.
     */
    List<DownloadablePackage> listAllStudioRemoteOrLocalPackages();

    /**
     * Lists packages available in remote and potentially overridden by a local
     * package.
     *
     * @deprecated Since 1.4. Use {@link #listRemoteOrLocalPackages(String)}
     *             instead.
     */
    @Deprecated
    List<DownloadablePackage> listRemoteOrLocalPackages();

    /**
     * Lists packages available in remote and potentially overridden by a local
     * package.
     *
     * @param targetPlatform
     * @since 1.4
     */
    List<DownloadablePackage> listRemoteOrLocalPackages(String targetPlatform);

    /**
     * Lists packages availab.e in remote and potentially overridden by a local
     * package.
     *
     * @deprecated Since 1.4. Use
     *             {@link #listRemoteOrLocalPackages(PackageType, String)}
     *             instead.
     */
    @Deprecated
    List<DownloadablePackage> listRemoteOrLocalPackages(PackageType type);

    /**
     * Lists most recent version of {@link DownloadablePackage} available on
     * connect server for a given {@link PackageType}.
     *
     * @param pkgType {@link PackageType}
     * @param targetPlatform
     * @since 1.4
     */
    List<DownloadablePackage> listRemoteOrLocalPackages(PackageType pkgType,
            String targetPlatform);

    /**
     * Lists all local or remote private packages, filtered on package type if
     * {@code pkgType} is not null.
     *
     * @param pkgType {@link PackageType}
     * @param targetPlatform
     * @since 1.4
     */
    List<DownloadablePackage> listPrivatePackages(PackageType pkgType,
            String targetPlatform);

    /**
     * @since 1.4
     * @see #listPrivatePackages(PackageType, String)
     */
    List<DownloadablePackage> listPrivatePackages(String targetPlatform);

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
     *
     * @throws Exception
     */
    DownloadingPackage download(String packageId) throws Exception;

    /**
     * Get the Download descriptors for a given list of package ids
     *
     * @param packageIds
     *
     * @throws Exception
     */
    List<DownloadingPackage> download(List<String> packageIds) throws Exception;

    /**
     * Start installation process via {@link PackageUpdateService}
     *
     * @param packageId Identifier of the {@link Package} to install
     * @param params Installation parameters (as collected via Wizard's form)
     * @throws Exception
     */
    void install(String packageId, Map<String, String> params) throws Exception;

    /**
     * Serial installation of several packages
     *
     * @param packageIds List of identifiers of the {@link Package}s to install
     * @param params Installation parameters (as collected via Wizard's form)
     * @throws Exception
     */
    void install(List<String> packageIds, Map<String, String> params)
            throws Exception;

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
     */
    DependencyResolution resolveDependencies(String pkgId, String targetPlatform);

    /**
     * @param pkgInstall
     * @param pkgRemove
     * @param pkgUpgrade
     * @param targetPlatform
     * @since 1.4
     */
    DependencyResolution resolveDependencies(List<String> pkgInstall,
            List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform);

    /**
     * Returns the packages uninstalled if the given {@link Package} is removed
     *
     * @param pkg the {@link Package} that is being uninstalled
     * @return List of all {@link DownloadablePackage} that must be uninstalled
     *         too
     * @deprecated Since 1.4. Use
     *             {@link #getUninstallDependencies(Package, String)} instead.
     */
    @Deprecated
    List<DownloadablePackage> getUninstallDependencies(Package pkg);

    /**
     * @param pkg the {@link Package} that is being uninstalled
     * @param targetPlatform If null, the constraint on target platform is
     *            relaxed.
     * @return List of all {@link DownloadablePackage} that must be uninstalled
     *         too
     * @since 1.4
     */
    List<DownloadablePackage> getUninstallDependencies(Package pkg,
            String targetPlatform);

    /**
     * @return all the packages, in all versions, properly managing classifiers
     */
    List<DownloadablePackage> listAllPackages();

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
     * @since 1.4
     * @return a Map of all packages by Name
     */
    Map<String, List<DownloadablePackage>> getAllPackagesByName();

    /**
     * Return the available {@link Version} for a given {@link Package} name.
     * Versions are sorted in the "preferred order":
     * <ul>
     * <li>already installed version (means no upgrade and no download)</li>
     * <li>already downloaded version (means no download)</li>
     * <li>remote versions sorted by version number (higher => last)</li>
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
     * Find a {@link Package} by it's id (will find masked versions on the
     * contrary of {@link PackageManager} getPackage
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
     * Returns all {@link Package} versions for a given name and
     * {@link VersionRange}
     *
     * @param packageName
     * @since 1.4
     */
    List<Version> getAvailableVersion(String pkgName, VersionRange range,
            String targetPlatform);

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
    String getNonCompliant(List<String> packages, String targetPlatform)
            throws PackageException;

    /**
     * @param requestPkgStr
     * @param targetPlatform
     * @return true if {@code requestPkgStr} is compliant with
     *         {@code targetPlatform}
     * @throws PackageException
     */
    boolean matchesPlatform(String requestPkgStr, String targetPlatform)
            throws PackageException;

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

}
