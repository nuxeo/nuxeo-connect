package org.nuxeo.connect.pm.tests;

import java.util.List;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.dependencies.DependencyResolution;

public class TestExclusions  extends AbstractPackageManagerTestCase {

    protected DummyPackageSource source;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("localExclusion.json");
        List<DownloadablePackage> remote = getDownloads("remoteExclusion.json");

        assertNotNull(local);
        assertTrue(local.size()>0);
        assertNotNull(remote);
        assertTrue(remote.size()>0);

        source = new DummyPackageSource(local, true);
        pm.registerSource(source, true);
        pm.registerSource(new DummyPackageSource(remote, false), false);
    }


    public void testResolutionOrder() throws Exception {

        // verify that CMF installation triggers DM uninstall
        DependencyResolution depResolution = pm.resolveDependencies("nuxeo-cmf-5.5.0", null);
        log.info("Dependency resolution : " + depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1,depResolution.getLocalPackagesToRemove().size());
        assertTrue(depResolution.getLocalPackagesToRemove().containsKey("nuxeo-dm"));
        assertEquals(1,depResolution.getLocalPackagesToUpgrade().size());
        assertTrue(depResolution.getLocalPackagesToUpgrade().containsKey("nuxeo-content-browser"));

        // Fake installation
        List<DownloadablePackage> local2 = getDownloads("localExclusion2.json");
        source.reset(local2);

        // check reverse install : installing DM removes CMF
        depResolution = pm.resolveDependencies("nuxeo-dm-5.5.0", null);
        log.info("Dependency resolution : " + depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1,depResolution.getLocalPackagesToRemove().size());
        assertTrue(depResolution.getLocalPackagesToRemove().containsKey("nuxeo-cmf"));
        assertEquals(1,depResolution.getLocalPackagesToUpgrade().size());
        assertTrue(depResolution.getLocalPackagesToUpgrade().containsKey("nuxeo-content-browser"));

    }

}
