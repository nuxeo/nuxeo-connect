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
import org.nuxeo.connect.packages.dependencies.DependencyException;
import org.nuxeo.connect.packages.dependencies.DependencyResolution;
import org.nuxeo.connect.update.PackageType;

/**
 * Service interface that wrapps all {@link PackageSource} to provide an unified view
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public interface PackageManager {

    /**
     * Returns all {@link DownloadablePackage} from all sources.
     */
    List<DownloadablePackage> listPackages();

    /**
     * Returns all {@link DownloadablePackage} from all sources for a give {@link PackageType}.
     */
    List<DownloadablePackage> listPackages(PackageType type);

    /**
     * Search for packages.
     */
    List<DownloadablePackage> searchPackages(String searchExpr);

    /**
     * Lists all {@link DownloadablePackage} locally present.
     */
    List<DownloadablePackage> listLocalPackages();

    /**
     * Lists all {@link DownloadablePackage} locally present for a give {@link PackageType}.
     */
    List<DownloadablePackage> listLocalPackages(PackageType type);

    /**
     * Lists all {@link DownloadablePackage} available on connect server.
     */
    List<DownloadablePackage> listRemotePackages();

    /**
     * Lists all {@link DownloadablePackage} available on connect server for a given {@link PackageType}.
     */
    List<DownloadablePackage> listRemotePackages(PackageType type);

    /**
     * Lists all {@link DownloadablePackage} that are locally present
     * and that can be updated by a higher version available on connect server.
     */
    List<DownloadablePackage> listUpdatePackages();

    /**
     * Lists all {@link DownloadablePackage} that are locally present
     * and that can be updated by a higher version available on connect server for a given {@link PackageType}.
     */
    List<DownloadablePackage> listUpdatePackages(PackageType type);

    /**
     * Lists all {@link DownloadablePackage} available only on the connect server
     * (ie no local version).
     */
    List<DownloadablePackage> listOnlyRemotePackages();

    /**
     * Lists all {@link DownloadablePackage} available only on the connect server (ie no local version)
     * for a given {@link PackageType}.
     */
    List<DownloadablePackage> listOnlyRemotePackages(PackageType type);

    /**
     * Lists all version of the studio packages associated to user account.
     */
    List<DownloadablePackage> listAllStudioRemotePackages();

    /**
     * Lists all version of the studio packages associated to user account in remote and potentially
     * overridden by a local package.
     */
    List<DownloadablePackage> listAllStudioRemoteOrLocalPackages();

    /**
     * Lists packages available in remote and potentially overridden by a local package.
     */
    List<DownloadablePackage> listRemoteOrLocalPackages();

    /**
     * Lists packages availab.e in remote and potentially overridden by a local package.
     */
    List<DownloadablePackage> listRemoteOrLocalPackages(PackageType type);

    void registerSource(PackageSource source, boolean local);

    DownloadingPackage download(String packageId) throws Exception ;

    void install(String packageId, Map<String, String> params) throws Exception;

    DownloadablePackage getPackage(String pkgId);

    DownloadablePackage getLocalPackage(String pkgId);

    DownloadablePackage getRemotePackage(String pkgId);

    void flushCache();

    List<DownloadablePackage> listInstalledPackages();

    DependencyResolution resolveDependencies(String pkgId, String targetPlatform) throws DependencyException ;

}
