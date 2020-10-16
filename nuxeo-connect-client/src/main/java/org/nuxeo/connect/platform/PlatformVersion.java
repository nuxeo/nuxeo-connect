/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 * @origin https://github.com/apache/maven/blob/master/maven-artifact/src/main/java/org/apache/maven/artifact/versioning/DefaultArtifactVersion.java
 */
package org.nuxeo.connect.platform;

import java.util.Locale;

/**
 * Implementation of a Nuxeo platform version.<br />
 * Constraints on the version are:
 * <ul>
 * <li>Format is like <b>Major[.Minor[.BuildNumber]][-Qualifier]</b></li>
 * <li>Minor, BuildNumber and Qualifier are optional</li>
 * <li>Major, Minor and BuildNumber are integers < 10 000</li>
 * <li>Qualifier is a string (i.e alpha, beta, RC, SNAPSHOT, etc... have no special meanings and will be treated as any
 * other string)</li>
 * </ul>
 */
public class PlatformVersion implements Comparable<PlatformVersion> {
    // Cannot use apachae.commons logs because of GWT compliance
    // private static final Log log = LogFactory.getLog(PlatformVersion.class);

    public static final int MAX_VERSION_PART_VALUE = 9_999;

    private Integer majorVersion;

    private Integer minorVersion;

    private Integer buildNumber;

    private String qualifier;

    private String comparable;

    public PlatformVersion(String version) {
        parseVersion(version);
    }

    @Override
    public int hashCode() {
        return comparable.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof PlatformVersion)) {
            return false;
        }

        return compareTo((PlatformVersion) other) == 0;
    }

    @Override
    public int compareTo(PlatformVersion otherVersion) {
        return this.comparable.compareTo(otherVersion.comparable);
    }

    public int getMajorVersion() {
        return majorVersion != null ? majorVersion : 0;
    }

    public int getMinorVersion() {
        return minorVersion != null ? minorVersion : 0;
    }

    public int getBuildNumber() {
        return buildNumber != null ? buildNumber : 0;
    }

    public String getQualifier() {
        return qualifier;
    }

    public String getComparable() {
        return comparable != null ? comparable : "0";
    }

    public final void parseVersion(String version) {
        if (isBlank(version)) {
            throw new IllegalArgumentException("Version cannot be blank");
        }
        if (version.contains(",") || version.contains("[") || version.contains("]") || version.contains("(")
                || version.contains(")")) {
            throw new IllegalArgumentException(
                    "Version cannot contain commas (','), brackets ('[]') or parenthesis ('()'): " + version);
        }
        String[] parts = version.split("-", -1);
        // version
        String[] versionParts = parts[0].split("\\.");
        if (!isDigits(versionParts[0].trim())) {
            throw new IllegalArgumentException("Version should at least explicit a major number: " + version);
        }
        majorVersion = tryParseVersionPartInt(versionParts[0].trim());
        if (versionParts.length >= 2) {
            minorVersion = tryParseVersionPartInt(versionParts[1].trim());
        }
        if (versionParts.length >= 3) {
            buildNumber = tryParseVersionPartInt(versionParts[2].trim());
        }
        if (versionParts.length >= 4) {
            // log.warn("Too many parts in version '" + version + "', parts after the 3rd dot will be ignored");
        }

        // qualifier
        if (parts.length >= 2) {
            String trimmedQualifier = version.substring(version.indexOf("-") + 1).trim();
            if (containsWhitespace(trimmedQualifier)) {
                throw new IllegalArgumentException("Version cannot contain whitespaces in qualifier: " + version);
            }
            qualifier = trimmedQualifier;
        }

        comparable = computeComparable(this);
    }

    public static String computeComparable(PlatformVersion version) {
        StringBuilder builder = new StringBuilder();
        builder.append(formatOn4Digits(version.getMajorVersion()))
               .append(".")
               .append(formatOn4Digits(version.getMinorVersion()))
               .append(".")
               .append(formatOn4Digits(version.getBuildNumber()));
        String qualifier = version.getQualifier();
        if (!isBlank(qualifier)) {
            builder.append("-").append(qualifier.toUpperCase(Locale.ROOT));
        }
        return builder.toString();
    }

    private static String formatOn4Digits(int number) {
        String result = "" + number;
        while (result.length() < 4) {
            result = "0" + result;
        }
        return result;
    }

    private static Integer tryParseVersionPartInt(String s) {
        // for performance, check digits instead of relying later on catching NumberFormatException
        if (!isDigits(s)) {
            // log.warn("Version part '" + s + "' is not a digit and will be ignored");
            return null;
        }

        try {
            long longValue = Long.parseLong(s);
            if (longValue > MAX_VERSION_PART_VALUE) {
                return null;
            }
            return (int) longValue;
        } catch (NumberFormatException e) {
            // log.warn("Version part '" + s + "' does not contain a parsable integer "
            // + "(may be outside the range for integers) and will be ignored");
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(asString());
        builder.append(" (").append(comparable).append(")");
        return builder.toString();
    }

    public String asString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getMajorVersion()).append(".").append(getMinorVersion());
        if (getBuildNumber() > 0) {
            builder.append(".").append(getBuildNumber());
        }
        if (!isBlank(qualifier)) {
            builder.append("-").append(qualifier);
        }
        return builder.toString();
    }

    public boolean isEqualTo(PlatformVersion otherVersion) {
        return this.compareTo(otherVersion) == 0;
    }

    public boolean isBeforeIncluding(PlatformVersion otherVersion) {
        return this.compareTo(otherVersion) <= 0;
    }

    public boolean isAfterIncluding(PlatformVersion otherVersion) {
        return this.compareTo(otherVersion) >= 0;
    }

    public boolean isBefore(PlatformVersion otherVersion) {
        return this.compareTo(otherVersion) < 0;
    }

    public boolean isAfter(PlatformVersion otherVersion) {
        return this.compareTo(otherVersion) > 0;
    }

    public boolean isBetween(PlatformVersion left, PlatformVersion right) {
        return isAfter(left) && isBefore(right);
    }

    /** Copy of org.apache.commons.lang3.StringUtils methods for GWT compliance */

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsWhitespace(final CharSequence seq) {
        if (seq == null) {
            return false;
        }
        final int strLen = seq.length();
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(seq.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDigits(final CharSequence cs) {
        if (cs == null || cs.length() == 0) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
