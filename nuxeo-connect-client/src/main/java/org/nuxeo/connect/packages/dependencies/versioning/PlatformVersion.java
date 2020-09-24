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
package org.nuxeo.connect.packages.dependencies.versioning;

import static org.apache.commons.lang3.math.NumberUtils.isDigits;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    private static final Log log = LogFactory.getLog(PlatformVersion.class);

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
        if (StringUtils.isBlank(version)) {
            throw new IllegalArgumentException("Version cannot be blank");
        }
        if (StringUtils.containsAny(version, ",", "[", "]", "(", ")")) {
            throw new IllegalArgumentException(String.format(
                    "Version cannot contain commas (','), brackets ('[]') or parenthesis ('()'): %s", version));
        }
        String[] parts = version.split("-", -1);
        // version
        String[] versionParts = parts[0].split("\\.");
        if (!isDigits(versionParts[0].trim())) {
            throw new IllegalArgumentException(
                    String.format("Version should at least explicit a major number: %s", version));
        }
        majorVersion = tryParseVersionPartInt(versionParts[0].trim());
        if (versionParts.length >= 2) {
            minorVersion = tryParseVersionPartInt(versionParts[1].trim());
        }
        if (versionParts.length >= 3) {
            buildNumber = tryParseVersionPartInt(versionParts[2].trim());
        }
        if (versionParts.length >= 4) {
            log.warn(String.format("Too many parts in version '%s', parts after the 3rd dot will be ignored", version));
        }

        // qualifier
        if (parts.length >= 2) {
            String trimmedQualifier = version.substring(version.indexOf("-") + 1).trim();
            if (StringUtils.containsWhitespace(trimmedQualifier)) {
                throw new IllegalArgumentException(
                        String.format("Version cannot contain whitespaces in qualifier: '%s'", version));
            }
            qualifier = trimmedQualifier;
        }

        comparable = computeComparable(this);
    }

    public static String computeComparable(PlatformVersion version) {
        StringBuilder builder = new StringBuilder();

        builder.append(String.format("%04d", version.getMajorVersion()))
               .append(".")
               .append(String.format("%04d", version.getMinorVersion()))
               .append(".")
               .append(String.format("%04d", version.getBuildNumber()));
        String qualifier = version.getQualifier();
        if (StringUtils.isNotBlank(qualifier)) {
            builder.append("-").append(qualifier.toUpperCase(Locale.ROOT));
        }
        return builder.toString();
    }

    private static Integer tryParseVersionPartInt(String s) {
        // for performance, check digits instead of relying later on catching NumberFormatException
        if (!isDigits(s)) {
            log.warn(String.format("Version part '%s' is not a digit and will be ignored", s));
            return null;
        }

        try {
            long longValue = Long.parseLong(s);
            if (longValue > MAX_VERSION_PART_VALUE) {
                return null;
            }
            return (int) longValue;
        } catch (NumberFormatException e) {
            log.warn(String.format("Version part '%s' does not contain a parsable integer "
                    + "(may be outside the range for integers) and will be ignored", s));
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getMajorVersion()).append(".").append(getMinorVersion()).append(".").append(getBuildNumber());
        if (StringUtils.isNotBlank(qualifier)) {
            builder.append("-").append(qualifier);
        }
        builder.append(" (").append(comparable).append(")");
        return builder.toString();
    }
}
