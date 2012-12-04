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
 *     bstefanescu, jcarsique
 */
package org.nuxeo.connect.update;

import java.util.EnumSet;

/**
 * @since 5.7. Before 5.7, it was an interface with same values.
 */
public enum PackageState {

    /**
     * Unknown package state. That should never be used. This value exists to
     * avoid the use of a null value.
     */
    UNKNOWN(-1, "unknown"),

    /**
     * The package is on the remote server. It is listed and can be downloaded
     * locally. This happens when the user wants to see package content (then
     * the package is downloaded and goes to DONLOADED state)
     */
    REMOTE(0, "remote"),

    /**
     * The package is downloading. Used by the download manager.
     */
    DOWNLOADING(1, "downloading"),

    /**
     * The package is in local cache. All information about the package are
     * available. A downloaded package can be installed. (if package guards
     * allows it)
     */
    DOWNLOADED(2, "downloaded"),

    /**
     * A local package is in the install process. The install process begin when
     * the user click on install (after the package was validated) and finish
     * when the package is either rollbacked, either installed. If rollbacked
     * the package goes to {@link #DOWNLOADED} state otherwise to INSTALLED
     * state. After installing a package it will be either automatically enter
     * {@link #STARTED} state (if it doesn't require server restart) or
     * INSTALLED state if a restart is required.
     */
    INSTALLING(3, "installing"),

    /**
     * An installed package. THese packages are in this state only after an
     * install and before being started. This happens when the package is
     * requiring a server restart. After the next server restart the package
     * will be put in STARTED state. From installed state a package can be
     * uninstalled and thus goes back to {@link #DOWNLOADED} state.
     */
    INSTALLED(4, "installed"),

    /**
     * A started package is an installed package that is currently running in
     * the platform. From started state the package can be uninstalled and this
     * way it goes back to {@link #DOWNLOADED} state
     */
    STARTED(5, "started");

    private final int value;

    private final String label;

    PackageState(int value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * @deprecated Since 1.4.5. Set as deprecated to encourage use of enum
     *             instead of int.
     */
    @Deprecated
    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static PackageState getByValue(int value) {
        for (final PackageState element : EnumSet.allOf(PackageState.class)) {
            if (element.value == value) {
                return element;
            }
        }
        return UNKNOWN;
    }

    /**
     * @param value A String representing an enum value (int), not a label.
     */
    public static PackageState getByValue(String value)
            throws NumberFormatException {
        return getByValue(Integer.valueOf(value));
    }

    public static PackageState getByLabel(String label) {
        for (final PackageState element : EnumSet.allOf(PackageState.class)) {
            if (element.label.equals(label)) {
                return element;
            }
        }
        return UNKNOWN;
    }

    /**
     * A package is considered as "installed" if it is in a state of installing,
     * installed or started.
     */
    public boolean isInstalled() {
        return this == INSTALLING || this == INSTALLED || this == STARTED;
    }

    @Override
    public String toString() {
        return getLabel();
    }

}
