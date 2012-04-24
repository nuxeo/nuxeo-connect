/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 * $Id$
 */

package org.nuxeo.connect.packages;

import java.util.List;
import java.util.Map;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.data.DownloadingPackage;
import org.nuxeo.connect.packages.dependencies.DependencyResolution;
import org.nuxeo.connect.packages.dependencies.DependencyResolver;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.PackageType;
import org.nuxeo.connect.update.PackageUpdateService;

/**
 * Service interface that wraps all {@link PackageSource} to provide an unified
 * view
 * The main purpose of this interface is to provide listing methods that return
 * the
 * most up to date version of packages for given filters
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public interface PackageManager extends BasePackageManager {

    /**
     * Returns most recent version of {@link DownloadablePackage} from all
     * sources.
     */
    List<DownloadablePackage> listPackages();

    /**
     * Returns most recent version of {@link DownloadablePackage} from all
     * sources for a give {@link PackageType}.
     */
    List<DownloadablePackage> listPackages(PackageType type);

    /**
     * Search for packages. (Currently not implemented)
     */
    List<DownloadablePackage> searchPackages(String searchExpr);

    /**
     * Lists most recent version of {@link DownloadablePackage} locally present.
     */
    List<DownloadablePackage> listLocalPackages();

    /**
     * Lists most recent version of {@link DownloadablePackage} locally present
     * for a give {@link PackageType}.
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
     * Lists all {@link DownloadablePackage} that are locally present
     * and that can be updated by a higher version available on connect server.
     */
    List<DownloadablePackage> listUpdatePackages();

    /**
     * Lists all {@link DownloadablePackage} that are locally present
     * and that can be updated by a higher version available on connect server
     * for a given {@link PackageType}.
     */
    List<DownloadablePackage> listUpdatePackages(PackageType type);

    /**
     * Lists most recent version of {@link DownloadablePackage} available only
     * on the connect server
     * (ie no local version).
     */
    List<DownloadablePackage> listOnlyRemotePackages();

    /**
     * Lists most recent version of {@link DownloadablePackage} available only
     * on the connect server (ie no local version)
     * for a given {@link PackageType}.
     */
    List<DownloadablePackage> listOnlyRemotePackages(PackageType type);

    /**
     * Lists all versions of the studio packages associated to user account.
     */
    List<DownloadablePackage> listAllStudioRemotePackages();

    /**
     * Lists all versions of the studio packages associated to user account in
     * remote and potentially
     * overridden by a local package.
     */
    List<DownloadablePackage> listAllStudioRemoteOrLocalPackages();

    /**
     * Lists packages available in remote and potentially overridden by a local
     * package.
     */
    List<DownloadablePackage> listRemoteOrLocalPackages();

    /**
     * Lists packages availab.e in remote and potentially overridden by a local
     * package.
     */
    List<DownloadablePackage> listRemoteOrLocalPackages(PackageType type);

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
     * Returns the packages uninstalled if the given {@link Package} is removed
     *
     * @param pkg the {@link Package} that is being uninstalled
     * @return List of all {@link DownloadablePackage} that must be uninstalled
     *         too
     */
    List<DownloadablePackage> getUninstallDependencies(Package pkg);
}
