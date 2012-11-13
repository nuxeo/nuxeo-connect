/*
 * (C) Copyright 2010-2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.nuxeo.connect.update.Version;

public class TestVersions extends TestCase {

    public void testVersions() {
        assertEquals(-1,
                new Version("5.2.1-RC1").compareTo(new Version("5.2.1")));

        List<Version> versions = new ArrayList<Version>();
        versions.add(new Version("5"));
        versions.add(new Version("5.0"));
        versions.add(new Version("5.0.0"));
        versions.add(new Version("5.0.1"));
        versions.add(new Version("5.2.1"));
        versions.add(new Version("5.2.1-SNAPSHOT"));
        versions.add(new Version("5.2.1-RC1"));
        versions.add(new Version("5.2.1-RC2"));
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
        assertEquals(new Version("5.2.1-RC1"), versions.get(7));
        assertEquals(new Version("5.2.1-RC2"), versions.get(8));
        assertEquals(new Version("5.2.1-SNAPSHOT"), versions.get(9));
        assertEquals(new Version("5.2.1"), versions.get(10));

    }
}
