/*
 * (C) Copyright 2017 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @since 1.4.24.2
 */
public class TestDependencies extends AbstractPackageManagerTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("local9.json");
        assertTrue(CollectionUtils.isNotEmpty(local));
        pm.registerSource(new DummyPackageSource(local, "local9"), true);
    }

    public void testVersionResolutionWithDifferentDependencies() throws Exception {
        // pkgA-1.0.3 has a dependency on pkgB, other versions of pkgA have no dependencies
        DependencyResolution depResolution = pm.resolveDependencies(Arrays.asList(new String[] { "pkgA" }), null, null,
                null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(2, depResolution.getOrderedPackageIdsToInstall().size());
        assertEquals("pkgB-1.0.1", depResolution.getOrderedPackageIdsToInstall().get(0));
        assertEquals("pkgA-1.0.3", depResolution.getOrderedPackageIdsToInstall().get(1));
    }

}
