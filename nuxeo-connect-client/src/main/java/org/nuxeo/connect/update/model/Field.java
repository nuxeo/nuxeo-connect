/*
 * (C) Copyright 2006-2010 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     bstefanescu
 */
package org.nuxeo.connect.update.model;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public interface Field {

    /**
     * Gets the field name that will be used the key to store the value entered
     * by the user.
     */
    String getName();

    /**
     * Gets the field label
     */
    String getLabel();

    /**
     * Gets the field type
     */
    String getType();

    /**
     * Whether the label should be displayed on top of the input widget
     * (vertically rather than horizontally).
     */
    boolean isVertical();

    /**
     * Whether the field is required
     */
    boolean isRequired();

    /**
     * Whether the field is read only or not.
     */
    boolean isReadOnly();

    /**
     * Gets the default value for this field, or null if none.
     */
    String getValue();

}
