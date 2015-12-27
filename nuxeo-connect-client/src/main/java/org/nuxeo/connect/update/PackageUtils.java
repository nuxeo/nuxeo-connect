/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     "Julien Carsique"
 */
package org.nuxeo.connect.update;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @since 1.4.18
 */
public class PackageUtils {

    public static final String SYMBOLIC_NAME_PATTERN = "[a-zA-Z_]+.*";

    public static final String VERSION_PATTERN = "\\d+\\.\\d+\\.\\d+(-.+)?";

    public static final Pattern NAME = Pattern.compile("(" + SYMBOLIC_NAME_PATTERN + ")-(" + VERSION_PATTERN + ")");

    private PackageUtils() {
    }

    /**
     * @param packageId In the form "name-version"
     * @return null if not match
     */
    public static String getPackageName(String packageId) {
        Matcher matcher = NAME.matcher(packageId);
        if (!matcher.matches()) { // avoid IllegalStateException on later call to Matcher.group()
            return null;
        }
        return matcher.group(1);
    }

    /**
     * @param packageId In the form "name-version"
     * @return null if not match
     */
    public static String getPackageVersion(String packageId) {
        Matcher matcher = NAME.matcher(packageId);
        if (!matcher.matches()) { // avoid IllegalStateException on later call to Matcher.group()
            return null;
        }
        return matcher.group(2);
    }

    public static boolean isValidPackageId(String packageId) {
        return getPackageName(packageId) != null && getPackageVersion(packageId) != null;
    }

}
