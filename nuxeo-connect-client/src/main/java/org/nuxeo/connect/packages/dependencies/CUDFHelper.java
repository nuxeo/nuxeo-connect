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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.equinox.p2.cudf.solver.OptimizationFunction.Criteria;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.InternalPackageManager;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.Version;
import org.nuxeo.connect.update.VersionRange;

/**
 * @since 1.4
 */
public class CUDFHelper {

    private static final Log log = LogFactory.getLog(CUDFHelper.class);

    public static final String newLine = System.getProperty("line.separator");

    protected InternalPackageManager pm;

    /**
     * Map of all NuxeoCUDFPackage per Nuxeo version, per package name
     *
     * nuxeo2CUDFMap = { "pkgName", { nuxeoVersion, NuxeoCUDFPackage }}
     */
    protected Map<String, Map<Version, NuxeoCUDFPackage>> nuxeo2CUDFMap = new HashMap<String, Map<Version, NuxeoCUDFPackage>>();

    /**
     * Map of all NuxeoCUDFPackage per CUDF unique ID (pkgName-pkgCUDFVersion)
     *
     * CUDF2NuxeoMap = { "pkgName-pkgCUDFVersion", NuxeoCUDFPackage }
     */
    protected Map<String, NuxeoCUDFPackage> CUDF2NuxeoMap = new HashMap<String, NuxeoCUDFPackage>();

    private String targetPlatform;

    public CUDFHelper(InternalPackageManager pm) {
        this.pm = pm;
    }

