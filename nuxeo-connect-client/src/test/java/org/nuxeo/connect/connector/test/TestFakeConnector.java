/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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
package org.nuxeo.connect.connector.test;

import java.util.List;

import junit.framework.TestCase;

import org.nuxeo.connect.connector.ConnectConnector;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.data.SubscriptionStatus;
import org.nuxeo.connect.identity.LogicalInstanceIdentifier;
import org.nuxeo.connect.update.PackageType;

public class TestFakeConnector extends TestCase {

    public void testFakeConnector() throws Exception {

        LogicalInstanceIdentifier CLID = new LogicalInstanceIdentifier(
                "toto--titi", "myInstance");
        CLID.save();

        ConnectConnector connector = new ConnectTestConnector();
        connector.flushCache();

        SubscriptionStatus status = connector.getConnectStatus();
        assertNotNull(status);
        assertEquals("active", status.getContractStatus());

        List<DownloadablePackage> packages = connector.getDownloads("fake",
                PackageType.HOT_FIX);
        assertNotNull(packages);
        assertEquals(2, packages.size());
        assertEquals("hot fix 1", packages.get(0).getTitle());
        assertEquals(PackageType.HOT_FIX, packages.get(0).getType());

        packages = connector.getDownloads("fake", PackageType.STUDIO);
        assertNotNull(packages);
        assertEquals(1, packages.size());
        assertEquals("my project", packages.get(0).getTitle());
        assertEquals(PackageType.STUDIO, packages.get(0).getType());
    }
}
