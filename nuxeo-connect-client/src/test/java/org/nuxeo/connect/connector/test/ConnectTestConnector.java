/*
 * (C) Copyright 2006-2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Nuxeo
 *     Yannis JULIENNE
 *
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
