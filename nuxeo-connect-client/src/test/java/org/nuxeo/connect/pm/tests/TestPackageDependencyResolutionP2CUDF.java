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

package org.nuxeo.connect.pm.tests;

import org.nuxeo.connect.packages.PackageManager;
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
        TestPackageDependencyResolution {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        pm.setResolver(PackageManager.P2CUDF_DEPENDENCY_RESOLVER);
    }

    @Override
    public void testSimpleDeps() throws Exception {
        depResolution = pm.resolveDependencies("C-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(4, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(5, depResolution.getNewPackagesToDownload().size());
    }

    @Override
    public void testSimpleUpgrade() throws Exception {
        depResolution = pm.resolveDependencies("E-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(4, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(3, depResolution.getNewPackagesToDownload().size());
    }

    @Override
    public void testDoubleDownload() throws Exception {
        depResolution = pm.resolveDependencies("F-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(4, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(6, depResolution.getNewPackagesToDownload().size());
    }

    /**
     * Loop dependency is not an issue for P2CUDFDependencyResolver
     */
    @Override
    public void testLoopDetection() throws Exception {
        depResolution = pm.resolveDependencies("G-1.1.0", null);
        log.info(depResolution.toString());
        assertFalse(depResolution.isFailed());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(4, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(4, depResolution.getNewPackagesToDownload().size());
        assertTrue(depResolution.getNewPackagesToDownload().containsKey("G"));
        assertTrue(depResolution.getNewPackagesToDownload().containsKey("H"));
    }

    /**
     * Missing (not installed) dependencies is not an issue for
     * P2CUDFDependencyResolver
     */
    @Override
    public void testMissingDep() throws Exception {
        depResolution = pm.resolveDependencies("I-1.0.0", null);
        log.info(depResolution.toString());
        assertFalse(depResolution.isFailed());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(4, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(3, depResolution.getNewPackagesToDownload().size());
        assertTrue(depResolution.getNewPackagesToDownload().containsKey("I"));
        assertTrue(depResolution.getNewPackagesToDownload().containsKey("Z1"));
        assertTrue(depResolution.getNewPackagesToDownload().containsKey("Z2"));
    }

    /**
     * There is no real conflict in that case; it would be better to have K and
     * L (which J depends on) to exclusively depend on, respectively, versions
     * 1.0.0 and 2.0.0 of M.
     * TODO test that case
     */
    @Override
    public void testConflictingDeps() throws Exception {
        depResolution = pm.resolveDependencies("J-1.0.0", null);
        log.info(depResolution.toString());
        assertFalse(depResolution.isFailed());
    }

    @Override
    public void test3LevelsDeps() throws Exception {
        depResolution = pm.resolveDependencies("O-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(4, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(7, depResolution.getNewPackagesToDownload().size());
    }

    @Override
    public void testDoubleUpgrade() throws Exception {
        depResolution = pm.resolveDependencies("CC-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(4, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(3, depResolution.getNewPackagesToDownload().size());
    }

    @Override
    public void testForceRemove() throws Exception {
        depResolution = pm.resolveDependencies("X1-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(4, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(4, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
    }

    @Override
    public void testForceUpgradeOverRemove() throws Exception {
        depResolution = pm.resolveDependencies("X2-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(4, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(4, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
    }

    @Override
    public void testPlatformFiltering() throws Exception {
        // test that Platform Filtering changes the resolution result
        depResolution = pm.resolveDependencies("PF1-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(4, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(4, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());

        depResolution = pm.resolveDependencies("PF1-1.0.0", "5.3.1");
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(4, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(4, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
        assertEquals(new Version("2.0.0"),
                depResolution.getNewPackagesToDownload().get("PF2"));

        depResolution = pm.resolveDependencies("PF1-1.0.0", "5.3.0");
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(4, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(4, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
        assertEquals(new Version("1.0.0"),
                depResolution.getNewPackagesToDownload().get("PF2"));

        depResolution = pm.resolveDependencies("PF1-1.0.0", "5.3.2");
        log.info(depResolution.toString());
        assertTrue(depResolution.isFailed());
    }

}
