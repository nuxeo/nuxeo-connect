/*
 * (C) Copyright 2006-2018 Nuxeo SA (http://nuxeo.com/) and others.
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
 */
package org.nuxeo.connect.connector;

import java.util.List;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.data.DownloadingPackage;
import org.nuxeo.connect.data.SubscriptionStatus;
import org.nuxeo.connect.update.PackageType;

/**
 * Interface for APIs exposed by the Connect Server.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public interface ConnectConnector {

    /**
     * @param type Can be null since 1.4
     * @return All type packages or all packages if type is null. Must not be null.
     * @throws ConnectServerError
     * @deprecated since 1.7.2, prefer to use {@link #getDownloads(PackageType, String)}
     */
    @Deprecated
    List<DownloadablePackage> getDownloads(PackageType type) throws ConnectServerError;

    /**
     * @param type The type of packages to retrieve, can be null.
     * @param currentTargetPlatform The target platform the retrieved packages must be compatible with, can be null.
     * @return The list of the downloadable packages of the given type, for the given targetPlatform. May be empty.
     * @throws ConnectServerError
     * @since 1.7.2
     */
    List<DownloadablePackage> getDownloads(PackageType type, String currentTargetPlatform) throws ConnectServerError;

    DownloadingPackage getDownload(String id) throws ConnectServerError;

    SubscriptionStatus getConnectStatus() throws ConnectServerError;

    /**
     * @since 1.4
     */
    void flushCache();

    /**
     * @since 1.4.19
     * @return Must not be null.
     * @deprecated since 1.7.2, prefer to use {@link #getRegisteredStudio(String)}
     */
    @Deprecated
    List<DownloadablePackage> getRegisteredStudio() throws ConnectServerError;

    /**
     * @since 1.7.2
     * @return The list of the downloadable Studio packages for the given targetPlatform. May be empty.
     */
    List<DownloadablePackage> getRegisteredStudio(String currentTargetPlatform) throws ConnectServerError;

    /**
     * Renews the current registration with the Connect server.
     *
     * @return the new clid in one-line form
     * @since 1.4.24.2
     */
    String remoteRenewRegistration() throws ConnectServerError;

}
