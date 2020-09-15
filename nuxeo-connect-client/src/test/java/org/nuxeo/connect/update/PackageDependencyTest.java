/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Florent Guillaume
 */
package org.nuxeo.connect.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PackageDependencyTest {

    @Test
    public void testGetters() {
        PackageDependency pd;
        PackageVersionRange vr;

        pd = new PackageDependency("foo");
        vr = pd.getVersionRange();
        assertEquals("foo", pd.toString());
        assertEquals("foo", pd.getName());
        assertEquals(PackageVersionRange.ANY, vr);

        pd = new PackageDependency("foo:1.2");
        vr = pd.getVersionRange();
        assertEquals("foo:1.2.0", pd.toString());
        assertEquals("foo", pd.getName());
        assertEquals(new Version("1.2.0"), vr.getMinVersion());
        assertNull(vr.getMaxVersion());

        pd = new PackageDependency("foo:1.2:3.4.5");
        vr = pd.getVersionRange();
        assertEquals("foo:1.2.0:3.4.5", pd.toString());
        assertEquals("foo", pd.getName());
        assertEquals(new Version("1.2.0"), vr.getMinVersion());
        assertEquals(new Version("3.4.5"), vr.getMaxVersion());
    }

    @Test
    public void testEqualsAndHashCode() {
        PackageDependency pd1 = new PackageDependency("foo:1.2:3.4.5");
        PackageDependency pd2 = new PackageDependency("foo:1.2:3.4.5");
        PackageDependency pd3 = new PackageDependency("foo:1.2");
        assertTrue(pd1.equals(pd2));
        assertFalse(pd1.equals(pd3));
        assertFalse(pd2.equals(pd3));
        assertTrue(pd1.hashCode() == pd2.hashCode());
        // don't test hashCode difference, they could be equal by chance
    }

}
