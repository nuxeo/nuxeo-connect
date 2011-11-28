package org.nuxeo.connect.pm.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nuxeo.connect.update.Version;

import junit.framework.TestCase;

public class TestVersions extends TestCase {

    public void testVersions() {

        List<Version> versions = new ArrayList<Version>();

        versions.add(new Version("5"));
        versions.add(new Version("5.0"));
        versions.add(new Version("5.0.0"));
        versions.add(new Version("5.0.1"));
        versions.add(new Version("5.2.1"));
        versions.add(new Version("5.0.1-SNAPSHOT"));
        versions.add(new Version("5.0.1-CMF"));
        versions.add(new Version("5.0.1-CMF-SNAPSHOT"));

        Collections.shuffle(versions);

        Collections.sort(versions);

        assertEquals(new Version("5"), versions.get(0));
        assertEquals(new Version("5"), versions.get(1));
        assertEquals(new Version("5"), versions.get(2));
        assertEquals(new Version("5.0.1-SNAPSHOT"), versions.get(3));
        assertEquals(new Version("5.0.1"), versions.get(4));
        assertEquals(new Version("5.0.1-CMF-SNAPSHOT"), versions.get(5));
        assertEquals(new Version("5.0.1-CMF"), versions.get(6));
        assertEquals(new Version("5.2.1"), versions.get(7));

    }
}
