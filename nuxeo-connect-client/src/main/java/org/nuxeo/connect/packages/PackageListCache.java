/*
 * (C) Copyright 2010-2016 Nuxeo SA (http://nuxeo.com/) and others.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.update.PackageType;

/**
 * @since 1.0
 */
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
            packageListCacheEntry = new PackageListCacheEntry();
            cache.put(pkg.getType().toString(), packageListCacheEntry);
        }
        packageListCacheEntry.getPackageCacheEntries().add(new PackageCacheEntry(pkg));
    }

    /**
     * @return an empty list if no entry in cache or if entry is expired
     */
    public List<DownloadablePackage> getFromCache(String type) {
        PackageListCacheEntry entry = cache.get(type);
        if (entry == null || entry.isExpired(cache_duration)) {
            return new ArrayList<>();
        }
        return entry.getPackages();
    }

    /**
     * @since 1.4.18
     */
    public DownloadablePackage getPackageByID(String packageId) {
        for (PackageListCacheEntry entry : cache.values()) {
            for (PackageCacheEntry pkgEntry : entry.getPackageCacheEntries()) {
                if (packageId.equals(pkgEntry.getPackage().getId())) {
                    if (pkgEntry.isExpired(cache_duration)) {
                        return null;
                    }
                    return pkgEntry.getPackage();
                }
            }
        }
        return null;
    }

    /**
     * Is the cache associated to the given type expired
     *
     * @see #isExpired(PackageListCacheEntry)
     * @since 1.4.18
     */
    public boolean isExpired(PackageType type) {
        PackageListCacheEntry packageListCacheForType = cache.get(type.toString());
        return packageListCacheForType == null || packageListCacheForType.isExpired(cache_duration);
    }

}
