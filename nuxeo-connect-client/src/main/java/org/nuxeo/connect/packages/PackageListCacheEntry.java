/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
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

import java.util.List;

import org.nuxeo.connect.data.DownloadablePackage;

public class PackageListCacheEntry {

    protected List<DownloadablePackage> pkgs;

    protected long ts;

    public PackageListCacheEntry(List<DownloadablePackage> pkgs) {
        this.pkgs = pkgs;
        ts = System.currentTimeMillis();
    }

    public List<DownloadablePackage> getPackages() {
        return pkgs;
    }

    public long getTimeStamp() {
        return ts;
    }

}
