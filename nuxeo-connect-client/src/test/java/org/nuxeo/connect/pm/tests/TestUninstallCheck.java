package org.nuxeo.connect.pm.tests;

import java.util.List;

import org.nuxeo.connect.data.DownloadablePackage;

public class TestUninstallCheck extends AbstractPackageManagerTestCase {

    protected DummyPackageSource source;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("localuninstall.json");

        assertNotNull(local);
        assertTrue(local.size() > 0);

        pm.registerSource(new DummyPackageSource(local, false), true);
    }

    public void testUninstallDependencies() throws Exception {

        DownloadablePackage pkg = pm.getPackage("A-1.0.0");
        assertNotNull(pkg);

        List<DownloadablePackage> pkgToRemove = pm.getUninstallDependencies(pkg);

        System.out.println(pkgToRemove);

        assertTrue(pkgToRemove.contains(pm.getPackage("B-1.0.0")));
        assertTrue(pkgToRemove.contains(pm.getPackage("C-1.0.0")));
        assertTrue(pkgToRemove.contains(pm.getPackage("D-1.0.0")));
        assertTrue(pkgToRemove.contains(pm.getPackage("E-1.0.0")));
        assertFalse(pkgToRemove.contains(pm.getPackage("F-1.0.0")));

        assertFalse(pkgToRemove.contains(pm.getPackage("G-1.0.0")));
        assertTrue(pkgToRemove.contains(pm.getPackage("H-1.0.0")));
    }

}
