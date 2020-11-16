/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Julien Carsique
 *
 */

package org.nuxeo.connect.packages.dependencies;

//import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.nuxeo.connect.platform.PlatformId;
import org.nuxeo.connect.pm.tests.AbstractPackageManagerTestCase;
import org.nuxeo.connect.pm.tests.DummyPackageSource;

/**
 * @since 1.4
 */
public class P2CUDFDependencyResolverTest extends AbstractPackageManagerTestCase {

    @Test
    public void testBasicResolution() throws Exception {
        pm.registerSource(new DummyPackageSource(getDownloads("local3.json"), "dummyLocal"), true);
        pm.registerSource(new DummyPackageSource(getDownloads("remote3.json"), "dummyRemote"), false);

        DependencyResolution resolution = pm.resolveDependencies(Arrays.asList("nuxeo-dm-5.5.0"), null, null,
                PlatformId.parse("old", "5.5.0"));
        assertFalse(resolution.toString(), resolution.isFailed());
        assertEquals(2, resolution.getRemovePackageIds().size());
        assertEquals(Arrays.asList(new String[] { "nuxeo-cmf-5.5.0", "nuxeo-content-browser-1.1.0-cmf" }),
                resolution.getRemovePackageIds());
        assertEquals(2, resolution.getOrderedPackageIdsToInstall().size());
        assertEquals(Arrays.asList(new String[] { "nuxeo-content-browser-1.1.0", "nuxeo-dm-5.5.0" }),
                resolution.getOrderedPackageIdsToInstall());
    }

    // Before: [DM-5.4.0.1-HF02-1.0.0, DM-5.4.0.1-HF03-1.0.0]
    // After: [DM-5.4.0.1-HF01-1.0.0, DM-5.4.0.1-HF02-1.0.0, DM-5.4.0.1-HF03-1.0.0, DM-5.4.0.1-HF04-1.0.0,
    // DM-5.4.0.1-HF05-1.0.0, DM-5.4.0.1-HF06-1.0.0, DM-5.4.0.1-HF07-1.1.0]
    @Test
    public void testHotfixResolution() throws Exception {
        pm.registerSource(new DummyPackageSource(getDownloads("local6.json"), "dummyLocal"), true);
        pm.registerSource(new DummyPackageSource(getDownloads("remote4.json"), "dummyRemote"), false);

        DependencyResolution resolution = pm.resolveDependencies(Arrays.asList("DM-5.4.0.1-HF07"), null, null, null);
        assertFalse(resolution.toString(), resolution.isFailed());

        log.info(resolution.toString());
        log.info("Packages that need to be removed from your instance: resolution.getRemovePackageIds()\n"
                + resolution.getRemovePackageIds() + "\n");
        assertEquals("There must be no package to remove", 0, resolution.getRemovePackageIds().size());
        log.info("Already installed packages that need to be upgraded: resolution.getUpgradePackageIds()\n"
                + resolution.getUpgradePackageIds() + "\n");
        assertEquals("There must be no already installed package to upgrade", 0,
                resolution.getUpgradePackageIds().size());
        log.info("Already downloaded packages that need to be installed: resolution.getLocalToInstallIds()\n"
                + resolution.getLocalToInstallIds() + "\n");
        assertEquals("There must be one already downloaded package to install", 1,
                resolution.getLocalToInstallIds().size());
        assertEquals("DM-5.4.0.1-HF01-1.0.0", resolution.getLocalToInstallIds().get(0));
        log.info("New packages that need to be downloaded and installed: resolution.getDownloadPackageIds()\n"
                + resolution.getDownloadPackageIds() + "\n");
        assertEquals("There must be five packages to download and install", 4,
                resolution.getDownloadPackageIds().size());
        assertTrue(resolution.getDownloadPackageIds()
                             .containsAll(Arrays.asList(new String[] { "DM-5.4.0.1-HF04-1.0.0", "DM-5.4.0.1-HF06-1.0.0",
                                     "DM-5.4.0.1-HF05-1.0.0", "DM-5.4.0.1-HF07-1.1.0", })));
        log.info(
                "Dependencies that are already installed on your instance and won't be changed: resolution.getUnchangedPackageIds()\n"
                        + resolution.getUnchangedPackageIds() + "\n");
        assertEquals("There must be two unchanged package", 2, resolution.getUnchangedPackageIds().size());
        assertTrue(resolution.getUnchangedPackageIds()
                             .containsAll(
                                     Arrays.asList(new String[] { "DM-5.4.0.1-HF02-1.0.0", "DM-5.4.0.1-HF03-1.0.0" })));
    }

