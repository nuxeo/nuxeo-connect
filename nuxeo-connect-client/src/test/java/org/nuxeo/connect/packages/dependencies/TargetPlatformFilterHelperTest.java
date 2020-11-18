/*
 * (C) Copyright 2020 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Yannis JULIENNE
 *
 */
package org.nuxeo.connect.packages.dependencies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nuxeo.connect.packages.dependencies.TargetPlatformFilterHelper.getPossibleStringForms;

import org.junit.Test;
import org.nuxeo.connect.platform.PlatformId;
import org.nuxeo.connect.platform.PlatformVersion;

public class TargetPlatformFilterHelperTest {

    @Test
    public void testGetPossibleStringForms() {
        checkPlatformVersionPossibleStringForms("0", "test-0", "test-0.0", "test-0.0.0");
        checkPlatformVersionPossibleStringForms("1", "test-1", "test-1.0", "test-1.0.0");
        checkPlatformVersionPossibleStringForms("1.1", "test-1.1", "test-1.1.0");
        checkPlatformVersionPossibleStringForms("1.1.1", "test-1.1.1");
        checkPlatformVersionPossibleStringForms("0-qualifier", "test-0-qualifier", "test-0.0-qualifier",
                "test-0.0.0-qualifier");
        checkPlatformVersionPossibleStringForms("1-qualifier", "test-1-qualifier", "test-1.0-qualifier",
                "test-1.0.0-qualifier");
        checkPlatformVersionPossibleStringForms("1.1-qualifier", "test-1.1-qualifier", "test-1.1.0-qualifier");
        checkPlatformVersionPossibleStringForms("1.1.1-qualifier", "test-1.1.1-qualifier");
    }

    private void checkPlatformVersionPossibleStringForms(String platformVersion, String... expectedStringForms) {
        assertThat(
                getPossibleStringForms(
                PlatformId.of("test", new PlatformVersion(platformVersion)))).containsExactlyInAnyOrder(
                        expectedStringForms);
    }
}
