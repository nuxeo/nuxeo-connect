/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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

package org.nuxeo.connect.packages.dependencies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.connect.connector.fake.FakeDownloadablePackage;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.pm.tests.AbstractPackageManagerTestCase;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.Version;

/**
 *
 *
 * @since 5.6
 */
public class CUDFHelperTest {

    private CUDFHelper cudfHelper;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        cudfHelper = getCUDFTestHelper();
    }

    public static CUDFHelper getCUDFTestHelper() {
        return new CUDFHelper(null) {

            @Override
            protected List<DownloadablePackage> getAllPackages() {
                List<DownloadablePackage> allPackages = new ArrayList<DownloadablePackage>();
                FakeDownloadablePackage pkg = new FakeDownloadablePackage(
                        "nuxeo-email-suggestion", new Version("1.1.0"));
                allPackages.add(pkg);
                pkg = new FakeDownloadablePackage("nuxeo-content-browser",
                        new Version("1.1.0"));
                pkg.addConflict(new PackageDependency(
                        "nuxeo-content-browser:1.1.0-cmf:1.1.0-cmf"));
                allPackages.add(pkg);
                pkg = new FakeDownloadablePackage("nuxeo-content-browser",
                        new Version("1.1.0-cmf"));
                pkg.addConflict(new PackageDependency(
                        "nuxeo-content-browser:1.1.0:1.1.0"));
                allPackages.add(pkg);
                for (String version : new String[] { "5.5.0", "5.6",
                        "5.6-SNAPSHOT" }) {
                    pkg = new FakeDownloadablePackage("nuxeo-dm", new Version(
                            version));
                    pkg.addDependency(new PackageDependency(
                            "nuxeo-content-browser:1.1.0:1.1.0"));
                    pkg.addConflict(new PackageDependency("nuxeo-cmf"));
                    allPackages.add(pkg);
                }
                pkg = new FakeDownloadablePackage("nuxeo-sc",
                        new Version("5.6"));
                pkg.addDependency(new PackageDependency("nuxeo-dm:5.6.0:5.6.0"));
                allPackages.add(pkg);
                for (String version : new String[] { "5.5.0", "5.6",
                        "5.6-SNAPSHOT" }) {
                    pkg = new FakeDownloadablePackage("nuxeo-cmf", new Version(
                            version));
                    pkg.addDependency(new PackageDependency(
                            "nuxeo-content-browser:1.1.0-cmf:1.1.0-cmf"));
                    allPackages.add(pkg);
                }
                return allPackages;
            }
        };
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testInitMapping() throws Exception {
        assertEquals(
                "First nuxeo-dm package should be 5.5.0",
                "5.5.0",
                cudfHelper.getCUDFPackage("nuxeo-dm-1").getNuxeoVersion().toString());
        assertEquals(
                "Second nuxeo-dm package should be 5.6.0-SNAPSHOT",
                "5.6.0-SNAPSHOT",
                cudfHelper.getCUDFPackage("nuxeo-dm-2").getNuxeoVersion().toString());
        assertEquals(
                "Third nuxeo-dm package should be 5.6.0",
                "5.6.0",
                cudfHelper.getCUDFPackage("nuxeo-dm-3").getNuxeoVersion().toString());
        assertEquals("There must be two nuxeo-content-browser packages", 2,
                cudfHelper.getCUDFPackages("nuxeo-content-browser").size());
        assertNull("nuxeo-content-browser:cmf must not be a package name",
                cudfHelper.getCUDFPackages("nuxeo-content-browser:cmf"));
    }

    @Test
    public void testGetCUDFFile() throws Exception {
        BufferedReader gen = new BufferedReader(new StringReader(
                cudfHelper.getCUDFFile()));
        BufferedReader ref = new BufferedReader(new InputStreamReader(
                CUDFHelperTest.class.getClassLoader().getResourceAsStream(
                        AbstractPackageManagerTestCase.TEST_DATA
                                + "universe.cudf")));
        try {
            while (gen.ready() && ref.ready()) {
                String generatedLine = gen.readLine().trim();
                assertEquals(
                        "Generated CUDF universe different than reference",
                        ref.readLine().trim(), generatedLine);
            }
            assertFalse(gen.ready() && ref.ready());
        } finally {
            gen.close();
            ref.close();
        }
    }
}
