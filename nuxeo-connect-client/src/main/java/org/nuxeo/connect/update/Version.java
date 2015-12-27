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
 *     bstefanescu, jcarsique
 */
package org.nuxeo.connect.update;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Those versions are used in the Marketplace packages. They are in the form major.minor.patch-classifier with some
 * "special" classifiers which are "rc", "alpha", "beta" (upper or lower case), a letter followed by a date in the form
 * AAAAMMDD (for instance "I20131022") and the SNAPSHOT classifier.<br>
 * Order is the following:<br>
 * <code>
 * x.y.z-beta<br>
 * x.y.z-r20131022<br>
 * x.y.z-SNAPSHOT<br>
 * x.y.z<br>
 * x.y.z-anyclassifier<br>
 * </code><br>
 * Classifiers are alphabetically ordered between themselves.<br>
 * Special classifiers are before the SNAPSHOT.<br>
 * SNAPSHOT is always just before the release (without classifier).<br>
 * Non-special classifiers are after the release.<br>
 * See <a href=
 * "https://github.com/nuxeo/nuxeo-connect/blob/master/nuxeo-connect-client/src/test/java/org/nuxeo/connect/pm/tests/TestVersions.java"
 * >TestVersions</a>.
 */
public class Version implements Comparable<Version> {

    private static final Log log = LogFactory.getLog(Version.class);

    /**
     * @since 1.4
     */
    public static final String SNAPSHOT = "-SNAPSHOT";

    public final static Version ZERO = new Version(0);

    public final static Pattern VERSION_PATTERN = Pattern.compile("([0-9]+)(\\.[0-9]+)?(\\.[0-9]+)?");

    /**
     * @since 1.4.4
     */
    public static final Pattern SPECIAL_CLASSIFIER = Pattern.compile("^(((RC|rc|alpha|ALPHA|beta|BETA)\\d*)|([a-zA-Z][0-9]{8})).*$");

    /**
     * @since 1.4.4
     */
    protected boolean specialClassifier = false;

    /**
     * Special classifiers are considered as earlier than versions without classifier or with a non-special classifier
     *
     * @since 1.4.4
     */
    public boolean isSpecialClassifier() {
        return specialClassifier;
    }

    protected int major;

    protected int minor;

    protected int patch;

    protected String classifier;

    protected boolean snapshot = false;

    public Version(String version) {
        // Get the versionNumber
        Matcher versionMatcher = VERSION_PATTERN.matcher(version);
        if (versionMatcher.find()) {
            String versionNumber = versionMatcher.group();
            // Get the details of the version number
            Pattern digitVersion = Pattern.compile("[0-9]+");
            versionMatcher = digitVersion.matcher(versionNumber);
            if (versionMatcher.find()) {
                major = Integer.parseInt(versionMatcher.group());
                if (versionMatcher.find()) {
                    minor = Integer.parseInt(versionMatcher.group());
                    if (versionMatcher.find()) {
                        patch = Integer.parseInt(versionMatcher.group());
                    }
                }
            }

            // Get the classifier, if any
            String versionClassifier = version.substring(versionNumber.length());
            // Determine if it's a SNAPSHOT version
            if (versionClassifier.contains(SNAPSHOT)) {
                snapshot = true;
                versionClassifier = versionClassifier.replaceFirst(SNAPSHOT, "");
            }
            if (versionClassifier != null && !versionClassifier.isEmpty()) { // classifier found
                classifier = versionClassifier.substring(1);
                specialClassifier = SPECIAL_CLASSIFIER.matcher(classifier).matches();
            }
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
        if (classifier != null) {
            specialClassifier = SPECIAL_CLASSIFIER.matcher(classifier).matches();
        }
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

    @Override
    public int compareTo(Version o) {
        log.trace("Comparing " + this + " with " + o);
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
            if (snapshot == o.isSnapshot()) {
                log.trace(" case 1 => 0");
                return 0;
            } else {
                if (isSnapshot()) {
                    log.trace(" case 2 => -1");
                    return -1;
                } else {
                    log.trace(" case 3 => 1");
                    return 1;
                }
            }
        } else {
            if (specialClassifier && o.isSpecialClassifier() || !specialClassifier && !o.isSpecialClassifier()) {
                log.trace(" case 4 => compare classifiers");
                return mClassifier.compareTo(oClassifier);
            } else if (specialClassifier) {
                log.trace(" case 1 => -1");
                return -1;
            } else {
                log.trace(" case 6 => 1");
                return 1;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        return (this == o || o != null && (o instanceof Version) && compareTo((Version) o) == 0);
    }

    @Override
    public int hashCode() {
        return (major << 16) | (minor << 8) | patch;
    }

    @Override
    public String toString() {
        String v = major + "." + minor + "." + patch;

        if (classifier != null) {
            v = v + "-" + classifier;
        }

        if (isSnapshot()) {
            v = v + SNAPSHOT;
        }

        return v;
    }

    public boolean isSnapshot() {
        return snapshot;
    }

    /**
     * @since 1.4
     */
    public void setSnapshot(boolean isSnapshot) {
        snapshot = isSnapshot;
    }

    /**
     * @since 1.4
     */
    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    /**
     * @return true if the current version is an upgrade comparing to the given version
     * @since 1.4.19
     */
    public boolean isUpgradeFor(Version version) {
        return isUpgradeFor(version, true);
    }

    /**
     * @param snapshotUpgrade whether to upgrade when both versions are SNAPSHOT and equal to each other
     * @return true if the current version is an upgrade comparing to the given version
     * @since 1.4.19
     */
    public boolean isUpgradeFor(Version version, boolean snapshotUpgrade) {
        return greaterThan(version) || snapshotUpgrade && isSnapshot() && equalsTo(version);
    }

}
