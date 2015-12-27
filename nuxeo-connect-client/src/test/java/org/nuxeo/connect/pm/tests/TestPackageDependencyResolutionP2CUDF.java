/*
 * (C) Copyright 2012-2014 Nuxeo SA (http://nuxeo.com/) and others.
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
 * Since Blocker1 and Blocker2 are installed but Z1 and Z2 are missing, either Z1 and Z2 must be installed or Blocker1
 * and Blocker2 must be uninstalled. That case wasn't detected by legacy resolver. That behavior difference requires to
 * override a few tests.
 *
 * @since 1.4
 */
public class TestPackageDependencyResolutionP2CUDF extends AbstractPackageManagerTestCase {

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

        pm.registerSource(new DummyPackageSource(local, true, "localDummy"), true);
        pm.registerSource(new DummyPackageSource(remote, false, "remoteDummy"), false);
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

    // Before: [AA-1.0.0, BB-1.0.0, Blocker-1.0.0, Blocker2-1.0.0, EE-1.0.0, NXBT-654.1-1.0.0, NXBT-654.2-1.0.0,
    // NXBT-654.3-1.0.0]
    // {"version":"1.0.0","name":"E","dependencies":["EE:1.1.0:2.0.0","NXBT-654.1","NXBT-654.2","NXBT-654.3"],"state":0}
    // After: [AA-1.1.0, BB-1.1.0, Blocker-1.0.0, Blocker2-1.1.0, E-1.0.0, EE-1.1.0, NXBT-654.1-1.0.1-SNAPSHOT,
    // NXBT-654.2-1.0.1-SNAPSHOT, NXBT-654.3-1.0.2-SNAPSHOT, Z1-2.0.0, Z2-2.0.0]
    public void testSimpleUpgrade() throws Exception {
        depResolution = pm.resolveDependencies("E-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(3, depResolution.getLocalPackagesToInstall().size());
        assertEquals(7, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(7, depResolution.getNewPackagesToDownload().size());
    }

    // Before: [AA-1.0.0, BB-1.0.0, Blocker-1.0.0, Blocker2-1.0.0, EE-1.0.0, NXBT-654.1-1.0.0, NXBT-654.2-1.0.0,
    // NXBT-654.3-1.0.0]
    // {"version":"1.0.0","name":"F","targetPlatforms":["5.3.0","5.3.1"],"dependencies":["EE:1.1.0:2.0.0","C:1.0.0:1.1.0","NXBT-654.1","NXBT-654.2","NXBT-654.3"],"state":0,"type":"addon"}
    // After: [AA-1.1.0, B-1.1.0, BB-1.1.0, Blocker-1.0.0, Blocker2-1.1.0, C-1.0.0, D-1.1.0, EE-1.1.0, F-1.0.0,
    // NXBT-654.1-1.0.1-SNAPSHOT, NXBT-654.2-1.0.1-SNAPSHOT, NXBT-654.3-1.0.2-SNAPSHOT, Z1-2.0.0, Z2-2.0.0]
    public void testDoubleDownload() throws Exception {
        depResolution = pm.resolveDependencies("F-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(3, depResolution.getLocalPackagesToInstall().size());
        assertEquals(7, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(10, depResolution.getNewPackagesToDownload().size());
    }

    // Before: [AA-1.0.0, BB-1.0.0, Blocker-1.0.0, Blocker2-1.0.0, EE-1.0.0, NXBT-654.1-1.0.0, NXBT-654.2-1.0.0,
    // NXBT-654.3-1.0.0]
    // {"version":"1.0.0","name":"O","targetPlatforms":["5.3.0","5.3.1"],"dependencies":["P:1.0.0:1.1.0","Q:1.0.0:1.1.1","NXBT-654.1","NXBT-654.2","NXBT-654.3"],"state":0,"type":"addon"}
    // After: [AA-1.1.0, B-1.1.0, BB-1.1.0, Blocker-1.0.0, Blocker2-1.1.0, EE-1.1.0, NXBT-654.1-1.0.1-SNAPSHOT,
    // NXBT-654.2-1.0.1-SNAPSHOT, NXBT-654.3-1.0.2-SNAPSHOT, O-1.0.0, P-1.0.0, Q-1.0.0, R-1.2.0, Z1-2.0.0, Z2-2.0.0]
    public void test3LevelsDeps() throws Exception {
        depResolution = pm.resolveDependencies("O-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(3, depResolution.getLocalPackagesToInstall().size());
        assertEquals(7, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(11, depResolution.getNewPackagesToDownload().size());
    }

    /**
     * @since 1.4.18
     */
    // Before: [AA-1.0.0, BB-1.0.0, Blocker-1.0.0, Blocker2-1.0.0, EE-1.0.0, NXBT-654.1-1.0.0, NXBT-654.2-1.0.0,
    // NXBT-654.3-1.0.0]
    // {"version":"1.0","name":"NXBT-872","targetPlatforms":["6.0"],"dependencies":["NXBT-872-S1","NXBT-872-S2:2.0"],"state":2,"type":"addon"}
    // After: [AA-1.0.0, BB-1.0.0, Blocker-1.0.0, Blocker2-1.0.0, EE-1.0.0, NXBT-654.1-1.0.1-SNAPSHOT,
    // NXBT-654.2-1.0.1-SNAPSHOT, NXBT-654.3-1.0.2-SNAPSHOT, NXBT-872-1.0.0, NXBT-872-S1-1.2.0-SNAPSHOT,
    // NXBT-872-S2-2.0.0]
    public void testStudioDeps() throws Exception {
        depResolution = pm.resolveDependencies("NXBT-872", "6.0");
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(4, depResolution.getLocalPackagesToInstall().size());
        assertEquals(3, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(5, depResolution.getLocalUnchangedPackages().size());
        assertEquals(2, depResolution.getNewPackagesToDownload().size());
    }

    // Before: [AA-1.0.0, BB-1.0.0, Blocker-1.0.0, Blocker2-1.0.0, EE-1.0.0, NXBT-654.1-1.0.0, NXBT-654.2-1.0.0,
    // NXBT-654.3-1.0.0]
    // {"version":"1.0.0","name":"CC","targetPlatforms":["5.3.0","5.3.1"],"dependencies":["AA:1.1.0:1.2.0","NXBT-654.1","NXBT-654.2","NXBT-654.3"],"state":0,"type":"addon"}
    // After: [AA-1.1.0, BB-1.1.0, Blocker-1.0.0, Blocker2-1.1.0, CC-1.0.0, EE-1.1.0, NXBT-654.1-1.0.1-SNAPSHOT,
    // NXBT-654.2-1.0.1-SNAPSHOT, NXBT-654.3-1.0.2-SNAPSHOT, Z1-2.0.0, Z2-2.0.0]
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
        assertEquals(new Version("2.0.0"), depResolution.getNewPackagesToDownload().get("PF2"));

        depResolution = pm.resolveDependencies("PF1-1.0.0", "5.3.0");
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(3, depResolution.getLocalPackagesToInstall().size());
        assertEquals(7, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(8, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
        assertEquals(new Version("1.0.0"), depResolution.getNewPackagesToDownload().get("PF2"));

        depResolution = pm.resolveDependencies("PF1-1.0.0", "5.3.2");
        log.info(depResolution.toString());
        assertTrue(depResolution.isFailed());
    }

}
