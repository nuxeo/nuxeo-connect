/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.PackageManager;
import org.nuxeo.connect.pm.tests.AbstractPackageManagerTestCase;
import org.nuxeo.connect.pm.tests.DummyPackageSource;

/**
 * @since 1.4.2
 *        Hotfix install test case
 */
public class P2CUDFDependencyResolverTest2 extends
        AbstractPackageManagerTestCase {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("local6.json");
        List<DownloadablePackage> remote = getDownloads("remote4.json");
        DummyPackageSource source = new DummyPackageSource(local, true);
        pm.registerSource(source, true);
        pm.registerSource(new DummyPackageSource(remote, false), false);
        pm.setResolver(PackageManager.P2CUDF_DEPENDENCY_RESOLVER);
    }

    @Test
    public void testResolve() throws Exception {
        DependencyResolution resolution = pm.resolveDependencies(
                "DM-5.4.0.1-HF07", null);
        assertFalse(resolution.toString(), resolution.isFailed());

        log.info(resolution.toString());
        log.info("Packages that need to be removed from your instance: resolution.getRemovePackageIds()\n"
                + resolution.getRemovePackageIds() + "\n");
        assertEquals("There must be no package to remove", 0,
                resolution.getRemovePackageIds().size());
        log.info("Already installed packages that need to be upgraded: resolution.getUpgradePackageIds()\n"
                + resolution.getUpgradePackageIds() + "\n");
        assertEquals("There must be one already installed package to upgrade",
                1, resolution.getUpgradePackageIds().size());
        assertEquals("DM-5.4.0.1-HF02-1.0.0",
                resolution.getUpgradePackageIds().get(0));
        log.info("Already downloaded packages that need to be installed: resolution.getLocalToInstallIds()\n"
                + resolution.getLocalToInstallIds() + "\n");
        assertEquals("There must be one already downloaded package to install",
                1, resolution.getLocalToInstallIds().size());
        assertEquals("DM-5.4.0.1-HF01-1.0.0",
                resolution.getLocalToInstallIds().get(0));
        log.info("New packages that need to be downloaded and installed: resolution.getDownloadPackageIds()\n"
                + resolution.getDownloadPackageIds() + "\n");
        assertEquals("There must be five packages to download and install", 5,
                resolution.getDownloadPackageIds().size());
        assertEquals(
                Arrays.asList(new String[] { "DM-5.4.0.1-HF02-1.0.7",
                        "DM-5.4.0.1-HF04-1.0.0", "DM-5.4.0.1-HF06-1.0.0",
                        "DM-5.4.0.1-HF05-1.0.0", "DM-5.4.0.1-HF07-1.1.0", }),
                resolution.getDownloadPackageIds());
        log.info("Dependencies that are already installed on your instance and won't be changed: resolution.getUnchangedPackageIds()\n"
                + resolution.getUnchangedPackageIds() + "\n");
        assertEquals("There must be one unchanged package", 1,
                resolution.getUnchangedPackageIds().size());
        assertEquals("DM-5.4.0.1-HF03-1.0.0",
                resolution.getUnchangedPackageIds().get(0));
    }
}
