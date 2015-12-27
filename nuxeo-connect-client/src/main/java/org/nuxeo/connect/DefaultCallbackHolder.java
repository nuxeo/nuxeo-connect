/*
 * (C) Copyright 2010-2013 Nuxeo SA (http://nuxeo.com/) and others.
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
 */
package org.nuxeo.connect;

import org.nuxeo.connect.update.PackageUpdateService;

public class DefaultCallbackHolder implements CallbackHolder {

    private PackageUpdateService pus;

    public DefaultCallbackHolder() {
    }

    /**
     * @since 1.4.13
     */
    public void setUpdateService(PackageUpdateService pus) {
        this.pus = pus;
    }

    @Override
    public String getHomePath() {
        return getProperty("java.io.tmpdir", null) + "/";
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }

    @Override
    public boolean isTestModeSet() {
        return Boolean.parseBoolean(getProperty(
                "org.nuxeo.connect.client.testMode", "false"));
    }

    @Override
    public PackageUpdateService getUpdateService() {
        return pus;
    }

}
