/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and others.
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

package org.nuxeo.connect.packages;

import java.util.Comparator;

import org.nuxeo.connect.update.Package;

/**
 * Compares {@link Package} by ID (name+version)
 *
 * @since 1.4
 */
public class PackageComparator implements Comparator<Package> {
    @Override
    public int compare(Package arg0, Package arg1) {
        if (!arg0.getType().equals(arg1.getType())) {
            return arg0.getType().compareTo(arg1.getType());
        }
        if (!arg0.getName().equals(arg1.getName())) {
            return arg0.getName().compareToIgnoreCase(arg1.getName());
        }
        return arg0.getVersion().compareTo(arg1.getVersion());
    }
}
