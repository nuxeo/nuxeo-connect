/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
package org.nuxeo.connect.update;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public interface PackageState {

    /**
     * The package is on the remote server. It is listed and can be downloaded
     * locally. This happens when the user wants to see package content (then
     * the package is downloaded and goes to DONLOADED state)
     */
    final static int REMOTE = 0;

    /**
     * The package is downloading. Used by the download manager.
     */
    final static int DOWNLOADING = 1;

    /**
     * The package is in local cache. All information about the package are
     * available. A downloaded package can be installed. (if package guards
     * allows it)
     */
    final static int DOWNLOADED = 2;

    /**
     * A local package is in the install process. The install process begin when
     * the user click on install (after the package was validated) and finish
     * when the package is either rollbacked, either installed. If rollbacked
     * the package goes to {@link #DOWNLOADED} state otherwise to INSTALLED
     * state. After installing a package it will be either automatically enter
     * {@link #STARTED} state (if it doesn't require server restart) or
     * INSTALLED state if a restart is required.
     */
    final static int INSTALLING = 3;

    /**
     * An installed package. THese packages are in this state only after an
     * install and before being started. This happens when the package is
     * requiring a server restart. After the next server restart the package
     * will be put in STARTED state. From installed state a package can be
     * uninstalled and thus goes back to {@link #DOWNLOADED} state.
     */
    final static int INSTALLED = 4;

    /**
     * A started package is an installed package that is currently running in
     * the platform. From started state the package can be uninstalled and this
     * way it goes back to {@link #DOWNLOADED} state
     */
    final static int STARTED = 5;

}
