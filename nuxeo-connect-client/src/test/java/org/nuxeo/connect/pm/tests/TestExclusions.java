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

import org.apache.commons.collections4.CollectionUtils;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.dependencies.DependencyResolution;

/**
 * @since 1.4
 */
public class TestExclusions extends AbstractPackageManagerTestCase {

    protected DummyPackageSource source;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("localExclusion.json");
        List<DownloadablePackage> remote = getDownloads("remoteExclusion.json");
        assertTrue(CollectionUtils.isNotEmpty(local));
        assertTrue(CollectionUtils.isNotEmpty(remote));
        source = new DummyPackageSource(local, "localExclusion");
        pm.registerSource(source, true);
        pm.registerSource(new DummyPackageSource(remote, "remoteExclusion"), false);
    }

    /**
     * Need to override since nuxeo-content-browser-cmf is considered as an upgrade of nuxeo-content-browser in Legacy
     * resolver whereas it's a replacement with P2CUDF
     */
    public void testResolutionOrder() throws Exception {
        // verify that CMF installation triggers DM uninstall
        DependencyResolution depResolution = pm.resolveDependencies(Arrays.asList("nuxeo-cmf-5.5.0"), null, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(2, depResolution.getLocalPackagesToRemove().size());
        assertTrue(depResolution.getLocalPackagesToRemove().containsKey("nuxeo-dm"));
        assertTrue(depResolution.getLocalPackagesToRemove().containsKey("nuxeo-content-browser"));
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertTrue(depResolution.getLocalPackagesToUpgrade().containsKey("nuxeo-content-browser"));

        // Fake installation
        List<DownloadablePackage> local2 = getDownloads("localExclusion2.json");
        source.reset(local2);

        // check reverse install: installing DM removes CMF
        depResolution = pm.resolveDependencies(Arrays.asList("nuxeo-dm-5.5.0"), null, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(2, depResolution.getLocalPackagesToRemove().size());
        assertTrue(depResolution.getLocalPackagesToRemove().containsKey("nuxeo-cmf"));
        assertTrue(depResolution.getLocalPackagesToRemove().containsKey("nuxeo-content-browser"));
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertTrue(depResolution.getLocalPackagesToUpgrade().containsKey("nuxeo-content-browser"));
    }

}
