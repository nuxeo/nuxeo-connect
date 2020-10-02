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

import java.util.StringTokenizer;

/**
 * Default implementation of artifact versioning.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class DefaultTargetPlatformVersion implements TargetPlatformVersion {
    private Integer majorVersion;

    private Integer minorVersion;

    private Integer incrementalVersion;

    private Integer buildNumber;

    private String qualifier;

    private ComparableVersion comparable;

    public DefaultTargetPlatformVersion(String version) {
        parseVersion(version);
    }

    @Override
    public int hashCode() {
        return 11 + comparable.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof TargetPlatformVersion)) {
            return false;
        }

        return compareTo((TargetPlatformVersion) other) == 0;
    }

    @Override
    public int compareTo(TargetPlatformVersion otherVersion) {
        if (otherVersion instanceof DefaultTargetPlatformVersion) {
            return this.comparable.compareTo(((DefaultTargetPlatformVersion) otherVersion).comparable);
        } else {
            return compareTo(new DefaultTargetPlatformVersion(otherVersion.toString()));
        }
    }

    @Override
    public int getMajorVersion() {
        return majorVersion != null ? majorVersion : 0;
    }

    @Override
    public int getMinorVersion() {
        return minorVersion != null ? minorVersion : 0;
    }

    @Override
    public int getIncrementalVersion() {
        return incrementalVersion != null ? incrementalVersion : 0;
    }

    @Override
    public int getBuildNumber() {
        return buildNumber != null ? buildNumber : 0;
    }

    @Override
    public String getQualifier() {
        return qualifier;
    }

    @Override
    public final void parseVersion(String version) {
        comparable = new ComparableVersion(version);

        int index = version.indexOf('-');

        String part1;
        String part2 = null;

        if (index < 0) {
            part1 = version;
        } else {
            part1 = version.substring(0, index);
            part2 = version.substring(index + 1);
        }

        if (part2 != null) {
            if (part2.length() == 1 || !part2.startsWith("0")) {
                buildNumber = tryParseInt(part2);
                if (buildNumber == null) {
                    qualifier = part2;
                }
            } else {
                qualifier = part2;
            }
        }

        if ((!part1.contains(".")) && !part1.startsWith("0")) {
            majorVersion = tryParseInt(part1);
            if (majorVersion == null) {
                // qualifier is the whole version, including "-"
                qualifier = version;
                buildNumber = null;
            }
        } else {
            boolean fallback = false;

            StringTokenizer tok = new StringTokenizer(part1, ".");
            if (tok.hasMoreTokens()) {
                majorVersion = getNextIntegerToken(tok);
                if (majorVersion == null) {
                    fallback = true;
                }
            } else {
                fallback = true;
            }
            if (tok.hasMoreTokens()) {
                minorVersion = getNextIntegerToken(tok);
                if (minorVersion == null) {
                    fallback = true;
                }
            }
            if (tok.hasMoreTokens()) {
                incrementalVersion = getNextIntegerToken(tok);
                if (incrementalVersion == null) {
                    fallback = true;
                }
            }
            if (tok.hasMoreTokens()) {
                qualifier = tok.nextToken();
                fallback = isDigits(qualifier);
            }

            // string tokenizer won't detect these and ignores them
            if (part1.contains("..") || part1.startsWith(".") || part1.endsWith(".")) {
                fallback = true;
            }

            if (fallback) {
                // qualifier is the whole version, including "-"
                qualifier = version;
                majorVersion = null;
                minorVersion = null;
                incrementalVersion = null;
                buildNumber = null;
            }
        }
    }

    private static Integer getNextIntegerToken(StringTokenizer tok) {
        String s = tok.nextToken();
        if ((s.length() > 1) && s.startsWith("0")) {
            return null;
        }
        return tryParseInt(s);
    }

    private static Integer tryParseInt(String s) {
        // for performance, check digits instead of relying later on catching NumberFormatException
        if (!isDigits(s)) {
            return null;
        }

        try {
            long longValue = Long.parseLong(s);
            if (longValue > Integer.MAX_VALUE) {
                return null;
            }
            return (int) longValue;
        } catch (NumberFormatException e) {
            // should never happen since checked isDigits(s) before
            return null;
        }
    }

    @Override
    public String toString() {
        return comparable.toString();
    }
}
