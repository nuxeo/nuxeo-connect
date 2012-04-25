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
 *      Mathieu Guillaume
 *
 */

package org.nuxeo.connect.packages.dependencies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.MapUtils;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.PackageManager;
import org.nuxeo.connect.update.Version;

/**
 * @since 5.6
 */
public class CUDFHelper {

    protected PackageManager pm;

    protected Map<String, Map<Version, NuxeoCUDFPackage>> nuxeo2CUDFMap = new HashMap<String, Map<Version, NuxeoCUDFPackage>>();

    protected Map<String, NuxeoCUDFPackage> CUDF2NuxeoMap = new HashMap<String, NuxeoCUDFPackage>();

    public CUDFHelper(PackageManager pm) {
        this.pm = pm;
        initMapping();
    }

    public void resetMapping() {
        pm.flushCache();
        initMapping();
    }

    /**
     * Map "name, version-classifier" to "name-classifier, version" (with
     * -SNAPSHOT being a specific case)
     */
    public void initMapping() {
        List<DownloadablePackage> allPackages = getAllPackages();
        // for each unique "name-classifier", sort versions so we can attribute
        // them a "CUDF posint" version
        // populate Nuxeo2CUDFMap and the reverse CUDF2NuxeoMap
        for (DownloadablePackage pkg : allPackages) {
            NuxeoCUDFPackage nuxeoCUDFPackage = new NuxeoCUDFPackage(pkg);
            Map<Version, NuxeoCUDFPackage> pkgVersions = nuxeo2CUDFMap.get(nuxeoCUDFPackage.getCUDFName());
            if (pkgVersions == null) {
                pkgVersions = new TreeMap<Version, NuxeoCUDFPackage>();
                nuxeo2CUDFMap.put(nuxeoCUDFPackage.getCUDFName(), pkgVersions);
            }
            pkgVersions.put(nuxeoCUDFPackage.getNuxeoVersion(),
                    nuxeoCUDFPackage);
        }
        for (String key : nuxeo2CUDFMap.keySet()) {
            Map<Version, NuxeoCUDFPackage> pkgVersions = nuxeo2CUDFMap.get(key);
            int posInt = 1;
            for (Version version : pkgVersions.keySet()) {
                NuxeoCUDFPackage pkg = pkgVersions.get(version);
                pkg.setCUDFVersion(posInt++);
                CUDF2NuxeoMap.put(
                        pkg.getCUDFName() + "-" + pkg.getCUDFVersion(), pkg);
            }
        }
        MapUtils.verbosePrint(System.out, "nuxeo2CUDFMap", nuxeo2CUDFMap);
        MapUtils.verbosePrint(System.out, "CUDF2NuxeoMap", CUDF2NuxeoMap);
    }

    protected List<DownloadablePackage> getAllPackages() {
        return pm.listAllPackages();
    }

}