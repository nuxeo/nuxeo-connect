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
 *     Florent Guillaume
 */
package org.nuxeo.connect.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.apache.commons.lang.mutable.MutableObject;
import org.junit.Test;
import org.nuxeo.connect.update.PackageDependency;

public class TestPackageDescriptor {

    @Test
    public void testFixTargetPlatforms() {
        String[] targets;
        MutableObject pd = new MutableObject();
        PackageDependency[] packageDependencies;

        targets = PackageDescriptor.fixTargetPlatforms("foo", new String[] { "bar-888" }, pd);
        assertEquals(Arrays.asList("bar-888"), Arrays.asList(targets));
        packageDependencies = (PackageDependency[]) pd.getValue();
        assertNull(packageDependencies);

        targets = PackageDescriptor.fixTargetPlatforms("foo", new String[] { "cap-123" }, pd);
        assertEquals(Arrays.asList("cap-123", "server-123"), Arrays.asList(targets));
        packageDependencies = (PackageDependency[]) pd.getValue();
        assertEquals(1, packageDependencies.length);
        assertEquals("nuxeo-jsf-ui", packageDependencies[0].toString());

        targets = PackageDescriptor.fixTargetPlatforms("foo", new String[] { "bar-888", "cap-123" }, pd);
        assertEquals(Arrays.asList("bar-888", "cap-123", "server-123"), Arrays.asList(targets));
        packageDependencies = (PackageDependency[]) pd.getValue();
        assertEquals(1, packageDependencies.length);
        assertEquals("nuxeo-jsf-ui", packageDependencies[0].toString());

        targets = PackageDescriptor.fixTargetPlatforms("foo", new String[] { "bar-888", "cap-123", "cap-456" }, pd);
        assertEquals(Arrays.asList("bar-888", "cap-123", "cap-456", "server-123", "server-456"),
                Arrays.asList(targets));
        packageDependencies = (PackageDependency[]) pd.getValue();
        assertEquals(1, packageDependencies.length);
        assertEquals("nuxeo-jsf-ui", packageDependencies[0].toString());

        targets = PackageDescriptor.fixTargetPlatforms("foo",
                new String[] { "bar-888", "cap-123", "cap-456", "server-123" }, pd);
        assertEquals(Arrays.asList("bar-888", "cap-123", "cap-456", "server-123", "server-456"),
                Arrays.asList(targets));
        packageDependencies = (PackageDependency[]) pd.getValue();
        assertEquals(1, packageDependencies.length);
        assertEquals("nuxeo-jsf-ui", packageDependencies[0].toString());

        // now for nuxeo-jsf-ui itself

        targets = PackageDescriptor.fixTargetPlatforms("nuxeo-jsf-ui", new String[] { "bar-888", "cap-123" }, pd);
        assertEquals(Arrays.asList("bar-888", "cap-123"), Arrays.asList(targets));
        packageDependencies = (PackageDependency[]) pd.getValue();
        assertNull(packageDependencies);
    }

}
