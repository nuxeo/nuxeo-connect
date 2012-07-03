/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 *
 */

package org.nuxeo.connect.data;

import org.nuxeo.connect.update.Package;

/**
 * Interface for {@link Package} that can be downloaded in Nuxeo Connect.
 *
 * Compared to a {@link Package} it adds some external meta-data that are
 * managed by Nuxeo Connect but are not part of the XML package descriptor
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public interface DownloadablePackage extends Package {

    String getSourceDigest();

    String getSourceUrl();

    long getSourceSize();

    int getRating();

    int getCommentsNumber();

    String getPictureUrl();

    int getDownloadsCount();

}
