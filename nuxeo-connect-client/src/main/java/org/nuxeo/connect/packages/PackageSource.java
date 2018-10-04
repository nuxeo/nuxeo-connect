/*
 * (C) Copyright 2006-2014 Nuxeo SA (http://nuxeo.com/) and others.
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

import java.util.Collection;
import java.util.List;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.PackageType;

/**
 * Interface for {@link Package} sources: classes providing access to {@link Package}.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public interface PackageSource {

    String getName();

    String getId();

    List<DownloadablePackage> listPackages();

    List<DownloadablePackage> listPackages(PackageType type);

    /**
     * @since 1.7.2
     */
    List<DownloadablePackage> listPackages(PackageType type, String currentTargetPlatform);

    void flushCache();

    /**
     * @since 1.4.18
     */
    DownloadablePackage getPackageById(String packageId);

    /**
     * @param packageName Must not be null
     * @since 1.4.18
     */
    Collection<? extends DownloadablePackage> listPackagesByName(String packageName);


    /**
     * @param packageName Must not be null
     * @since 1.7.2
     */
    Collection<? extends DownloadablePackage> listPackagesByName(String pkgName, String currentTargetPlatform);

    /**
     * @return Studio packages associated with the current registration.
     * @since 1.4.19
     */
    List<DownloadablePackage> listStudioPackages();

    /**
     * @param currentTargetPlatform
     * @return Studio packages associated with the current registration, filtered by the given currentTargetPlatform.
     * @since 1.7.2
     */
    List<DownloadablePackage> listStudioPackages(String currentTargetPlatform);


}
