/*
 * (C) Copyright 2012-2015 Nuxeo SA (http://nuxeo.com/) and others.
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
 * @since 1.4
 */
public class TestRemoveP2CUDF extends AbstractPackageManagerTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("local4.json");
        assertTrue(CollectionUtils.isNotEmpty(local));
        pm.registerSource(new DummyPackageSource(local, "local4"), true);
    }

    public void testResolutionOrder() throws Exception {
        List<String> uninstalls = new ArrayList<>();
        uninstalls.add("nuxeo-content-browser:1.0.0");
        // SNAPSHOT allowed
        DependencyResolution depResolution = pm.resolveDependencies(null, uninstalls, null, null, true);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(3, depResolution.getRemovePackageIds().size());
        assertEquals(
                "[nuxeo-social-collaboration-5.6.0-SNAPSHOT, nuxeo-dm-5.6.0-SNAPSHOT, nuxeo-content-browser-1.1.0]",
                depResolution.getOrderedPackageIdsToRemove().toString());
        assertEquals(new Version("1.1.0"), depResolution.getLocalPackagesToRemove().get("nuxeo-content-browser"));
        // SNAPSHOT forbidden
        depResolution = pm.resolveDependencies(null, uninstalls, null, null, false);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(3, depResolution.getRemovePackageIds().size());
        assertEquals(
                "[nuxeo-social-collaboration-5.6.0-SNAPSHOT, nuxeo-dm-5.6.0-SNAPSHOT, nuxeo-content-browser-1.1.0]",
                depResolution.getOrderedPackageIdsToRemove().toString());
        assertEquals(new Version("1.1.0"), depResolution.getLocalPackagesToRemove().get("nuxeo-content-browser"));
    }

}
