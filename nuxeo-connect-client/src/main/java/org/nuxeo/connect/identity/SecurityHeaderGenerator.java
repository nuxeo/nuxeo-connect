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

package org.nuxeo.connect.identity;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.connector.ConnectSecurityError;
import org.nuxeo.connect.connector.ProtocolConst;

/**
 * Helper to generate Security Header when communication with Nuxeo Connect Server.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class SecurityHeaderGenerator {

    public static final String HASH_METHOD = "MD5";

    public static Map<String, String> getHeaders() throws ConnectSecurityError {
        String CLID1;
        String CLID2;
        String CTID;
        String TS;
        String digest;
        Map<String, String> headers = new HashMap<String, String>();

        try {
            CLID1 = LogicalInstanceIdentifier.instance().getCLID1();
            CLID2 = LogicalInstanceIdentifier.instance().getCLID2();
            CTID = TechnicalInstanceIdentifier.instance().getCTID();
            TS = "" + System.currentTimeMillis();

            String toDigest = CLID2 + CTID + TS;
            digest = Base64.encodeBytes(MessageDigest.getInstance(HASH_METHOD)
                    .digest(toDigest.getBytes()));
        } catch (Exception e) {
            throw new ConnectSecurityError(
                    "Unable to construct Security Headers", e);
        }

        headers.put(ProtocolConst.CLID_HEADER, CLID1);
        headers.put(ProtocolConst.CTID_HEADER, CTID);
        headers.put(ProtocolConst.TS_HEADER, TS);
        headers.put(ProtocolConst.DIGEST_HEADER, digest);


        headers.put(ProtocolConst.VERSION_HEADER, NuxeoConnectClient.getVersion());
        headers.put(ProtocolConst.DIGEST_METHOD_HEADER, HASH_METHOD);

        return headers;
    }

}
