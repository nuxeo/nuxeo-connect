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
package org.nuxeo.connect.packages.dependencies;

/**
*
* Exception during Dependency resolution process
*
* @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
*
*/
public class DependencyException extends Exception {

    private static final long serialVersionUID = 1L;

    public DependencyException(String message) {
        super(message);
    }

    public DependencyException(String message, Throwable t) {
        super(message,t);
    }
}
