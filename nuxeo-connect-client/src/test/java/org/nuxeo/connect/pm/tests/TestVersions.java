/*
 * (C) Copyright 2010-2013 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 */
package org.nuxeo.connect.pm.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.nuxeo.connect.update.Version;

public class TestVersions extends TestCase {

    public void testVersions() {
        assertEquals(-1, new Version("5.2.1-RC1").compareTo(new Version("5.2.1")));
        List<Version> versions = new ArrayList<Version>();
        versions.add(new Version("5"));
        versions.add(new Version("5.0"));
        versions.add(new Version("5.0.0"));
        versions.add(new Version("5.0.1"));
        versions.add(new Version("5.2.1"));
        versions.add(new Version("5.2.1-SNAPSHOT"));
        versions.add(new Version("5.2.1-RC1"));
        versions.add(new Version("5.2.1-RC2"));
        versions.add(new Version("5.0.1-SNAPSHOT"));
        versions.add(new Version("5.0.1-CMF"));
        versions.add(new Version("5.0.1-CMF-SNAPSHOT"));
        versions.add(new Version("5.0.1-beta"));
        versions.add(new Version("5.0.1-BETA"));
        versions.add(new Version("5.0.1-something"));
        versions.add(new Version("5.0.1-anything"));
        versions.add(new Version("5.0.1-I20130101"));
        versions.add(new Version("5.0.1-I20121225"));
        Collections.shuffle(versions);
        Collections.sort(versions);

        List<Version> expectedOrder = new ArrayList<Version>();
        expectedOrder.add(new Version("5"));
        expectedOrder.add(new Version("5"));
        expectedOrder.add(new Version("5"));
        expectedOrder.add(new Version("5.0.1-BETA"));
        expectedOrder.add(new Version("5.0.1-I20121225"));
        expectedOrder.add(new Version("5.0.1-I20130101"));
        expectedOrder.add(new Version("5.0.1-beta"));
        expectedOrder.add(new Version("5.0.1-SNAPSHOT"));
        expectedOrder.add(new Version("5.0.1"));
        expectedOrder.add(new Version("5.0.1-CMF-SNAPSHOT"));
        expectedOrder.add(new Version("5.0.1-CMF"));
        expectedOrder.add(new Version("5.0.1-anything"));
        expectedOrder.add(new Version("5.0.1-something"));
        expectedOrder.add(new Version("5.2.1-RC1"));
        expectedOrder.add(new Version("5.2.1-RC2"));
        expectedOrder.add(new Version("5.2.1-SNAPSHOT"));
        expectedOrder.add(new Version("5.2.1"));
        assertEquals(expectedOrder, versions);
    }

    public void testParsingVersions() {
        // Reference version for the test
        Version versionRef = new Version(7, 3, 12, "BETA");
        versionRef.setSnapshot(true);

        // Test with the "-SNAPSHOT" at the end
        Version version1 = new Version("7.3.12-BETA-SNAPSHOT");
        assertEquals(versionRef, version1);
        assertEquals(7, version1.major());
        assertEquals(3, version1.minor());
        assertEquals(12, version1.patch());
        assertEquals("BETA", version1.classifier());
        assertTrue(version1.isSnapshot());
        assertTrue(version1.isSpecialClassifier());

        // Test with the "-SNAPSHOT" after the version
        Version version2 = new Version("7.3.12-SNAPSHOT-BETA");
        assertEquals(versionRef, version1);
        assertEquals(version1, version2);
        assertEquals("BETA", version2.classifier());
        assertTrue(version2.isSnapshot());
        assertTrue(version2.isSpecialClassifier());
    }
}