    @Test
    public void testSnapshotDependencyResolution() throws Exception {
        pm.registerSource(new DummyPackageSource(getDownloads("remote5.json"), "dummyRemote"), false);

        /* With snapshots not allowed */
        DependencyResolution resolution = pm.resolveDependencies(Arrays.asList("U"), null, null, null, false);
        assertTrue(resolution.toString(), resolution.isFailed());
        assertEquals(
                "\nFailed to resolve dependencies: Couldn't order [U-1.0.1] missing [V:1.0.2-SNAPSHOT] (consider using --relax true or --snapshot).",
                resolution.toString());

        resolution = pm.resolveDependencies(Arrays.asList("U-1.0.0"), null, null, null, false);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds().containsAll(Arrays.asList("U-1.0.0", "V-1.0.1")));

        resolution = pm.resolveDependencies(Arrays.asList("U-1.0.1-SNAPSHOT"), null, null, null, false);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds().containsAll(Arrays.asList("U-1.0.1-SNAPSHOT", "V-1.0.1")));

        resolution = pm.resolveDependencies(Arrays.asList("U-1.0.1"), null, null, null, false);
        assertTrue(resolution.toString(), resolution.isFailed());
        assertEquals(
                "\nFailed to resolve dependencies: Couldn't order [U-1.0.1] missing [V:1.0.2-SNAPSHOT] (consider using --relax true or --snapshot).",
                resolution.toString());

        resolution = pm.resolveDependencies(Arrays.asList("U-1.0.2-SNAPSHOT"), null, null, null, false);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds().containsAll(Arrays.asList("U-1.0.2-SNAPSHOT", "V-1.0.1")));

        resolution = pm.resolveDependencies(Arrays.asList("UU"), null, null, null, false);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds().containsAll(Arrays.asList("UU-1.0.0", "VV-1.0.0")));

        resolution = pm.resolveDependencies(Arrays.asList("UU-1.0.0"), null, null, null, false);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds().containsAll(Arrays.asList("UU-1.0.0", "VV-1.0.0")));

        resolution = pm.resolveDependencies(Arrays.asList("UU-1.0.1-SNAPSHOT"), null, null, null, false);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds().containsAll(Arrays.asList("UU-1.0.1-SNAPSHOT", "VV-1.0.0")));

        resolution = pm.resolveDependencies(Arrays.asList("UUU"), null, null, null, false);
        assertTrue(resolution.toString(), resolution.isFailed());
        assertEquals(
                "\nFailed to resolve dependencies: Couldn't order [UUU-1.0.1] missing [VVV:1.0.2-SNAPSHOT:1.0.2-SNAPSHOT] (consider using --relax true or --snapshot).",
                resolution.toString());

        resolution = pm.resolveDependencies(Arrays.asList("UUU-1.0.0"), null, null, null, false);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds().containsAll(Arrays.asList("UUU-1.0.0", "VVV-1.0.0")));

        resolution = pm.resolveDependencies(Arrays.asList("UUU-1.0.1-SNAPSHOT"), null, null, null, false);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds()
                             .containsAll(Arrays.asList("UUU-1.0.1-SNAPSHOT", "VVV-1.0.1-SNAPSHOT")));

        resolution = pm.resolveDependencies(Arrays.asList("UUU-1.0.1"), null, null, null, false);
        assertTrue(resolution.toString(), resolution.isFailed());
        assertEquals(
                "\nFailed to resolve dependencies: Couldn't order [UUU-1.0.1] missing [VVV:1.0.2-SNAPSHOT:1.0.2-SNAPSHOT] (consider using --relax true or --snapshot).",
                resolution.toString());

        resolution = pm.resolveDependencies(Arrays.asList("UUU-1.0.2-SNAPSHOT"), null, null, null, false);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds().containsAll(Arrays.asList("UUU-1.0.2-SNAPSHOT", "VVV-1.0.1")));
    }

    @Test
    public void testSnapshotDependencyResolutionWithSnapshotsAllowed() throws Exception {
        pm.registerSource(new DummyPackageSource(getDownloads("remote5.json"), "dummyRemote"), false);

        /* With snapshots allowed */
        DependencyResolution resolution = pm.resolveDependencies(Arrays.asList("U"), null, null, null, true);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(
                resolution.getDownloadPackageIds().containsAll(Arrays.asList("U-1.0.2-SNAPSHOT", "V-1.0.2-SNAPSHOT")));

        resolution = pm.resolveDependencies(Arrays.asList("U-1.0.0"), null, null, null, true);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds().containsAll(Arrays.asList("U-1.0.0", "V-1.0.2-SNAPSHOT")));

        resolution = pm.resolveDependencies(Arrays.asList("U-1.0.1-SNAPSHOT"), null, null, null, true);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(
                resolution.getDownloadPackageIds().containsAll(Arrays.asList("U-1.0.1-SNAPSHOT", "V-1.0.2-SNAPSHOT")));

        resolution = pm.resolveDependencies(Arrays.asList("U-1.0.1"), null, null, null, true);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds().containsAll(Arrays.asList("U-1.0.1", "V-1.0.2-SNAPSHOT")));

        resolution = pm.resolveDependencies(Arrays.asList("U-1.0.2-SNAPSHOT"), null, null, null, true);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(
                resolution.getDownloadPackageIds().containsAll(Arrays.asList("U-1.0.2-SNAPSHOT", "V-1.0.2-SNAPSHOT")));

        resolution = pm.resolveDependencies(Arrays.asList("UU"), null, null, null, true);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds()
                             .containsAll(Arrays.asList("UU-1.0.1-SNAPSHOT", "VV-1.0.1-SNAPSHOT")));

        resolution = pm.resolveDependencies(Arrays.asList("UU-1.0.0"), null, null, null, true);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds().containsAll(Arrays.asList("UU-1.0.0", "VV-1.0.1-SNAPSHOT")));

        resolution = pm.resolveDependencies(Arrays.asList("UU-1.0.1-SNAPSHOT"), null, null, null, true);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds()
                             .containsAll(Arrays.asList("UU-1.0.1-SNAPSHOT", "VV-1.0.1-SNAPSHOT")));

        resolution = pm.resolveDependencies(Arrays.asList("UUU"), null, null, null, true);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds().containsAll(Arrays.asList("UUU-1.0.2-SNAPSHOT", "VVV-1.0.1")));

        resolution = pm.resolveDependencies(Arrays.asList("UUU-1.0.0"), null, null, null, true);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds().containsAll(Arrays.asList("UUU-1.0.0", "VVV-1.0.0")));

        resolution = pm.resolveDependencies(Arrays.asList("UUU-1.0.1-SNAPSHOT"), null, null, null, true);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds()
                             .containsAll(Arrays.asList("UUU-1.0.1-SNAPSHOT", "VVV-1.0.1-SNAPSHOT")));

        resolution = pm.resolveDependencies(Arrays.asList("UUU-1.0.1"), null, null, null, true);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds().containsAll(Arrays.asList("UUU-1.0.1", "VVV-1.0.2-SNAPSHOT")));

        resolution = pm.resolveDependencies(Arrays.asList("UUU-1.0.2-SNAPSHOT"), null, null, null, true);
        assertFalse(resolution.toString(), resolution.isFailed());
        assertTrue(resolution.getDownloadPackageIds().containsAll(Arrays.asList("UUU-1.0.2-SNAPSHOT", "VVV-1.0.1")));
    }

}
