/*
 * (C) Copyright 2013-2016 Nuxeo SA (http://nuxeo.com/) and others.
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
package org.nuxeo.connect.pm.tests;

import java.util.ArrayList;
import java.util.Arrays;
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
        // Before: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        // {"version":"1.0.0","name":"A","state":5,"type":"addon"}
        // {"version":"1.0.1-SNAPSHOT","name":"A","state":2,"type":"addon"}
        // {"version":"1.0.2-SNAPSHOT","name":"A","state":0,"type":"addon"}
        // After: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        DependencyResolution depResolution = pm.resolveDependencies(Arrays.asList("A"), null, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertFalse(depResolution.requireChanges());

        // Before: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        // {"version":"1.0.0","name":"A","state":5,"type":"addon"}
        // {"version":"1.0.1-SNAPSHOT","name":"A","state":2,"type":"addon"}
        // {"version":"1.0.2-SNAPSHOT","name":"A","state":0,"type":"addon"}
        // After: [A-1.0.1-SNAPSHOT, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        List<String> installs = new ArrayList<>();
        installs.add("A-1.0.1-SNAPSHOT");
        depResolution = pm.resolveDependencies(installs, null, null, null, true);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(new Version("1.0.1-SNAPSHOT"), depResolution.getLocalPackagesToInstall().get("A"));
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(2, depResolution.getLocalUnchangedPackages().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
    }

    public void testBWithoutSNAPSHOT() throws Exception {
        // Before: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.1-SNAPSHOT]
        // {"version":"1.0.0","name":"B","state":2,"type":"addon"}
        // {"version":"1.0.1-SNAPSHOT","name":"B","state":5,"type":"addon"}
        // {"version":"1.0.1","name":"B","state":0,"type":"addon"}
        // {"version":"1.0.2-SNAPSHOT","name":"B","state":2,"type":"addon"}
        // After: [A-1.0.0, B-1.0.2-SNAPSHOT, B2-1.0.1-SNAPSHOT]
        DependencyResolution depResolution = pm.resolveDependencies(Arrays.asList("B-1.0.2-SNAPSHOT"), null, null,
                null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(new Version("1.0.2-SNAPSHOT"), depResolution.getLocalPackagesToInstall().get("B"));
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(new Version("1.0.1-SNAPSHOT"), depResolution.getLocalPackagesToUpgrade().get("B"));
        assertEquals(2, depResolution.getLocalUnchangedPackages().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());

        // Before: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        // {"version":"1.0.0","name":"B","state":2,"type":"addon"}
        // {"version":"1.0.1-SNAPSHOT","name":"B","state":5,"type":"addon"}
        // {"version":"1.0.1","name":"B","state":0,"type":"addon"}
        // {"version":"1.0.2-SNAPSHOT","name":"B","state":2,"type":"addon"}
        // After: [A-1.0.0, B-1.0.1, B2-1.0.2-SNAPSHOT]
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
        // Before: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.1-SNAPSHOT]
        // {"version":"1.0.0","name":"B2","state":2,"type":"addon"}
        // {"version":"1.0.1-SNAPSHOT","name":"B2","state":5,"type":"addon"}
        // {"version":"1.0.1","name":"B2","state":0,"type":"addon"}
        // {"version":"1.0.2-SNAPSHOT","name":"B2","state":2,"type":"addon"}
        // After: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        DependencyResolution depResolution = pm.resolveDependencies(Arrays.asList("B2-1.0.2-SNAPSHOT"), null, null,
                null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(2, depResolution.getLocalUnchangedPackages().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());

        // Before: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.1-SNAPSHOT]
        // {"version":"1.0.0","name":"B2","state":2,"type":"addon"}
        // {"version":"1.0.1-SNAPSHOT","name":"B2","state":5,"type":"addon"}
        // {"version":"1.0.1","name":"B2","state":0,"type":"addon"}
        // {"version":"1.0.2-SNAPSHOT","name":"B2","state":2,"type":"addon"}
        // After: [A-1.0.0, B-1.0.1-SNAPSHOT]
        List<String> uninstalls = new ArrayList<>();
        uninstalls.add("B2");
        depResolution = pm.resolveDependencies(null, uninstalls, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(0, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(2, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getLocalPackagesToRemove().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());

        // Before: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.1-SNAPSHOT]
        // {"version":"1.0.0","name":"B2","state":2,"type":"addon"}
        // {"version":"1.0.1-SNAPSHOT","name":"B2","state":5,"type":"addon"}
        // {"version":"1.0.1","name":"B2","state":0,"type":"addon"}
        // {"version":"1.0.2-SNAPSHOT","name":"B2","state":2,"type":"addon"}
        // After: [A-1.0.0]
        uninstalls = new ArrayList<>();
        uninstalls.add("B");
        // this is a downgrade
        uninstalls.add("B2-1.0.1-SNAPSHOT");
        depResolution = pm.resolveDependencies(null, uninstalls, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(0, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(1, depResolution.getLocalUnchangedPackages().size());
        assertEquals(2, depResolution.getLocalPackagesToRemove().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());
    }

    public void testCWithoutSNAPSHOT() throws Exception {
        // Before: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        // {"version":"1.0.0","name":"C","state":2,"type":"addon"}
        // {"version":"1.0.1-SNAPSHOT","name":"C","state":2,"type":"addon"}
        // {"version":"1.0.1","name":"C","state":0,"type":"addon"}
        // {"version":"1.0.2-SNAPSHOT","name":"C","state":2,"type":"addon"}
        // After: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT, C-1.0.1]
        DependencyResolution depResolution = pm.resolveDependencies(Arrays.asList("C"), null, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(0, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(3, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getNewPackagesToDownload().size());
        assertEquals(new Version("1.0.1"), depResolution.getNewPackagesToDownload().get("C"));
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());

        // Before: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        // {"version":"1.0.0","name":"C","state":2,"type":"addon"}
        // {"version":"1.0.1-SNAPSHOT","name":"C","state":2,"type":"addon"}
        // {"version":"1.0.1","name":"C","state":0,"type":"addon"}
        // {"version":"1.0.2-SNAPSHOT","name":"C","state":2,"type":"addon"}
        // After: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT, C-1.0.1]
        depResolution = pm.resolveDependencies(Arrays.asList("C-1.0.1"), null, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(0, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(3, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getNewPackagesToDownload().size());
        assertEquals(new Version("1.0.1"), depResolution.getNewPackagesToDownload().get("C"));
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());

        // Before: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        // {"version":"1.0.0","name":"C","state":2,"type":"addon"}
        // {"version":"1.0.1-SNAPSHOT","name":"C","state":2,"type":"addon"}
        // {"version":"1.0.1","name":"C","state":0,"type":"addon"}
        // {"version":"1.0.2-SNAPSHOT","name":"C","state":2,"type":"addon"}
        // After: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT, C-1.0.1-SNAPSHOT]
        depResolution = pm.resolveDependencies(Arrays.asList("C-1.0.1-SNAPSHOT"), null, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(new Version("1.0.1-SNAPSHOT"), depResolution.getLocalPackagesToInstall().get("C"));
        assertEquals(0, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(3, depResolution.getLocalUnchangedPackages().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
    }

    public void testAWithSNAPSHOT() throws Exception {
        // Before: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        // {"version":"1.0.0","name":"A","state":5,"type":"addon"}
        // {"version":"1.0.1-SNAPSHOT","name":"A","state":2,"type":"addon"}
        // {"version":"1.0.2-SNAPSHOT","name":"A","state":0,"type":"addon"}
        // After: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        List<String> installs = new ArrayList<>();
        installs.add("A");
        DependencyResolution depResolution = pm.resolveDependencies(installs, null, null, null, true);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertFalse(depResolution.requireChanges());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(0, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(3, depResolution.getLocalUnchangedPackages().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());

        // Before: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        // {"version":"1.0.0","name":"A","state":5,"type":"addon"}
        // {"version":"1.0.1-SNAPSHOT","name":"A","state":2,"type":"addon"}
        // {"version":"1.0.2-SNAPSHOT","name":"A","state":0,"type":"addon"}
        // After: [A-1.0.2-SNAPSHOT, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        installs = new ArrayList<>();
        installs.add("A-1.0.2-SNAPSHOT");
        depResolution = pm.resolveDependencies(installs, null, null, null, true);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(2, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getNewPackagesToDownload().size());
        assertEquals(new Version("1.0.2-SNAPSHOT"), depResolution.getNewPackagesToDownload().get("A"));
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
    }

    public void testBWithSNAPSHOT() throws Exception {
        // Before: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        // {"version":"1.0.0","name":"B","state":2,"type":"addon"}
        // {"version":"1.0.1-SNAPSHOT","name":"B","state":5,"type":"addon"}
        // {"version":"1.0.1","name":"B","state":0,"type":"addon"}
        // {"version":"1.0.2-SNAPSHOT","name":"B","state":2,"type":"addon"}
        // After: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        List<String> installs = new ArrayList<>();
        installs.add("B");
        DependencyResolution depResolution = pm.resolveDependencies(installs, null, null, null, true);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertFalse(depResolution.requireChanges());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(0, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(3, depResolution.getLocalUnchangedPackages().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());

        // Before: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        // {"version":"1.0.0","name":"B","state":2,"type":"addon"}
        // {"version":"1.0.1-SNAPSHOT","name":"B","state":5,"type":"addon"}
        // {"version":"1.0.1","name":"B","state":0,"type":"addon"}
        // {"version":"1.0.2-SNAPSHOT","name":"B","state":2,"type":"addon"}
        // After: [A-1.0.0, B-1.0.2-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        installs = new ArrayList<>();
        installs.add("B-1.0.2-SNAPSHOT");
        depResolution = pm.resolveDependencies(installs, null, null, null, true);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(new Version("1.0.2-SNAPSHOT"), depResolution.getLocalPackagesToInstall().get("B"));
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(2, depResolution.getLocalUnchangedPackages().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());

        // Before: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        // {"version":"1.0.0","name":"B","state":2,"type":"addon"}
        // {"version":"1.0.1-SNAPSHOT","name":"B","state":5,"type":"addon"}
        // {"version":"1.0.1","name":"B","state":0,"type":"addon"}
        // {"version":"1.0.2-SNAPSHOT","name":"B","state":2,"type":"addon"}
        // After: [A-1.0.0, B-1.0.1, B2-1.0.2-SNAPSHOT]
        installs = new ArrayList<>();
        installs.add("B-1.0.1");
        depResolution = pm.resolveDependencies(installs, null, null, null, true);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(2, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getNewPackagesToDownload().size());
        assertEquals(new Version("1.0.1"), depResolution.getNewPackagesToDownload().get("B"));
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
    }

    public void testCWithSNAPSHOT() throws Exception {
        // Before: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        // {"version":"1.0.0","name":"C","state":2,"type":"addon"}
        // {"version":"1.0.1-SNAPSHOT","name":"C","state":2,"type":"addon"}
        // {"version":"1.0.1","name":"C","state":0,"type":"addon"}
        // {"version":"1.0.2-SNAPSHOT","name":"C","state":2,"type":"addon"}
        // After: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT, C-1.0.2-SNAPSHOT]
        List<String> installs = new ArrayList<>();
        installs.add("C");
        DependencyResolution depResolution = pm.resolveDependencies(installs, null, null, null, true);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(0, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(3, depResolution.getLocalUnchangedPackages().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());

        // Before: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT]
        // {"version":"1.0.0","name":"C","state":2,"type":"addon"}
        // {"version":"1.0.1-SNAPSHOT","name":"C","state":2,"type":"addon"}
        // {"version":"1.0.1","name":"C","state":0,"type":"addon"}
        // {"version":"1.0.2-SNAPSHOT","name":"C","state":2,"type":"addon"}
        // After: [A-1.0.0, B-1.0.1-SNAPSHOT, B2-1.0.2-SNAPSHOT, C-1.0.1]
        installs = new ArrayList<>();
        installs.add("C-1.0.1");
        depResolution = pm.resolveDependencies(installs, null, null, null, true);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(0, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(3, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getNewPackagesToDownload().size());
        assertEquals(new Version("1.0.1"), depResolution.getNewPackagesToDownload().get("C"));
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
    }

}
