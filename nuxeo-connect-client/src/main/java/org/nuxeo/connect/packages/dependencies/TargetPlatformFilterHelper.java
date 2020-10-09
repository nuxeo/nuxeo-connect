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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.connect.platform.PlatformVersion;
import org.nuxeo.connect.platform.PlatformVersionRange;
import org.nuxeo.connect.update.Package;

/**
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class TargetPlatformFilterHelper {

    protected static Log log = LogFactory.getLog(TargetPlatformFilterHelper.class);

    public static boolean isCompatibleWithTargetPlatform(Package pkg, String targetPlatform,
            String targetPlatformVersion) {
        String tpRangeSpec = pkg.getTargetPlatformRange();
        if (StringUtils.isBlank(tpRangeSpec) || targetPlatformVersion == null) {
            // keep backward compatibility with former target platforms list
            return isCompatibleWithTargetPlatform(pkg.getTargetPlatforms(), targetPlatform);
        }

        try {
            PlatformVersion tpVersion = new PlatformVersion(targetPlatformVersion);
            PlatformVersionRange pkgAllowedTpRange = PlatformVersionRange.fromRangeSpec(tpRangeSpec);
            return pkgAllowedTpRange.containsVersion(tpVersion);
        } catch (IllegalArgumentException e) {
            log.warn(String.format(
                    "Could not parse target platform range expression '%s' for package '%s' "
                            + "or current platform version '%s', using former compatibility format.",
                    tpRangeSpec, pkg.getId(), targetPlatformVersion), e);
            return isCompatibleWithTargetPlatform(pkg.getTargetPlatforms(), targetPlatform);
        }

    }

    /**
     * @param targetPlatforms The target platforms on which to check compliance.
     * @param targetPlatform The target platform to match with.
     * @since 1.4.24
     */
    private static boolean isCompatibleWithTargetPlatform(String[] targetPlatforms, String targetPlatform) {
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
