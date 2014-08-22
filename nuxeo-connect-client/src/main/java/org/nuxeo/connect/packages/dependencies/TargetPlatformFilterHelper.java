/*
 * (C) Copyright 2006-2014 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 *
 */
package org.nuxeo.connect.packages.dependencies;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

import org.nuxeo.connect.update.Package;

/**
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 *
 */
public class TargetPlatformFilterHelper {

    public static boolean isCompatibleWithTargetPlatform(Package pkg,
            String targetPlatform) {
        if (targetPlatform == null || pkg.getTargetPlatforms() == null
                || pkg.getTargetPlatforms().length == 0) {
            return true;
        }
        for (String target : pkg.getTargetPlatforms()) {
            if (FilenameUtils.wildcardMatch(targetPlatform, target,
                    IOCase.INSENSITIVE)) {
                return true;
            }
        }
        return false;
    }
}
