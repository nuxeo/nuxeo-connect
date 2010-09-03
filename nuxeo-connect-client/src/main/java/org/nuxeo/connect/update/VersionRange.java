/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     bstefanescu
 */
package org.nuxeo.connect.update;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class VersionRange {

    public static final VersionRange ANY = new VersionRange((Version) null);

    protected Version minVersion;

    protected Version maxVersion;

    /**
     * expr is of the form <code>v1:v2</code>
     *
     * @param expr
     */
    public VersionRange(String expr) {
        if (expr != null && expr.length() > 0) {
            int p = expr.indexOf(':');
            if (p == -1) { // only min version
                minVersion = new Version(expr);
            } else {
                minVersion = new Version(expr.substring(0, p));
                maxVersion = new Version(expr.substring(p + 1));
            }
        }
    }

    public VersionRange(Version minVersion) {
        this.minVersion = minVersion;
    }

    public VersionRange(Version minVersion, Version maxVersion) {
        this.minVersion = minVersion == null && maxVersion != null ? Version.ZERO
                : minVersion;
        this.maxVersion = maxVersion;
    }

    public Version getMinVersion() {
        return minVersion;
    }

    public Version getMaxVersion() {
        return maxVersion;
    }

    public boolean matchVersion(Version version) {
        if (minVersion == null && maxVersion == null) {
            return true;
        }
        if (minVersion != null) {
            if (maxVersion != null) { // check range
                return version.greaterOrEqualThan(minVersion)
                        && version.lessOrEqualsThan(maxVersion);
            } else { // greater than min version
                return version.greaterOrEqualThan(minVersion);
            }
        } // else max version is not null
        // less than maxVersion
        return version.lessOrEqualsThan(maxVersion);
    }

    @Override
    public String toString() {
        if (minVersion == null && maxVersion == null) {
            return "";
        }
        if (minVersion != null) {
            if (maxVersion != null) {
                return minVersion.toString() + ':' + maxVersion.toString();
            } else {
                return minVersion.toString();
            }
        } // else maxVersion is non null
        return Version.ZERO.toString() + ':' + maxVersion.toString();
    }

}
