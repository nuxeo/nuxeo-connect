/*
 * (C) Copyright 2010-2015 Nuxeo SA (http://nuxeo.com/) and others.
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
import java.util.List;

import org.nuxeo.connect.data.DownloadablePackage;

public class PackageListCacheEntry {

    protected List<DownloadablePackage> pkgs = new ArrayList<>();

    protected long ts;

    /**
     * @since 1.4.21
     */
    public PackageListCacheEntry() {
        ts = System.currentTimeMillis();
    }

    /**
     * @param pkgs If null, <code>pkgs</code> is replaced with an empty list.
     */
    public PackageListCacheEntry(List<DownloadablePackage> pkgs) {
        this();
        if (pkgs != null) {
            this.pkgs = pkgs;
        }
    }

    public List<DownloadablePackage> getPackages() {
        return pkgs;
    }

    public long getTimeStamp() {
        return ts;
    }

}
