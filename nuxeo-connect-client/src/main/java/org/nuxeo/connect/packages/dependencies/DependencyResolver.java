/*
 * (C) Copyright 2006-2016 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *     Yannis JULIENNE
 *
 */

package org.nuxeo.connect.packages.dependencies;

import java.util.List;

import org.nuxeo.connect.platform.PlatformId;

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
    public DependencyResolution resolve(String pkgIdOrName, PlatformId targetPlatform) throws DependencyException;

    /**
     * @throws DependencyException
     * @since 1.4
     */
    public DependencyResolution resolve(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            PlatformId targetPlatform) throws DependencyException;

    /**
     * @throws DependencyException
     * @since 1.4.13
     */
    public DependencyResolution resolve(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            PlatformId targetPlatform, boolean allowSNAPSHOT) throws DependencyException;

    /**
     * @param targetPlatformVersion
     * @throws DependencyException
     * @since 1.4.14
     */
    public DependencyResolution resolve(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            PlatformId targetPlatform, boolean allowSNAPSHOT, boolean doKeep) throws DependencyException;

    /**
     * Compute a {@link DependencyResolution} that will match the requested packages installation, remove and upgrade
     * for the specified target platform.
     * 
     * @param isSubResolution if true, do not check for optional dependencies on installed packages
     * @since 1.4.27
     */
    public DependencyResolution resolve(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            PlatformId targetPlatform, boolean allowSNAPSHOT, boolean doKeep, boolean isSubResolution)
            throws DependencyException;

    /**
     * Compute a {@link DependencyResolution} that will match the requested packages installation, remove and upgrade
     * for the specified target platform. <b>Note</b> : prefer to use {@link #resolve(List, List, List, String)} if you
     * are not sure of what to give as a solverCriteria
     *
     * @param solverCriteria specify the criteria string to be used by the solver
     * @since 1.4.26
     */
    public DependencyResolution resolve(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            PlatformId targetPlatform, String solverCriteria) throws DependencyException;

    /**
     * Compute a {@link DependencyResolution} that will match the requested packages installation, remove and upgrade
     * for the specified target platform. <b>Note</b> : prefer to use
     * {@link #resolve(List, List, List, String, boolean, boolean)} if you are not sure of what to give as a
     * solverCriteria
     *
     * @param allowSNAPSHOT true to allow SNAPSHOT packages to be part of the computed solution
     * @param doKeep false to uninstall all packages that do not need to be part of the computed solution
     * @param solverCriteria specify the criteria string to be used by the solver
     * @param isSubResolution if true, do not check for optional dependencies on installed packages
     * @since 1.4.27
     */
    public DependencyResolution resolve(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            PlatformId targetPlatform, boolean allowSNAPSHOT, boolean doKeep, String solverCriteria,
            boolean isSubResolution) throws DependencyException;
}
