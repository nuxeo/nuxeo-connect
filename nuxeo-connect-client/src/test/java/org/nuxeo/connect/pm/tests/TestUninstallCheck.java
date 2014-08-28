/*
 * (C) Copyright 2012-2014 Nuxeo SA (http://nuxeo.com/) and contributors.
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
import org.nuxeo.connect.update.PackageState;

/**
 * @since 1.4
 */
public class TestUninstallCheck extends AbstractPackageManagerTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("localuninstall.json");
        assertNotNull(local);
        assertTrue(local.size() > 0);
        pm.registerSource(new DummyPackageSource(local, false), true);
    }

    public void testUninstallDependencies() throws Exception {
        DownloadablePackage pkg = pm.getPackage("A-1.0.0");
        assertNotNull(pkg);
        List<DownloadablePackage> pkgToRemove = performUninstall(pkg);
        log.info(pkgToRemove);
        assertFalse(pkgToRemove.contains(pkg));
        assertTrue(pkgToRemove.contains(pm.getPackage("B-1.0.0")));
        assertTrue(pkgToRemove.contains(pm.getPackage("C-1.0.0")));
        assertTrue(pkgToRemove.contains(pm.getPackage("D-1.0.0")));
        assertTrue(pkgToRemove.contains(pm.getPackage("E-1.0.0")));
        assertFalse(pkgToRemove.contains(pm.getPackage("F-1.0.0")));
        assertFalse(pkgToRemove.contains(pm.getPackage("G-1.0.0")));
        assertTrue(pm.getPackage("H-1.0.0").getPackageState() != PackageState.INSTALLED
                || pkgToRemove.contains(pm.getPackage("H-1.0.0")));
    }

    protected List<DownloadablePackage> performUninstall(DownloadablePackage pkg) {
        return pm.getUninstallDependencies(pkg, null);
    }

}