    /**
     * Map "name, version-classifier" to "name-classifier, version" (with
     * -SNAPSHOT being a specific case)
     *
     * @param packagesInRequest
     */
    public void initMapping() {
        nuxeo2CUDFMap.clear();
        CUDF2NuxeoMap.clear();
        List<DownloadablePackage> allPackages = getAllPackages();
        // for each unique "name-classifier", sort versions so we can attribute
        // them a "CUDF posint" version
        // populate Nuxeo2CUDFMap and the reverse CUDF2NuxeoMap
        for (DownloadablePackage pkg : allPackages) {
            // ignore incompatible packages when a targetPlatform is set
            if (targetPlatform != null
                    && !pkg.isLocal()
                    && !TargetPlatformFilterHelper.isCompatibleWithTargetPlatform(
                            pkg, targetPlatform)) {
                continue;
            }
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
        if (log.isDebugEnabled()) {
            MapUtils.verbosePrint(System.out, "nuxeo2CUDFMap", nuxeo2CUDFMap);
            MapUtils.verbosePrint(System.out, "CUDF2NuxeoMap", CUDF2NuxeoMap);
        }
    }

    protected List<DownloadablePackage> getAllPackages() {
        return pm.listAllPackages();
    }

    /**
     *
     * @param cudfKey in the form "pkgName-pkgCUDFVersion"
     * @return NuxeoCUDFPackage corresponding to the given cudfKey
     */
    public NuxeoCUDFPackage getCUDFPackage(String cudfKey) {
        return CUDF2NuxeoMap.get(cudfKey);
    }

    /**
     *
     * @param cudfName a package name
     * @return all NuxeoCUDFPackage versions corresponding to the given package
     */
    public Map<Version, NuxeoCUDFPackage> getCUDFPackages(String cudfName) {
        return nuxeo2CUDFMap.get(cudfName);
    }

    /**
     * @param pkgName a package name
     * @return the NuxeoCUDFPackage corresponding to the given package name
     *         which is installed. Null if not found.
     */
    public NuxeoCUDFPackage getInstalledCUDFPackage(String pkgName) {
        Map<Version, NuxeoCUDFPackage> packages = getCUDFPackages(pkgName);
        if (packages != null) {
            for (NuxeoCUDFPackage pkg : packages.values()) {
                if (pkg.isInstalled()) {
                    return pkg;
                }
            }
        }
        return null;
    }

    /**
     * @return a CUDF universe as a String
     * @throws DependencyException
     */
    public String getCUDFFile() throws DependencyException {
        initMapping();
        StringBuilder sb = new StringBuilder();
        for (String cudfKey : CUDF2NuxeoMap.keySet()) {
            NuxeoCUDFPackage cudfPackage = CUDF2NuxeoMap.get(cudfKey);
            sb.append(cudfPackage.getCUDFStanza());
            sb.append(NuxeoCUDFPackage.CUDF_DEPENDS
                    + formatCUDF(cudfPackage.getDependencies(), false)
                    + newLine);
            sb.append(NuxeoCUDFPackage.CUDF_CONFLICTS
                    + formatCUDF(cudfPackage.getConflicts(), false) + newLine);
            sb.append(NuxeoCUDFPackage.CUDF_PROVIDES
                    + formatCUDF(cudfPackage.getProvides(), false) + newLine);
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }

    private String formatCUDF(PackageDependency[] dependencies,
            boolean failOnError) throws DependencyException {
        if (dependencies == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (PackageDependency packageDependency : dependencies) {
            String cudfName = NuxeoCUDFPackage.getCUDFName(packageDependency);
            Map<Version, NuxeoCUDFPackage> versionsMap = nuxeo2CUDFMap.get(cudfName);
            if (versionsMap == null) {
                String errMsg = "Missing mapping for " + packageDependency
                        + " with target platform " + targetPlatform;
                if (failOnError) {
                    throw new DependencyException(errMsg);
                } else {
                    log.warn(errMsg);
                    continue;
                }
            }
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
                    sb.append(cudfName + ", ");
                } else {
                    sb.append(cudfName + " = " + cudfMinVersion + ", ");
                }
                continue;
            }
            if (cudfMinVersion != -1) {
                sb.append(cudfName + " >= " + cudfMinVersion + ", ");
            }
            if (cudfMaxVersion != -1) {
                sb.append(cudfName + " <= " + cudfMaxVersion + ", ");
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
     * @throws DependencyException
     */
    public String getCUDFFile(PackageDependency[] pkgInstall,
            PackageDependency[] pkgRemove, PackageDependency[] pkgUpgrade)
            throws DependencyException {
        StringBuilder sb = new StringBuilder(getCUDFFile());
        sb.append(NuxeoCUDFPackage.CUDF_REQUEST + newLine);
        sb.append(NuxeoCUDFPackage.CUDF_INSTALL + formatCUDF(pkgInstall, true)
                + newLine);
        sb.append(NuxeoCUDFPackage.CUDF_REMOVE + formatCUDF(pkgRemove, true)
                + newLine);
        sb.append(NuxeoCUDFPackage.CUDF_UPGRADE + formatCUDF(pkgUpgrade, true)
                + newLine);
        return sb.toString();
    }

    /**
     * @param solution CUDF solution
     * @param details
     * @return a DependencyResolution built from the given CUDF solution
     * @throws DependencyException
     */
    public DependencyResolution buildResolution(
            Collection<InstallableUnit> solution,
            Map<Criteria, List<String>> details) throws DependencyException {
        if (solution == null) {
            throw new DependencyException("No solution found.");
        }
        log.debug("\nP2CUDF resolution details: ");
        for (Criteria criteria : Criteria.values()) {
            if (!details.get(criteria).isEmpty()) {
                log.debug(criteria.label + ": " + details.get(criteria));
            }
        }
        DependencyResolution res = new DependencyResolution();
        completeResolution(res, details, solution);
        if (res.isFailed()) {
            throw new DependencyException(res.failedMessage);
        }
        res.markAsSuccess();
        res.sort(pm);
        return res;
    }

    /**
     * TODO NXP-9268 should use results from {@link Criteria#NEW},
     * {@value Criteria#NOTUPTODATE}, {@link Criteria#RECOMMENDED} and
     * {@link Criteria#VERSION_CHANGED}
     *
     * @param res
     * @param details
     * @param solution
     */
    private void completeResolution(DependencyResolution res,
            Map<Criteria, List<String>> details,
            Collection<InstallableUnit> solution) {
        // Complete with removals
        for (String pkgName : details.get(Criteria.REMOVED)) {
            NuxeoCUDFPackage pkg = getInstalledCUDFPackage(pkgName);
            if (pkg != null) {
                res.markPackageForRemoval(pkg.getNuxeoName(),
                        pkg.getNuxeoVersion());
            }
        }

        // Complete with additions
        details.get(Criteria.CHANGED).removeAll(details.get(Criteria.REMOVED));
        // log.debug("changed-removed: " + details.get(Criteria.CHANGED));
        List<InstallableUnit> sortedSolution = new ArrayList<InstallableUnit>(
                solution);
        Collections.sort(sortedSolution);
        log.debug("Sorted solution: " + sortedSolution);
        Collections.reverse(sortedSolution);
        Map<String, NuxeoCUDFPackage> map = new LinkedHashMap<String, NuxeoCUDFPackage>();
        for (InstallableUnit iu : sortedSolution) {
            NuxeoCUDFPackage pkg = getCUDFPackage(iu.getId() + "-"
                    + iu.getVersion());
            if (pkg == null) {
                log.warn("Couldn't find " + pkg);
                continue;
            }
            if (details.get(Criteria.CHANGED).contains(iu.getId())
                    && (!map.containsKey(pkg.getNuxeoName()) || pkg.getNuxeoVersion().greaterThan(
                            map.get(pkg.getNuxeoName()).getNuxeoVersion()))) {
                map.put(pkg.getNuxeoName(), pkg);
            }
        }
        for (NuxeoCUDFPackage pkg : map.values()) {
            if (!res.addPackage(pkg.getNuxeoName(), pkg.getNuxeoVersion())) {
                break;
            }
        }
    }

    public void setTargetPlatform(String targetPlatform) {
        this.targetPlatform = targetPlatform;
    }

}