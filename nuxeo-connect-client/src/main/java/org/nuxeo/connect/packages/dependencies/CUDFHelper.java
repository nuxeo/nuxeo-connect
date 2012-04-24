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

import java.util.List;
import java.util.Map;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.PackageManager;


public class CUDFHelper {

    protected PackageManager pm;

    protected Map<String[], String[]> Nuxeo2CUDFMap = null;

    protected Map<String[], String[]> CUDF2NuxeoMap = null;

    public CUDFHelper(PackageManager pm) {
        this.pm = pm;
        initMapping();
    }

    public void resetMapping() {
        pm.flushCache();
        initMapping();
    }

    public void initMapping() {
        List<DownloadablePackage> allPackages = pm.listPackages();
        // map "name, version-classifier" to "name-classifier, version" (with -SNAPSHOT being a specific case)
        // for each unique "name-classifier", sort versions so we can attribute them a "CUDF posint" version
        // populate Nuxeo2CUDFMap and the reverse CUDF2NuxeoMap
    }

}