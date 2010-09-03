/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 */

package org.nuxeo.connect.pm.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.data.PackageDescriptor;
import org.nuxeo.connect.identity.LogicalInstanceIdentifier;
import org.nuxeo.connect.packages.PackageManager;
import org.nuxeo.connect.packages.PackageManagerImpl;

public class TestPackageManager extends TestCase {

    protected PackageManager pm;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        System.setProperty("org.nuxeo.connect.client.testMode", "true");
        LogicalInstanceIdentifier.cleanUp();

        pm = NuxeoConnectClient.getPackageManager();

        assertNotNull(pm);

        ((PackageManagerImpl)pm).resetSources();

    }

    public static final String TEST_DATA = "test-data/";

    protected static List<String> readLines(InputStream in) throws IOException {
        List<String> lines = new ArrayList<String>();
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

    protected static List<DownloadablePackage> getDownloads(String filename) throws Exception {

        List<DownloadablePackage> result = new ArrayList<DownloadablePackage>();

        InputStream is = TestPackageManager.class.getClassLoader().getResourceAsStream(TEST_DATA + filename);

        List<String> lines = readLines(is);

        for (String data : lines) {
            PackageDescriptor pkg = PackageDescriptor.loadFromJSON(data);
            result.add(pkg);
        }
        return result;
    }

    protected void dumpPkgList(String label,List<DownloadablePackage> pkgs) {
        StringBuffer sb = new StringBuffer();

        sb.append(label);
        sb.append("=[");

        for (DownloadablePackage pkg : pkgs) {
            sb.append(pkg.getId());
            sb.append(" (");
            sb.append(pkg.getName());
            sb.append("  ");
            sb.append(pkg.getVersion().toString());
            sb.append(") ");
            sb.append(" [");
            sb.append(pkg.getState());
            sb.append("] ,");
        }

        sb.append("]");

        System.out.println(sb.toString());
    }

    public void testPM() throws Exception {

        List<DownloadablePackage> local = getDownloads("local1.json");
        List<DownloadablePackage> remote = getDownloads("remote1.json");

        assertNotNull(local);
        assertTrue(local.size()>0);
        assertNotNull(remote);
        assertTrue(remote.size()>0);

        pm.registerSource(new DummyPackageSource(local, true), true);
        pm.registerSource(new DummyPackageSource(remote, false), false);

        List<DownloadablePackage> remotes = pm.listRemotePackages();
        dumpPkgList("remote", remotes);
        assertEquals(4, remotes.size());

        List<DownloadablePackage> locals = pm.listLocalPackages();
        dumpPkgList("local", locals);
        assertEquals(2, locals.size());

        List<DownloadablePackage> all = pm.listPackages();
        dumpPkgList("all", all);
        assertEquals(5, all.size());

        List<DownloadablePackage> updates = pm.listUpdatePackages();
        dumpPkgList("update", updates);
        assertEquals(2, updates.size());

        List<DownloadablePackage> remoteOnly = pm.listOnlyRemotePackages();
        dumpPkgList("remoteOnly", remoteOnly);
        assertEquals(3, remoteOnly.size());

        List<DownloadablePackage> studioOnly = pm.listAllStudioRemotePackages();
        dumpPkgList("studioOnly", studioOnly);
        assertEquals(2, studioOnly.size());

    }

    public void testPMLocalOverride() throws Exception {

        List<DownloadablePackage> local = getDownloads("local2.json");
        List<DownloadablePackage> remote = getDownloads("remote2.json");

        assertNotNull(local);
        assertTrue(local.size()>0);
        assertNotNull(remote);
        assertTrue(remote.size()>0);

        pm.registerSource(new DummyPackageSource(local, true), true);
        pm.registerSource(new DummyPackageSource(remote, false), false);

        List<DownloadablePackage> remotes = pm.listRemotePackages();
        dumpPkgList("remote", remotes);
        assertEquals(2, remotes.size());

        List<DownloadablePackage> locals = pm.listLocalPackages();
        dumpPkgList("local", locals);
        assertEquals(2, locals.size());

        List<DownloadablePackage> all = pm.listPackages();
        dumpPkgList("all", all);
        assertEquals(3, all.size());

        DownloadablePackage downloading = all.get(0);
        assertEquals(2, downloading.getState());

        List<DownloadablePackage> updates = pm.listUpdatePackages();
        dumpPkgList("update", updates);
        assertEquals(0, updates.size());

        List<DownloadablePackage> remoteOnly = pm.listOnlyRemotePackages();
        dumpPkgList("remoteOnly", remoteOnly);
        assertEquals(1, remoteOnly.size());

        List<DownloadablePackage> remoteOrLocal = pm.listRemoteOrLocalPackages();
        dumpPkgList("remoteOrLocal", remoteOrLocal);
        assertEquals(2, remoteOrLocal.size());
        downloading = remoteOrLocal.get(0);
        assertEquals(2, downloading.getState());

    }

}
