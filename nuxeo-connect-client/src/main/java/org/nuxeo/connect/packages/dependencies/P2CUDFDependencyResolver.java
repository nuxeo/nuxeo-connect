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
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.connect.packages.dependencies;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.connect.packages.PackageManager;

/**
* This implementation uses the p2cudf resolver to solve complex dependencies
*/
public class P2CUDFDependencyResolver implements DependencyResolver {

    protected static Log log = LogFactory.getLog(P2CUDFDependencyResolver.class);

    protected PackageManager pm;

    public P2CUDFDependencyResolver(PackageManager pm) {
        this.pm=pm;
    }

    public DependencyResolution resolve(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade, String targetPlatform) {
        // get helper
        // create mapping
        // generate "package universe" CUDF
        // generate request stanza
        // pass to p2cudf for solving
        // build a DependencyResolution from the result
        return null;
    }

    public DependencyResolution resolve(String pkgId, String targetPlatform)  throws DependencyException{
        List<String> pkgInstall = new ArrayList<String>();
        pkgInstall.add(pkgId);
        return resolve(pkgInstall, null, null, targetPlatform);
    }

}
