/*
 * (C) Copyright 2010-2015 Nuxeo SA (http://nuxeo.com/) and contributors.
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

package org.nuxeo.connect.packages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.update.PackageType;

public class PackageListCache {
    /**
     * @since 1.4.19
     */
    public static final String STUDIO_REGISTERED_KEY = "StudioRegistered";

    /**
     * @since 1.4.18
     */
    public static final String CONNECT_CLIENT_CACHE_MINUTES_PROPERTY = "org.nuxeo.ecm.connect.client.cache";

    protected Map<String, PackageListCacheEntry> cache = new HashMap<>();

    // in minutes
    protected int cache_duration = 5;

    public PackageListCache() {
        String cacheParam = NuxeoConnectClient.getProperty(CONNECT_CLIENT_CACHE_MINUTES_PROPERTY, "5");
        cache_duration = Integer.parseInt(cacheParam);
    }

    public void add(List<DownloadablePackage> pkgs, String type) {
        cache.put(type, new PackageListCacheEntry(pkgs));
    }

    /**
     * @since 1.4.18
     */
    public void add(DownloadablePackage pkg) {
        PackageListCacheEntry packageListCacheEntry = cache.get(pkg.getType().toString());
        if (packageListCacheEntry == null) {
            packageListCacheEntry = new PackageListCacheEntry(new ArrayList<DownloadablePackage>());
            cache.put(pkg.getType().toString(), packageListCacheEntry);
        }
        packageListCacheEntry.getPackages().add(pkg);
        // Reset cache timestamp?
    }

    /**
     * @return null if no entry in cache or if entry is expired
     */
    public List<DownloadablePackage> getFromCache(String type) {
        PackageListCacheEntry entry = cache.get(type);
        if (entry == null || isExpired(entry)) {
            return null;
        }
        return entry.getPackages();
    }

    /**
     * @since 1.4.18
     */
    public DownloadablePackage getPackageByID(String packageId) {
        for (PackageListCacheEntry entry : cache.values()) {
            if (isExpired(entry)) {
                continue;
            }
            for (DownloadablePackage pkg : entry.getPackages()) {
                if (packageId.equals(pkg.getId())) {
                    return pkg;
                }
            }
        }
        return null;
    }

    /**
     * Is the given cache expired
     *
     * @see #CONNECT_CLIENT_CACHE_MINUTES_PROPERTY
     * @since 1.4.18
     */
    public boolean isExpired(PackageListCacheEntry entry) {
        return (System.currentTimeMillis() - entry.getTimeStamp() > cache_duration * 60 * 1000);
    }

    /**
     * Is the cache associated to the given type expired
     *
     * @see #isExpired(PackageListCacheEntry)
     * @since 1.4.18
     */
    public boolean isExpired(PackageType type) {
        return isExpired(cache.get(type.toString()));
    }

}
