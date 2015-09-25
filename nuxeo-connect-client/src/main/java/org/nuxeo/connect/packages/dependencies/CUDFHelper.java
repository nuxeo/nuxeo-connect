/*
 * (C) Copyright 2006-2015 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *      Mathieu Guillaume, Julien Carsique
 *
 */

package org.nuxeo.connect.packages.dependencies;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.equinox.p2.cudf.solver.OptimizationFunction.Criteria;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.PackageManager;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.PackageType;
import org.nuxeo.connect.update.Version;
import org.nuxeo.connect.update.VersionRange;

/**
 * @since 1.4
 */
public class CUDFHelper {

    private static final Log log = LogFactory.getLog(CUDFHelper.class);

    public static final String newLine = System.getProperty("line.separator");

    /**
     * Convenient default value about SNAPSHOT inclusion in the CUDF universe. Prefer use of parameter in the relevant
     * methods.
     *
     * @since 1.4.13
     * @see #initMapping(PackageDependency[], PackageDependency[], PackageDependency[])
     * @see #setAllowSNAPSHOT(boolean)
     */
    public static boolean defaultAllowSNAPSHOT = false;

    protected PackageManager pm;

    /**
     * Map of all NuxeoCUDFPackage per Nuxeo version, per package name nuxeo2CUDFMap = { "pkgName", { nuxeoVersion,
     * NuxeoCUDFPackage }}
     */
    protected Map<String, Map<Version, NuxeoCUDFPackage>> nuxeo2CUDFMap = new HashMap<>();

    /**
     * Map of all NuxeoCUDFPackage per CUDF unique ID (pkgName-pkgCUDFVersion) CUDF2NuxeoMap = {
     * "pkgName-pkgCUDFVersion", NuxeoCUDFPackage }
     */
    protected Map<String, NuxeoCUDFPackage> CUDF2NuxeoMap = new HashMap<>();

    private String targetPlatform;

    private boolean allowSNAPSHOT = defaultAllowSNAPSHOT;

    private boolean keep = true;

    /**
     * @since 5.9.2
     * @param keep Whether to keep the installed packages in the resolution
     */
    public void setKeep(boolean keep) {
        this.keep = keep;
    }

    public CUDFHelper(PackageManager pm) {
        this.pm = pm;
    }

    /**
     * Map "name, version-classifier" to "name-classifier, version" (with -SNAPSHOT being a specific case)
     */
    public void initMapping() {
        initMapping(null, null, null);
    }

