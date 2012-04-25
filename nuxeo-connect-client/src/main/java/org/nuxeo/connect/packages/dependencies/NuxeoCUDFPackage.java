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

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.update.Version;

class NuxeoCUDFPackage {

    private DownloadablePackage pkg;

    private int cudfVersion;

    private String cudfName;

    /**
     * @param name Nuxeo name
     * @param version Nuxeo version
     */
    public NuxeoCUDFPackage(DownloadablePackage pkg) {
        this.pkg = pkg;
        cudfName = pkg.getName();
        if (pkg.getVersion().classifier() != null) {
            cudfName += ":" + pkg.getVersion().classifier();
        }
    }

    /**
     * @param name Nuxeo package name
     * @param version Nuxeo package version
     */
    public String getCUDFName() {
        return cudfName;
    }

    /**
     * @return cleaned Nuxeo version
     */
    public Version getNuxeoVersion() {
        Version version = new Version(pkg.getVersion().toString());
        version.setSnapshot(version.isSnapshot());
        version.setClassifier(null);
        return version;
    }

    /**
     * @param posInt positive integer
     */
    public void setCUDFVersion(int posInt) {
        cudfVersion = posInt;
    }

    public int getCUDFVersion() {
        return cudfVersion;
    }

    @Override
    public String toString() {
        return cudfName + " " + cudfVersion;
    }
}