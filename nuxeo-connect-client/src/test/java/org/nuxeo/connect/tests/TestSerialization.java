/*
 * (C) Copyright 2006-2014 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Nuxeo - initial API and implementation
 *
 */

package org.nuxeo.connect.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.junit.Test;
import org.nuxeo.connect.connector.NuxeoClientInstanceType;
import org.nuxeo.connect.data.AbstractJSONSerializableData;
import org.nuxeo.connect.data.PackageDescriptor;
import org.nuxeo.connect.data.SubscriptionStatus;
import org.nuxeo.connect.update.NuxeoValidationState;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.PackageState;
import org.nuxeo.connect.update.PackageType;
import org.nuxeo.connect.update.ProductionState;
import org.nuxeo.connect.update.Version;

public class TestSerialization {

    private static final Log log = LogFactory.getLog(TestSerialization.class);

    @Test
    public void testSerializeSubscriptionStatus() throws JSONException {
        SubscriptionStatus status = new SubscriptionStatus();
        status.setEndDate("25/11/2011");
        status.setContractStatus("OK");
        status.setDescription("MyInstance");
        status.setInstanceType(NuxeoClientInstanceType.DEV);
        status.setMessage("Yo");

        String json = status.serializeAsJSON();
        assertNotNull(json);
        log.info(json);

        SubscriptionStatus s2 = AbstractJSONSerializableData.loadFromJSON(
                SubscriptionStatus.class, json);
        assertNotNull(s2);

        assertEquals(status.getContractStatus(), s2.getContractStatus());
        assertEquals(status.getDescription(), s2.getDescription());
        assertEquals(status.getEndDate(), s2.getEndDate());
        assertEquals(status.getMessage(), s2.getMessage());
        assertEquals(status.getInstanceType(), s2.getInstanceType());
    }

    protected void doTestSerializePackage(boolean cap) throws Exception {
        PackageDescriptor p = new PackageDescriptor();
        p.setClassifier("MyClassifier");
        p.setDescription("MyDescription");
        p.setHomePage("http://www.nuxeo.org");
        p.setName("testPackage");
        List<String> targets = new ArrayList<>();
        targets.add("5.3.0");
        targets.add("5.3.1");
        if (cap) {
            targets.add("cap-8.3");
        }
        p.setTargetPlatforms(targets);
        p.setTitle("My Title");
        p.setType(PackageType.STUDIO);
        p.setVersion(new Version(1, 0, 2));
        PackageDependency[] deps = {
                new PackageDependency("my-package:1.1:1.2"),
                new PackageDependency("my-package:2.0:2.2") };
        p.setDependencies(deps);
        p.setCommentsNumber(8);
        p.setRating(4);
        p.setDownloadsCount(1000);
        p.setPictureUrl("http://xxx");
        p.setPackageState(PackageState.INSTALLED);
        p.setNuxeoValidationState(NuxeoValidationState.NUXEO_CERTIFIED);
        p.setProductionState(ProductionState.PRODUCTION_READY);
        p.setSupported(true);
        p.setSupportsHotReload(true);

        String json = p.serializeAsJSON();
        assertNotNull(json);
        log.info(json);

        PackageDescriptor p2 = AbstractJSONSerializableData.loadFromJSON(
                PackageDescriptor.class, json);
        assertNotNull(p2);
        assertEquals(p.getHomePage(), p2.getHomePage());
        assertEquals(p.getDescription(), p2.getDescription());
        assertEquals(p.getClassifier(), p2.getClassifier());
        assertEquals(p.getId(), p2.getId());
        assertEquals(p.getName(), p2.getName());
        assertEquals(p.getPackageState(), p2.getPackageState());
        assertEquals(p.getTitle(), p2.getTitle());
        assertEquals(p.getType(), p2.getType());
        assertEquals(p.getVersion(), p2.getVersion());
        assertEquals(p.getDownloadsCount(), p2.getDownloadsCount());
        assertEquals(p.getRating(), p2.getRating());
        assertEquals(p.getPictureUrl(), p2.getPictureUrl());
        assertEquals(p.getCommentsNumber(), p2.getCommentsNumber());
        assertEquals(PackageState.INSTALLED, p2.getPackageState());
        assertEquals(ProductionState.PRODUCTION_READY, p2.getProductionState());
        assertEquals(NuxeoValidationState.NUXEO_CERTIFIED,
                p2.getValidationState());
        assertEquals(true, p2.isSupported());
        assertEquals(true, p2.supportsHotReload());
        Set<String> expectedTargetPlatforms = new HashSet<>(Arrays.asList("5.3.0", "5.3.1"));
        if (cap) {
            expectedTargetPlatforms.add("cap-8.3");
            expectedTargetPlatforms.add("server-8.3");
        }
        assertEquals(expectedTargetPlatforms, new HashSet<>(Arrays.asList(p2.getTargetPlatforms())));
        Set<String> dependencies = new HashSet<>();
        for (PackageDependency pd : p2.getDependencies()) {
            dependencies.add(pd.toString());
        }
        Set<String> expectedDependencies = new HashSet<>(
                Arrays.asList("my-package:1.1.0:1.2.0", "my-package:2.0.0:2.2.0"));
        if (cap) {
            expectedDependencies.add("nuxeo-jsf-ui");
        }
        assertEquals(expectedDependencies, dependencies);
    }

    @Test
    public void testSerializePackage() throws Exception {
        doTestSerializePackage(false);
    }

    @Test
    public void testSerializePackageWithCap() throws Exception {
        doTestSerializePackage(true);
    }
}
