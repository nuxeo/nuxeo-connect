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

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.dependencies.DependencyResolution;

/**
 * @since 1.4
 */
public class TestHotFixes extends AbstractPackageManagerTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("localhf2.json");
        List<DownloadablePackage> remote = getDownloads("remotehf2.json");
        assertTrue(CollectionUtils.isNotEmpty(local));
        assertTrue(CollectionUtils.isNotEmpty(remote));
        pm.registerSource(new DummyPackageSource(local, "localhf2"), true);
        pm.registerSource(new DummyPackageSource(remote, "remotehf2"), false);
    }

    public void testResolutionOrder() throws Exception {
        DependencyResolution depResolution = pm.resolveDependencies("hf11-1.0.0", null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        String expectedOrder = "hf00-1.0.0/hf01-1.0.0/hf01PATCH-1.0.0/hf02-1.0.0/hf03-1.0.0/hf04-1.0.0/hf05-1.0.0/hf06-1.0.0/hf07-1.0.0/hf08-1.0.0/hf09-1.0.0/hf10-1.0.0/hf11-1.0.0";
        assertEquals("Bad dependencies order", expectedOrder, depResolution.getInstallationOrderAsString());
    }

}