    /**
     * @param upgrades Packages for which we'll feed the CUDF universe with the remote one if they're SNAPSHOT, in order
     *            to allow their upgrade
     * @param installs
     * @param removes
     * @since 1.4.11
     */
    public void initMapping(PackageDependency[] installs, PackageDependency[] removes, PackageDependency[] upgrades) {
        nuxeo2CUDFMap.clear();
        CUDF2NuxeoMap.clear();
        Map<String, PackageDependency> upgradesMap = new HashMap<>();
        Set<String> involvedPackages = new HashSet<>();
        List<String> installedOrRequiredSNAPSHOTPackages = new ArrayList<>();
        if (upgrades != null) {
            computeInvolvedPackages(upgrades, upgradesMap, involvedPackages, installedOrRequiredSNAPSHOTPackages);
        }
        if (installs != null) {
            computeInvolvedPackages(installs, involvedPackages, installedOrRequiredSNAPSHOTPackages);
        }
        if (removes != null) {
            computeInvolvedPackages(removes, involvedPackages, installedOrRequiredSNAPSHOTPackages);
        }

        // Build a map <pkgName,pkg>
        List<DownloadablePackage> allPackages = getAllPackages();
        Map<String, List<DownloadablePackage>> allPackagesMap = new HashMap<>();
        for (DownloadablePackage pkg : allPackages) {
            String key = pkg.getName();
            List<DownloadablePackage> list;
            if (!allPackagesMap.containsKey(key)) {
                list = new ArrayList<>();
                allPackagesMap.put(key, list);
            } else {
                list = allPackagesMap.get(key);
            }
            list.add(pkg);
            // in the mean time, add installed packages to the involved packages list
            if (keep && pkg.getPackageState().isInstalled()) {
                involvedPackages.add(pkg.getName());
            }
        }
        for (DownloadablePackage pkg : allPackages) {
            computeInvolvedReferences(involvedPackages, installedOrRequiredSNAPSHOTPackages, pkg, allPackagesMap);
        }
        installedOrRequiredSNAPSHOTPackages.addAll(getInstalledSNAPSHOTPackages());
        // for each unique "name-classifier", sort versions so we can attribute them a "CUDF posint" version populate
        // Nuxeo2CUDFMap and the reverse CUDF2NuxeoMap
        for (DownloadablePackage pkg : allPackages) {
            // ignore not involved packages
            if (!involvedPackages.contains(pkg.getName())) {
                if (installedOrRequiredSNAPSHOTPackages.contains(pkg.getName())) {
                    log.error("Ignore installedOrRequiredSNAPSHOTPackage " + pkg);
                }

                // check provides
                boolean involved = false;
                PackageDependency[] provides = pkg.getProvides();
                for (PackageDependency provide : provides) {
                    if (involvedPackages.contains(provide.getName())) {
                        involved = true;
                        break;
                    }
                }
                if (!involved) {
                    log.debug("Ignore " + pkg + " (not involved by request)");
                    continue;
                }
            }

            // ignore incompatible packages when a targetPlatform is set
            if (targetPlatform != null && !pkg.isLocal()
                    && !TargetPlatformFilterHelper.isCompatibleWithTargetPlatform(pkg, targetPlatform)) {
                log.debug("Ignore " + pkg + " (incompatible target platform)");
                continue;
            }
            // Exclude SNAPSHOT by default for non Studio packages
            if (!allowSNAPSHOT && pkg.getVersion().isSnapshot() && pkg.getType() != PackageType.STUDIO
                    && !installedOrRequiredSNAPSHOTPackages.contains(pkg.getName())) {
                log.debug("Ignore " + pkg + " (excluded SNAPSHOT)");
                continue;
            }

            // SNAPSHOT upgrade requires referring the remote package
            if (pkg.getVersion().isSnapshot() && pkg.isLocal() && upgradesMap.containsKey(pkg.getName())) {
                PackageDependency upgrade = upgradesMap.get(pkg.getName());
                if (upgrade.getVersionRange().matchVersion(pkg.getVersion())) {
                    DownloadablePackage remotePackage = pm.getRemotePackage(pkg.getId());
                    if (remotePackage != null) {
                        log.debug(String.format("Upgrade with remote %s", remotePackage));
                        pkg = remotePackage;
                    }
                }
            }
            NuxeoCUDFPackage nuxeoCUDFPackage = new NuxeoCUDFPackage(pkg);
            // if (!keep && !involvedPackages.contains(pkg.getName())) {
            // nuxeoCUDFPackage.setInstalled(false);
            // }
            Map<Version, NuxeoCUDFPackage> pkgVersions = nuxeo2CUDFMap.get(nuxeoCUDFPackage.getCUDFName());
            if (pkgVersions == null) {
                pkgVersions = new TreeMap<>();
                nuxeo2CUDFMap.put(nuxeoCUDFPackage.getCUDFName(), pkgVersions);
            }
            pkgVersions.put(nuxeoCUDFPackage.getNuxeoVersion(), nuxeoCUDFPackage);
        }
        for (String key : nuxeo2CUDFMap.keySet()) {
            Map<Version, NuxeoCUDFPackage> pkgVersions = nuxeo2CUDFMap.get(key);
            int posInt = 1;
            for (Version version : pkgVersions.keySet()) {
                NuxeoCUDFPackage pkg = pkgVersions.get(version);
                pkg.setCUDFVersion(posInt++);
                CUDF2NuxeoMap.put(pkg.getCUDFName() + "-" + pkg.getCUDFVersion(), pkg);
            }
        }

        if (log.isDebugEnabled()) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(outputStream);
            MapUtils.verbosePrint(out, "nuxeo2CUDFMap", nuxeo2CUDFMap);
            MapUtils.verbosePrint(out, "CUDF2NuxeoMap", CUDF2NuxeoMap);
            log.debug(outputStream.toString());
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * Parse request to compute the list of directly involved packages
     *
     * @since 1.4.18
     */
    protected void computeInvolvedPackages(PackageDependency[] packageDependencies, Set<String> involvedPackages,
            List<String> installedOrRequiredSNAPSHOTPackages) {
        computeInvolvedPackages(packageDependencies, null, involvedPackages, installedOrRequiredSNAPSHOTPackages);
    }

    /**
     * Parse request to compute the list of directly involved packages
     *
     * @since 1.4.18
     */
    protected void computeInvolvedPackages(PackageDependency[] packageDependencies,
            Map<String, PackageDependency> upgradesMap, Set<String> involvedPackages,
            List<String> installedOrRequiredSNAPSHOTPackages) {
        for (PackageDependency packageDependency : packageDependencies) {
            if (upgradesMap != null) {
                upgradesMap.put(packageDependency.getName(), packageDependency);
            }
            involvedPackages.add(packageDependency.getName());
            addIfSNAPSHOT(installedOrRequiredSNAPSHOTPackages, packageDependency);
        }
    }

    /**
     * Browse the given package's "dependencies", "conflicts" and "provides" to populate the list of involved packages
     *
     * @param installedOrRequiredSNAPSHOTPackages
     * @since 1.4.18
     */
    protected void computeInvolvedReferences(Set<String> involvedPackages,
            List<String> installedOrRequiredSNAPSHOTPackages, DownloadablePackage pkg,
            Map<String, List<DownloadablePackage>> allPackagesMap) {
        if (involvedPackages.contains(pkg.getName())) {
            computeInvolvedReferences(involvedPackages, installedOrRequiredSNAPSHOTPackages, pkg.getDependencies(),
                    allPackagesMap);
            computeInvolvedReferences(involvedPackages, installedOrRequiredSNAPSHOTPackages, pkg.getConflicts(),
                    allPackagesMap);
            for (PackageDependency pkgDep : pkg.getProvides()) {
                involvedPackages.add(pkgDep.getName());
            }
        }
    }

    /**
     * Browse the given packages' "dependencies", "conflicts" and "provides" to populate the list of involved packages
     *
     * @since 1.4.18
     */
    protected void computeInvolvedReferences(Set<String> involvedPackages,
            List<String> installedOrRequiredSNAPSHOTPackages, PackageDependency[] pkgDeps,
            Map<String, List<DownloadablePackage>> allPackagesMap) {
        for (PackageDependency pkgDep : pkgDeps) {
            if (involvedPackages.add(pkgDep.getName())) {
                addIfSNAPSHOT(installedOrRequiredSNAPSHOTPackages, pkgDep);
                List<DownloadablePackage> downloadablePkgDeps = allPackagesMap.get(pkgDep.getName());
                if (downloadablePkgDeps == null) {
                    log.warn("Unknown dependency: " + pkgDep);
                    continue;
                }
                for (DownloadablePackage downloadablePkgDep : downloadablePkgDeps) {
                    computeInvolvedReferences(involvedPackages, installedOrRequiredSNAPSHOTPackages,
                            downloadablePkgDep, allPackagesMap);
                }
            }
        }
    }

    protected void addIfSNAPSHOT(List<String> installedOrRequiredSNAPSHOTPackages, PackageDependency pd) {
        Version minVersion = pd.getVersionRange().getMinVersion();
        Version maxVersion = pd.getVersionRange().getMaxVersion();
        if (minVersion != null && minVersion.isSnapshot() || maxVersion != null && maxVersion.isSnapshot()) {
            installedOrRequiredSNAPSHOTPackages.add(pd.getName());
        }
    }

    protected List<String> getInstalledSNAPSHOTPackages() {
        List<String> installedSNAPSHOTPackages = new ArrayList<>();
        for (DownloadablePackage pkg : pm.listInstalledPackages()) {
            if (pkg.getVersion().isSnapshot()) {
                installedSNAPSHOTPackages.add(pkg.getName());
            }
        }
        return installedSNAPSHOTPackages;
    }

    protected List<DownloadablePackage> getAllPackages() {
        return pm.listAllPackages();
    }

    /**
     * @param cudfKey in the form "pkgName-pkgCUDFVersion"
     * @return NuxeoCUDFPackage corresponding to the given cudfKey
     */
    public NuxeoCUDFPackage getCUDFPackage(String cudfKey) {
        return CUDF2NuxeoMap.get(cudfKey);
    }

    /**
     * @param cudfName a package name
     * @return all NuxeoCUDFPackage versions corresponding to the given package
     */
    public Map<Version, NuxeoCUDFPackage> getCUDFPackages(String cudfName) {
        return nuxeo2CUDFMap.get(cudfName);
    }

    /**
     * @param pkgName a package name
     * @return the NuxeoCUDFPackage corresponding to the given package name which is installed. Null if not found.
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
        StringBuilder sb = new StringBuilder();
        for (String cudfKey : CUDF2NuxeoMap.keySet()) {
            sb.append(formatCUDF(CUDF2NuxeoMap.get(cudfKey)));
            sb.append(newLine);
        }
        return sb.toString();
    }

    /**
     * @return A string representation of a {@link NuxeoCUDFPackage}
     * @since 1.4.20
     */
    public String formatCUDF(NuxeoCUDFPackage cudfPackage) throws DependencyException {
        StringBuilder sb2 = new StringBuilder();
        sb2.append(cudfPackage.getCUDFStanza());
        sb2.append(CUDFPackage.TAG_DEPENDS + formatCUDFDeps(cudfPackage.getDependencies(), false, true) + newLine);
        // Add conflicts to other versions of the same package
        String conflictsFormatted = formatCUDFDeps(cudfPackage.getConflicts(), false, false);
        conflictsFormatted += (conflictsFormatted.trim().length() > 0 ? ", " : "") + cudfPackage.getCUDFName() + " != "
                + cudfPackage.getCUDFVersion();
        sb2.append(CUDFPackage.TAG_CONFLICTS + conflictsFormatted + newLine);
        sb2.append(CUDFPackage.TAG_PROVIDES + formatCUDFDeps(cudfPackage.getProvides(), false, false) + newLine);
        return sb2.toString();
    }

    protected String formatCUDFDeps(PackageDependency[] dependencies, boolean failOnError, boolean warnOnError)
            throws DependencyException {
        if (dependencies == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (PackageDependency packageDependency : dependencies) {
            String cudfName = NuxeoCUDFPackage.getCUDFName(packageDependency);
            Map<Version, NuxeoCUDFPackage> versionsMap = nuxeo2CUDFMap.get(cudfName);
            if (versionsMap == null) {
                String errMsg = "Missing mapping for " + packageDependency + " with target platform " + targetPlatform;
                if (failOnError) {
                    throw new DependencyException(errMsg);
                } else if (warnOnError) {
                    log.warn(errMsg);
                } else {
                    log.debug(errMsg);
                }
                continue;
            }
            VersionRange versionRange = packageDependency.getVersionRange();
            int cudfMinVersion, cudfMaxVersion;
            if (versionRange.getMinVersion() == null) {
                cudfMinVersion = -1;
            } else {
                CUDFPackage cudfPackage = versionsMap.get(versionRange.getMinVersion());
                cudfMinVersion = (cudfPackage == null) ? -1 : cudfPackage.getCUDFVersion();
            }
            if (versionRange.getMaxVersion() == null) {
                cudfMaxVersion = -1;
            } else {
                CUDFPackage cudfPackage = versionsMap.get(versionRange.getMaxVersion());
                cudfMaxVersion = (cudfPackage == null) ? -1 : cudfPackage.getCUDFVersion();
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
     * Parse a CUDF universe string
     *
     * @param reader
     * @return The CUDF universe as a map of {@link NuxeoCUDFPackageDescription} per CUDF unique ID
     *         (pkgName-pkgCUDFVersion). The map uses the natural ordering of its keys.
     * @throws IOException
     * @throws DependencyException
     * @since 1.4.20
     * @see #getCUDFFile()
     * @see #formatCUDF(NuxeoCUDFPackage)
     */
    public Map<String, NuxeoCUDFPackageDescription> parseCUDFFile(BufferedReader reader) throws IOException,
            DependencyException {
        Map<String, NuxeoCUDFPackageDescription> map = new TreeMap<>();
        NuxeoCUDFPackageDescription nuxeoCUDFPkgDesc = null;
        Pattern linePattern = Pattern.compile(CUDFPackage.LINE_PATTERN);

        while (reader.ready()) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            line = line.trim();
            log.debug("Parsing line >> " + line);
            if (line.trim().isEmpty()) {
                if (nuxeoCUDFPkgDesc == null) {
                    throw new DependencyException("Invalid CUDF file starting with an empty line");
                }
                map.put(nuxeoCUDFPkgDesc.getCUDFName() + "-" + nuxeoCUDFPkgDesc.getCUDFVersion(), nuxeoCUDFPkgDesc);
                nuxeoCUDFPkgDesc = null;
            } else {
                Matcher m = linePattern.matcher(line);
                if (!m.matches()) {
                    throw new DependencyException("Invalid CUDF line: " + line);
                }
                String tag = m.group(1);
                if (tag.endsWith(":")) {
                    tag += " ";
                }
                String value = m.group(2);
                if (nuxeoCUDFPkgDesc == null) {
                    if (!CUDFPackage.TAG_PACKAGE.equals(tag)) {
                        throw new DependencyException("Invalid CUDF file not starting with " + CUDFPackage.TAG_PACKAGE);
                    }
                    nuxeoCUDFPkgDesc = new NuxeoCUDFPackageDescription();
                    nuxeoCUDFPkgDesc.setCUDFName(value);
                    continue;
                }
                switch (tag) {
                case CUDFPackage.TAG_VERSION:
                    nuxeoCUDFPkgDesc.setCUDFVersion(Integer.parseInt(value));
                    break;
                case CUDFPackage.TAG_INSTALLED:
                    nuxeoCUDFPkgDesc.setInstalled(Boolean.parseBoolean(value));
                    break;
                case CUDFPackage.TAG_DEPENDS:
                    nuxeoCUDFPkgDesc.setDependencies(parseCUDFDeps(value));
                    break;
                case CUDFPackage.TAG_CONFLICTS:
                    nuxeoCUDFPkgDesc.setConflicts(parseCUDFDeps(value));
                    break;
                case CUDFPackage.TAG_PROVIDES:
                    nuxeoCUDFPkgDesc.setProvides(parseCUDFDeps(value));
                    break;
                case CUDFPackage.TAG_REQUEST:
                case CUDFPackage.TAG_INSTALL:
                case CUDFPackage.TAG_REMOVE:
                case CUDFPackage.TAG_UPGRADE:
                    log.debug("Ignore request stanza " + line);
                    break;
                case CUDFPackage.TAG_PACKAGE:
                default:
                    throw new DependencyException("Invalid CUDF line: " + line);
                }
            }
        }
        if (nuxeoCUDFPkgDesc != null) { // CUDF file without newline at end of file
            map.put(nuxeoCUDFPkgDesc.getCUDFName() + "-" + nuxeoCUDFPkgDesc.getCUDFVersion(), nuxeoCUDFPkgDesc);
        }
        return map;
    }

    /**
     * @param value CUDF dependencies
     * @return An array of {@link PackageDependency}
     * @throws DependencyException In case of parsing issue
     * @since 1.4.20
     * @see CUDFPackage#TAG_DEPENDS
     * @see CUDFPackage#TAG_CONFLICTS
     * @see CUDFPackage#TAG_PROVIDES
     * @see #formatCUDFDeps(PackageDependency[], boolean, boolean)
     */
    protected List<PackageDependency> parseCUDFDeps(String value) throws DependencyException {
        // Map<Version, NuxeoCUDFPackage> versionsMap = nuxeo2CUDFMap.get(cudfName);
        // CUDF2NuxeoMap.
        Map<String, PackageDependency> deps = new HashMap<>();
        if (value.trim().length() == 0) {
            return new ArrayList<>(deps.values());
        }
        for (String pkgDep : value.split(",")) {
            String[] split = pkgDep.trim().split("\\s");
            if (split.length == 1) {
                deps.put(pkgDep.trim(), new PackageDependency(pkgDep.trim()));
                continue;
            }
            if (split.length != 3) {
                throw new DependencyException("Invalid dependency value: " + value);
            }
            String name = split[0].trim();
            String rel = split[1].trim();
            Version version = new Version(split[2].trim());
            PackageDependency previous = deps.get(name);
            switch (rel) {
            case "=":
                if (previous != null) {
                    throw new DependencyException("Conflicting dependency value: " + value + " with " + previous);
                }
                deps.put(name, new PackageDependency(name, version, version));
                break;
            case "<": // Not managed, let's consider it's "<="
            case "<=":
                if (previous == null) {
                    deps.put(name, new PackageDependency(name, Version.ZERO, version));
                } else {
                    VersionRange versionRange = previous.getVersionRange();
                    if (versionRange.getMaxVersion() != null) {
                        throw new DependencyException("Conflicting dependency value: " + value + " with " + previous);
                    }
                    versionRange.setMaxVersion(version);
                }
                break;
            case ">": // Not managed, let's consider it's ">="
            case ">=":
                if (previous == null) {
                    deps.put(name, new PackageDependency(name, version));
                } else {
                    VersionRange versionRange = previous.getVersionRange();
                    if (versionRange.getMinVersion() != null) {
                        throw new DependencyException("Conflicting dependency value: " + value + " with " + previous);
                    }
                    versionRange.setMinVersion(version);
                }
                break;

            case "!=": // Not managed, ignore
                break;

            default:
                throw new DependencyException("Invalid dependency value: " + value);
            }

        }
        return new ArrayList<>(deps.values());
    }

    /**
     * @param pkgInstall
     * @param pkgRemove
     * @param pkgUpgrade
     * @return a CUDF string with packages universe and request stanza
     * @throws DependencyException
     */
    public String getCUDFFile(PackageDependency[] pkgInstall, PackageDependency[] pkgRemove,
            PackageDependency[] pkgUpgrade) throws DependencyException {
        initMapping(pkgInstall, pkgRemove, pkgUpgrade);
        StringBuilder sb = new StringBuilder(getCUDFFile());
        sb.append(CUDFPackage.TAG_REQUEST + newLine);
        sb.append(CUDFPackage.TAG_INSTALL + formatCUDFDeps(pkgInstall, true, true) + newLine);
        sb.append(CUDFPackage.TAG_REMOVE + formatCUDFDeps(pkgRemove, true, true) + newLine);
        sb.append(CUDFPackage.TAG_UPGRADE + formatCUDFDeps(pkgUpgrade, true, true) + newLine);
        return sb.toString();
    }

    /**
     * @param solution CUDF solution
     * @param details
     * @return a DependencyResolution built from the given CUDF solution
     * @throws DependencyException
     */
    public DependencyResolution buildResolution(Collection<InstallableUnit> solution,
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
        pm.order(res);
        return res;
    }

    /**
     * TODO NXP-9268 should use results from {@link Criteria#NOTUPTODATE} and {@link Criteria#RECOMMENDED}
     *
     * @param res
     * @param details
     * @param solution
     */
    protected void completeResolution(DependencyResolution res, Map<Criteria, List<String>> details,
            Collection<InstallableUnit> solution) {
        // Complete with removals
        for (String pkgName : details.get(Criteria.REMOVED)) {
            NuxeoCUDFPackage pkg = getInstalledCUDFPackage(pkgName);
            if (pkg != null) {
                res.markPackageForRemoval(pkg.getNuxeoName(), pkg.getNuxeoVersion(), true);
            }
        }

        List<InstallableUnit> sortedSolution = new ArrayList<>(solution);
        Collections.sort(sortedSolution);
        log.debug("Solution: " + sortedSolution);

        if (log.isTraceEnabled()) {
            log.trace("P2CUDF printed solution");
            for (InstallableUnit iu : sortedSolution) {
                log.trace("  package: " + iu.getId());
                log.trace("  version: " + iu.getVersion().getMajor());
                log.trace("  installed: " + iu.isInstalled());
            }
        }

        for (InstallableUnit iu : sortedSolution) {
            NuxeoCUDFPackage pkg = getCUDFPackage(iu.getId() + "-" + iu.getVersion());
            if (pkg == null) {
                log.warn("Couldn't find " + pkg);
                continue;
            }
            if (details.get(Criteria.NEW).contains(iu.getId())
                    || details.get(Criteria.VERSION_CHANGED).contains(iu.getId())) {
                if (!res.addPackage(pkg.getNuxeoName(), pkg.getNuxeoVersion(), true)) {
                    log.error("Failed to add " + pkg);
                }
            } else if (!details.get(Criteria.REMOVED).contains(iu.getId())) {
                if (!res.addUnchangedPackage(pkg.getNuxeoName(), pkg.getNuxeoVersion())) {
                    log.error("Failed to add " + pkg);
                }
            } else {
                log.debug("Ignored: " + pkg);
            }
        }
    }

    public void setTargetPlatform(String targetPlatform) {
        this.targetPlatform = targetPlatform;
    }

    /**
     * @since 1.4.13
     */
    public void setAllowSNAPSHOT(boolean allowSNAPSHOT) {
        this.allowSNAPSHOT = allowSNAPSHOT;
    }

}
