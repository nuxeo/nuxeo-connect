/*
 * (C) Copyright 2010-2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 */

package org.nuxeo.connect.pm.tests;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.PackageSource;
import org.nuxeo.connect.update.PackageType;

public class DummyPackageSource implements PackageSource {

    protected List<DownloadablePackage> pkgs;

    protected boolean local;

    public DummyPackageSource(List<DownloadablePackage> pkgs, boolean local) {
        this.pkgs = pkgs;
        this.local = local;
    }

    public String getName() {
        return null;
    }

    public String getId() {
        return "dummy";
    }

    public List<DownloadablePackage> listPackages() {
        return pkgs;
    }

    public List<DownloadablePackage> listPackages(PackageType type) {

        List<DownloadablePackage> result = new ArrayList<DownloadablePackage>();

        for (DownloadablePackage pkg : pkgs) {
            if (type.equals(pkg.getType())) {
                result.add(pkg);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        if (local) {
            return "local : " + pkgs.size() + " packages";
        } else {
            return "remote : " + pkgs.size() + " packages";
        }
    }

    public void flushCache() {
        // NOP
    }

    public void reset(List<DownloadablePackage> packages) {
        this.pkgs = packages;
    }
}
