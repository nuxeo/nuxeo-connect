/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 */

package org.nuxeo.connect.packages.dependencies;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.connect.packages.PackageManager;
import org.nuxeo.connect.update.PackageDependency;

/**
 * This implementation uses the p2cudf resolver to solve complex dependencies
 *
 * @since 5.6
 */
public class P2CUDFDependencyResolver implements DependencyResolver {

    protected static Log log = LogFactory.getLog(P2CUDFDependencyResolver.class);

    protected PackageManager pm;

    protected CUDFHelper cudfHelper;

    protected P2CUDFDependencyResolver() {
    }

    public P2CUDFDependencyResolver(PackageManager pm) {
        this.pm = pm;
        cudfHelper = new CUDFHelper(pm);
    }

    public DependencyResolution resolve(List<String> pkgInstall,
            List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform) throws DependencyException {
        // generate CUDF "package universe"
        String cudf = cudfHelper.getCUDFFile();
        // System.out.println("CUDF universe:\n" + cudf);
        // get request stanza
        cudf = cudfHelper.getCUDFFile(str2PkgDep(pkgInstall),
                str2PkgDep(pkgRemove), str2PkgDep(pkgUpgrade));
        System.out.println("CUDF request:\n" + cudf);
        // pass to p2cudf for solving
        // build a DependencyResolution from the result
        return null;
    }

    private PackageDependency[] str2PkgDep(List<String> pkgList) {
        List<PackageDependency> list = new ArrayList<PackageDependency>();
        if (pkgList == null || pkgList.size() == 0) {
            return list.toArray(new PackageDependency[0]);
        }
        for (String pkgStr : pkgList) {
            list.add(new PackageDependency(pkgStr));
        }
        return list.toArray(new PackageDependency[list.size()]);
    }

    public DependencyResolution resolve(String pkgId, String targetPlatform)
            throws DependencyException {
        List<String> pkgInstall = new ArrayList<String>();
        pkgInstall.add(pkgId);
        return resolve(pkgInstall, null, null, targetPlatform);
    }

}
