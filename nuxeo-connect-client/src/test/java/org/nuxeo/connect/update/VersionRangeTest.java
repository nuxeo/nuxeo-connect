/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Julien Carsique
 *
 */

package org.nuxeo.connect.update;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @since 1.4.4
 */
public class VersionRangeTest {

    /**
     * Test method for
     * {@link org.nuxeo.connect.update.VersionRange#matchVersion(org.nuxeo.connect.update.Version)}
     * .
     */
    @Test
    public void testMatchVersion() {
        Version v4 = new Version("4");
        Version v5 = new Version("5");
        Version v7 = new Version("7");
        Version v10 = new Version("10");
        Version v12 = new Version("12");
        VersionRange vr5To10 = new VersionRange(v5, v10);
        VersionRange vr5Min = new VersionRange(v5);
        VersionRange vr10Max = new VersionRange(null, v10);
        // [5,10] versus 4, 5, 7, 10, 12
        assertFalse(vr5To10 + " must not match " + v4, vr5To10.matchVersion(v4));
        assertTrue(vr5To10 + " must match " + v5, vr5To10.matchVersion(v5));
        assertTrue(vr5To10 + " must match " + v7, vr5To10.matchVersion(v7));
        assertTrue(vr5To10 + " must match " + v10, vr5To10.matchVersion(v10));
        assertFalse(vr5To10 + " must not match " + v12,
                vr5To10.matchVersion(v12));
        // [5,) versus 4, 5, 7, 10, 12
        assertFalse(vr5Min + " must not match " + v4, vr5Min.matchVersion(v4));
        assertTrue(vr5Min + " must match " + v5, vr5Min.matchVersion(v5));
        assertTrue(vr5Min + " must match " + v7, vr5Min.matchVersion(v7));
        assertTrue(vr5Min + " must match " + v10, vr5Min.matchVersion(v10));
        assertTrue(vr5Min + " must not match " + v12, vr5Min.matchVersion(v12));
        // (,10] versus 4, 5, 7, 10, 12
        assertTrue(vr10Max + " must not match " + v4, vr10Max.matchVersion(v4));
        assertTrue(vr10Max + " must match " + v5, vr10Max.matchVersion(v5));
        assertTrue(vr10Max + " must match " + v7, vr10Max.matchVersion(v7));
        assertTrue(vr10Max + " must match " + v10, vr10Max.matchVersion(v10));
        assertFalse(vr10Max + " must not match " + v12,
                vr10Max.matchVersion(v12));
    }

    /**
     * Test method for
     * {@link org.nuxeo.connect.update.VersionRange#matchVersionRange(org.nuxeo.connect.update.VersionRange)}
     * .
     */
    @Test
    public void testMatchVersionRange() {
        VersionRange vr5To10 = new VersionRange("5:10");
        VersionRange vr15To20 = new VersionRange("15:20");
        VersionRange vr7To25 = new VersionRange("7:25");
        VersionRange vr12Min = new VersionRange("12");
        VersionRange vr12Max = new VersionRange(null, new Version("12"));
        // [5,10] versus [5,10], [15,20], [7,25], [12,), (,12]
        assertTrue(vr5To10 + " must match " + vr5To10,
                vr5To10.matchVersionRange(vr5To10));
        assertFalse(vr5To10 + " must not match " + vr15To20,
                vr5To10.matchVersionRange(vr15To20));
        assertFalse(vr15To20 + " must not match " + vr5To10,
                vr15To20.matchVersionRange(vr5To10));
        assertTrue(vr5To10 + " must match " + vr7To25,
                vr5To10.matchVersionRange(vr7To25));
        assertFalse(vr5To10 + " must not match " + vr12Min,
                vr5To10.matchVersionRange(vr12Min));
        assertTrue(vr5To10 + " must match " + vr12Max,
                vr5To10.matchVersionRange(vr12Max));
        // [7,25] versus [5,10], [15,20], [7,25], [12,), (,12]
        assertTrue(vr7To25 + " must match " + vr5To10,
                vr7To25.matchVersionRange(vr5To10));
        assertTrue(vr7To25 + " must match " + vr15To20,
                vr7To25.matchVersionRange(vr15To20));
        assertTrue(vr7To25 + " must match " + vr7To25,
                vr7To25.matchVersionRange(vr7To25));
        assertTrue(vr7To25 + " must match " + vr12Min,
                vr7To25.matchVersionRange(vr12Min));
        assertTrue(vr7To25 + " must match " + vr12Max,
                vr7To25.matchVersionRange(vr12Max));
        // [12,) versus (,12] and themselves
        assertTrue(vr12Min + " must match " + vr12Max,
                vr12Min.matchVersionRange(vr12Max));
        assertTrue(vr12Min + " must match " + vr12Min,
                vr12Min.matchVersionRange(vr12Min));
        assertTrue(vr12Max + " must match " + vr12Min,
                vr12Max.matchVersionRange(vr12Min));
        assertTrue(vr12Max + " must match " + vr12Max,
                vr12Max.matchVersionRange(vr12Max));
    }

}
