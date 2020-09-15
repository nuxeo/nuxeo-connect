/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and others.
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
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.dependencies.TargetPlatformFilterHelper;
import org.nuxeo.connect.update.PackageState;
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
        return listPackages(type, null, null);
    }

    @Override
    public List<DownloadablePackage> listPackages(PackageType type, String currentTargetPlatform,
            String currentTargetPlatformVersion) {
        List<DownloadablePackage> all = listPackages();
        return all.stream()
                  .filter(pkg -> {
                      // the TP filter only applies on remote packages
                      return (pkg.getPackageState() != PackageState.REMOTE || StringUtils.isBlank(currentTargetPlatform)
                              || TargetPlatformFilterHelper.isCompatibleWithTargetPlatform(pkg, currentTargetPlatform,
                                      currentTargetPlatformVersion))
                              && (type == null || type.equals(pkg.getType()));
                  })
                  .collect(Collectors.toList());
    }

    @Override
    public void flushCache() {
        // NOP
    }

    @Override
    public Collection<? extends DownloadablePackage> listPackagesByName(String packageName) {
        return listPackagesByName(packageName, null, null);
    }

    @Override
    public Collection<? extends DownloadablePackage> listPackagesByName(String packageName, String targetPlatform,
            String currentTargetPlatformVersion) {
        List<DownloadablePackage> result = new ArrayList<>();
        for (DownloadablePackage pkg : listPackages(null, targetPlatform, currentTargetPlatformVersion)) {
            if (packageName.equals(pkg.getName())) {
                result.add(pkg);
            }
        }
        return result;
    }

    @Override
    public List<DownloadablePackage> listStudioPackages() {
        // Should only return the registration-associated package
        return listStudioPackages(null, null);
    }

    @Override
    public List<DownloadablePackage> listStudioPackages(String currentTargetPlatform,
            String currentTargetPlatformVersion) {
        return listPackages(PackageType.STUDIO, currentTargetPlatform, currentTargetPlatformVersion);
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
