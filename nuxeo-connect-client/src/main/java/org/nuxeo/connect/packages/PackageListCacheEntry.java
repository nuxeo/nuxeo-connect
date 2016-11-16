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
import java.util.List;
import org.nuxeo.connect.data.DownloadablePackage;

public class PackageListCacheEntry {

    protected List<PackageCacheEntry> pkgEntries = new ArrayList<>();

    protected long ts;

    /**
     * @since 1.4.21
     */
    public PackageListCacheEntry() {
        // do not set the timestamp as the list is not filled yet
    }

    public PackageListCacheEntry(List<DownloadablePackage> pkgs) {
        if (pkgs != null) {
            for (DownloadablePackage pkg : pkgs) {
                pkgEntries.add(new PackageCacheEntry(pkg));
            }
            ts = System.currentTimeMillis();
        }
    }

    /**
     * @since 1.4.26
     */
    public List<PackageCacheEntry> getPackageCacheEntries(){
        return pkgEntries;
    }

    public long getTimeStamp() {
        return ts;
    }

    public List<DownloadablePackage> getPackages() {
        List<DownloadablePackage> result = new ArrayList<>();
        for (PackageCacheEntry pkgEntry : pkgEntries) {
            result.add(pkgEntry.getPackage());
        }
        return result;
    }

    /**
     * @since 1.4.26
     */
    public boolean isExpired(long cacheDurationInMinutes) {
        return (System.currentTimeMillis() - getTimeStamp() > cacheDurationInMinutes * 60 * 1000);
    }

}
