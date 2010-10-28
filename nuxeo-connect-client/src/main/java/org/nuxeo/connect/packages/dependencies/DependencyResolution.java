package org.nuxeo.connect.packages.dependencies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.PackageManagerImpl;
import org.nuxeo.connect.update.PackageState;
import org.nuxeo.connect.update.Version;

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

    public void sort(PackageManagerImpl pm) {

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
