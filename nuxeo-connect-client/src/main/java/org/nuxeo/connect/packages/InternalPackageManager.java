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

package org.nuxeo.connect.packages;

import org.nuxeo.connect.update.Package;

/**
 * Interface used by the Dependency resolution system to access the
 * {@link Package}
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 * @deprecated Since 1.4. Directly use {@link PackageManager} instead.
 */
@Deprecated
public interface InternalPackageManager extends PackageManager {

}
