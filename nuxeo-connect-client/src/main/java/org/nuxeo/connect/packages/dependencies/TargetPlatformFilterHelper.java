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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.connect.platform.PlatformId;
import org.nuxeo.connect.platform.PlatformVersionRange;
import org.nuxeo.connect.update.Package;

/**
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class TargetPlatformFilterHelper {

    protected static Log log = LogFactory.getLog(TargetPlatformFilterHelper.class);

    public static boolean isCompatibleWithTargetPlatform(Package pkg, PlatformId targetPlatform) {
        if (targetPlatform == null) {
            return true;
        }
        if (StringUtils.isBlank(pkg.getTargetPlatformRange()) || StringUtils.isBlank(pkg.getTargetPlatformName())) {
            // Use former target platforms list
            return isCompatibleWithTargetPlatform(pkg.getTargetPlatforms(), targetPlatform);
        }

        try {
            PlatformVersionRange pkgAllowedTpRange = PlatformVersionRange.fromRangeSpec(pkg.getTargetPlatformRange());
            return pkgAllowedTpRange.containsVersion(targetPlatform.version())
                    && pkg.getTargetPlatformName().equalsIgnoreCase(targetPlatform.name());
        } catch (IllegalArgumentException e) {
            log.warn(String.format(
                    "Could not parse target platform range expression '%s' for package '%s', using former compatibility format.",
                    pkg.getTargetPlatformRange(), pkg.getId()), e);
            // Use former target platforms list
            return isCompatibleWithTargetPlatform(pkg.getTargetPlatforms(), targetPlatform);
        }

    }

    /**
     * @param targetPlatforms The target platforms on which to check compliance.
     * @param targetPlatform The target platform to match with.
     * @since 1.4.24
     */
    private static boolean isCompatibleWithTargetPlatform(String[] targetPlatforms, PlatformId targetPlatform) {
        if (targetPlatforms == null || targetPlatforms.length == 0) {
            return true;
        }
        Set<String> possibleStringForms = getPossibleStringForms(targetPlatform);
        for (String target : targetPlatforms) {
            if (possibleStringForms.stream()
                                   .anyMatch(tpString -> FilenameUtils.wildcardMatch(tpString, target,
                                           IOCase.INSENSITIVE))) {
                return true;
            }
        }
        return false;
    }

    protected static Set<String> getPossibleStringForms(PlatformId targetPlatform) {
        Set<String> result = new HashSet<>();
        String defaultAsString = targetPlatform.asString();
        result.add(defaultAsString);
        if (targetPlatform.version().getBuildNumber() == 0) {
            String qualifier = targetPlatform.version().getQualifier();
            boolean hasQualifier = StringUtils.isNotBlank(qualifier);
            String qualifierSuffix = hasQualifier ? "-" + qualifier : "";
            String noQualifier = StringUtils.removeEnd(defaultAsString, qualifierSuffix);
            String with0BuildNumber = noQualifier + ".0" + qualifierSuffix;
            result.add(with0BuildNumber);
            if (targetPlatform.version().getMinorVersion() == 0) {
                String without0Minor = StringUtils.removeEnd(noQualifier, ".0") + qualifierSuffix;
                result.add(without0Minor);
            }
        }
        return result;
    }
}
