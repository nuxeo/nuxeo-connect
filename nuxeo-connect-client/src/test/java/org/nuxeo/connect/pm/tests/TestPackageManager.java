/*
 * (C) Copyright 2010-2015 Nuxeo SA (http://nuxeo.com/) and contributors.
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

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.update.PackageState;

public class TestPackageManager extends AbstractPackageManagerTestCase {

    public void testPM() throws Exception {
        List<DownloadablePackage> local = getDownloads("local1.json");
        List<DownloadablePackage> remote = getDownloads("remote1.json");
        assertTrue(CollectionUtils.isNotEmpty(local));
        assertTrue(CollectionUtils.isNotEmpty(remote));
        pm.registerSource(new DummyPackageSource(local, "local1"), true);
        pm.registerSource(new DummyPackageSource(remote, "remote1"), false);

        List<DownloadablePackage> remotes = pm.listRemotePackages();
        dumpPkgList("remote", remotes);
        assertEquals(5, remotes.size());

        List<DownloadablePackage> locals = pm.listLocalPackages();
        dumpPkgList("local", locals);
        assertEquals(2, locals.size());

        List<DownloadablePackage> all = pm.listPackages();
        dumpPkgList("all", all);
        assertEquals(7, all.size());

        List<DownloadablePackage> updates = pm.listUpdatePackages();
        dumpPkgList("update", updates);
        assertEquals(1, updates.size());

        List<DownloadablePackage> remoteOnly = pm.listOnlyRemotePackages(null, null);
        dumpPkgList("remoteOnly", remoteOnly);
        assertEquals(4, remoteOnly.size());

        List<DownloadablePackage> studioOnly = pm.listRemoteAssociatedStudioPackages();
        dumpPkgList("studioOnly", studioOnly);
        assertEquals(2, studioOnly.size());
    }

    public void testPMLocalOverride() throws Exception {
        List<DownloadablePackage> local = getDownloads("local2.json");
        List<DownloadablePackage> remote = getDownloads("remote2.json");
        assertTrue(CollectionUtils.isNotEmpty(local));
        assertTrue(CollectionUtils.isNotEmpty(remote));
        pm.registerSource(new DummyPackageSource(local, "local2"), true);
        pm.registerSource(new DummyPackageSource(remote, "remote2"), false);

        List<DownloadablePackage> remotes = pm.listRemotePackages();
        dumpPkgList("remote", remotes);
        assertEquals(3, remotes.size());

        List<DownloadablePackage> locals = pm.listLocalPackages();
        dumpPkgList("local", locals);
        assertEquals(3, locals.size());

        List<DownloadablePackage> all = pm.listPackages();
        dumpPkgList("all", all);
        assertEquals(4, all.size());

        DownloadablePackage downloading = all.get(1);
        assertEquals(PackageState.DOWNLOADING, downloading.getPackageState());

        List<DownloadablePackage> updates = pm.listUpdatePackages();
        dumpPkgList("update", updates);
        assertEquals(0, updates.size());

        List<DownloadablePackage> remoteOnly = pm.listOnlyRemotePackages(null, null);
        dumpPkgList("remoteOnly", remoteOnly);
        assertEquals(1, remoteOnly.size());

        List<DownloadablePackage> remoteOrLocal = pm.listRemoteOrLocalPackages(null, null);
        dumpPkgList("remoteOrLocal", remoteOrLocal);
        assertEquals(3, remoteOrLocal.size());
        downloading = remoteOrLocal.get(1);
        assertEquals(PackageState.INSTALLED, downloading.getPackageState());
    }

    public void testUpdateListing() throws Exception {
        List<DownloadablePackage> local = getDownloads("localhf1.json");
        List<DownloadablePackage> remote = getDownloads("remotehf1.json");
        assertTrue(CollectionUtils.isNotEmpty(local));
        assertTrue(CollectionUtils.isNotEmpty(remote));
        pm.registerSource(new DummyPackageSource(local, "localhf1"), true);
        pm.registerSource(new DummyPackageSource(remote, "remotehf1"), false);

        List<DownloadablePackage> remotes = pm.listRemotePackages();
        dumpPkgList("remote", remotes);
        assertEquals(4, remotes.size());

        List<DownloadablePackage> locals = pm.listLocalPackages();
        dumpPkgList("local", locals);
        assertEquals(3, locals.size());

        List<DownloadablePackage> updates = pm.listUpdatePackages();
        dumpPkgList("updates", updates);
        assertEquals(3, updates.size());

        // check that none of them is already installed
        for (DownloadablePackage update : updates) {
            assertFalse((update.getPackageState() == PackageState.INSTALLING)
                    || (update.getPackageState() == PackageState.INSTALLED)
                    || (update.getPackageState() == PackageState.STARTED));
        }
    }
}
