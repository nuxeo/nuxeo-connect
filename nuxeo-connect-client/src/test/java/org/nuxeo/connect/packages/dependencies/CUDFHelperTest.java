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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.equinox.p2.cudf.Parser;
import org.eclipse.equinox.p2.cudf.metadata.IProvidedCapability;
import org.eclipse.equinox.p2.cudf.metadata.IRequiredCapability;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.equinox.p2.cudf.metadata.NotRequirement;
import org.eclipse.equinox.p2.cudf.metadata.RequiredCapability;
import org.eclipse.equinox.p2.cudf.metadata.VersionRange;
import org.eclipse.equinox.p2.cudf.solver.ProfileChangeRequest;
import org.eclipse.equinox.p2.cudf.solver.SimplePlanner;
import org.eclipse.equinox.p2.cudf.solver.SolverConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.InternalPackageManager;
import org.nuxeo.connect.pm.tests.AbstractPackageManagerTestCase;
import org.nuxeo.connect.pm.tests.DummyPackageSource;

/**
 *
 *
 * @since 1.4
 */
public class CUDFHelperTest extends AbstractPackageManagerTestCase {

    /**
     * Add a space at the end of lines ending with ':'. That is made at runtime
     * because we don't want to rely on ending spaces in the test resource file
     * which may (should) be trimmed by editors.
     */
    public class AddSpaceInputStream extends InputStream {

        private InputStream is;

        private int previousByte;

        public AddSpaceInputStream(InputStream resourceAsStream) {
            is = resourceAsStream;
        }

        @Override
        public int read() throws IOException {
            if (!is.markSupported()) {
                log.error("Cannot add spaces, that aspect won't be tested.");
                return is.read();
            }
            is.mark(2);
            int nextByte = is.read();
            if (nextByte == 10 && previousByte == 58) {
                // just read a '\n' after a ':' so return a ' ' (space) instead
                nextByte = 32;
                // reset marker before '\n'
                is.reset();
            }
            previousByte = nextByte;
            return nextByte;
        }
    }

    private CUDFHelper cudfHelper;

    private ProfileChangeRequest pcr;

    private ProfileChangeRequest pcr2;

