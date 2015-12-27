/*
 * (C) Copyright 2006-2009 Nuxeo SA (http://nuxeo.com/) and others.
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

package org.nuxeo.connect.connector;

/**
 * Enum for types of Nuxeo Instances
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public enum NuxeoClientInstanceType {

    DEV("dev"),
    PREPROD("preprod"),
    PROD("prod");

    private final String value;

    NuxeoClientInstanceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static NuxeoClientInstanceType fromString(String value) {
        for (NuxeoClientInstanceType nit : NuxeoClientInstanceType.values()) {
            if (nit.getValue().equals(value)) {
                return nit;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return value;
    }

}
