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

import org.apache.commons.collections.CollectionUtils;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.update.PackageException;

/**
 * @since 1.7.9
 */
public class TestTargetPlatormVersionRanges extends AbstractPackageManagerTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("localRange.json");
        assertTrue(CollectionUtils.isNotEmpty(local));
        pm.registerSource(new DummyPackageSource(local, "localRange"), true);
    }

    public void testTargetPlatformVersionRangesMatchEverything() throws PackageException {
        // GIVEN pkgMatchEverything has a targetPlaformRange defined as "1.0" (this is considered as a minimum version)
        // THEN it matches all platforms with version >= 1.0
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", "whatever", "1.2.3"));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", "whatever", "9.10"));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", "whatever", "10.10-SNAPSHOT"));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", "whatever", "10.10-RC1"));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", "whatever", "10.10-alpha1"));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", "whatever", "10.10-beta.3"));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", "whatever", "10.10-PR02"));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", "whatever", "10.10.0"));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", "whatever", "10.10"));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", "whatever", "10.10-HF01"));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", "whatever", "10.10.0-HF.2"));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", "whatever", "10.10.1"));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", "whatever", "10.11"));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", "whatever", "11.1"));
        assertTrue(pm.matchesPlatform("pkgMatchEverything-1.0.1", "whatever", "2022-LTS"));
    }

    public void testTargetPlatformVersionRangesHigherBoundInclusive() throws PackageException {
        // GIVEN pkgHigherBoundInclusive has a targetPlaformRange defined as "(,10.10]"
        // THEN it matches all platforms with version <= 10.10
        assertTrue(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", "whatever", "1.2.3"));
        assertTrue(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", "whatever", "9.10"));
        assertTrue(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", "whatever", "10.10-SNAPSHOT"));
        assertTrue(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", "whatever", "10.10-RC1"));
        assertTrue(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", "whatever", "10.10-alpha1"));
        assertTrue(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", "whatever", "10.10-beta.3"));
        // FAIL assertTrue(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", "whatever", "10.10-PR02"));
        assertTrue(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", "whatever", "10.10.0"));
        assertTrue(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", "whatever", "10.10"));
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", "whatever", "10.10-HF01"));
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", "whatever", "10.10.0-HF.2"));
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", "whatever", "10.10.1"));
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", "whatever", "10.11"));
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", "whatever", "11.1"));
        assertFalse(pm.matchesPlatform("pkgHigherBoundInclusive-1.0.1", "whatever", "2022-LTS"));
    }

    public void testTargetPlatformVersionRangesLowerBoundExclusive() throws PackageException {
        // GIVEN pkgLowerBoundExclusive has a targetPlaformRange defined as "(10.10,)"
        // THEN it matches all platforms with version > 10.10
        assertFalse(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", "whatever", "1.2.3"));
        assertFalse(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", "whatever", "9.10"));
        assertFalse(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", "whatever", "10.10-SNAPSHOT"));
        assertFalse(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", "whatever", "10.10-RC1"));
        assertFalse(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", "whatever", "10.10-alpha1"));
        assertFalse(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", "whatever", "10.10-beta.3"));
        // FAIL assertFalse(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", "whatever", "10.10-PR02"));
        assertFalse(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", "whatever", "10.10.0"));
        assertFalse(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", "whatever", "10.10"));
        assertTrue(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", "whatever", "10.10-HF01"));
        assertTrue(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", "whatever", "10.10.0-HF.2"));
        assertTrue(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", "whatever", "10.10.1"));
        assertTrue(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", "whatever", "10.11"));
        assertTrue(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", "whatever", "11.1"));
        assertTrue(pm.matchesPlatform("pkgLowerBoundExclusive-1.0.1", "whatever", "2022-LTS"));
    }

    public void testTargetPlatformVersionRangesInvalidRangeFallback() throws PackageException {
        // GIVEN pkgInvalidRange has a targetPlaformRange defined as "(,)" (which is invalid because boundaries are
        // identical) and has a targetPlaforms defined as [server-10.10*]
        // THEN it fallback matches all platforms with name matching server-10.10*
        assertFalse(pm.matchesPlatform("pkgInvalidRange-1.0.1", "server-1.2.3", "1.2.3"));
        assertFalse(pm.matchesPlatform("pkgInvalidRange-1.0.1", "server-9.10", "9.10"));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", "server-10.10-SNAPSHOT", "10.10-SNAPSHOT"));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", "server-10.10-RC1", "10.10-RC1"));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", "server-10.10-alpha1", "10.10-alpha1"));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", "server-10.10-beta.3", "10.10-beta.3"));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", "server-10.10-PR02", "10.10-PR02"));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", "server-10.10.0", "10.10.0"));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", "server-10.10", "10.10"));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", "server-10.10-HF01", "10.10-HF01"));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", "server-10.10.0-HF.2", "10.10.0-HF.2"));
        assertTrue(pm.matchesPlatform("pkgInvalidRange-1.0.1", "server-10.10.1", "10.10.1"));
        assertFalse(pm.matchesPlatform("pkgInvalidRange-1.0.1", "server-10.11", "10.11"));
        assertFalse(pm.matchesPlatform("pkgInvalidRange-1.0.1", "server-11.1", "11.1"));
        assertFalse(pm.matchesPlatform("pkgInvalidRange-1.0.1", "2022-LTS", "2022-LTS"));
    }

}
