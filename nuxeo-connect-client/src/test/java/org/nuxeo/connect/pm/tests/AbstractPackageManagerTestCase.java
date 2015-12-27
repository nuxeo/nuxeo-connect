/*
 * (C) Copyright 2010-2015 Nuxeo SA (http://nuxeo.com/) and others.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import org.nuxeo.connect.DefaultCallbackHolder;
import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.data.AbstractJSONSerializableData;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.data.PackageDescriptor;
import org.nuxeo.connect.identity.LogicalInstanceIdentifier;
import org.nuxeo.connect.packages.PackageManager;
import org.nuxeo.connect.packages.PackageManagerImpl;
import org.nuxeo.connect.update.MockPackageUpdateService;

public abstract class AbstractPackageManagerTestCase extends TestCase {

    protected PackageManager pm;

    protected static Log log = LogFactory.getLog(TestPackageManager.class);

    public static final String TEST_DATA = "test-data/";

    protected static List<String> readLines(InputStream in) throws IOException {
        List<String> lines = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        return lines;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("org.nuxeo.connect.client.testMode", "true");
        LogicalInstanceIdentifier.cleanUp();
        pm = NuxeoConnectClient.getPackageManager();
        assertNotNull(pm);
        ((PackageManagerImpl) pm).resetSources();
        ((DefaultCallbackHolder) NuxeoConnectClient.getCallBackHolder()).setUpdateService(new MockPackageUpdateService(
                pm));

    }

    protected static List<DownloadablePackage> getDownloads(String filename) throws IOException, JSONException {
        return getDownloads(filename, false);
    }

    /**
     * @param filename
     * @param isLocal
     * @throws IOException
     * @throws JSONException
     * @since 1.4.13
     */
    protected static List<DownloadablePackage> getDownloads(String filename, boolean isLocal) throws IOException,
            JSONException {
        List<DownloadablePackage> result = new ArrayList<>();
        InputStream is = TestPackageManager.class.getClassLoader().getResourceAsStream(TEST_DATA + filename);
        List<String> lines = readLines(is);
        for (String data : lines) {
            PackageDescriptor pkg = AbstractJSONSerializableData.loadFromJSON(PackageDescriptor.class, new JSONObject(
                    data));
            if (isLocal) {
                pkg.setLocal(true);
            }
            result.add(pkg);
        }
        return result;
    }

    public AbstractPackageManagerTestCase() {
        super();
    }

    public AbstractPackageManagerTestCase(String name) {
        super(name);
    }

    protected void dumpPkgList(String label, List<DownloadablePackage> pkgs) {
        StringBuffer sb = new StringBuffer();
        sb.append(label);
        sb.append("={");
        for (DownloadablePackage pkg : pkgs) {
            sb.append(pkg.getId());
            sb.append(" (");
            sb.append(pkg.getName());
            sb.append("  ");
            sb.append(pkg.getVersion().toString());
            sb.append(") ");
            sb.append(" [");
            sb.append(pkg.getPackageState());
            sb.append("], ");
        }
        sb.append("}");
        log.info(sb.toString());
    }

}
