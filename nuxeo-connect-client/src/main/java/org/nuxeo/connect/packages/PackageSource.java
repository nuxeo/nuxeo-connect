/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 * $Id$
 */

package org.nuxeo.connect.packages;

import java.util.List;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.PackageType;

/**
 * Interface for {@link Package} sources: classes providing access to {@link Package}.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public interface PackageSource {

    String getName();

    List<DownloadablePackage> listPackages();

    List<DownloadablePackage> listPackages(PackageType type);

    void flushCache();

}
