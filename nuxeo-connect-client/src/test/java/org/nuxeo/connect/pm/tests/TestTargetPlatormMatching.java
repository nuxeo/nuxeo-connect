/*
 * (C) Copyright 2020 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Yannis JULIENNE
 *
 */

package org.nuxeo.connect.pm.tests;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.platform.PlatformId;
import org.nuxeo.connect.update.PackageException;

/**
 * @since 1.7.9
 */
public class TestTargetPlatormMatching extends AbstractPackageManagerTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("localTPMatching.json");
        assertTrue(CollectionUtils.isNotEmpty(local));
        pm.registerSource(new DummyPackageSource(local, "localTPMatching"), true);
    }

    public void testTargetPlatformVersionRangesMatchEverything() throws PackageException {
        // GIVEN pkgMatchEverything has a targetPlaformRange defined as "[0,)"
        // THEN it matches all platforms with version >= 0
        assertFalse(pm.matchesPlatform("pkgMatchEverything-1.0.1", PlatformId.parse("anotherName", "1.2.3")));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", PlatformId.parse("server", "1.2.3")));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", PlatformId.parse("server", "9.10")));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", PlatformId.parse("server", "10.10.0")));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", PlatformId.parse("server", "10.10")));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", PlatformId.parse("server", "10.10-alpha1")));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", PlatformId.parse("server", "10.10-beta.3")));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", PlatformId.parse("server", "10.10-HF01")));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", PlatformId.parse("server", "10.10.0-HF.2")));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", PlatformId.parse("server", "10.10-PR02")));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", PlatformId.parse("server", "10.10-RC1")));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", PlatformId.parse("server", "10.10-SNAPSHOT")));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", PlatformId.parse("server", "10.10.1")));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", PlatformId.parse("server", "10.10.001")));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", PlatformId.parse("server", "10.11")));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", PlatformId.parse("server", "11.1")));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", PlatformId.parse("server", "2022-LTS")));
    }

    public void testTargetPlatformVersionRangesSingleVersion() throws PackageException {
        // GIVEN pkgSingleVersion has a targetPlaformRange defined as "10.10.1"
        // THEN it matches only the platform with version = 10.10.1
        assertFalse(pm.matchesPlatform("pkgSingleVersion-1.0.1", PlatformId.parse("anotherName", "1.2.3")));
        assertFalse(pm.matchesPlatform("pkgSingleVersion-1.0.1", PlatformId.parse("server", "1.2.3")));
        assertFalse(pm.matchesPlatform("pkgSingleVersion-1.0.1", PlatformId.parse("server", "9.10")));
        assertFalse(pm.matchesPlatform("pkgSingleVersion-1.0.1", PlatformId.parse("server", "10.10.0")));
        assertFalse(pm.matchesPlatform("pkgSingleVersion-1.0.1", PlatformId.parse("server", "10.10")));
        assertFalse(pm.matchesPlatform("pkgSingleVersion-1.0.1", PlatformId.parse("server", "10.10-alpha1")));
        assertFalse(pm.matchesPlatform("pkgSingleVersion-1.0.1", PlatformId.parse("server", "10.10-beta.3")));
        assertFalse(pm.matchesPlatform("pkgSingleVersion-1.0.1", PlatformId.parse("server", "10.10-HF01")));
        assertFalse(pm.matchesPlatform("pkgSingleVersion-1.0.1", PlatformId.parse("server", "10.10.0-HF.2")));
        assertFalse(pm.matchesPlatform("pkgSingleVersion-1.0.1", PlatformId.parse("server", "10.10-PR02")));
        assertFalse(pm.matchesPlatform("pkgSingleVersion-1.0.1", PlatformId.parse("server", "10.10-RC1")));
        assertFalse(pm.matchesPlatform("pkgSingleVersion-1.0.1", PlatformId.parse("server", "10.10-SNAPSHOT")));
        assertTrue(pm.matchesPlatform("pkgSingleVersion-1.0.1", PlatformId.parse("server", "10.10.1")));
        assertTrue(pm.matchesPlatform("pkgSingleVersion-1.0.1", PlatformId.parse("server", "10.10.001")));
        assertFalse(pm.matchesPlatform("pkgSingleVersion-1.0.1", PlatformId.parse("anotherName", "10.10.001")));
        assertFalse(pm.matchesPlatform("pkgSingleVersion-1.0.1", PlatformId.parse("server", "10.11")));
        assertFalse(pm.matchesPlatform("pkgSingleVersion-1.0.1", PlatformId.parse("server", "11.1")));
        assertFalse(pm.matchesPlatform("pkgSingleVersion-1.0.1", PlatformId.parse("server", "2022-LTS")));
    }

    public void testTargetPlatformVersionRangesInclusiveBounds() throws PackageException {
        // GIVEN pkgInclusiveBounds has a targetPlaformRange defined as "[9.10,10.10-HF02]"
        // THEN it matches all platforms with version >= 9.10 AND <= 10.10-HF02
        assertFalse(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "1.2.3")));
        assertTrue(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "9.10")));
        assertTrue(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "9.10.00000")));
        assertTrue(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "9.10-BLABLA")));
        assertTrue(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "9.10-SNAPSHOT")));
        assertTrue(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "9.10.1")));
        assertTrue(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.0")));
        assertTrue(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "10.10")));
        assertTrue(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "10.10-alpha1")));
        assertTrue(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "10.10-beta.3")));
        assertTrue(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "10.10-HF01")));
        assertTrue(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.0-HF.2")));
        assertTrue(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.0-HF0.2")));
        assertTrue(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.0-HF002")));
        assertTrue(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.0-HF02")));
        assertTrue(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.0-hf02")));
        assertFalse(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("anotherName", "10.10.0-hf02")));
        assertFalse(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.0-HF02.")));
        assertFalse(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.0-HF020")));
        assertFalse(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "10.10-PR02")));
        assertFalse(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "10.10-RC1")));
        assertFalse(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "10.10-SNAPSHOT")));
        assertFalse(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.1")));
        assertFalse(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.001")));
        assertFalse(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "10.11")));
        assertFalse(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "11.1")));
        assertFalse(pm.matchesPlatform("pkgInclusiveBounds-1.0.1", PlatformId.parse("server", "2022-LTS")));
    }

    public void testTargetPlatformVersionRangesExclusiveBounds() throws PackageException {
        // GIVEN pkgExclusiveBounds has a targetPlaformRange defined as "(9.10,10.10-HF02)"
        // THEN it matches all platforms with version > 9.10 AND < 10.10-HF02
        assertFalse(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "1.2.3")));
        assertFalse(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "9.10")));
        assertFalse(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "9.10.00000")));
        assertFalse(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("anotherName", "9.10-BLABLA")));
        assertTrue(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "9.10-BLABLA")));
        assertTrue(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "9.10-SNAPSHOT")));
        assertTrue(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "9.10.1")));
        assertTrue(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.0")));
        assertTrue(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "10.10")));
        assertTrue(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "10.10-alpha1")));
        assertTrue(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "10.10-beta.3")));
        assertTrue(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "10.10-HF01")));
        assertTrue(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.0-HF.2")));
        assertTrue(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.0-HF0.2")));
        assertTrue(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.0-HF002")));
        assertFalse(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.0-HF02")));
        assertFalse(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.0-hf02")));
        assertFalse(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.0-HF02.")));
        assertFalse(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.0-HF020")));
        assertFalse(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "10.10-PR02")));
        assertFalse(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "10.10-RC1")));
        assertFalse(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "10.10-SNAPSHOT")));
        assertFalse(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.1")));
        assertFalse(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "10.10.001")));
        assertFalse(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "10.11")));
        assertFalse(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "11.1")));
        assertFalse(pm.matchesPlatform("pkgExclusiveBounds-1.0.1", PlatformId.parse("server", "2022-LTS")));
    }

    public void testTargetPlatformVersionRangesHigherBoundInclusive() throws PackageException {
        // GIVEN pkgHigherBoundInclusive has a targetPlaformRange defined as "(,10.10]"
        // THEN it matches all platforms with version <= 10.10
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", PlatformId.parse("anotherName", "1.2.3")));
        assertTrue(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", PlatformId.parse("server", "1.2.3")));
        assertTrue(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", PlatformId.parse("server", "9.10")));
        assertTrue(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", PlatformId.parse("server", "10.10.0")));
        assertTrue(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", PlatformId.parse("server", "10.10")));
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", PlatformId.parse("server", "10.10-alpha1")));
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", PlatformId.parse("server", "10.10-beta.3")));
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", PlatformId.parse("server", "10.10-HF01")));
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", PlatformId.parse("server", "10.10.0-HF.2")));
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", PlatformId.parse("server", "10.10-PR02")));
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", PlatformId.parse("server", "10.10-RC1")));
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", PlatformId.parse("server", "10.10-SNAPSHOT")));
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", PlatformId.parse("server", "10.10.1")));
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", PlatformId.parse("server", "10.10.001")));
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", PlatformId.parse("server", "10.11")));
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", PlatformId.parse("server", "11.1")));
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", PlatformId.parse("server", "2022-LTS")));
    }

    public void testTargetPlatformVersionRangesLowerBoundExclusive() throws PackageException {
        // GIVEN pkgLowerBoundExclusive has a targetPlaformRange defined as "(10.10,)"
        // THEN it matches all platforms with version > 10.10
        assertFalse(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", PlatformId.parse("server", "1.2.3")));
        assertFalse(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", PlatformId.parse("server", "9.10")));
        assertFalse(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", PlatformId.parse("server", "10.10.0")));
        assertFalse(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", PlatformId.parse("server", "10.10")));
        assertFalse(
                pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", PlatformId.parse("anotherName", "10.10-alpha1")));
        assertTrue(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", PlatformId.parse("server", "10.10-alpha1")));
        assertTrue(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", PlatformId.parse("server", "10.10-beta.3")));
        assertTrue(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", PlatformId.parse("server", "10.10-HF01")));
        assertTrue(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", PlatformId.parse("server", "10.10.0-HF.2")));
        assertTrue(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", PlatformId.parse("server", "10.10-PR02")));
        assertTrue(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", PlatformId.parse("server", "10.10-RC1")));
        assertTrue(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", PlatformId.parse("server", "10.10-SNAPSHOT")));
        assertTrue(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", PlatformId.parse("server", "10.10.1")));
        assertTrue(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", PlatformId.parse("server", "10.10.001")));
        assertTrue(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", PlatformId.parse("server", "10.11")));
        assertTrue(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", PlatformId.parse("server", "11.1")));
        assertTrue(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", PlatformId.parse("server", "2022-LTS")));
    }

    public void testTargetPlatformVersionRangesInvalidRangeFallback() throws PackageException {
        // GIVEN pkgInvalidRange-1.0.1 has a targetPlaformRange defined as "(1,1)" (which is invalid because boundaries
        // are identical and exclusive) and has a targetPlaforms defined as [server-10.10*]
        // THEN it fallback matches all platforms with name matching server-10.10*
        assertFalse(pm.matchesPlatform("pkgInvalidRange-1.0.1", PlatformId.parse("server", "1.2.3")));
        assertFalse(pm.matchesPlatform("pkgInvalidRange-1.0.1", PlatformId.parse("server", "9.10")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", PlatformId.parse("server", "10.10.0")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", PlatformId.parse("server", "10.10")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", PlatformId.parse("server", "10.10-alpha1")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", PlatformId.parse("server", "10.10-beta.3")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", PlatformId.parse("server", "10.10-HF01")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", PlatformId.parse("server", "10.10.0-HF.2")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", PlatformId.parse("server", "10.10-PR02")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", PlatformId.parse("server", "10.10-RC1")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", PlatformId.parse("server", "10.10-SNAPSHOT")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", PlatformId.parse("server", "10.10.1")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", PlatformId.parse("server", "10.10.001")));
        assertFalse(pm.matchesPlatform("pkgInvalidRange-1.0.1", PlatformId.parse("server", "10.11")));
        assertFalse(pm.matchesPlatform("pkgInvalidRange-1.0.1", PlatformId.parse("server", "11.1")));
        assertFalse(pm.matchesPlatform("pkgInvalidRange-1.0.1", PlatformId.parse("2022-LTS", "2022-LTS")));

        // GIVEN pkgInvalidRange-1.0.2 has a targetPlaformRange defined as "" (which is invalid because blank) and has a
        // targetPlaforms defined as [server-10.10*]
        // THEN it fallback matches all platforms with name matching server-10.10*
        assertFalse(pm.matchesPlatform("pkgInvalidRange-1.0.2", PlatformId.parse("server", "1.2.3")));
        assertFalse(pm.matchesPlatform("pkgInvalidRange-1.0.2", PlatformId.parse("server", "9.10")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.2", PlatformId.parse("server", "10.10.0")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.2", PlatformId.parse("server", "10.10")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.2", PlatformId.parse("server", "10.10-alpha1")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.2", PlatformId.parse("server", "10.10-beta.3")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.2", PlatformId.parse("server", "10.10-HF01")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.2", PlatformId.parse("server", "10.10.0-HF.2")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.2", PlatformId.parse("server", "10.10-PR02")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.2", PlatformId.parse("server", "10.10-RC1")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.2", PlatformId.parse("server", "10.10-SNAPSHOT")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.2", PlatformId.parse("server", "10.10.1")));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.2", PlatformId.parse("server", "10.10.001")));
        assertFalse(pm.matchesPlatform("pkgInvalidRange-1.0.2", PlatformId.parse("server", "10.11")));
        assertFalse(pm.matchesPlatform("pkgInvalidRange-1.0.2", PlatformId.parse("server", "11.1")));
        assertFalse(pm.matchesPlatform("pkgInvalidRange-1.0.2", PlatformId.parse("2022-LTS", "2022-LTS")));

        // GIVEN pkgNoRange-1.0.1 has no targetPlaformRange defined and has a targetPlaforms defined as [server-10.10*]
        // THEN it fallback matches all platforms with name matching server-10.10*
        assertFalse(pm.matchesPlatform("pkgNoRange-1.0.1", PlatformId.parse("server", "1.2.3")));
        assertFalse(pm.matchesPlatform("pkgNoRange-1.0.1", PlatformId.parse("server", "9.10")));
        assertTrue(pm.matchesPlatform("pkgNoRange-1.0.1", PlatformId.parse("server", "10.10.0")));
        assertTrue(pm.matchesPlatform("pkgNoRange-1.0.1", PlatformId.parse("server", "10.10")));
        assertTrue(pm.matchesPlatform("pkgNoRange-1.0.1", PlatformId.parse("server", "10.10-alpha1")));
        assertTrue(pm.matchesPlatform("pkgNoRange-1.0.1", PlatformId.parse("server", "10.10-beta.3")));
        assertTrue(pm.matchesPlatform("pkgNoRange-1.0.1", PlatformId.parse("server", "10.10-HF01")));
        assertTrue(pm.matchesPlatform("pkgNoRange-1.0.1", PlatformId.parse("server", "10.10.0-HF.2")));
        assertTrue(pm.matchesPlatform("pkgNoRange-1.0.1", PlatformId.parse("server", "10.10-PR02")));
        assertTrue(pm.matchesPlatform("pkgNoRange-1.0.1", PlatformId.parse("server", "10.10-RC1")));
        assertTrue(pm.matchesPlatform("pkgNoRange-1.0.1", PlatformId.parse("server", "10.10-SNAPSHOT")));
        assertTrue(pm.matchesPlatform("pkgNoRange-1.0.1", PlatformId.parse("server", "10.10.1")));
        assertTrue(pm.matchesPlatform("pkgNoRange-1.0.1", PlatformId.parse("server", "10.10.001")));
        assertFalse(pm.matchesPlatform("pkgNoRange-1.0.1", PlatformId.parse("server", "10.11")));
        assertFalse(pm.matchesPlatform("pkgNoRange-1.0.1", PlatformId.parse("server", "11.1")));
        assertFalse(pm.matchesPlatform("pkgNoRange-1.0.1", PlatformId.parse("2022-LTS", "2022-LTS")));
    }

    public void testTargetPlatformVersionRangesInvalidNameFallback() throws PackageException {
        // GIVEN pkgInvalidName-1.0.1 has a targetPlaformName defined as " " (which is invalid because name cannot be
        // blank) and has a targetPlaforms defined as [server-10.10*]
        // THEN it fallback matches all platforms with name matching server-10.10*
        assertFalse(pm.matchesPlatform("pkgInvalidName-1.0.1", PlatformId.parse("server", "1.2.3")));
        assertFalse(pm.matchesPlatform("pkgInvalidName-1.0.1", PlatformId.parse("server", "9.10")));
        assertTrue(pm.matchesPlatform("pkgInvalidName-1.0.1", PlatformId.parse("server", "10.10.0")));
        assertTrue(pm.matchesPlatform("pkgInvalidName-1.0.1", PlatformId.parse("server", "10.10")));
        assertTrue(pm.matchesPlatform("pkgInvalidName-1.0.1", PlatformId.parse("server", "10.10-alpha1")));
        assertTrue(pm.matchesPlatform("pkgInvalidName-1.0.1", PlatformId.parse("server", "10.10-beta.3")));
        assertTrue(pm.matchesPlatform("pkgInvalidName-1.0.1", PlatformId.parse("server", "10.10-HF01")));
        assertTrue(pm.matchesPlatform("pkgInvalidName-1.0.1", PlatformId.parse("server", "10.10.0-HF.2")));
        assertTrue(pm.matchesPlatform("pkgInvalidName-1.0.1", PlatformId.parse("server", "10.10-PR02")));
        assertTrue(pm.matchesPlatform("pkgInvalidName-1.0.1", PlatformId.parse("server", "10.10-RC1")));
        assertTrue(pm.matchesPlatform("pkgInvalidName-1.0.1", PlatformId.parse("server", "10.10-SNAPSHOT")));
        assertTrue(pm.matchesPlatform("pkgInvalidName-1.0.1", PlatformId.parse("server", "10.10.1")));
        assertTrue(pm.matchesPlatform("pkgInvalidName-1.0.1", PlatformId.parse("server", "10.10.001")));
        assertFalse(pm.matchesPlatform("pkgInvalidName-1.0.1", PlatformId.parse("server", "10.11")));
        assertFalse(pm.matchesPlatform("pkgInvalidName-1.0.1", PlatformId.parse("server", "11.1")));
        assertFalse(pm.matchesPlatform("pkgInvalidName-1.0.1", PlatformId.parse("2022-LTS", "2022-LTS")));

        // GIVEN pkgNoName-1.0.1 has no targetPlaformName defined and has a targetPlaforms defined as [server-10.10*]
        // THEN it fallback matches all platforms with name matching server-10.10*
        assertFalse(pm.matchesPlatform("pkgNoName-1.0.1", PlatformId.parse("server", "1.2.3")));
        assertFalse(pm.matchesPlatform("pkgNoName-1.0.1", PlatformId.parse("server", "9.10")));
        assertTrue(pm.matchesPlatform("pkgNoName-1.0.1", PlatformId.parse("server", "10.10.0")));
        assertTrue(pm.matchesPlatform("pkgNoName-1.0.1", PlatformId.parse("server", "10.10")));
        assertTrue(pm.matchesPlatform("pkgNoName-1.0.1", PlatformId.parse("server", "10.10-alpha1")));
        assertTrue(pm.matchesPlatform("pkgNoName-1.0.1", PlatformId.parse("server", "10.10-beta.3")));
        assertTrue(pm.matchesPlatform("pkgNoName-1.0.1", PlatformId.parse("server", "10.10-HF01")));
        assertTrue(pm.matchesPlatform("pkgNoName-1.0.1", PlatformId.parse("server", "10.10.0-HF.2")));
        assertTrue(pm.matchesPlatform("pkgNoName-1.0.1", PlatformId.parse("server", "10.10-PR02")));
        assertTrue(pm.matchesPlatform("pkgNoName-1.0.1", PlatformId.parse("server", "10.10-RC1")));
        assertTrue(pm.matchesPlatform("pkgNoName-1.0.1", PlatformId.parse("server", "10.10-SNAPSHOT")));
        assertTrue(pm.matchesPlatform("pkgNoName-1.0.1", PlatformId.parse("server", "10.10.1")));
        assertTrue(pm.matchesPlatform("pkgNoName-1.0.1", PlatformId.parse("server", "10.10.001")));
        assertFalse(pm.matchesPlatform("pkgNoName-1.0.1", PlatformId.parse("server", "10.11")));
        assertFalse(pm.matchesPlatform("pkgNoName-1.0.1", PlatformId.parse("server", "11.1")));
        assertFalse(pm.matchesPlatform("pkgNoName-1.0.1", PlatformId.parse("2022-LTS", "2022-LTS")));
    }

    public void testTargetPlatformPatternMatching() throws PackageException {
        assertTrue(pm.matchesPlatform("pkgPattern-1.0.1", PlatformId.parse("lts", "2021.0.0")));
        assertTrue(pm.matchesPlatform("pkgPattern-1.0.2", PlatformId.parse("lts", "2021.0.0")));
        assertTrue(pm.matchesPlatform("pkgPattern-1.0.3", PlatformId.parse("lts", "2021.0.0")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.4", PlatformId.parse("lts", "2021.0.0")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.5", PlatformId.parse("lts", "2021.0.0")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.6", PlatformId.parse("lts", "2021.0.0")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.7", PlatformId.parse("lts", "2021.0.0")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.8", PlatformId.parse("lts", "2021.0.0")));
        assertTrue(pm.matchesPlatform("pkgPattern-1.0.9", PlatformId.parse("lts", "2021.0.0")));
        assertTrue(pm.matchesPlatform("pkgPattern-1.0.10", PlatformId.parse("lts", "2021.0.0")));
        assertTrue(pm.matchesPlatform("pkgPattern-1.0.11", PlatformId.parse("lts", "2021.0.0")));

        assertFalse(pm.matchesPlatform("pkgPattern-1.0.1", PlatformId.parse("lts", "2021.0.1")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.2", PlatformId.parse("lts", "2021.0.1")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.3", PlatformId.parse("lts", "2021.0.1")));
        assertTrue(pm.matchesPlatform("pkgPattern-1.0.4", PlatformId.parse("lts", "2021.0.1")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.5", PlatformId.parse("lts", "2021.0.1")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.6", PlatformId.parse("lts", "2021.0.1")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.7", PlatformId.parse("lts", "2021.0.1")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.8", PlatformId.parse("lts", "2021.0.1")));
        assertTrue(pm.matchesPlatform("pkgPattern-1.0.9", PlatformId.parse("lts", "2021.0.1")));
        assertTrue(pm.matchesPlatform("pkgPattern-1.0.10", PlatformId.parse("lts", "2021.0.1")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.11", PlatformId.parse("lts", "2021.0.1")));

        assertFalse(pm.matchesPlatform("pkgPattern-1.0.1", PlatformId.parse("lts", "2021.1.0")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.2", PlatformId.parse("lts", "2021.1.0")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.3", PlatformId.parse("lts", "2021.1.0")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.4", PlatformId.parse("lts", "2021.1.0")));
        assertTrue(pm.matchesPlatform("pkgPattern-1.0.5", PlatformId.parse("lts", "2021.1.0")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.6", PlatformId.parse("lts", "2021.1.0")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.7", PlatformId.parse("lts", "2021.1.0")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.8", PlatformId.parse("lts", "2021.1.0")));
        assertTrue(pm.matchesPlatform("pkgPattern-1.0.9", PlatformId.parse("lts", "2021.1.0")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.10", PlatformId.parse("lts", "2021.1.0")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.11", PlatformId.parse("lts", "2021.1.0")));

        assertFalse(pm.matchesPlatform("pkgPattern-1.0.1", PlatformId.parse("lts", "2021-HF01")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.2", PlatformId.parse("lts", "2021-HF01")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.3", PlatformId.parse("lts", "2021-HF01")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.4", PlatformId.parse("lts", "2021-HF01")));
        assertFalse(pm.matchesPlatform("pkgPattern-1.0.5", PlatformId.parse("lts", "2021-HF01")));
        assertTrue(pm.matchesPlatform("pkgPattern-1.0.6", PlatformId.parse("lts", "2021-HF01")));
        assertTrue(pm.matchesPlatform("pkgPattern-1.0.7", PlatformId.parse("lts", "2021-HF01")));
        assertTrue(pm.matchesPlatform("pkgPattern-1.0.8", PlatformId.parse("lts", "2021-HF01")));
        assertTrue(pm.matchesPlatform("pkgPattern-1.0.9", PlatformId.parse("lts", "2021-HF01")));
        assertTrue(pm.matchesPlatform("pkgPattern-1.0.10", PlatformId.parse("lts", "2021-HF01")));
        assertTrue(pm.matchesPlatform("pkgPattern-1.0.11", PlatformId.parse("lts", "2021-HF01")));
    }

}
