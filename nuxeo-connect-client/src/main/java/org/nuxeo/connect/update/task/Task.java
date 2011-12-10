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
package org.nuxeo.connect.update.task;

import java.io.File;
import java.util.Map;

import org.nuxeo.connect.update.LocalPackage;
import org.nuxeo.connect.update.PackageException;
import org.nuxeo.connect.update.ValidationStatus;

/**
 * A task implements the logic of an install or uninstall. If this gets wrong
 * (the run method is throwing an exception) then the rollback method should be
 * invoked to revert all modifications that was done by the run method.
 * 
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public interface Task {

    /**
     * Initializes the task. Must be called before executing or validating the
     * task.
     * <p>
     * The given parameters are the ones filled by the user in the installation
     * wizard. If no parameters are specified an empty or a null map is
     * specified.
     */
    void initialize(LocalPackage pkg, boolean restart) throws PackageException;

    /**
     * Validates that the task can be run. Returns a validation state. If the
     * state contain errors the task cannot be run on the running platform. If
     * the state contains warnings the user should be asked if it really want to
     * run the task.
     */
    ValidationStatus validate() throws PackageException;

    /**
     * Run the task. Throws an exception if something goes wrong. At the end of
     * the run the commands log must be written on disk.
     * 
     * @param params the user parameters or null if none.
     */
    void run(Map<String, String> params) throws PackageException;

    /**
     * Rollback the work done so far. Should be called if the run method failed.
     */
    void rollback() throws PackageException;

    /**
     * The target package.
     */
    LocalPackage getPackage();

    /**
     * Whether or not the platform must be restarted after the task is executed.
     * 
     * @return
     */
    boolean isRestartRequired();

    /**
     * Sets if restart is required
     */
    void setRestartRequired(boolean isRestartRequired);

    /**
     * Get the file path relative to server home. If the file is not located
     * inside the server home, the absolute file path of the file is returned.
     * 
     * @param file
     * @return
     */
    String getRelativeFilePath(File file);
}
