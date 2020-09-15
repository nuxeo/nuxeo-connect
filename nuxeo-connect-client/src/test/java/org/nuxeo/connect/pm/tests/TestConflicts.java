/*
 * (C) Copyright 2018 Nuxeo SA (http://nuxeo.com/) and others.
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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.dependencies.DependencyResolution;

/**
 * @since 1.7.1
 */
public class TestConflicts extends AbstractPackageManagerTestCase {

    protected DummyPackageSource remoteSource;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("localConflict.json");
        List<DownloadablePackage> remote = getDownloads("remoteConflict.json");
        assertTrue(CollectionUtils.isNotEmpty(local));
        assertTrue(CollectionUtils.isNotEmpty(remote));
        pm.registerSource(new DummyPackageSource(local, "localConflict"), true);
        remoteSource = new DummyPackageSource(remote, "remoteConflict");
        pm.registerSource(remoteSource, false);
    }

    public void testUninstallOnConflict() throws Exception {
        // verify that nuxeo-jsf-ui-9.10.0 installation triggers nuxeo-9.10-HF01 uninstall
        DependencyResolution depResolution = pm.resolveDependencies(Arrays.asList("nuxeo-jsf-ui-9.10.0"), null, null,
                null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToRemove().size());
        assertTrue(depResolution.getLocalPackagesToRemove().containsKey("nuxeo-9.10-HF01"));
        assertEquals(1, depResolution.getOrderedPackageIdsToInstall().size());
        assertTrue(depResolution.getOrderedPackageIdsToInstall().contains("nuxeo-jsf-ui-9.10.0"));
    }

    public void testInstallWithoutConflict() throws Exception {
        // verify that nuxeo-jsf-ui-9.10.0-HF01 installation is ok when not precising the version of jsf-ui to install
        DependencyResolution depResolution = pm.resolveDependencies(Arrays.asList("nuxeo-jsf-ui"), null, null, null,
                null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
        assertEquals(1, depResolution.getOrderedPackageIdsToInstall().size());
        assertTrue(depResolution.getOrderedPackageIdsToInstall().contains("nuxeo-jsf-ui-9.10.0-HF01"));

        // Change remote source to make only nuxeo-jsf-ui-9.10.0-HF01 available
        remoteSource.reset(getDownloads("remoteConflict2.json"));

        // verify that nuxeo-jsf-ui-9.10.0-HF01 installation is ok when nuxeo-jsf-ui-9.10.0 is not available (see
        // NXP-25164)
        depResolution = pm.resolveDependencies(Arrays.asList("nuxeo-jsf-ui-9.10.0-HF01"), null, null, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
        assertEquals(1, depResolution.getOrderedPackageIdsToInstall().size());
        assertTrue(depResolution.getOrderedPackageIdsToInstall().contains("nuxeo-jsf-ui-9.10.0-HF01"));
    }

}
