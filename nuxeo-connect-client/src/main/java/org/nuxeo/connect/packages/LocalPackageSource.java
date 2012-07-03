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
import org.nuxeo.connect.update.LocalPackage;
import org.nuxeo.connect.update.PackageException;
import org.nuxeo.connect.update.PackageType;
import org.nuxeo.connect.update.PackageUpdateService;

/**
 * {@link PackageSource} implementation for listing packages already downloaded
 * and potentially already installed.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class LocalPackageSource implements PackageSource {

    protected static final Log log = LogFactory.getLog(LocalPackageSource.class);

    public String getName() {
        return "Local";
    }

    public String getId() {
        return "local";
    }

    public List<DownloadablePackage> listPackages() {
        List<DownloadablePackage> result = new ArrayList<DownloadablePackage>();
        PackageUpdateService pus = NuxeoConnectClient.getPackageUpdateService();
        if (pus == null) {
            log.error("Unable to locate PackageUpdateService");
            return result;
        }
        try {
            List<LocalPackage> pkgs = pus.getPackages();
            for (LocalPackage pkg : pkgs) {
                result.add(new LocalPackageAsDownloadablePackage(pkg));
            }
        } catch (PackageException e) {
            log.error("Error when getting local packages", e);
        }
        return result;
    }

    public List<DownloadablePackage> listPackages(PackageType type) {
        List<DownloadablePackage> all = listPackages();
        if (type == null) {
            return all;
        }
        List<DownloadablePackage> result = new ArrayList<DownloadablePackage>();
        for (DownloadablePackage pkg : all) {
            if (pkg.getType() != null && pkg.getType().equals(type)) {
                result.add(pkg);
            }
        }
        return result;
    }

    public void flushCache() {
        // NOP
    }

}
