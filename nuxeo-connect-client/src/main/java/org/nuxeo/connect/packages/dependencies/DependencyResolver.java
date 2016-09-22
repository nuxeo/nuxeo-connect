/*
 * (C) Copyright 2006-2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Yannis JULIENNE
 */

package org.nuxeo.connect.packages.dependencies;

import java.util.List;

/**
 * Main entry point for Dependency resolution.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public interface DependencyResolver {

    /**
     * This method was used for backward compatibility with the {@link LegacyDependencyResolver} API. It is not possible
     * with the current implementation (1.4.26) to upgrade a single package without specifying its version, so calling
     * this method on an already installed package will return a no-change solution.
     *
     * @deprecated since 1.4.26 Use {@link #resolve(List, List, List, String)} instead
     */
    @Deprecated
    public DependencyResolution resolve(String pkgIdOrName, String targetPlatform) throws DependencyException;

    /**
     * @throws DependencyException
     * @since 1.4
     */
    public DependencyResolution resolve(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform) throws DependencyException;

    /**
     * @throws DependencyException
     * @since 1.4.13
     */
    public DependencyResolution resolve(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform, boolean allowSNAPSHOT) throws DependencyException;

    /**
     * @throws DependencyException
     * @since 1.4.14
     */
    public DependencyResolution resolve(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform, boolean allowSNAPSHOT, boolean doKeep) throws DependencyException;

    /**
     * Compute a {@link DependencyResolution} that will match the requested packages installation, remove and upgrade
     * for the specified target platform. <b>Note</b> : prefer to use {@link #resolve(List, List, List, String)} if you
     * are not sure of what to give as a solverCriteria
     *
     * @param solverCriteria specify the criteria string to be used by the solver
     * @since 1.4.26
     */
    public DependencyResolution resolve(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform, String solverCriteria) throws DependencyException;

    /**
     * Compute a {@link DependencyResolution} that will match the requested packages installation, remove and upgrade
     * for the specified target platform. <b>Note</b> : prefer to use
     * {@link #resolve(List, List, List, String, boolean, boolean)} if you are not sure of what to give as a
     * solverCriteria
     *
     * @param allowSNAPSHOT true to allow SNAPSHOT packages to be part of the computed solution
     * @param doKeep false to uninstall all packages that do not need to be part of the computed solution
     * @param solverCriteria specify the criteria string to be used by the solver
     * @since 1.4.26
     */
    public DependencyResolution resolve(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform, boolean allowSNAPSHOT, boolean doKeep, String solverCriteria)
            throws DependencyException;
}
