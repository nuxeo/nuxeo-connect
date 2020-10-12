/*
 * (C) Copyright 2010-2018 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Nuxeo
 *     Yannis JULIENNE
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
import org.nuxeo.connect.platform.PlatformId;
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
        return listPackages(null, null);
    }

    @Override
    public List<DownloadablePackage> listStudioPackages(PlatformId currentTargetPlatform) {
        List<DownloadablePackage> result = new ArrayList<>();
        if (!NuxeoConnectClient.getConnectGatewayComponent().isInstanceRegistered()) {
            log.info("Server is not registered");
            return result;
        }
        String cacheKey = PackageListCache.STUDIO_REGISTERED_KEY
                + ((currentTargetPlatform != null) ? "_" + currentTargetPlatform.asString() : "");
        result = cache.getFromCache(cacheKey);
        if (!result.isEmpty()) {
            return result;
        }
        try {
            ConnectRegistrationService crs = NuxeoConnectClient.getConnectRegistrationService();
            result = crs.getConnector().getRegisteredStudio(currentTargetPlatform);
            cache.add(result, cacheKey);
        } catch (ConnectServerError e) {
            log.debug(e, e);
            log.warn("Unable to fetch remote packages list: " + e.getMessage());
            // do not store an empty list to force retries
        }
        return result;
    }

    @Override
    public List<DownloadablePackage> listPackages(PackageType type, PlatformId currentTargetPlatform) {
        if (type == null) {
            List<DownloadablePackage> all = new ArrayList<>();
            for (PackageType pkgType : PackageType.values()) {
                all.addAll(listPackages(pkgType, currentTargetPlatform));
            }
            return all;
        }
        String cacheKey = type.toString()
                + ((currentTargetPlatform != null) ? "_" + currentTargetPlatform.asString() : "");
        List<DownloadablePackage> result = cache.getFromCache(cacheKey);
        if (!result.isEmpty()) {
            return result;
        }
        try {
            ConnectRegistrationService crs = NuxeoConnectClient.getConnectRegistrationService();
            result = crs.getConnector().getDownloads(type, currentTargetPlatform);
        } catch (ConnectServerError e) {
            log.debug(e, e);
            log.warn("Unable to fetch remote packages list: " + e.getMessage());
            // store an empty list to avoid calling back the server since anyway we probably have no connection...
            result = new ArrayList<>();
        }
        cache.add(result, cacheKey);
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
    public Collection<? extends DownloadablePackage> listPackagesByName(String packageName,
            PlatformId currentTargetPlatform) {
        List<DownloadablePackage> result = new ArrayList<>();
        for (PackageType type : PackageType.values()) {
            for (DownloadablePackage pkg : listPackages(type, currentTargetPlatform)) {
                if (packageName.equals(pkg.getName())) {
                    result.add(pkg);
                }
            }
        }
        return result;
    }

}
