package org.nuxeo.connect.pm.tests;

import java.util.List;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.dependencies.DependencyResolution;
import org.nuxeo.connect.update.Version;

public class TestPackageDependencyResolution extends AbstractPackageManagerTestCase {

    protected DependencyResolution depResolution;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("localdep1.json");
        List<DownloadablePackage> remote = getDownloads("remotedep1.json");

        assertNotNull(local);
        assertTrue(local.size()>0);
        assertNotNull(remote);
        assertTrue(remote.size()>0);

        pm.registerSource(new DummyPackageSource(local, true), true);
        pm.registerSource(new DummyPackageSource(remote, false), false);
    }

    public void testSimpleDeps() throws Exception {
        // test simple dependency : 1 local package to install and one to download
        depResolution = pm.resolveDependencies("C-1.0.0", null);
        log.info("Dependency resolution : " + depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(0, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getNewPackagesToDownload().size());
    }

    public void testSimpleUpgrade() throws Exception {
        // check simple upgrade
        depResolution = pm.resolveDependencies("E-1.0.0", null);
        log.info("Dependency resolution : " + depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());
    }

    public void testDoubleDownload() throws Exception {
        // check double download
        depResolution = pm.resolveDependencies("F-1.0.0", null);
        log.info("Dependency resolution : " + depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(2, depResolution.getNewPackagesToDownload().size());
    }

    public void testLoopDetection() throws Exception {
        // test loop detection
        depResolution = pm.resolveDependencies("G-1.1.0", null);
        log.info("Dependency resolution : " + depResolution.toString());
        assertTrue(depResolution.isFailed());
    }

    public void testMissingDep() throws Exception {
        // test missing dep
        depResolution = pm.resolveDependencies("I-1.0.0", null);
        log.info("Dependency resolution : " + depResolution.toString());
        assertTrue(depResolution.isFailed());
    }

    public void testConflictingDeps() throws Exception {
        // test conflicting dependencies
        depResolution = pm.resolveDependencies("J-1.0.0", null);
        log.info("Dependency resolution : " + depResolution.toString());
        assertTrue(depResolution.isFailed());
    }

    public void test3LevelsDeps() throws Exception {
        // test 3 levels deps
        depResolution = pm.resolveDependencies("O-1.0.0", null);
        log.info("Dependency resolution : " + depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToInstall().size());
        assertEquals(0, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(3, depResolution.getNewPackagesToDownload().size());
    }

    public void testDoubleUpgrade() throws Exception {
        // test double upgrade : direct + implied upgrade
        depResolution = pm.resolveDependencies("CC-1.0.0", null);
        log.info("Dependency resolution : " + depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(2, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(0, depResolution.getNewPackagesToDownload().size());
    }

    public void testForceRemove() throws Exception {
        // test force removal
        depResolution = pm.resolveDependencies("X1-1.0.0", null);
        log.info("Dependency resolution : " + depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(0, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(2, depResolution.getNewPackagesToDownload().size());
        assertEquals(1, depResolution.getLocalPackagesToRemove().size());
    }

    public void testForceUpgradeOverRemove() throws Exception {
        // test that resolution will choose upgrade over removal
        depResolution = pm.resolveDependencies("X2-1.0.0", null);
        log.info("Dependency resolution : " + depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(1, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(2, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
    }

    public void testPlatformFiltering() throws Exception {
        // test that Platform Filtering changes the resolution result

        depResolution = pm.resolveDependencies("PF1-1.0.0", null);
        log.info("Dependency resolution : " + depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(0, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());

        depResolution = pm.resolveDependencies("PF1-1.0.0", "5.3.1");
        log.info("Dependency resolution : " + depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(0, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
        assertEquals(new Version("2.0.0"), depResolution.getNewPackagesToDownload().get("PF2"));

        depResolution = pm.resolveDependencies("PF1-1.0.0", "5.3.0");
        log.info("Dependency resolution : " + depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToInstall().size());
        assertEquals(0, depResolution.getLocalPackagesToUpgrade().size());
        assertEquals(0, depResolution.getLocalUnchangedPackages().size());
        assertEquals(1, depResolution.getNewPackagesToDownload().size());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
        assertEquals(new Version("1.0.0"), depResolution.getNewPackagesToDownload().get("PF2"));

        depResolution = pm.resolveDependencies("PF1-1.0.0", "5.3.2");
        log.info("Dependency resolution : " + depResolution.toString());
        assertTrue(depResolution.isFailed());
    }

}
