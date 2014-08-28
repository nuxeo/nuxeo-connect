/*
 * (C) Copyright 2006-2014 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 */
package org.nuxeo.connect.update;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public interface Package {

    /**
     * Get the package unique ID.
     *
     * The ID is composed by the package name and version:
     * <code>name-version</code>
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
     * Get the package vendor ID. The vendor represent the entity providing the
     * package.
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
     * Gets the list of package dependencies for this package. If no dependency
     * exists, either null or an empty array is returned.
     *
     * @see PackageDependency
     * @see #getConflicts()
     * @see #getProvides()
     */
    PackageDependency[] getDependencies();

    /**
     * Gets the list of conflicts of this package. If no conflict
     * exists, either null or an empty array is returned.
     *
     * @see PackageDependency
     * @see #getDependencies()
     * @see #getProvides()
     *
     * @since 1.3.3
     */
    PackageDependency[] getConflicts();

    /**
     * Gets the list of provides of this package. If no provide
     * exists, either null or an empty array is returned.
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
     * @deprecated Since 5.9.6. Use {@link #getPackageState()} instead.
     */
    @Deprecated
    int getState();

    /**
     * Gets the package life cycle status.
     *
     * @see PackageState
     */
    PackageState getPackageState();

    /**
     * Get the URL where more information can be found about this package. Can
     * be null.
     *
     * @return The package web page URL. May be null.
     */
    String getHomePage();

    /**
     * Gets the license type. Examples: GPL, BSD, LGPL, etc.
     */
    String getLicenseType();

    /**
     * Gets an URL for the license. Return null if no URL exists. In that case
     * the license content must be included in the package as the content of the
     * license.txt file.
     */
    String getLicenseUrl();

    /**
     * Gets the package classifier: open source etc ?
     */
    String getClassifier(); // TODO use enum

    /**
     * Tests whether this package is local or a remote one. A local package has
     * a package data attached.
     */
    boolean isLocal();

    /**
     * Get the production status of the package (testing, production ready ...)
     */
    ProductionState getProductionState();

    /**
     * Get the validation state of the package (not certified, in process,
     * certified ...)
     */
    NuxeoValidationState getValidationState();

    /**
     * Tests if the package can be hot reloaded
     */
    boolean supportsHotReload();

    /**
     * Test if the package is supported by Nuxeo
     */
    boolean isSupported();

    /**
     * The package visibility: marketplace, dev, public
     *
     * @since 1.4
     */
    PackageVisibility getVisibility();

}
