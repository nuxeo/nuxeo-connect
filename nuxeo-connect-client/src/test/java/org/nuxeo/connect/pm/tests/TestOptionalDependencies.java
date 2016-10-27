/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 * @since 1.4.26
 */
public class TestOptionalDependencies extends AbstractPackageManagerTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("local8.json");
        assertTrue(CollectionUtils.isNotEmpty(local));
        pm.registerSource(new DummyPackageSource(local, "local8"), true);
    }

    public void testOptionalDependencyIsNotRequired() throws Exception {
        // test-opt-dep1-1.0.0 has an optional dependency on zz-nuxeo-jsf-ui:1.0.0:1.0.0
        DependencyResolution depResolution = pm.resolveDependencies(
                Arrays.asList(new String[] { "test-opt-dep1-1.0.0" }), null, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getOrderedPackageIdsToInstall().size());
        assertEquals("test-opt-dep1-1.0.0", depResolution.getOrderedPackageIdsToInstall().get(0));
        // test-opt-dep4-1.0.0 has an optional dependency on zz-nuxeo-jsf-ui and zz-nuxeo-web-ui
        depResolution = pm.resolveDependencies(Arrays.asList(new String[] { "test-opt-dep4-1.0.0" }), null, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getOrderedPackageIdsToInstall().size());
        assertEquals("test-opt-dep4-1.0.0", depResolution.getOrderedPackageIdsToInstall().get(0));
    }

    public void testOptionalDependencyIsOrderedFirst() throws Exception {
        // test-opt-dep1-1.0.0 and test-opt-dep2-1.0.0 have an optional dependency on zz-nuxeo-jsf-ui:1.0.0:1.0.0
        // zz-nuxeo-jsf-ui-1.0.0 has an optional dependency on zz-nuxeo-web-ui:1.0.0
        // zz-nuxeo-web-ui-1.0.0 is already installed
        DependencyResolution depResolution = pm.resolveDependencies(
                Arrays.asList(
                        new String[] { "test-opt-dep2", "test-opt-dep1", "zz-nuxeo-jsf-ui", "zz-nuxeo-web-ui-1.0.2" }),
                null, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(4, depResolution.getOrderedPackageIdsToInstall().size());
        assertEquals("zz-nuxeo-web-ui-1.0.2", depResolution.getOrderedPackageIdsToInstall().get(0));
        assertEquals("zz-nuxeo-jsf-ui-1.0.0", depResolution.getOrderedPackageIdsToInstall().get(1));
        assertEquals("test-opt-dep1-1.0.0", depResolution.getOrderedPackageIdsToInstall().get(2));
        assertEquals("test-opt-dep2-1.0.0", depResolution.getOrderedPackageIdsToInstall().get(3));
    }

    public void testTransitiveOptionalDependencyIsOrderedFirst() throws Exception {
        // test-opt-dep1-1.0.0 and test-opt-dep2-1.0.0 have an optional dependency on zz-nuxeo-jsf-ui:1.0.0:1.0.0
        // zz-nuxeo-jsf-ui-1.0.0 has an optional dependency on zz-nuxeo-web-ui:1.0.0
        // zz-nuxeo-web-ui-1.0.0 is already installed
        // aa-test-trans-dep1 has an required dependency on test-opt-dep1
        DependencyResolution depResolution = pm.resolveDependencies(
                Arrays.asList(new String[] { "aa-test-trans-dep1", "test-opt-dep1" }), null, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(2, depResolution.getOrderedPackageIdsToInstall().size());
        assertEquals("test-opt-dep1-1.0.0", depResolution.getOrderedPackageIdsToInstall().get(0));
        assertEquals("aa-test-trans-dep1-1.0.0", depResolution.getOrderedPackageIdsToInstall().get(1));
        // aa-test-trans-dep2 has an required dependency on test-opt-dep2
        depResolution = pm.resolveDependencies(Arrays.asList(new String[] { "aa-test-trans-dep2", "zz-nuxeo-jsf-ui" }),
                null, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(3, depResolution.getOrderedPackageIdsToInstall().size());
        assertEquals("zz-nuxeo-jsf-ui-1.0.0", depResolution.getOrderedPackageIdsToInstall().get(0));
        assertEquals("test-opt-dep2-1.0.0", depResolution.getOrderedPackageIdsToInstall().get(1));
        assertEquals("aa-test-trans-dep2-1.0.0", depResolution.getOrderedPackageIdsToInstall().get(2));
    }

    public void testReinstallForLatelyResolvedOptionalDependency() throws Exception {
        // zz-nuxeo-web-ui-1.0.0 has an optional dependency on test-opt-dep3
        // it should be reinstalled
        DependencyResolution depResolution = pm.resolveDependencies(
                Arrays.asList(new String[] { "test-opt-dep3-1.0.0" }), null, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(2, depResolution.getOrderedPackageIdsToInstall().size());
        assertEquals("test-opt-dep3-1.0.0", depResolution.getOrderedPackageIdsToInstall().get(0));
        assertEquals("zz-nuxeo-web-ui-1.0.0", depResolution.getOrderedPackageIdsToInstall().get(1));
        assertEquals(1, depResolution.getOrderedPackageIdsToRemove().size());
        assertEquals("zz-nuxeo-web-ui-1.0.0", depResolution.getOrderedPackageIdsToRemove().get(0));
    }

}
