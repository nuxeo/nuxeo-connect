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

import junit.framework.TestCase;

import org.nuxeo.connect.identity.LogicalInstanceIdentifier;
import org.nuxeo.connect.identity.TechnicalInstanceIdentifier;

public class TestIDGens extends TestCase {

    public void testCTIDGen() throws Exception {

        TechnicalInstanceIdentifier ctid = new TechnicalInstanceIdentifier();

        String ctId1 = ctid.getCTID();

        TechnicalInstanceIdentifier ctid2 = new TechnicalInstanceIdentifier();

        String ctId2 = ctid2.getCTID();

        System.out.print(ctId1);

        assertEquals(ctId1, ctId2);

    }

    protected void dotestCLID() throws Exception {
        LogicalInstanceIdentifier CLID = new LogicalInstanceIdentifier("toto--titi", "myInstance");

        CLID.save();

        LogicalInstanceIdentifier CLID2 = LogicalInstanceIdentifier.load();

        assertNotNull(CLID2);

        assertEquals(CLID.getCLID1(), CLID2.getCLID1());
        assertEquals(CLID.getCLID2(), CLID2.getCLID2());
        assertEquals(CLID.getInstanceDescription(), CLID2.getInstanceDescription());
    }

    public void testCLID() throws Exception {

        LogicalInstanceIdentifier.USE_BASE64_SAVE=false;
        dotestCLID();

        LogicalInstanceIdentifier.USE_BASE64_SAVE=true;
        dotestCLID();
    }
}
