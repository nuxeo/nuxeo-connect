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

import java.util.List;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.Version;
import org.nuxeo.connect.update.VersionRange;

/**
 * Interface used by the Dependency resolution system to access the
 * {@link Package}
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public interface InternalPackageManager extends PackageManager {

    /**
     * Return the available {@link Version} for a given {@link Package} name.
     * Versions are sorted in the "preferred order" :
     * - already installed version (means no upgrade and no download)
     * - already downloaded version (means no download)
     * - remote versions sorted by version number (higher => last)
     *
     * @param pkgName
     */
    List<Version> getPreferedVersions(String pkgName);

    /**
     * Returns all remote {@link Package} versions for a given name
     *
     * @param packageName
     */
    List<DownloadablePackage> findRemotePackages(String packageName);

    /**
     * Find a {@link Package} by it's id
     * (will find masked versions on the contrary of {@link PackageManager}
     * getPackage
     *
     * @param packageId
     */
    DownloadablePackage findPackageById(String packageId);

    /**
     * Returns all local {@link Package} versions for a given name
     *
     * @param packageName
     */
    List<Version> findLocalPackageVersions(String packageName);

    /**
     * Returns all local {@link Package} installed versions for a given name
     *
     * @param packageName
     */
    List<Version> findLocalPackageInstalledVersions(String packageName);

    /**
     * Returns all {@link Package} versions for a given name and
     * {@link VersionRange}
     *
     * @param packageName
     */
    List<Version> getAvailableVersion(String pkgName, VersionRange range,
            String targetPlatform);
}
