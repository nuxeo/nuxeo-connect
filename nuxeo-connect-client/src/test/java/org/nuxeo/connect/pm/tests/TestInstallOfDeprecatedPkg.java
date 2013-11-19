/*
 * (C) Copyright 2012-2013 Nuxeo SA (http://nuxeo.com/) and contributors.
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

import java.util.Arrays;
import java.util.List;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.dependencies.DependencyResolution;
import org.nuxeo.connect.update.PackageException;

/**
 * @since 1.4
 */
public class TestInstallOfDeprecatedPkg extends AbstractPackageManagerTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("local7.json");
        assertNotNull(local);
        assertTrue(local.size() > 0);
        pm.registerSource(new DummyPackageSource(local, true), true);
    }

    public void testResolutionOrder() {
        DependencyResolution depResolution = pm.resolveDependencies(
                Arrays.asList(new String[] { "nuxeo-poll" }), null, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(2, depResolution.getOrderedPackageIdsToInstall().size());
        assertEquals("nuxeo-dm-5.6.0",
                depResolution.getOrderedPackageIdsToInstall().get(0));
        assertEquals("nuxeo-poll-1.0.0",
                depResolution.getOrderedPackageIdsToInstall().get(1));
    }

    public void testTargetPlatforms() throws PackageException {
        assertTrue(pm.matchesPlatform("nuxeo-birt-integration-2.1.0", "cap-5.5"));
        assertFalse(pm.matchesPlatform("nuxeo-birt-integration-2.1.0",
                "cap-5.6"));
        // Such target platform is not valid...
        assertTrue(pm.matchesPlatform("nuxeo-platform-user-registration-1.2.1",
                "Nuxeo CAP 5.6"));
        // Test wildcards
        assertTrue(pm.matchesPlatform("nuxeo-flavors-unicolor-1.0.0", "cap-5.5"));
        assertTrue(pm.matchesPlatform("nuxeo-flavors-unicolor-1.0.0",
                "cap-5.5.0-HF00"));
        assertTrue(pm.matchesPlatform("nuxeo-flavors-unicolor-1.0.0",
                "cap-5.5.0-HF01"));
        assertFalse(pm.matchesPlatform("nuxeo-flavors-unicolor-1.0.0",
                "cap-5.5.0-something"));
        assertFalse(pm.matchesPlatform("nuxeo-flavors-unicolor-1.0.0",
                "cap-5.6.0"));
    }

}
