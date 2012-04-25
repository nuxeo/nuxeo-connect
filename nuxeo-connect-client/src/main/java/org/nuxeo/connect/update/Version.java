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
 *     bstefanescu, jcarsique
 */
package org.nuxeo.connect.update;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public class Version implements Comparable<Version> {

    /**
     * @since 5.6
     */
    public static final String SNAPSHOT = "-SNAPSHOT";

    public final static Version ZERO = new Version(0);

    protected int major;

    protected int minor;

    protected int patch;

    protected String classifier;

    protected boolean snapshot = false;

    public Version(String version) {
        int i = version.indexOf(SNAPSHOT);
        if (i > 0) {
            version = version.substring(0, i);
            snapshot = true;
        }
        int p = version.lastIndexOf('-');
        if (p > 0) { // classifier found
            classifier = version.substring(p + 1);
            version = version.substring(0, p);
        }
        p = version.indexOf('.', 0);
        if (p > -1) {
            major = Integer.parseInt(version.substring(0, p));
            int q = version.indexOf('.', p + 1);
            if (q > -1) {
                minor = Integer.parseInt(version.substring(p + 1, q));
                patch = Integer.parseInt(version.substring(q + 1));
            } else {
                minor = Integer.parseInt(version.substring(p + 1));
            }
        } else {
            major = Integer.parseInt(version);
        }
    }

    public Version(int major) {
        this(major, 0, 0);
    }

    public Version(int major, int minor) {
        this(major, minor, 0);
    }

    public Version(int major, int minor, int patch) {
        this(major, minor, patch, null);
    }

    public Version(int major, int minor, int patch, String classifier) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.classifier = classifier;
    }

    public int major() {
        return major;
    }

    public int minor() {
        return minor;
    }

    public int patch() {
        return patch;
    }

    public String classifier() {
        return classifier;
    }

    public boolean lessThan(Version v) {
        return compareTo(v) < 0;
    }

    public boolean lessOrEqualsThan(Version v) {
        return compareTo(v) <= 0;
    }

    public boolean equalsTo(Version v) {
        return compareTo(v) == 0;
    }

    public boolean greaterThan(Version v) {
        return compareTo(v) > 0;
    }

    public boolean greaterOrEqualThan(Version v) {
        return compareTo(v) >= 0;
    }

    public int compareTo(Version o) {
        int d = major - o.major;
        if (d != 0) {
            return d;
        }
        d = minor - o.minor;
        if (d != 0) {
            return d;
        }
        d = patch - o.patch;
        if (d != 0) {
            return d;
        }

        String mClassifier = (classifier == null) ? "" : classifier;
        String oClassifier = (o.classifier == null) ? "" : o.classifier;

        if (mClassifier.equals(oClassifier)) {
            if (new Boolean(snapshot).equals(o.isSnapshot())) {
                return 0;
            } else {
                if (isSnapshot()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        } else {
            return mClassifier.compareTo(oClassifier);
        }
    }

    @Override
    public boolean equals(Object o) {
        return (this == o || o != null && (o instanceof Version)
                && compareTo((Version) o) == 0);
    }

    @Override
    public int hashCode() {
        return (major << 16) | (minor << 8) | patch;
    }

    public String toString() {
        String v;
        if (classifier == null) {
            v = major + "." + minor + "." + patch;
        } else {
            v = major + "." + minor + "." + patch + "-" + classifier;
        }
        if (isSnapshot()) {
            v = v + SNAPSHOT;
        }
        return v;
    }

    public static void main(String[] args) {
        System.out.println(new Version("1.0" + SNAPSHOT));
        System.out.println(new Version("1" + SNAPSHOT));
        System.out.println(new Version("1.0.0" + SNAPSHOT));
        System.out.println(new Version(
                new Version("1.0.0" + SNAPSHOT).toString()));
    }

    public boolean isSnapshot() {
        return snapshot;
    }

    /**
     * @since 5.6
     */
    public void setSnapshot(boolean isSnapshot) {
        snapshot = isSnapshot;
    }

    /**
     * @since 5.6
     */
    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

}
