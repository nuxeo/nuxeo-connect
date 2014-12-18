/*
 * (C) Copyright 2010-2014 Nuxeo SA (http://nuxeo.com/) and contributors.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.data.DownloadablePackage;

public class PackageListCache {

    protected Map<String, PackageListCacheEntry> cache = new HashMap<>();

    // in minutes
    protected int cache_duration = 5;

    public PackageListCache() {
        String cacheParam = NuxeoConnectClient.getProperty("org.nuxeo.ecm.connect.client.cache", "5");
        cache_duration = Integer.parseInt(cacheParam);
    }

    public void add(List<DownloadablePackage> pkgs, String type) {
        cache.put(type, new PackageListCacheEntry(pkgs));
    }

    public List<DownloadablePackage> getFromCache(String type) {

        PackageListCacheEntry entry = cache.get(type);

        if (entry!=null) {
            long delta = System.currentTimeMillis() - entry.getTimeStamp();
            if (delta < (cache_duration*60*1000)) {
                return entry.getPackages();
            }
        }
        return null;
    }

}
