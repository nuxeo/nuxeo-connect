/*
 * (C) Copyright 2011-2015 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Thierry Delprat, jcarsique
 *
 */
package org.nuxeo.connect.packages;

import org.nuxeo.connect.update.Package;

/**
 * Compares {@link Package} by ID (name+version)
 *
 * @since 1.3
 * @deprecated Since 1.4.19. Use {@link PackageComparator} instead.
 * @see PackageComparator
 */
@Deprecated
public class VersionPackageComparator extends PackageComparator {
}
