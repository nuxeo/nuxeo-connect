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
 * @origin https://github.com/apache/maven/blob/master/maven-artifact/src/main/java/org/apache/maven/artifact/versioning/Restriction.java
 */

package org.nuxeo.connect.platform;

import org.apache.commons.lang3.StringUtils;

public class PlatformVersionRange {
    private final PlatformVersion lowerBound;

    private final boolean lowerBoundInclusive;

    private final PlatformVersion upperBound;

    private final boolean upperBoundInclusive;

    public static final PlatformVersionRange EVERYTHING = new PlatformVersionRange(null, false, null, false);

    public PlatformVersionRange(PlatformVersion lowerBound, boolean lowerBoundInclusive, PlatformVersion upperBound,
            boolean upperBoundInclusive) {
        this.lowerBound = lowerBound;
        this.lowerBoundInclusive = lowerBoundInclusive;
        this.upperBound = upperBound;
        this.upperBoundInclusive = upperBoundInclusive;
    }

    public PlatformVersion getLowerBound() {
        return lowerBound;
    }

    public boolean isLowerBoundInclusive() {
        return lowerBoundInclusive;
    }

    public PlatformVersion getUpperBound() {
        return upperBound;
    }

    public boolean isUpperBoundInclusive() {
        return upperBoundInclusive;
    }

    public static PlatformVersionRange fromRangeSpec(String rangeSpec) {
        if (StringUtils.isBlank(rangeSpec)) {
            throw new IllegalArgumentException("Range cannot be blank");
        }
        rangeSpec = rangeSpec.trim();
        boolean lowerBoundInclusive = rangeSpec.startsWith("[");
        boolean upperBoundInclusive = rangeSpec.endsWith("]");

        String process;
        if (!rangeSpec.contains(",")) {
            // this is a single version
            if (lowerBoundInclusive && upperBoundInclusive) {
                process = rangeSpec.substring(1, rangeSpec.length() - 1).trim();
            } else if (lowerBoundInclusive || upperBoundInclusive || rangeSpec.startsWith("(")
                    || rangeSpec.endsWith(")")) {
                throw new IllegalArgumentException(
                        "Single version can only have inclusive boundaries ('[x.y.z]'): " + rangeSpec);
            } else {
                process = rangeSpec.trim();
                lowerBoundInclusive = upperBoundInclusive = true;
            }

            PlatformVersion version = new PlatformVersion(process);
            return new PlatformVersionRange(version, lowerBoundInclusive, version, upperBoundInclusive);
        } else {
            // this is a range
            if (!lowerBoundInclusive && !rangeSpec.startsWith("(")) {
                throw new IllegalArgumentException("Range should start with '[' or '(': " + rangeSpec);
            }
            if (!upperBoundInclusive && !rangeSpec.endsWith(")")) {
                throw new IllegalArgumentException("Range should end with ']' or ')': " + rangeSpec);
            }
            process = rangeSpec.substring(1, rangeSpec.length() - 1).trim();
            int index = process.indexOf(',');

            String lowerBound = process.substring(0, index).trim();
            String upperBound = process.substring(index + 1).trim();

            PlatformVersion lowerVersion = null;
            if (lowerBound.length() > 0) {
                lowerVersion = new PlatformVersion(lowerBound);
            }
            PlatformVersion upperVersion = null;
            if (upperBound.length() > 0) {
                upperVersion = new PlatformVersion(upperBound);
            }

            if (upperVersion != null && lowerVersion != null) {
                if ((!lowerBoundInclusive || !upperBoundInclusive) && upperVersion.equals(lowerVersion)) {
                    throw new IllegalArgumentException(
                            "Range cannot have identical boundaries with exclusions: " + rangeSpec);
                }
                if (upperVersion.compareTo(lowerVersion) < 0) {
                    throw new IllegalArgumentException("Range defies version ordering: " + rangeSpec);
                }
            }

            return new PlatformVersionRange(lowerVersion, lowerBoundInclusive, upperVersion, upperBoundInclusive);
        }
    }

    public boolean containsVersion(PlatformVersion version) {
        if (lowerBound != null) {
            int comparison = lowerBound.compareTo(version);

            if ((comparison == 0) && !lowerBoundInclusive) {
                return false;
            }
            if (comparison > 0) {
                return false;
            }
        }
        if (upperBound != null) {
            int comparison = upperBound.compareTo(version);

            if ((comparison == 0) && !upperBoundInclusive) {
                return false;
            }
            if (comparison < 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 13;

        if (lowerBound == null) {
            result += 1;
        } else {
            result += lowerBound.hashCode();
        }

        result *= lowerBoundInclusive ? 1 : 2;

        if (upperBound == null) {
            result -= 3;
        } else {
            result -= upperBound.hashCode();
        }

        result *= upperBoundInclusive ? 2 : 3;

        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof PlatformVersionRange)) {
            return false;
        }

        PlatformVersionRange restriction = (PlatformVersionRange) other;
        if (lowerBound != null) {
            if (!lowerBound.equals(restriction.lowerBound)) {
                return false;
            }
        } else if (restriction.lowerBound != null) {
            return false;
        }

        if (lowerBoundInclusive != restriction.lowerBoundInclusive) {
            return false;
        }

        if (upperBound != null) {
            if (!upperBound.equals(restriction.upperBound)) {
                return false;
            }
        } else if (restriction.upperBound != null) {
            return false;
        }

        return upperBoundInclusive == restriction.upperBoundInclusive;

    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(isLowerBoundInclusive() ? '[' : '(');
        if (getLowerBound() != null) {
            buf.append(getLowerBound().toString());
        }
        buf.append(',');
        if (getUpperBound() != null) {
            buf.append(getUpperBound().toString());
        }
        buf.append(isUpperBoundInclusive() ? ']' : ')');

        return buf.toString();
    }
}
