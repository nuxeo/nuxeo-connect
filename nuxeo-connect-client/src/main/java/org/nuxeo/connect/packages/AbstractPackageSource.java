/*
 * (C) Copyright 2015-2017 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Ronan DANIELLOU <rdaniellou@nuxeo.com>
 */
package org.nuxeo.connect.packages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang.mutable.MutableObject;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.update.PackageType;

/**
 * @since 1.4.19
 */
public abstract class AbstractPackageSource implements PackageSource {

    protected String id;

    protected String name;

    /**
     * Keeps track of the target platform that has been used when filling the cache. <code>null</code> value means no
     * filter has been used.
     *
     * @since TODO
     */
    protected String targetPlatformUsedtoFillTheCache;

    /**
     * @param type If null, returns all packages.
     * @see #listPackages()
     */
    @Override
    public List<DownloadablePackage> listPackages(PackageType type) {
        return listPackages(type, null);
    }

    public List<DownloadablePackage> listPackages(PackageType type, String targetPlatform) {
        List<DownloadablePackage> all = listPackages();
        if (type == null && targetPlatform == null) {
            return all;
        }
        List<DownloadablePackage> result = new ArrayList<>();
        for (DownloadablePackage pkg : all) {
            if (type != null) {
                if (!type.equals(pkg.getType())) {
                    continue;
                }
            }

            targetPlatformUsedtoFillTheCache = targetPlatform;
            if (targetPlatform == null) {
                result.add(pkg);
            } else {
                String[] tps = pkg.getTargetPlatforms();
                if (tps != null) {
                    String regex;
                    String value;

                    for (String tp : tps) {
                        if (targetPlatform.contains("*") || targetPlatform.contains("?")) {
                            // input parameter is a regex
                            regex = targetPlatform;
                            value = tp;
                        } else {
                            // input parameter is not a regex
                            // The values in a package target platforms list, can be a regex
                            value = targetPlatform;
                            regex = tp;
                        }
                        if (tp != null && FilenameUtils.wildcardMatch(value, regex, IOCase.INSENSITIVE)) {
                            result.add(pkg);
                            break;
                        }
                    }
                }
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
        return listPackagesByName(packageName, null);
    }

    @Override
    public Collection<? extends DownloadablePackage> listPackagesByName(String packageName, String targetPlatform) {
        List<DownloadablePackage> result = new ArrayList<>();
        for (DownloadablePackage pkg : listPackages(null, targetPlatform)) {
            if (packageName.equals(pkg.getName())) {
                result.add(pkg);
            }
        }
        return result;
    }

    @Override
    public List<DownloadablePackage> listStudioPackages() {
        return listStudioPackages(null);
    }

    @Override
    public List<DownloadablePackage> listStudioPackages(String targetPlatform) {
        // Should only return the registration-associated package
        return listPackages(PackageType.STUDIO, targetPlatform);
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
