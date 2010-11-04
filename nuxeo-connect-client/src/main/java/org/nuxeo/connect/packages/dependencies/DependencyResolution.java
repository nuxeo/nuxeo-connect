/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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

package org.nuxeo.connect.packages.dependencies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.InternalPackageManager;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.PackageState;
import org.nuxeo.connect.update.Version;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

/**
* Represents the result of the dependencies resolution process :
*
*  - resolution succeed or not
*
*   - list of {@link Package} selected for update / install / remove
*
* @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
*
*/
public class DependencyResolution {

    protected Boolean resolution = null;

    protected boolean sorted=false;

    protected String failedMessage;

    protected Map<String, Version> allPackages = new HashMap<String, Version>();

    protected Map<String, Version> newPackagesToDownload = new HashMap<String, Version>();

    protected Map<String, Version> localPackagesToInstall = new HashMap<String, Version>();

    protected Map<String, Version> localPackagesToUpgrade = new HashMap<String, Version>();

    protected Map<String, Version> localPackagesToRemove = new HashMap<String, Version>();

    protected Map<String, Version> localUnchangedPackages = new HashMap<String, Version>();

    public DependencyResolution() {

    }

    public DependencyResolution(DependencyException ex) {
        markAsFailed();
        failedMessage=ex.getMessage();
    }

    public void markAsFailed() {
        resolution=false;
    }

    public void markAsSuccess() {
        resolution=true;
    }

    public boolean isValidated() {
        if (resolution==null) {
            return false;
        }
        return resolution;
    }

    public boolean isFailed() {
        if (resolution==null) {
            return false;
        }
        return !resolution;
    }

    public boolean addPackage(String pkgName, Version v) {
        if (allPackages.containsKey(pkgName)) {
            if (!allPackages.get(pkgName).equals(v)) {
                resolution=false;
            }
        } else {
            allPackages.put(pkgName, v);
        }

        return !isFailed();
    }

    public void markPackageForRemoval(String pkgName, Version v) {
        localPackagesToRemove.put(pkgName, v);
    }

    public void sort(InternalPackageManager pm) {

        localPackagesToUpgrade.clear();
        newPackagesToDownload.clear();
        localPackagesToInstall.clear();
        localUnchangedPackages.clear();

        for (String pkgName : allPackages.keySet()) {
            String id = pkgName + "-" + allPackages.get(pkgName).toString();
            DownloadablePackage pkg = pm.findPackageById(id);
            List<Version> existingVersions = pm.findLocalPackageVersions(pkg.getName());
            if (existingVersions.size()>0 && ! existingVersions.contains(pkg.getVersion())) {
                localPackagesToUpgrade.put(pkg.getName(), pkg.getVersion());
            } else {
                if (pkg.getState()==PackageState.REMOTE) {
                    newPackagesToDownload.put(pkg.getName(), pkg.getVersion());
                } else if (pkg.getState()>PackageState.REMOTE && pkg.getState()<PackageState.INSTALLING) {
                    localPackagesToInstall.put(pkg.getName(), pkg.getVersion());
                } else if ( pkg.getState()>PackageState.INSTALLING) {
                    localUnchangedPackages.put(pkg.getName(), pkg.getVersion());
                }
            }
        }
        sorted=true;

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
        if (localPackagesToRemove.size()>0) {
            return true;
        }
        if (localPackagesToUpgrade.size()>0) {
            return true;
        }
        if (localPackagesToInstall.size()>0) {
            return true;
        }
        if (newPackagesToDownload.size()>0) {
            return true;
        }
        return false;
    }

    public List<String> getUnchangedPackageIds() {
        List<String> res = new ArrayList<String>();
        for (Entry<String,Version> entry : getLocalUnchangedPackages().entrySet()) {
            res.add(entry.getKey()+"-"+entry.getValue().toString());
        }
        return res;
    }

    public List<String> getUpgradePackageIds() {
        List<String> res = new ArrayList<String>();
        for (Entry<String,Version> entry : getLocalPackagesToUpgrade().entrySet()) {
            res.add(entry.getKey()+"-"+entry.getValue().toString());
        }
        return res;
    }

    public List<String> getInstallPackageIds() {
        List<String> res = new ArrayList<String>();
        for (Entry<String,Version> entry : getLocalPackagesToInstall().entrySet()) {
            res.add(entry.getKey()+"-"+entry.getValue().toString());
        }
        return res;
    }

    public List<String> getDownloadPackageIds() {
        List<String> res = new ArrayList<String>();
        for (Entry<String,Version> entry : getNewPackagesToDownload().entrySet()) {
            res.add(entry.getKey()+"-"+entry.getValue().toString());
        }
        return res;
    }

    public List<String> getRemovePackageIds() {
        List<String> res = new ArrayList<String>();
        for (Entry<String,Version> entry : getLocalPackagesToRemove().entrySet()) {
            res.add(entry.getKey()+"-"+entry.getValue().toString());
        }
        return res;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        if (isFailed()) {
            sb.append("Failed to resolve dependencies : ");
            sb.append(failedMessage);
        }
        else {
            if (!sorted) {
                for (String pkgName : allPackages.keySet()) {
                    sb.append(pkgName);
                    sb.append(":");
                    sb.append(allPackages.get(pkgName).toString());
                    sb.append(", ");
                }
            } else {
                sb.append("Packages to download: ");
                for (String pkgName : newPackagesToDownload.keySet()) {
                    sb.append(pkgName);
                    sb.append(":");
                    sb.append(newPackagesToDownload.get(pkgName).toString());
                    sb.append(", ");
                }
                sb.append("\nPackages to install (already in local): ");
                for (String pkgName : localPackagesToInstall.keySet()) {
                    sb.append(pkgName);
                    sb.append(":");
                    sb.append(localPackagesToInstall.get(pkgName).toString());
                    sb.append(", ");
                }
                sb.append("\nPackages to upgrade : ");
                for (String pkgName : localPackagesToUpgrade.keySet()) {
                    sb.append(pkgName);
                    sb.append(":");
                    sb.append(localPackagesToUpgrade.get(pkgName).toString());
                    sb.append(", ");
                }
                sb.append("\nUnchanged packages : ");
                for (String pkgName : localUnchangedPackages.keySet()) {
                    sb.append(pkgName);
                    sb.append(":");
                    sb.append(localUnchangedPackages.get(pkgName).toString());
                    sb.append(", ");
                }
                sb.append("\nLocal packages to remove: ");
                for (String pkgName : localPackagesToRemove.keySet()) {
                    sb.append(pkgName);
                    sb.append(":");
                    sb.append(localPackagesToRemove.get(pkgName).toString());
                    sb.append(", ");
                }

            }
        }
        return sb.toString();
    }
}
