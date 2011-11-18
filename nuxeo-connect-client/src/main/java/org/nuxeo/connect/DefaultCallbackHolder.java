/*
 * (C) Copyright 2010-2011 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 */
package org.nuxeo.connect;

import org.nuxeo.connect.update.PackageUpdateService;

public class DefaultCallbackHolder implements CallbackHolder {

    public String getHomePath() {
        return getProperty("java.io.tmpdir", null) + "/";
    }

    public String getProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }

    public boolean isTestModeSet() {
        return Boolean.parseBoolean(getProperty(
                "org.nuxeo.connect.client.testMode", "false"));
    }

    @Override
    public PackageUpdateService getUpdateService() {
        return null;
    }
}
