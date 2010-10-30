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

import org.nuxeo.connect.data.DownloadablePackage;

/**
*
* @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
*
*/
public class TargetPlatformFilterHelper {

    public static boolean isCompatibleWithTargetPlatform(DownloadablePackage pkg, String targetPlatform) {
        if (targetPlatform==null) {
            return true;
        }
        if (pkg.getTargetPlatforms()==null || pkg.getTargetPlatforms().length==0) {
            return true;
        }
        for (String pf : pkg.getTargetPlatforms()) {
            if (pf.equals(targetPlatform)) {
                return true;
            }
        }
        return false;
    }
}
