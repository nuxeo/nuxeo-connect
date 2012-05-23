/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.connect.tests;

import java.util.List;

import junit.framework.TestCase;

import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.connector.ConnectConnector;
import org.nuxeo.connect.connector.fake.UnregistredFakeConnector;
import org.nuxeo.connect.connector.http.ConnectHttpConnector;
import org.nuxeo.connect.connector.service.ConnectGatewayComponent;
import org.nuxeo.connect.connector.test.ConnectTestConnector;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.downloads.ConnectDownloadManager;
import org.nuxeo.connect.identity.LogicalInstanceIdentifier;
import org.nuxeo.connect.packages.PackageManager;
import org.nuxeo.connect.registration.ConnectRegistrationService;

public class TestService extends TestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("org.nuxeo.connect.client.testMode", "true");

        LogicalInstanceIdentifier.cleanUp();
        NuxeoConnectClient.resetPackageManager();
    }

    public void testServiceLookup() {
        ConnectRegistrationService crs = NuxeoConnectClient.getConnectRegistrationService();
        assertNotNull(crs);

        ConnectConnector connector = NuxeoConnectClient.getConnectConnector();
        assertNotNull(connector);
    }

    public void testServiceAsUnregistered() {
        ConnectRegistrationService crs = NuxeoConnectClient.getConnectRegistrationService();

        assertFalse(crs.isInstanceRegistred());

        ConnectConnector connector = NuxeoConnectClient.getConnectConnector();

        assertTrue(connector instanceof UnregistredFakeConnector);
    }

    public void testServiceAsRegistred() throws Exception {
        ConnectRegistrationService crs = NuxeoConnectClient.getConnectRegistrationService();

        assertFalse(crs.isInstanceRegistred());

        crs.localRegisterInstance("toto--titi", "my test server");

        assertTrue(crs.isInstanceRegistred());

        ConnectConnector connector = NuxeoConnectClient.getConnectConnector();

        assertTrue(connector instanceof ConnectHttpConnector);
    }

    public void testServiceAsTest() throws Exception {
        ConnectRegistrationService crs = NuxeoConnectClient.getConnectRegistrationService();

        assertFalse(crs.isInstanceRegistred());

        ConnectGatewayComponent.testConnector = new ConnectTestConnector();

        ConnectConnector connector = NuxeoConnectClient.getConnectConnector();

        assertTrue(connector instanceof ConnectTestConnector);
    }

    public void testDownloadService() throws Exception {

        ConnectDownloadManager cdm = NuxeoConnectClient.getDownloadManager();
        assertNotNull(cdm);

    }

    public void testPackageManagerSimple() throws Exception {

        PackageManager pm = NuxeoConnectClient.getPackageManager();
        assertNotNull(pm);

        ConnectRegistrationService crs = NuxeoConnectClient.getConnectRegistrationService();

        crs.localRegisterInstance("toto--titi", "my test server");

        ConnectGatewayComponent.testConnector = new ConnectTestConnector();
        ConnectGatewayComponent.testConnector.flushCache();

        List<DownloadablePackage> pkgs = pm.listPackages();

        assertEquals(3, pkgs.size());

    }

}
