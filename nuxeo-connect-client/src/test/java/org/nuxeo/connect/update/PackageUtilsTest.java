package org.nuxeo.connect.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class PackageUtilsTest {

    @Test
    public void testPackageNames() {
        List<String> packageIds1 = new ArrayList<>();
        packageIds1.add("jcarsique-SANDBOX-0.0.2");
        packageIds1.add("jcarsique-SANDBOX-5.0.1-BETA");
        packageIds1.add("jcarsique-SANDBOX-5.0.1-I20121225");
        packageIds1.add("jcarsique-SANDBOX-5.0.1-I20130101");
        packageIds1.add("jcarsique-SANDBOX-5.0.1-beta");
        packageIds1.add("jcarsique-SANDBOX-5.0.1-SNAPSHOT");
        packageIds1.add("jcarsique-SANDBOX-5.0.1");
        packageIds1.add("jcarsique-SANDBOX-5.0.1-CMF-SNAPSHOT");
        packageIds1.add("jcarsique-SANDBOX-5.0.1-CMF");
        packageIds1.add("jcarsique-SANDBOX-5.0.1-anything");
        packageIds1.add("jcarsique-SANDBOX-5.0.1-something");
        List<String> packageIds2 = new ArrayList<>();
        packageIds2.add("FooBar-0.0.0-SNAPSHOT");
        packageIds2.add("FooBar-0.0.2-afterRename");
        packageIds2.add("FooBar-0.0.3-afterResetAll");

        for (String packageId : packageIds1) {
            assertEquals("jcarsique-SANDBOX", PackageUtils.getPackageName(packageId));
        }
        for (String packageId : packageIds2) {
            assertEquals("FooBar", PackageUtils.getPackageName(packageId));
        }
        assertFalse(PackageUtils.isValidPackageId("somePackageName"));
        assertEquals("somePackageName", PackageUtils.getPackageName("somePackageName-3.2.10"));
        assertEquals("3.2.10", PackageUtils.getPackageVersion("somePackageName-3.2.10"));
    }
}
