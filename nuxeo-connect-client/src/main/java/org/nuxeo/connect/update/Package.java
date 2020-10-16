/*
 * (C) Copyright 2006-2019 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     bstefanescu
 *     Yannis JULIENNE
 */
package org.nuxeo.connect.update;

/**
 * Describe the APIs to read minimal package properties (i.e loaded from package.xml)
 * 
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public interface Package {

    /**
     * Get the package unique ID. The ID is composed by the package name and version: <code>name-version</code>
     */
    String getId();

    /**
     * The package name.
     */
    String getName();

    /**
     * The package title.
     */
    String getTitle();

    /**
     * The package short description.
     */
    String getDescription();

    /**
     * The package type: addon, hotfix, etc.
     */
    PackageType getType();

    /**
     * Get the package vendor ID. The vendor represent the entity providing the package.
     */
    String getVendor();

    /**
     * The package version.
     */
    Version getVersion();

    /**
     * The list of platforms that supports this package.
     */
    String[] getTargetPlatforms();

    /**
     * @since 1.8.0 The range of platforms that supports this package.
     */
    String getTargetPlatformRange();

    /**
     * @since 1.8.0 The name of platforms that supports this package.
     */
    String getTargetPlatformName();

    /**
     * Gets the list of package dependencies for this package. If no dependency exists, either null or an empty array is
     * returned.
     *
     * @see PackageDependency
     * @see #getConflicts()
     * @see #getProvides()
     */
    PackageDependency[] getDependencies();

    /**
     * Gets the list of package optional dependencies for this package. If no optional dependency exists, either null or
     * an empty array is returned.
     *
     * @see PackageDependency
     * @see #getDependencies()
     * @see #getConflicts()
     * @see #getProvides()
     * @since 1.4.26
     */
    default PackageDependency[] getOptionalDependencies() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the list of conflicts of this package. If no conflict exists, either null or an empty array is returned.
     *
     * @see PackageDependency
     * @see #getDependencies()
     * @see #getProvides()
     * @since 1.3.3
     */
    PackageDependency[] getConflicts();

    /**
     * Gets the list of provides of this package. If no provide exists, either null or an empty array is returned.
     *
     * @see PackageDependency
     * @see #getDependencies()
     * @see #getConflicts()
     * @since 1.3.3
     */
    PackageDependency[] getProvides();

    /**
     * Gets the package life cycle status.
     *
     * @see PackageState
     * @deprecated Since 1.4.17. Use {@link #getPackageState()} instead.
     */
    @Deprecated
    int getState();

    /**
     * Gets the package life cycle status.
     *
     * @since 1.4.17
     * @see PackageState
     */
    PackageState getPackageState();

    /**
     * Gets the license type. Examples: GPL, BSD, LGPL, etc.
     */
    String getLicenseType();

    /**
     * Gets an URL for the license. Return null if no URL exists. In that case the license content must be included in
     * the package as the content of the license.txt file.
     */
    String getLicenseUrl();

    /**
     * Gets the package classifier: open source etc ?
     */
    String getClassifier(); // TODO use enum

    /**
     * Tests whether this package is local or a remote one. A local package has a package data attached.
     */
    boolean isLocal();

    /**
     * Tests if the package can be hot reloaded
     */
    boolean supportsHotReload();

}
