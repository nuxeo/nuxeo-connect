package org.nuxeo.connect.packages.dependencies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.connect.data.DownloadablePackage;

public class UpdateCheckResult {

    protected boolean requireUpdate=false;

    protected boolean transparentUpdate=false;

    protected List<DownloadablePackage> packagesToRemove = new ArrayList<DownloadablePackage>();

    protected Map<String, Boolean> updatePossible = new HashMap<String, Boolean>();

    protected List<DownloadablePackage> packagesToAdd = new ArrayList<DownloadablePackage>();

    public List<DownloadablePackage> getPackagesToRemove() {
        return packagesToRemove;
    }

    public List<DownloadablePackage> getPackagesToAdd() {
        return packagesToAdd;
    }

    public boolean isRequireUpdate() {
        return requireUpdate;
    }

    public void setRequireUpdate(boolean requireUpdate) {
        this.requireUpdate = requireUpdate;
    }

    public boolean isUpdatePossible() {
        for (Boolean possible : updatePossible.values()) {
            if (!possible) {
                return false;
            }
        }
        return true;
    }

    public void setTransparentUpdate() {
        updatePossible.clear();
        packagesToRemove.clear();
        transparentUpdate=true;
    }

    public void setUpdatePossible(String pkgName, boolean updatePossible) {
        this.updatePossible.put(pkgName, updatePossible);
    }

    public void addPackageToRemove(DownloadablePackage pkg) {
        if (!packagesToRemove.contains(pkg)) {
            packagesToRemove.add(pkg);
        }
    }

    public void addPackage(DownloadablePackage pkg) {
        if (!packagesToAdd.contains(pkg)) {
            packagesToAdd.add(pkg);
        }
    }

    public boolean isTransparentUpdate() {
        return transparentUpdate;
    }

}
