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
 * Simple form that have a title, a description text and an optional image. A
 * form is used within an wizard to render a set of fields to be filled by the
 * users.
 *
 * A form field is composed from two widgets - a label and an input widget that
 * are usually arranged horizontally in a tab;e row of two columns. If the field
 * label is null then the input widget will span both row columns. To display
 * the input widget vertically under the label you should set the vertical field
 * attribute to true.
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public interface Form {

    /**
     * Gets the form title.
     */
    String getTitle();

    /**
     * Gets the form description.
     */
    String getDescription();

    /**
     * Gets the form image. Can be null if no image is provided.
     */
    String getImage();

    /**
     * Gets the form fields.
     */
    Field[] getFields();

}
