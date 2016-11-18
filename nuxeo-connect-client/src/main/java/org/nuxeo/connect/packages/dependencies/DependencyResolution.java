/*
 * (C) Copyright 2006-2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Nuxeo
 *     tdelprat
 *     jcarsique
 *     Yannis JULIENNE
 *
 */

package org.nuxeo.connect.packages.dependencies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.PackageManager;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.PackageState;
import org.nuxeo.connect.update.Version;

/**
 * Represents the result of the dependencies resolution process : - resolution succeed or not - list of {@link Package}
 * selected for update / install / remove
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class DependencyResolution {

    private static final Log log = LogFactory.getLog(DependencyResolution.class);

    protected Boolean resolution = null;

    protected boolean sorted = false;

    protected String failedMessage;

    protected Map<String, Version> allPackages = new HashMap<>();

    protected Map<String, Version> newPackagesToDownload = new HashMap<>();

    protected Map<String, Version> localPackagesToInstall = new HashMap<>();

    protected Map<String, Version> localPackagesToUpgrade = new HashMap<>();

    protected Map<String, Version> localPackagesToRemove = new HashMap<>();

    protected Map<String, Version> localUnchangedPackages = new HashMap<>();

    protected List<String> orderedInstallablePackages = new ArrayList<>();

    protected List<String> orderedRemovablePackages = new ArrayList<>();

    protected List<String> allPackagesToDownload = new ArrayList<>();

    public DependencyResolution() {

    }

    public DependencyResolution(DependencyException ex) {
        markAsFailed(ex.getMessage());
    }

    /**
     * @deprecated Since 1.4, use {@link #markAsFailed(String)} instead
     */
    @Deprecated
    public void markAsFailed() {
        resolution = false;
    }

    /**
     * @since 1.4
     * @param message failed message
     */
    public void markAsFailed(String message) {
        resolution = false;
        failedMessage = message;
        log.warn(failedMessage);
    }

    public void markAsSuccess() {
        resolution = true;
    }

    public boolean isValidated() {
        if (resolution == null) {
            return false;
        }
        return resolution;
    }

    public boolean isFailed() {
        if (resolution == null) {
            return false;
        }
        return !resolution;
    }

    public boolean addPackage(String pkgName, Version v) {
        return addPackage(pkgName, v, false);
    }

    public synchronized boolean addPackage(String pkgName, Version v, boolean fifo) {
        if (!allPackages.containsKey(pkgName)) { // Add package
            log.debug("addPackage " + pkgName + " " + v);
            allPackages.put(pkgName, v);
            if (fifo) {
                orderedInstallablePackages.add(pkgName + "-" + v.toString());
            } else {
                orderedInstallablePackages.add(0, pkgName + "-" + v.toString());
            }
        } else if (!allPackages.get(pkgName).equals(v)) { // Version conflict
            markAsFailed("addPackage conflict " + pkgName + " " + v + " with " + allPackages.get(pkgName));
        } else { // Package already added in the same version
            log.debug("addPackage ignored " + pkgName + " " + v);
        }
        return !isFailed();
    }

    /**
     * @since 1.4.12
     */
    public boolean addUnchangedPackage(String pkgName, Version v) {
        if (!allPackages.containsKey(pkgName)) { // Add package
            log.debug("addPackage " + pkgName + " " + v);
            allPackages.put(pkgName, v);
            // orderedInstallablePackages.add(pkgName + "-" + v.toString());
        } else if (!allPackages.get(pkgName).equals(v)) { // Version conflict
            markAsFailed("addPackage conflict " + pkgName + " " + v + " with " + allPackages.get(pkgName));
        } else { // Package already added in the same version
            log.debug("addPackage ignored " + pkgName + " " + v);
        }
        return !isFailed();
    }

    public void markPackageForRemoval(String pkgName, Version v) {
        markPackageForRemoval(pkgName, v, false);
    }

    public synchronized void markPackageForRemoval(String pkgName, Version v, boolean fifo) {
        log.debug("markPackageForRemoval " + pkgName + " " + v);
        localPackagesToRemove.put(pkgName, v);
        if (fifo) {
            orderedRemovablePackages.add(pkgName + "-" + v.toString());
        } else {
            orderedRemovablePackages.add(0, pkgName + "-" + v.toString());
        }
    }

    public synchronized void sort(PackageManager pm) {
        localPackagesToUpgrade.clear();
        newPackagesToDownload.clear();
        localPackagesToInstall.clear();
        localUnchangedPackages.clear();
        allPackagesToDownload.clear();
        for (String pkgName : allPackages.keySet()) {
            String id = pkgName + "-" + allPackages.get(pkgName).toString();
            DownloadablePackage pkg = pm.findPackageById(id);
            List<Version> installedVersions = pm.findLocalPackageInstalledVersions(pkg.getName());
            if (pkg.getPackageState().isInstalled() && !orderedRemovablePackages.contains(id)) {
                // Already installed in the wanted version and not to be removed, nothing to do
                localUnchangedPackages.put(pkg.getName(), pkg.getVersion());
            } else {
                if (installedVersions.size() > 0 && !installedVersions.contains(pkg.getVersion())) {
                    // Upgrade case: already installed in other version(s)
                    localPackagesToUpgrade.put(pkg.getName(), installedVersions.get(installedVersions.size() - 1));
                }
                if (pkg.getPackageState() == PackageState.REMOTE) {
                    // Needs to be download
                    newPackagesToDownload.put(pkg.getName(), pkg.getVersion());
                    allPackagesToDownload.add(id);
                } else {
                    // Already in local cache
                    localPackagesToInstall.put(pkg.getName(), pkg.getVersion());
                }
            }
        }
        sorted = true;
    }

    public Map<String, Version> getNewPackagesToDownload() {
        return newPackagesToDownload;
    }

    public Map<String, Version> getLocalPackagesToInstall() {
        return localPackagesToInstall;
    }

    public Map<String, Version> getLocalPackagesToUpgrade() {
        return localPackagesToUpgrade;
    }

    public Map<String, Version> getLocalPackagesToRemove() {
        return localPackagesToRemove;
    }

    public Map<String, Version> getLocalUnchangedPackages() {
        return localUnchangedPackages;
    }

    public boolean requireChanges() {
        return !(localPackagesToRemove.isEmpty() && localPackagesToUpgrade.isEmpty() && localPackagesToInstall.isEmpty()
                && newPackagesToDownload.isEmpty());
    }

    public List<String> getUnchangedPackageIds() {
        List<String> res = new ArrayList<>();
        for (Entry<String, Version> entry : getLocalUnchangedPackages().entrySet()) {
            res.add(entry.getKey() + "-" + entry.getValue().toString());
        }
        Collections.sort(res);
        return res;
    }

    public List<String> getUpgradePackageIds() {
        List<String> res = new ArrayList<>();
        for (Entry<String, Version> entry : getLocalPackagesToUpgrade().entrySet()) {
            res.add(entry.getKey() + "-" + entry.getValue().toString());
        }
        Collections.sort(res);
        return res;
    }

    public List<String> getInstallPackageIds() {
        List<String> res = new ArrayList<>();
        for (Entry<String, Version> entry : getLocalPackagesToInstall().entrySet()) {
            res.add(entry.getKey() + "-" + entry.getValue().toString());
        }
        for (Entry<String, Version> entry : getNewPackagesToDownload().entrySet()) {
            res.add(entry.getKey() + "-" + entry.getValue().toString());
        }
        Collections.sort(res);
        return res;
    }

    /**
     * Return names of all packages to be installed (local and remote)
     * 
     * @since 1.4.26
     */
    public List<String> getInstallPackageNames() {
        List<String> res = new ArrayList<>();
        res.addAll(getLocalPackagesToInstall().keySet());
        res.addAll(getNewPackagesToDownload().keySet());
        Collections.sort(res);
        return res;
    }

    public List<String> getDownloadPackageIds() {
        return allPackagesToDownload;
    }

    public List<String> getRemovePackageIds() {
        List<String> res = new ArrayList<>();
        for (Entry<String, Version> entry : getLocalPackagesToRemove().entrySet()) {
            res.add(entry.getKey() + "-" + entry.getValue().toString());
        }
        Collections.sort(res);
        return res;
    }

    /**
     * @since 1.4.26
     */
    public List<String> getRemovePackageNames() {
        List<String> res = new ArrayList<>();
        res.addAll(getLocalPackagesToRemove().keySet());
        Collections.sort(res);
        return res;
    }

    /**
     * @since 1.4.2
     * @return List of already downloaded packages that need to be installed
     */
    public List<String> getLocalToInstallIds() {
        List<String> res = new ArrayList<>();
        for (Entry<String, Version> entry : getLocalPackagesToInstall().entrySet()) {
            res.add(entry.getKey() + "-" + entry.getValue().toString());
        }
        Collections.sort(res);
        return res;
    }

    @Override
    public synchronized String toString() {
        StringBuffer sb = new StringBuffer();
        if (isFailed()) {
            sb.append("\nFailed to resolve dependencies: ");
            sb.append(failedMessage);
        } else if (!sorted) {
            append(sb, allPackages, "\nUnsorted packages: ");
        } else if (!isEmpty()) {
            sb.append("\nDependency resolution:\n");
            append(sb, orderedInstallablePackages, "Installation order");
            append(sb, orderedRemovablePackages, "Uninstallation order");
            append(sb, localUnchangedPackages, "Unchanged packages");
            append(sb, localPackagesToUpgrade, "Packages to upgrade");
            append(sb, newPackagesToDownload, "Packages to download");
            append(sb, localPackagesToInstall, "Local packages to install");
            append(sb, localPackagesToRemove, "Local packages to remove");
        } else {
            sb.append("\nDependency resolution:\n");
            append(sb, localUnchangedPackages, "Unchanged packages");
        }
        return sb.toString();
    }

    private StringBuffer append(StringBuffer sb, Map<String, Version> pkgMap, String title) {
        if (!pkgMap.isEmpty()) {
            append(sb, title, pkgMap.size());
            for (String pkgName : pkgMap.keySet()) {
                sb.append(pkgName);
                sb.append(":");
                sb.append(pkgMap.get(pkgName).toString());
                sb.append(", ");
            }
            sb.replace(sb.length() - 2, sb.length(), "\n");
        }
        return sb;
    }

    private StringBuffer append(StringBuffer sb, List<String> pkgList, String title) {
        if (!pkgList.isEmpty()) {
            append(sb, title, pkgList.size());
            for (String pkg : pkgList) {
                sb.append(pkg + "/");
            }
            sb.replace(sb.length() - 1, sb.length(), "\n");
        }
        return sb;
    }

    private void append(StringBuffer sb, String title, int size) {
        if (title.length() > 0) {
            sb.append(String.format("  %-30s ", title + " (" + size + "):"));
        }
    }

    public synchronized String getInstallationOrderAsString() {
        return removeLineReturn(append(new StringBuffer(), orderedInstallablePackages, ""));
    }

    private String removeLineReturn(StringBuffer sb) {
        if (sb.length() > 0) { // remove ending \n
            return sb.substring(0, sb.length() - 1);
        } else {
            return "";
        }
    }

    public synchronized List<String> getOrderedPackageIdsToInstall() {
        return orderedInstallablePackages;
    }

    public synchronized List<String> getOrderedPackageIdsToRemove() {
        return orderedRemovablePackages;
    }

    public String getAllPackagesToDownloadAsString() {
        return removeLineReturn(append(new StringBuffer(), allPackagesToDownload, ""));
    }

    public int getNbPackagesToDownload() {
        return allPackagesToDownload.size();
    }

    /**
     * @since 1.4
     */
    public boolean isEmpty() {
        if (!sorted) {
            return allPackages.isEmpty();
        } else {
            return orderedInstallablePackages.isEmpty() && orderedRemovablePackages.isEmpty()
                    && newPackagesToDownload.isEmpty() && localPackagesToInstall.isEmpty()
                    && localPackagesToUpgrade.isEmpty();
        }
    }

    /**
     * @since 1.4
     * @return true after {@link #sort(PackageManager)} successful call.
     */
    public boolean isSorted() {
        return sorted;
    }

}
