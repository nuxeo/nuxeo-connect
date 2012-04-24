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
import org.json.JSONObject;
import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.data.PackageDescriptor;
import org.nuxeo.connect.identity.LogicalInstanceIdentifier;
import org.nuxeo.connect.packages.PackageManager;
import org.nuxeo.connect.packages.PackageManagerImpl;

public abstract class AbstractPackageManagerTestCase extends TestCase {

    protected PackageManager pm;

    protected static Log log = LogFactory.getLog(TestPackageManager.class);

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

    @Override
    public void setUp() throws Exception {
        super.setUp();

        System.setProperty("org.nuxeo.connect.client.testMode", "true");
        LogicalInstanceIdentifier.cleanUp();

        pm = NuxeoConnectClient.getPackageManager();

        assertNotNull(pm);

        ((PackageManagerImpl) pm).resetSources();

    }

    protected static List<DownloadablePackage> getDownloads(String filename)
            throws Exception {

        List<DownloadablePackage> result = new ArrayList<DownloadablePackage>();

        InputStream is = TestPackageManager.class.getClassLoader().getResourceAsStream(
                TEST_DATA + filename);

        List<String> lines = readLines(is);

        for (String data : lines) {
            PackageDescriptor pkg = PackageDescriptor.loadFromJSON(PackageDescriptor.class, new JSONObject(data));
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

}
