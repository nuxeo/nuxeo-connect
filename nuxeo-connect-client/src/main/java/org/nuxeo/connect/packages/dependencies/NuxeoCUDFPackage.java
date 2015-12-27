/*
 * (C) Copyright 2012-2015 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Julien Carsique
 *
 */

package org.nuxeo.connect.packages.dependencies;

import java.util.Arrays;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.Version;

class NuxeoCUDFPackage implements CUDFPackage {

    public final String newLine = System.getProperty("line.separator");

    protected DownloadablePackage pkg;

    protected int cudfVersion;

    protected String cudfName;

    protected boolean installed;

    public NuxeoCUDFPackage(DownloadablePackage pkg) {
        this.pkg = pkg;
        cudfName = pkg.getName();
        // NXP-9268: Workaround for nuxeo-content-browser
        if ("nuxeo-content-browser".equals(cudfName) && "cmf".equalsIgnoreCase(pkg.getVersion().classifier())) {
            cudfName += "*" + pkg.getVersion().classifier();
        }
        setInstalled(pkg.getPackageState().isInstalled());
    }

    /**
     * @since 1.4.20
     */
    protected NuxeoCUDFPackage() {
    }

    /**
     * @param pkg
     * @since 1.4.11
     */
    public void setPkg(DownloadablePackage pkg) {
        this.pkg = pkg;
        cudfName = pkg.getName();
        setInstalled(pkg.getPackageState().isInstalled());
    }

    @Override
    public String getCUDFName() {
        return cudfName;
    }

    public String getNuxeoName() {
        return pkg.getName();
    }

    public Version getNuxeoVersion() {
        return pkg.getVersion();
    }

    /**
     * @param posInt positive integer
     */
    public void setCUDFVersion(int posInt) {
        cudfVersion = posInt;
    }

    @Override
    public int getCUDFVersion() {
        return cudfVersion;
    }

    @Override
    public String toString() {
        return getCUDFName() + " CUDF {" + cudfVersion + "}" + " Nuxeo {" + pkg.getVersion() + "} " + pkg.getClass();
    }

    @Override
    public String getCUDFStanza() {
        StringBuffer sb = new StringBuffer();
        sb.append(TAG_PACKAGE + cudfName + newLine);
        sb.append(TAG_VERSION + cudfVersion + newLine);
        sb.append(TAG_INSTALLED + installed + newLine);
        return sb.toString();
    }

    @Override
    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    @Override
    public boolean isInstalled() {
        return installed;
    }

    @Override
    public PackageDependency[] getDependencies() {
        return pkg.getDependencies();
    }

    @Override
    public PackageDependency[] getConflicts() {
        // NXP-9268: Workaround for nuxeo-content-browser
        PackageDependency[] conflicts = pkg.getConflicts();
        if ("nuxeo-content-browser*cmf".equals(cudfName)) {
            return addToConflicts(conflicts, "nuxeo-content-browser");
        } else if ("nuxeo-content-browser".equals(cudfName)) {
            return addToConflicts(conflicts, "nuxeo-content-browser*cmf");
        } else {
            return conflicts;
        }
    }

    private PackageDependency[] addToConflicts(PackageDependency[] conflicts, String dep) {
        PackageDependency[] withContentBrowser;
        if (conflicts != null && conflicts.length > 0) {
            withContentBrowser = Arrays.copyOf(conflicts, conflicts.length + 1);
        } else {
            withContentBrowser = new PackageDependency[1];
        }
        withContentBrowser[withContentBrowser.length - 1] = new PackageDependency(dep);
        return withContentBrowser;
    }

    @Override
    public PackageDependency[] getProvides() {
        return pkg.getProvides();
    }

    public String getNuxeoId() {
        return pkg.getId();
    }

    /**
     * @param dependency
     */
    public static String getCUDFName(PackageDependency dependency) {
        // NXP-9268: Workaround for nuxeo-content-browser
        if ("nuxeo-content-browser".equals(dependency.getName())
                && dependency.getVersionRange().toString().contains("cmf")) {
            return dependency.getName() + "*cmf";
        } else {
            return dependency.getName();
        }
    }

}
