/*
 * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and contributors.
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

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.dependencies.DependencyResolution;

/**
 * Tests the "keep/noKeep" option to resolveDependencies introduced for the mp-set command
 *
 * @since 1.4.14
 */
public class TestNoKeep extends AbstractPackageManagerTestCase {

    public void testInstallB() throws Exception {
        List<DownloadablePackage> local = getDownloads("localNoKeep1.json");
        pm.registerSource(new DummyPackageSource(local, true), true);
        List<String> pkgToSet = new ArrayList<String>();
        pkgToSet.add("B");
        DependencyResolution depResolution = pm.resolveDependencies(pkgToSet, null, null, null, false, false);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(2, depResolution.getLocalPackagesToInstall().size());
        assertEquals(0, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
    }

    public void testInstallBC() throws Exception {
        List<DownloadablePackage> local = getDownloads("localNoKeep2.json");
        pm.registerSource(new DummyPackageSource(local, true), true);
        List<String> pkgToSet = new ArrayList<String>();
        pkgToSet.add("B");
        DependencyResolution depResolution = pm.resolveDependencies(pkgToSet, null, null, null, false, false);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(2, depResolution.getLocalPackagesToInstall().size());
        assertEquals(0, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
    }

}
