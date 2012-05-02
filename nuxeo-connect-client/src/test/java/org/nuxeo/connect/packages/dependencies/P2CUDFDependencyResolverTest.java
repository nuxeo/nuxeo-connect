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

import org.junit.Before;
import org.junit.Test;

/**
 * @since 5.6
 */
public class P2CUDFDependencyResolverTest {

    private final class P2CUDFDependencyResolverExtension extends
            P2CUDFDependencyResolver {
        public P2CUDFDependencyResolverExtension(CUDFHelper cudfHelper) {
            this.cudfHelper = cudfHelper;
        }

    }

    private CUDFHelper cudfHelper;

    private P2CUDFDependencyResolver p2cudfDependencyResolver;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        cudfHelper = CUDFHelperTest.getCUDFTestHelper();
        p2cudfDependencyResolver = new P2CUDFDependencyResolverExtension(
                cudfHelper);
    }

    @Test
    public void testResolve() throws Exception {
        p2cudfDependencyResolver.resolve("nuxeo-dm", null);
    }

}
