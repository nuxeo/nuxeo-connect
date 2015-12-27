/*
 * (C) Copyright 2006-2015 Nuxeo SA (http://nuxeo.com/) and others.
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
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.connector.ConnectServerError;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.registration.ConnectRegistrationService;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.PackageType;

/**
 * Implements {@link PackageSource} for remote {@link Package} hosted on Nuxeo Connect Server.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class RemotePackageSource extends AbstractPackageSource implements PackageSource {

    protected static final Log log = LogFactory.getLog(RemotePackageSource.class);

    protected PackageListCache cache;

    public RemotePackageSource() {
        cache = new PackageListCache();
        id = "remote";
        name = "Connect Server";
    }

    @Override
    public List<DownloadablePackage> listPackages() {
        List<DownloadablePackage> all = new ArrayList<>();
        for (PackageType type : PackageType.values()) {
            all.addAll(listPackages(type));
        }
        return all;
    }

    @Override
    public List<DownloadablePackage> listStudioPackages() {
        List<DownloadablePackage> result = new ArrayList<>();
        if (!NuxeoConnectClient.getConnectGatewayComponent().isInstanceRegistred()) {
            log.info("Server is not registered");
            return result;
        }
        result = cache.getFromCache(PackageListCache.STUDIO_REGISTERED_KEY);
        if (!result.isEmpty()) {
            return result;
        }
        try {
            ConnectRegistrationService crs = NuxeoConnectClient.getConnectRegistrationService();
            result = crs.getConnector().getRegisteredStudio();
            cache.add(result, PackageListCache.STUDIO_REGISTERED_KEY);
        } catch (ConnectServerError e) {
            log.debug(e, e);
            log.warn("Unable to fetch remote packages list: " + e.getMessage());
            // do not store an empty list to force retries
        }
        return result;
    }

    @Override
    public List<DownloadablePackage> listPackages(PackageType type) {
        if (type == null) {
            return listPackages();
        }
        List<DownloadablePackage> result = cache.getFromCache(type.toString());
        if (!result.isEmpty()) {
            return result;
        }
        try {
            ConnectRegistrationService crs = NuxeoConnectClient.getConnectRegistrationService();
            result = crs.getConnector().getDownloads(type);
        } catch (ConnectServerError e) {
            log.debug(e, e);
            log.warn("Unable to fetch remote packages list: " + e.getMessage());
            // store an empty list to avoid calling back the server since anyway we probably have no connection...
            result = new ArrayList<>();
        }
        cache.add(result, type.toString());
        return result;
    }

    @Override
    public void flushCache() {
        // memory cache
        cache = new PackageListCache();
        // disk cache
        ConnectRegistrationService crs = NuxeoConnectClient.getConnectRegistrationService();
        crs.getConnector().flushCache();
    }

    @Override
    public DownloadablePackage getPackageById(String packageId) {
        DownloadablePackage pkg = cache.getPackageByID(packageId);
        if (pkg == null) {
            ConnectRegistrationService crs = NuxeoConnectClient.getConnectRegistrationService();
            try {
                pkg = crs.getConnector().getDownload(packageId);
            } catch (ConnectServerError e) {
                log.debug(e, e);
                log.warn("Unable to fetch remote package with ID '" + packageId + "': " + e.getMessage());
            }
            if (pkg != null) {
                cache.add(pkg);
            }
        }
        return pkg;
    }

    @Override
    public Collection<? extends DownloadablePackage> listPackagesByName(String packageName) {
        List<DownloadablePackage> result = new ArrayList<>();
        for (PackageType type : PackageType.values()) {
            for (DownloadablePackage pkg : listPackages(type)) {
                if (packageName.equals(pkg.getName())) {
                    result.add(pkg);
                }
            }
        }
        return result;
    }

}
