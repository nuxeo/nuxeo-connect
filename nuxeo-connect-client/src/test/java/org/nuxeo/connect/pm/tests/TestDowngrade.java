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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.dependencies.DependencyResolution;

/**
 * @since 1.4
 */
public class TestDowngrade extends AbstractPackageManagerTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("local5.json");
        assertTrue(CollectionUtils.isNotEmpty(local));
        pm.registerSource(new DummyPackageSource(local, "local5"), true);
    }

    public void testResolutionOrder() throws Exception {
        DependencyResolution depResolution = pm.resolveDependencies(
                Arrays.asList(new String[] { "nuxeo-birt-integration-2.0.0" }), null, null, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getOrderedPackageIdsToInstall().size());
        assertEquals("nuxeo-birt-integration-2.0.0", depResolution.getOrderedPackageIdsToInstall().get(0));
    }

}
