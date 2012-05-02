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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.PackageManager;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.Version;
import org.nuxeo.connect.update.VersionRange;

/**
 * @since 5.6
 */
public class CUDFHelper {

    private static final Log log = LogFactory.getLog(CUDFHelper.class);

    public static final String newLine = System.getProperty("line.separator");

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
            if (pm != null) {
                nuxeoCUDFPackage.setInstalled(pm.isInstalled(pkg));
            }
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
        if (log.isDebugEnabled() || true) {
            MapUtils.verbosePrint(System.out, "nuxeo2CUDFMap", nuxeo2CUDFMap);
            MapUtils.verbosePrint(System.out, "CUDF2NuxeoMap", CUDF2NuxeoMap);
        }
    }

    protected List<DownloadablePackage> getAllPackages() {
        return pm.listAllPackages();
    }

    public NuxeoCUDFPackage getCUDFPackage(String cudfKey) {
        return CUDF2NuxeoMap.get(cudfKey);
    }

    public Map<Version, NuxeoCUDFPackage> getCUDFPackages(String cudfName) {
        return nuxeo2CUDFMap.get(cudfName);
    }

    /**
     * @return a CUDF universe as a String
     */
    public String getCUDFFile() {
        StringBuilder sb = new StringBuilder();
        for (String cudfKey : CUDF2NuxeoMap.keySet()) {
            NuxeoCUDFPackage cudfPackage = CUDF2NuxeoMap.get(cudfKey);
            sb.append(cudfPackage.getCUDFStanza());
            sb.append(NuxeoCUDFPackage.CUDF_DEPENDS
                    + formatCUDF(cudfPackage.getDependencies()) + newLine);
            sb.append(NuxeoCUDFPackage.CUDF_CONFLICTS
                    + formatCUDF(cudfPackage.getConflicts()) + newLine);
            sb.append(NuxeoCUDFPackage.CUDF_PROVIDES
                    + formatCUDF(cudfPackage.getProvides()) + newLine);
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }

    private String formatCUDF(PackageDependency[] dependencies) {
        if (dependencies == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (PackageDependency packageDependency : dependencies) {
            Map<Version, NuxeoCUDFPackage> versionsMap = nuxeo2CUDFMap.get(packageDependency.getName());
            VersionRange versionRange = packageDependency.getVersionRange();
            int cudfMinVersion, cudfMaxVersion;
            if (versionRange.getMinVersion() == null) {
                cudfMinVersion = -1;
            } else {
                NuxeoCUDFPackage cudfPackage = versionsMap.get(versionRange.getMinVersion());
                cudfMinVersion = (cudfPackage == null) ? -1
                        : cudfPackage.getCUDFVersion();
            }
            if (versionRange.getMaxVersion() == null) {
                cudfMaxVersion = -1;
            } else {
                NuxeoCUDFPackage cudfPackage = versionsMap.get(versionRange.getMaxVersion());
                cudfMaxVersion = (cudfPackage == null) ? -1
                        : cudfPackage.getCUDFVersion();
            }
            if (cudfMinVersion == cudfMaxVersion) {
                if (cudfMinVersion == -1) {
                    sb.append(packageDependency.getName() + ", ");
                } else {
                    sb.append(packageDependency.getName() + " = "
                            + cudfMinVersion + ", ");
                }
                continue;
            }
            if (cudfMinVersion != -1) {
                sb.append(packageDependency.getName() + " >= " + cudfMinVersion
                        + ", ");
            }
            if (cudfMaxVersion != -1) {
                sb.append(packageDependency.getName() + " <= " + cudfMaxVersion
                        + ", ");
            }
        }
        if (sb.length() > 0) { // remove ending comma
            return sb.toString().substring(0, sb.length() - 2);
        } else {
            return "";
        }
    }

    /**
     * @param pkgInstall
     * @param pkgRemove
     * @param pkgUpgrade
     * @return a CUDF string with packages universe and request stanza
     */
    public String getCUDFFile(PackageDependency[] pkgInstall,
            PackageDependency[] pkgRemove, PackageDependency[] pkgUpgrade) {
        StringBuilder sb = new StringBuilder(getCUDFFile());
        sb.append(NuxeoCUDFPackage.CUDF_REQUEST + newLine);
        sb.append(NuxeoCUDFPackage.CUDF_INSTALL + formatCUDF(pkgInstall)
                + newLine);
        sb.append(NuxeoCUDFPackage.CUDF_REMOVE + formatCUDF(pkgRemove)
                + newLine);
        sb.append(NuxeoCUDFPackage.CUDF_UPGRADE + formatCUDF(pkgUpgrade)
                + newLine);
        return sb.toString();
    }

}