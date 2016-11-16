/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Yannis JULIENNE
 */

package org.nuxeo.connect.packages;

import org.nuxeo.connect.data.DownloadablePackage;

/**
 * @since 1.4.26
 */
public class PackageCacheEntry {

    protected DownloadablePackage pkg;

    protected long ts;

    public PackageCacheEntry() {
        ts = System.currentTimeMillis();
    }

    public PackageCacheEntry(DownloadablePackage pkg) {
        this();
        this.pkg = pkg;
    }

    public DownloadablePackage getPackage() {
        return pkg;
    }

    public long getTimeStamp() {
        return ts;
    }

    public boolean isExpired(long cacheDurationInMinutes) {
        return (System.currentTimeMillis() - getTimeStamp() > cacheDurationInMinutes * 60 * 1000);
    }

}
