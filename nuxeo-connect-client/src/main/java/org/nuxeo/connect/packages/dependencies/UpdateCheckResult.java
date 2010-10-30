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

import org.nuxeo.connect.data.DownloadablePackage;

/**
*
* @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
*
*/
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
