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
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.Version;

class NuxeoCUDFPackage {

    public static final String CUDF_PACKAGE = "package: ";

    public static final String CUDF_VERSION = "version: ";

    public static final String CUDF_INSTALLED = "installed: ";

    public static final String CUDF_DEPENDS = "depends: ";

    public static final String CUDF_CONFLICTS = "conflicts: ";

    public static final String CUDF_PROVIDES = "provides: ";

    public static final String CUDF_REQUEST = "request: ";

    public static final String CUDF_INSTALL = "install: ";

    public static final String CUDF_REMOVE = "remove: ";

    public static final String CUDF_UPGRADE = "upgrade: ";

    private final String newLine = System.getProperty("line.separator");

    private DownloadablePackage pkg;

    private int cudfVersion;

    private String cudfName;

    private boolean installed;

    public NuxeoCUDFPackage(DownloadablePackage pkg) {
        this.pkg = pkg;
        cudfName = pkg.getName();
        // No more add classifier at the end of name
        // if (pkg.getVersion().classifier() != null) {
        // cudfName += ":" + pkg.getVersion().classifier();
        // }
    }

    public String getCUDFName() {
        return cudfName;
    }

    public Version getNuxeoVersion() {
        // No more remove classifier from version
        // Version version = new Version(pkg.getVersion().toString());
        // version.setSnapshot(version.isSnapshot());
        // version.setClassifier(null);
        return pkg.getVersion();
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
        return "CUDF {" + cudfVersion + "}" + " Nuxeo {" + pkg.getVersion()
                + "} " + pkg.getClass();
    }

    /**
     * @return CUDF stanza for that package; see
     *         {@link "http://www.mancoosi.org/cudf/"}
     */
    public String getCUDFStanza() {
        StringBuffer sb = new StringBuffer();
        sb.append(CUDF_PACKAGE + cudfName + newLine);
        sb.append(CUDF_VERSION + cudfVersion + newLine);
        sb.append(CUDF_INSTALLED + installed + newLine);
        return sb.toString();
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public PackageDependency[] getDependencies() {
        return pkg.getDependencies();
    }

    public PackageDependency[] getConflicts() {
        return pkg.getConflicts();
    }

    public PackageDependency[] getProvides() {
        return pkg.getProvides();
    }
}