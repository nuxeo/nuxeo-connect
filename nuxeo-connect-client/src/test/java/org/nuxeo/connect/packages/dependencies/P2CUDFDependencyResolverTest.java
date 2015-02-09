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

package org.nuxeo.connect.packages.dependencies;

//import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.pm.tests.AbstractPackageManagerTestCase;
import org.nuxeo.connect.pm.tests.DummyPackageSource;

/**
 * @since 1.4
 */
public class P2CUDFDependencyResolverTest extends AbstractPackageManagerTestCase {

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("local3.json");
        List<DownloadablePackage> remote = getDownloads("remote3.json");
        DummyPackageSource source = new DummyPackageSource(local, true);
        pm.registerSource(source, true);
        pm.registerSource(new DummyPackageSource(remote, false), false);
    }

    @Test
    public void testResolve() throws Exception {
        DependencyResolution resolution = pm.resolveDependencies("nuxeo-dm-5.5.0", "5.5.0");
        assertFalse(resolution.toString(), resolution.isFailed());
        assertEquals(2, resolution.getRemovePackageIds().size());
        assertEquals(Arrays.asList(new String[] { "nuxeo-cmf-5.5.0", "nuxeo-content-browser-1.1.0-cmf" }),
                resolution.getRemovePackageIds());
        assertEquals(2, resolution.getOrderedPackageIdsToInstall().size());
        assertEquals(Arrays.asList(new String[] { "nuxeo-content-browser-1.1.0", "nuxeo-dm-5.5.0" }),
                resolution.getOrderedPackageIdsToInstall());
    }

}
