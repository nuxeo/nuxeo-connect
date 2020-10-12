/*
 * (C) Copyright 2012-2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Julien Carsique
 *     Yannis JULIENNE
 */

package org.nuxeo.connect.packages.dependencies;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.pm.tests.AbstractPackageManagerTestCase;
import org.nuxeo.connect.pm.tests.DummyPackageSource;

/**
 * @since 1.4.2 Hotfix install test case
 */
public class P2CUDFDependencyResolverTest2 extends AbstractPackageManagerTestCase {

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("local6.json");
        List<DownloadablePackage> remote = getDownloads("remote4.json");
        pm.registerSource(new DummyPackageSource(local, "dummyLocal"), true);
        pm.registerSource(new DummyPackageSource(remote, "dummyRemote"), false);
    }

    // Before: [DM-5.4.0.1-HF02-1.0.0, DM-5.4.0.1-HF03-1.0.0]
    // After: [DM-5.4.0.1-HF01-1.0.0, DM-5.4.0.1-HF02-1.0.0, DM-5.4.0.1-HF03-1.0.0, DM-5.4.0.1-HF04-1.0.0,
    // DM-5.4.0.1-HF05-1.0.0, DM-5.4.0.1-HF06-1.0.0, DM-5.4.0.1-HF07-1.1.0]
    @Test
    public void testResolve() throws Exception {
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
}
