/*
 * (C) Copyright 2006-2015 Nuxeo SA (http://nuxeo.com/) and others.
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
import org.apache.commons.lang.StringUtils;
import org.nuxeo.connect.update.Package;

/**
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class TargetPlatformFilterHelper {

    public static boolean isCompatibleWithTargetPlatform(Package pkg, String targetPlatform) {
        return isCompatibleWithTargetPlatform(pkg.getTargetPlatforms(), targetPlatform);
    }

    /**
     * @param targetPlatforms The target platforms on which to check compliance.
     * @param targetPlatform The target platform to match with.
     * @since 1.4.24
     */
    public static boolean isCompatibleWithTargetPlatform(String[] targetPlatforms, String targetPlatform) {
        if (StringUtils.isBlank(targetPlatform) || targetPlatforms == null || targetPlatforms.length == 0) {
            return true;
        }
        for (String target : targetPlatforms) {
            if (FilenameUtils.wildcardMatch(targetPlatform, target, IOCase.INSENSITIVE)) {
                return true;
            }
        }
        return false;
    }
}