    private ProfileChangeRequest pcrRemove;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("local3.json");
        List<DownloadablePackage> remote = getDownloads("remote3.json");
        DummyPackageSource source = new DummyPackageSource(local, true);
        pm.registerSource(source, true);
        pm.registerSource(new DummyPackageSource(remote, false), false);
        // cudfHelper = getCUDFTestHelper(pm);
        cudfHelper = new CUDFHelper((InternalPackageManager) pm);
        pcr = new Parser().parse(new AddSpaceInputStream(
                this.getClass().getClassLoader().getResourceAsStream(
                        AbstractPackageManagerTestCase.TEST_DATA
                                + "request.cudf")));
        pcr2 = new Parser().parse(new AddSpaceInputStream(
                this.getClass().getClassLoader().getResourceAsStream(
                        AbstractPackageManagerTestCase.TEST_DATA
                                + "request2.cudf")));
        pcrRemove = new Parser().parse(new AddSpaceInputStream(
                this.getClass().getClassLoader().getResourceAsStream(
                        AbstractPackageManagerTestCase.TEST_DATA
                                + "requestRemove.cudf")));
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
        assertEquals("There must be one nuxeo-content-browser package", 1,
                cudfHelper.getCUDFPackages("nuxeo-content-browser").size());
        assertEquals("There must be one nuxeo-content-browser*cmf package", 1,
                cudfHelper.getCUDFPackages("nuxeo-content-browser*cmf").size());
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
            int i = 1;
            while (gen.ready() && ref.ready()) {
                String generatedLine = gen.readLine().trim();
                assertEquals(
                        "Generated CUDF universe different than reference at line "
                                + i, ref.readLine().trim(), generatedLine);
                i++;
            }
            assertFalse(gen.ready() && ref.ready());
        } finally {
            gen.close();
            ref.close();
        }
    }

    private InstallableUnit getIU(String id) {
        @SuppressWarnings("unchecked")
        Iterator<InstallableUnit> it = pcr.getInitialState().iterator();
        while (it.hasNext()) {
            InstallableUnit iu = it.next();
            if (id.equals(iu.getId()))
                return iu;
        }
        fail("Can't find: " + id);
        return null;
    }

    @SuppressWarnings("unused")
    private void assertNotRequirement(IRequiredCapability asserted,
            IRequiredCapability[] reqs) {
        for (int i = 0; i < reqs.length; i++) {
            if (asserted.getName().equals(reqs[i].getName())) {
                if (asserted.getRange().equals(reqs[i].getRange())
                        && asserted.getArity() == reqs[i].getArity()
                        && asserted.isNegation() == reqs[i].isNegation())
                    fail("Requirement not expected:" + asserted);
            }
        }
    }

    private void assertRequirement(String message,
            IRequiredCapability asserted, IRequiredCapability[] reqs) {
        boolean found = false;
        for (int i = 0; i < reqs.length; i++) {
            if (asserted.getName().equals(reqs[i].getName())) {
                if (asserted.getRange().equals(reqs[i].getRange())
                        && asserted.getArity() == reqs[i].getArity()
                        && asserted.isNegation() == reqs[i].isNegation())
                    found = true;
            }
        }
        assertEquals(message, true, found);
    }

    @SuppressWarnings("unused")
    private void assertProvide(String message, IProvidedCapability asserted,
            IProvidedCapability[] caps) {
        boolean found = true;
        for (int i = 0; i < caps.length; i++) {
            if (asserted.getName().equals(caps[i].getName())) {
                assertEquals(asserted.getVersion(), caps[i].getVersion());
            }
        }
        assertEquals(message, true, found);
    }

    @Test
    public void testP2CUDFParserCheckPackages() {
        InstallableUnit iu = getIU("nuxeo-dm");
        assertRequirement(
                "nuxeo-dm must conflict with nuxeo-cmf in all versions",
                new NotRequirement(new RequiredCapability("nuxeo-cmf",
                        VersionRange.emptyRange)), iu.getRequiredCapabilities());
        assertEquals("Wrong nuxeo-dm version", "1", iu.getVersion().toString());
        assertFalse("nuxeo-dm is not installed", iu.isInstalled());

        iu = getIU("nuxeo-social-collaboration");
        assertRequirement(
                "nuxeo-social-collaboration must depend on nuxeo-dm 1",
                new RequiredCapability("nuxeo-dm", new VersionRange(
                        new org.eclipse.equinox.p2.cudf.metadata.Version(1))),
                iu.getRequiredCapabilities());
        assertEquals("Wrong nuxeo-social-collaboration version", "1",
                iu.getVersion().toString());
        assertFalse("nuxeo-social-collaboration is not installed",
                iu.isInstalled());

        iu = getIU("nuxeo-cmf");
        assertRequirement(
                "nuxeo-cmf must conflict with nuxeo-dm in all versions",
                new NotRequirement(new RequiredCapability("nuxeo-dm",
                        VersionRange.emptyRange)), iu.getRequiredCapabilities());
        assertEquals("Wrong nuxeo-cmf version", "1", iu.getVersion().toString());
        assertTrue("nuxeo-cmf is installed", iu.isInstalled());
    }

    @Test
    public void testP2CUDFParserCheckRequest() {
        @SuppressWarnings("unchecked")
        ArrayList<IRequiredCapability> requests = pcr.getAllRequests();
        assertNotNull(requests);
        assertTrue(requests.size() > 0);
        SolverConfiguration configuration = new SolverConfiguration(
                SolverConfiguration.OBJ_PARANOID);
        Object result = new SimplePlanner().getSolutionFor(pcr, configuration);
        assertEquals(
                "[nuxeo-addon 1, nuxeo-dm 1, nuxeo-social-collaboration 1, nuxeo-independent-addon 1]",
                result.toString());

        configuration = new SolverConfiguration(
                SolverConfiguration.OBJ_PARANOID);
        result = new SimplePlanner().getSolutionFor(pcr2, configuration);
        assertEquals("[nuxeo-cmf 1, nuxeo-independent-addon 1]",
                result.toString());

        configuration = new SolverConfiguration(
                SolverConfiguration.OBJ_PARANOID);
        result = new SimplePlanner().getSolutionFor(pcrRemove, configuration);
        assertEquals("[nuxeo-independent-addon 1]", result.toString());
    }
}
