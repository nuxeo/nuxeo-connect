/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     jcarsique
 */
package org.nuxeo.connect.packages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.update.PackageType;

/**
 * @since 1.4.19
 */
public abstract class AbstractPackageSource implements PackageSource {

    protected String id;

    protected String name;

    /**
     * @param type If null, returns all packages.
     * @see #listPackages()
     */
    @Override
    public List<DownloadablePackage> listPackages(PackageType type) {
        List<DownloadablePackage> all = listPackages();
        if (type == null) {
            return all;
        }
        List<DownloadablePackage> result = new ArrayList<>();
        for (DownloadablePackage pkg : all) {
            if (type.equals(pkg.getType())) {
                result.add(pkg);
            }
        }
        return result;
    }

    @Override
    public void flushCache() {
        // NOP
    }

    @Override
    public Collection<? extends DownloadablePackage> listPackagesByName(String packageName) {
        List<DownloadablePackage> result = new ArrayList<>();
        for (DownloadablePackage pkg : listPackages()) {
            if (packageName.equals(pkg.getName())) {
                result.add(pkg);
            }
        }
        return result;
    }

    @Override
    public List<DownloadablePackage> listStudioPackages() {
        // Should only return the registration-associated package
        return listPackages(PackageType.STUDIO);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return id;
    }

}
