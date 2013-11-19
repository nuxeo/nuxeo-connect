/*
 * (C) Copyright 2006-2013 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *
 */

package org.nuxeo.connect.packages.dependencies;

import java.util.List;

/**
 * Main entry point for Dependency resolution.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 *
 */
public interface DependencyResolver {

    public DependencyResolution resolve(String pkgIdOrName,
            String targetPlatform) throws DependencyException;

    /**
     * @throws DependencyException
     * @since 1.4
     */
    public DependencyResolution resolve(List<String> pkgInstall,
            List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform) throws DependencyException;

    /**
     * @throws DependencyException
     * @since 1.4.13
     */
    public DependencyResolution resolve(List<String> pkgInstall,
            List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform, boolean allowSNAPSHOT)
            throws DependencyException;

}
