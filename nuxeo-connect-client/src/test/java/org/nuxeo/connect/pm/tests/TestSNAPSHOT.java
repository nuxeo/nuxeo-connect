/*
 * (C) Copyright 2013-2015 Nuxeo SA (http://nuxeo.com/) and contributors.
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.dependencies.DependencyResolution;
import org.nuxeo.connect.update.Version;

/**
 * Test feature introduced by NXBT-654: "Exclude SNAPSHOT packages from resolution if not relevant". SNAPSHOT must be
 * excluded by default, included if allowed by option, included if explicitly needed by the request. Test (install,
 * upgrade and downgrade) by (name or ID) with (SNAPSHOT allowed or not) and package already installed in the (required,
 * greater, lower version or not at all).
 *
 * @since 1.4.13
 */
public class TestSNAPSHOT extends AbstractPackageManagerTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("localsnapshot.json");
        List<DownloadablePackage> remote = getDownloads("remotesnapshot.json");
        assertTrue(CollectionUtils.isNotEmpty(local));
        assertTrue(CollectionUtils.isNotEmpty(remote));
        pm.registerSource(new DummyPackageSource(local, "localsnapshot"), true);
        pm.registerSource(new DummyPackageSource(remote, "remotesnapshot"), false);
    }

    public void testAWithoutSNAPSHOT() throws Exception {
        DependencyResolution depResolution = pm.resolveDependencies("A", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        // requireChanges is true because of automatic upgrade
        // assertFalse(depResolution.requireChanges());

        List<String> installs = new ArrayList<>();
        installs.add("A-1.0.1-SNAPSHOT");
        depResolution = pm.resolveDependencies(installs, null, null, null, true);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(2, depResolution.getLocalPackagesToInstall().size());
        assertEquals(new Version("1.0.1-SNAPSHOT"), depResolution.getLocalPackagesToInstall().get("A"));
        assertEquals(2, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
    }

    public void testBWithoutSNAPSHOT() throws Exception {
        DependencyResolution depResolution = pm.resolveDependencies("B", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(new Version("1.0.2-SNAPSHOT"), depResolution.getLocalPackagesToInstall().get("B"));
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(new Version("1.0.1-SNAPSHOT"), depResolution.getLocalPackagesToUpgrade().get("B"));
        assertEquals(2, depResolution.getLocalUnchangedPackages().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());

        List<String> installs = new ArrayList<>();
        installs.add("B-1.0.1");
        depResolution = pm.resolveDependencies(installs, null, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(2, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getNewPackagesToDownload().size());
        assertEquals(new Version("1.0.1"), depResolution.getNewPackagesToDownload().get("B"));
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
    }

    public void testB2WithoutSNAPSHOT() throws Exception {
        DependencyResolution depResolution = pm.resolveDependencies("B2", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(2, depResolution.getLocalUnchangedPackages().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());

        List<String> uninstalls = new ArrayList<>();
        uninstalls.add("B2");
        depResolution = pm.resolveDependencies(null, uninstalls, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getLocalPackagesToRemove().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());

        uninstalls = new ArrayList<>();
        uninstalls.add("B");
        uninstalls.add("B2-1.0.2-SNAPSHOT");
        depResolution = pm.resolveDependencies(null, uninstalls, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getLocalPackagesToRemove().size());
        assertEquals(1, depResolution.getNewPackagesToDownload().size());
    }

    public void testCWithoutSNAPSHOT() throws Exception {
        DependencyResolution depResolution = pm.resolveDependencies("C", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(2, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());

        depResolution = pm.resolveDependencies("C-1.0.1", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(2, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getNewPackagesToDownload().size());
        assertEquals(new Version("1.0.1"), depResolution.getNewPackagesToDownload().get("C"));
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());

        depResolution = pm.resolveDependencies("C-1.0.1-SNAPSHOT", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(2, depResolution.getLocalPackagesToInstall().size());
        assertEquals(new Version("1.0.1-SNAPSHOT"), depResolution.getLocalPackagesToInstall().get("C"));
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(2, depResolution.getLocalUnchangedPackages().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
    }

    public void testAWithSNAPSHOT() throws Exception {
        List<String> installs = new ArrayList<>();
        installs.add("A");
        DependencyResolution depResolution = pm.resolveDependencies(installs, null, null, null, true);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(2, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());

        installs = new ArrayList<>();
        installs.add("A-1.0.2-SNAPSHOT");
        depResolution = pm.resolveDependencies(installs, null, null, null, true);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(2, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getNewPackagesToDownload().size());
        assertEquals(new Version("1.0.2-SNAPSHOT"), depResolution.getNewPackagesToDownload().get("A"));
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
    }

    public void testBWithSNAPSHOT() throws Exception {
        List<String> installs = new ArrayList<>();
        installs.add("B");
        DependencyResolution depResolution = pm.resolveDependencies(installs, null, null, null, true);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(2, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());

        installs = new ArrayList<>();
        installs.add("B-1.0.2-SNAPSHOT");
        depResolution = pm.resolveDependencies(installs, null, null, null, true);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(new Version("1.0.2-SNAPSHOT"), depResolution.getLocalPackagesToInstall().get("B"));
        assertEquals(2, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());

        installs = new ArrayList<>();
        installs.add("B-1.0.1");
        depResolution = pm.resolveDependencies(installs, null, null, null, true);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(2, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(2, depResolution.getNewPackagesToDownload().size());
        assertEquals(new Version("1.0.1"), depResolution.getNewPackagesToDownload().get("B"));
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
    }

    public void testCWithSNAPSHOT() throws Exception {
        List<String> installs = new ArrayList<>();
        installs.add("C");
        DependencyResolution depResolution = pm.resolveDependencies(installs, null, null, null, true);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(2, depResolution.getLocalPackagesToInstall().size());
        assertEquals(2, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());

        installs = new ArrayList<>();
        installs.add("C-1.0.1");
        depResolution = pm.resolveDependencies(installs, null, null, null, true);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(2, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(2, depResolution.getNewPackagesToDownload().size());
        assertEquals(new Version("1.0.1"), depResolution.getNewPackagesToDownload().get("C"));
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
    }

}
