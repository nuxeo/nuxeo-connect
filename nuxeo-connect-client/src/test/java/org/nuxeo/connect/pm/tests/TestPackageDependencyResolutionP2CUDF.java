/*
 * (C) Copyright 2012-2013 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *
 */

package org.nuxeo.connect.pm.tests;

import java.util.List;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.dependencies.CUDFHelper;
import org.nuxeo.connect.packages.dependencies.DependencyResolution;
import org.nuxeo.connect.update.Version;

/**
 * Since Blocker1 and Blocker2 are installed but Z1 and Z2 are missing, either
 * Z1 and Z2 must be installed or Blocker1 and Blocker2 must be uninstalled.
 * That case wasn't detected by legacy resolver. That behavior difference
 * requires to override a few tests.
 *
 * @since 1.4
 *
 */
public class TestPackageDependencyResolutionP2CUDF extends
        AbstractPackageManagerTestCase {

    protected DependencyResolution depResolution;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("localdep1.json", true);
        List<DownloadablePackage> remote = getDownloads("remotedep1.json");

        assertNotNull(local);
        assertTrue(local.size() > 0);
        assertNotNull(remote);
        assertTrue(remote.size() > 0);

        pm.registerSource(new DummyPackageSource(local, true, "localDummy"),
                true);
        pm.registerSource(new DummyPackageSource(remote, false, "remoteDummy"),
                false);
        CUDFHelper.defaultAllowSNAPSHOT = true;
    }

    @Override
    protected void tearDown() throws Exception {
        CUDFHelper.defaultAllowSNAPSHOT = false;
    }

    public void testSimpleDeps() throws Exception {
        depResolution = pm.resolveDependencies("C-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(3, depResolution.getLocalPackagesToInstall().size());
        assertEquals(7, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(9, depResolution.getNewPackagesToDownload().size());
    }

    public void testSimpleUpgrade() throws Exception {
        depResolution = pm.resolveDependencies("E-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(3, depResolution.getLocalPackagesToInstall().size());
        assertEquals(7, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(7, depResolution.getNewPackagesToDownload().size());
    }

    public void testDoubleDownload() throws Exception {
        depResolution = pm.resolveDependencies("F-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(3, depResolution.getLocalPackagesToInstall().size());
        assertEquals(7, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(10, depResolution.getNewPackagesToDownload().size());
    }

    public void test3LevelsDeps() throws Exception {
        depResolution = pm.resolveDependencies("O-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(3, depResolution.getLocalPackagesToInstall().size());
        assertEquals(7, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(11, depResolution.getNewPackagesToDownload().size());
    }

    public void testDoubleUpgrade() throws Exception {
        depResolution = pm.resolveDependencies("CC-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(3, depResolution.getLocalPackagesToInstall().size());
        assertEquals(7, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(7, depResolution.getNewPackagesToDownload().size());
    }

    public void testForceRemove() throws Exception {
        depResolution = pm.resolveDependencies("X1-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(3, depResolution.getLocalPackagesToInstall().size());
        assertEquals(7, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(8, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
    }

    public void testForceUpgradeOverRemove() throws Exception {
        depResolution = pm.resolveDependencies("X2-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(3, depResolution.getLocalPackagesToInstall().size());
        assertEquals(7, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(8, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
    }

    public void testPlatformFiltering() throws Exception {
        // test that Platform Filtering changes the resolution result
        depResolution = pm.resolveDependencies("PF1-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(3, depResolution.getLocalPackagesToInstall().size());
        assertEquals(7, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(8, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());

        depResolution = pm.resolveDependencies("PF1-1.0.0", "5.3.1");
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(3, depResolution.getLocalPackagesToInstall().size());
        assertEquals(7, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(8, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
        assertEquals(new Version("2.0.0"),
                depResolution.getNewPackagesToDownload().get("PF2"));

        depResolution = pm.resolveDependencies("PF1-1.0.0", "5.3.0");
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(3, depResolution.getLocalPackagesToInstall().size());
        assertEquals(7, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(8, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
        assertEquals(new Version("1.0.0"),
                depResolution.getNewPackagesToDownload().get("PF2"));

        depResolution = pm.resolveDependencies("PF1-1.0.0", "5.3.2");
        log.info(depResolution.toString());
        assertTrue(depResolution.isFailed());
    }

}
