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

package org.nuxeo.connect.connector;

/**
 * Misc const used in the protocol for communicating with Nuxeo Connect Server.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class ProtocolConst {

    public static final String CTID_HEADER = "X-NUXEO-CONNECT-CTID";

    public static final String CLID_HEADER = "X-NUXEO-CONNECT-CLID";

    public static final String TS_HEADER = "X-NUXEO-CONNECT-TS";

    public static final String DIGEST_HEADER = "X-NUXEO-CONNECT-DIGEST";

    public static final String VERSION_HEADER = "X-NUXEO-CONNECT-CLIENT-VERSION";

    public static final String DIGEST_METHOD_HEADER = "X-NUXEO-CONNECT-DIGEST-METHOD";
}
