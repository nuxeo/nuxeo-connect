/*
 * (C) Copyright 2010-2015 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 */

package org.nuxeo.connect.pm.tests;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.AbstractPackageSource;
import org.nuxeo.connect.packages.PackageSource;

public class DummyPackageSource extends AbstractPackageSource implements PackageSource {

    protected List<DownloadablePackage> pkgs = new ArrayList<>();

    /**
     * @deprecated Since 1.4.19. Use {@link #DummyPackageSource(List)}
     */
    @Deprecated
    public DummyPackageSource(List<DownloadablePackage> pkgs, boolean local) {
        this(pkgs, "dummy");
    }

    /**
     * @since 1.4.13
     * @deprecated Since 1.4.19. Use {@link #DummyPackageSource(List, String)}
     */
    @Deprecated
    public DummyPackageSource(List<DownloadablePackage> pkgs, boolean local, String id) {
        this(pkgs, id);
    }

    /**
     * @since 1.4.19
     */
    public DummyPackageSource(List<DownloadablePackage> pkgs) {
        this(pkgs, "dummy");
    }

    /**
     * @since 1.4.19
     */
    public DummyPackageSource(List<DownloadablePackage> pkgs, String id) {
        this.pkgs = pkgs;
        this.id = id;
        name = id;
    }

    @Override
    public List<DownloadablePackage> listPackages() {
        return pkgs;
    }

    @Override
    public String toString() {
        return name + " : " + pkgs.size() + " packages";
    }

    public void reset(List<DownloadablePackage> packages) {
        pkgs = packages;
    }

    @Override
    public DownloadablePackage getPackageById(String packageId) {
        for (DownloadablePackage pkg : pkgs) {
            if (packageId.equals(pkg.getId())) {
                return pkg;
            }
        }
        return null;
    }
}
