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
package org.nuxeo.connect.packages.dependencies;

import org.nuxeo.connect.update.PackageDependency;

/**
 * <p>
 * CUDF (for Common Upgradeability Description Format) is a format for describing upgrade scenarios in package-based
 * Free and Open Source Software distribution.
 * </p>
 * <p>
 * In every such scenario there exists a package universe (i.e. a set of packages) known to a package manager
 * application, a package status (i.e. the currently installed packages), and a user request (i.e. a wish to change the
 * set of installed packages) that need to be fulfilled.
 * <ul>
 * <li>CUDF permits to describe an upgrade scenario in a way that is both distribution-independent and
 * package-manager-independent</li>
 * <li>CUDF offers a rigorous semantics of dependency solving that enables to independently check the correctness of
 * upgrade solutions proposed by package managers.</li>
 * </ul>
 * </p>
 * See <a href="http://www.mancoosi.org/cudf/">http://www.mancoosi.org/cudf/</a>
 *
 * @since 1.4.20
 */
interface CUDFPackage {

    public static final String TAG_PACKAGE = "package: ";

    public static final String TAG_VERSION = "version: ";

    public static final String TAG_INSTALLED = "installed: ";

    public static final String TAG_DEPENDS = "depends: ";

    public static final String TAG_CONFLICTS = "conflicts: ";

    public static final String TAG_PROVIDES = "provides: ";

    public static final String TAG_REQUEST = "request: ";

    public static final String TAG_INSTALL = "install: ";

    public static final String TAG_REMOVE = "remove: ";

    public static final String TAG_UPGRADE = "upgrade: ";

    public static final String LINE_PATTERN = "(.*:)\\s?(.*)";

    public abstract String getCUDFName();

    public abstract int getCUDFVersion();

    /**
     * @return CUDF stanza for that package
     */
    public abstract String getCUDFStanza();

    public abstract void setInstalled(boolean installed);

    public abstract boolean isInstalled();

    public abstract PackageDependency[] getDependencies();

    public abstract PackageDependency[] getConflicts();

    public abstract PackageDependency[] getProvides();

}