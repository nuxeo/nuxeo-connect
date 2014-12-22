/*
 * (C) Copyright 2006-2014 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *
 */

package org.nuxeo.connect.packages;

import java.util.ArrayList;
import java.util.Collection;
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
 * {@link PackageSource} implementation for listing packages already downloaded and potentially already installed.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class LocalPackageSource implements PackageSource {

    protected static final Log log = LogFactory.getLog(LocalPackageSource.class);

    @Override
    public String getName() {
        return "Local";
    }

    @Override
    public String getId() {
        return "local";
    }

    @Override
    public List<DownloadablePackage> listPackages() {
        List<DownloadablePackage> result = new ArrayList<>();
        PackageUpdateService pus = NuxeoConnectClient.getPackageUpdateService();
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

    @Override
    public List<DownloadablePackage> listPackages(PackageType type) {
        List<DownloadablePackage> all = listPackages();
        if (type == null) {
            return all;
        }
        List<DownloadablePackage> result = new ArrayList<>();
        for (DownloadablePackage pkg : all) {
            if (pkg.getType() != null && pkg.getType().equals(type)) {
                result.add(pkg);
            }
        }
        return result;
    }

    @Override
    public void flushCache() {
        // NOP
    }

    @Override
    public LocalPackageAsDownloadablePackage getPackageById(String packageId) {
        PackageUpdateService pus = NuxeoConnectClient.getPackageUpdateService();
        LocalPackageAsDownloadablePackage pkg = null;
        try {
            LocalPackage localPackage = pus.getPackage(packageId);
            if (localPackage != null) {
                pkg = new LocalPackageAsDownloadablePackage(localPackage);
            }
        } catch (PackageException e) {
            log.error("Error when getting local package " + packageId, e);
        }
        return pkg;
    }

    @Override
    public Collection<? extends DownloadablePackage> listPackagesByName(String packageName) {
        List<DownloadablePackage> result = new ArrayList<>();
        for (DownloadablePackage pkg : listPackages()) {
            if (packageName.equals(pkg.getName())) {
                result.add(pkg);
            }
        }
        return result;
    }

}
