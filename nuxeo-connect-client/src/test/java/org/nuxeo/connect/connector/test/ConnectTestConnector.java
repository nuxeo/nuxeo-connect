/*
 * (C) Copyright 2006-2009 Nuxeo SA (http://nuxeo.com/) and others.
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

import org.nuxeo.connect.connector.fake.AbstractFakeConnector;
import org.nuxeo.connect.update.PackageType;

public class ConnectTestConnector extends AbstractFakeConnector {

    @Override
    protected String getJSONDataForStatus() {
        return "{ contractStatus : 'active', endDate : '10/12/2010'}";
    }

    @Override
    protected String getJSONDataForDownloads(String type) {
        String data = null;
        if (PackageType.HOT_FIX.getValue().equals(type)) {
            data = "[ ";
            data += "{id : 'hotfix1-5.3.1', name : 'hotfix1',  title : 'hot fix 1', description : 'this is hot fix 1', version : '5.3.1', type:'"
                    + type + "'}";
            data += ", ";
            data += "{id : 'hotfix2-5.3.1', name : 'hotfix2', title : 'hot fix 2', description : 'this is hot fix 2', version : '5.3.1', type:'"
                    + type + "'}";
            data += "] ";
        } else if (PackageType.STUDIO.getValue().equals(type)) {
            data = "[ ";
            data += "{id : 'myproject-5.3.1', name : 'myproject', title : 'my project', description : 'this is myproject', version : '5.3.1', type:'"
                    + type + "'}";
            data += "] ";
        } else {
            data = "[]";
        }
        return data;
    }

    @Override
    protected String getJSONDataForDownload(String pkgId) {
        throw new UnsupportedOperationException();
    }

}
