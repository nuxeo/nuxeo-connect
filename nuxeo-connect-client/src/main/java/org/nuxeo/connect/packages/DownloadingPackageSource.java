/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *
 * $Id$
 */

package org.nuxeo.connect.packages;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.data.DownloadingPackage;
import org.nuxeo.connect.downloads.ConnectDownloadManager;
import org.nuxeo.connect.update.PackageType;

/**
 * {@link PackageSource} implementation for listing packages being downloaded.
 * 
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class DownloadingPackageSource implements PackageSource {

    protected static final Log log = LogFactory.getLog(DownloadingPackageSource.class);

    public String getName() {
        return "Downloading";
    }

    public String getId() {
        return "downloading";
    }

    public List<DownloadablePackage> listPackages() {

        List<DownloadablePackage> result = new ArrayList<DownloadablePackage>();
        ConnectDownloadManager cdm = NuxeoConnectClient.getDownloadManager();

        List<DownloadingPackage> pkgs = cdm.listDownloadingPackages();
        for (DownloadablePackage pkg : pkgs) {
            result.add(pkg);
        }

        return result;
    }

    public List<DownloadablePackage> listPackages(PackageType type) {
        List<DownloadablePackage> all = listPackages();
        List<DownloadablePackage> result = new ArrayList<DownloadablePackage>();

        for (DownloadablePackage pkg : all) {
            if (pkg.getType().equals(type)) {
                result.add(pkg);
            }
        }
        return result;
    }

    public void flushCache() {
        // NOP
    }

}
