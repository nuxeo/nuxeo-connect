/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Julien Carsique
 *
 */

package org.nuxeo.connect.update;

/**
 * Packages visibility
 *
 * @since 1.4
 */
public enum PackageVisibility {

    /**
     * To avoid null value. Package visibility is unknown or unset.
     *
     * @since 1.4.5
     */
    UNKNOWN,

    /**
     * Package is publicly visible but download is restricted to registered
     * users.
     */
    MARKETPLACE,

    /**
     * Package is visible in development channel. Download may be restricted.
     */
    DEV,

    /**
     * Package is visible and downloadable without restrictions.
     */
    PUBLIC,

    /**
     * Package visibility and download are restricted to a specific private
     * channel.
     */
    PRIVATE;

}
