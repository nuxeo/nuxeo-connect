/*
 * (C) Copyright 2010-2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 */

package org.nuxeo.connect.update;

import java.util.EnumSet;

/**
 * Enum for types of packages
 *
 * @author tiry
 *
 */
public enum PackageType {

    STUDIO("studio"), HOT_FIX("hotfix"), ADDON("addon");

    private final String value;

    PackageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }

    public static PackageType getByValue(String value) {
        for (final PackageType element : EnumSet.allOf(PackageType.class)) {
            if (element.toString().equals(value)) {
                return element;
            }
        }
        return null;
    }

}
