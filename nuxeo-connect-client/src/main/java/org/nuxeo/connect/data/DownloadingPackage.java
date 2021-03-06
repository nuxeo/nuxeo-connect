/*
 * (C) Copyright 2006-2015 Nuxeo SA (http://nuxeo.com/) and others.
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

package org.nuxeo.connect.data;

import org.nuxeo.connect.update.Package;

/**
 * Interface for {@link Package} being downloaded.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public interface DownloadingPackage extends DownloadablePackage {

    /**
     * Verify Digest
     */
    boolean isDigestOk();

    /**
     * Indicates if Download process is terminated
     */
    boolean isCompleted();

    /**
     * Return Download progress in %
     */
    int getDownloadProgress();

    /**
     * @return Error description in case of download failure
     * @since 1.4.3
     */
    String getErrorMessage();

    /**
     * @return {@code true} if the download error seems not related to a specific package (server is unavailable,
     *         timeout on connections...)
     * @since 1.4.24
     */
    boolean isServerError();

}
