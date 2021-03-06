/*
 * (C) Copyright 2006-2015 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     bstefanescu, mguillaume, jcarsique
 */
package org.nuxeo.connect.update;

import java.io.File;
import java.io.InputStream;
import java.nio.file.attribute.FileTime;
import java.util.List;

import org.nuxeo.connect.update.model.PackageDefinition;
import org.nuxeo.connect.update.task.Command;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public interface PackageUpdateService {

    /**
     * Get the directory where files packages and meta-data files are are stored
     */
    File getDataDir();

    /**
     * Initialize the service. This is usually doing file system initialization and loading the list of local packages.
     *
     * @throws PackageException
     */
    void initialize() throws PackageException;

    /**
     * Close any resource held by this service. The service instance will no more be available.
     *
     * @throws PackageException
     */
    void shutdown() throws PackageException;

    /**
     * Set the state for the given package.
     *
     * @param pkg
     * @param state
     * @see PackageState
     * @deprecated Since 1.4.5. See {@link #setPackageState(LocalPackage, PackageState)}
     */
    @Deprecated
    void setPackageState(LocalPackage pkg, int state) throws PackageException;

    /**
     * @param pkg
     * @param state
     * @since 1.4.5
     */
    void setPackageState(LocalPackage pkg, PackageState state) throws PackageException;

    /**
     * Add a new package to the packages registry given the package file (a zip or directory). The package will be added
     * and put in {@link PackageState#DOWNLOADED} state.
     *
     * @param file the package file.
     * @return the package object.
     * @throws PackageException
     */
    LocalPackage addPackage(File file) throws PackageException;

    /**
     * Removes a package from the packages registry given its ID. The package has to be in
     * {@link PackageState#DOWNLOADED} state.
     *
     * @param id
     * @throws PackageException
     */
    void removePackage(String id) throws PackageException;

    /**
     * Get a package object given its ID
     *
     * @param id
     * @return the package or null if not found
     */
    LocalPackage getPackage(String id) throws PackageException;

    /**
     * Get a list with all local packages (that were already registered - i.e. downloaded).
     */
    List<LocalPackage> getPackages() throws PackageException;

    /**
     * Get the local package having the given name and which is in either one of the following states:
     * <ul>
     * <li> {@link PackageState#INSTALLING}
     * <li> {@link PackageState#INSTALLED}
     * <li> {@link PackageState#STARTED}
     * </ul>
     * Return null if no such package is found.
     *
     * @param name the package name
     * @return the package or null if no package is found
     */
    LocalPackage getActivePackage(String name) throws PackageException;

    /**
     * Restart the running platform.
     */
    void restart() throws PackageException;

    /**
     * Load the package definition from the given package ZIP.
     *
     * @param zip
     */
    public PackageDefinition loadPackageFromZip(File zip) throws PackageException;

    /**
     * Load the package definition from the given XML file.
     *
     * @param file
     */
    public PackageDefinition loadPackage(File file) throws PackageException;

    /**
     * Load the package definition for the given XML input stream.
     *
     * @param in
     * @throws PackageException
     */
    public PackageDefinition loadPackage(InputStream in) throws PackageException;

    /**
     * Reset the index file: mark all packages as downloaded. Do not modify package data. This method should be used
     * after a system upgrade to reset packages that were previously installed. This is usually invoked from command
     * line tools by the administrator after an upgrade to reset the downloaded package states.
     *
     * @throws PackageException
     */
    public void reset() throws PackageException;

    /**
     * @since 1.4
     * @param id
     * @throws PackageException
     */
    public abstract Command getCommand(String id) throws PackageException;

    /**
     * Returns the class name of the InstallTask suitable for this instance of the service.
     *
     * @since 1.4
     */
    public abstract String getDefaultInstallTaskType();

    /**
     * Returns the class name of the UninstallTask suitable for this instance of the service.
     *
     * @since 1.4
     */
    public abstract String getDefaultUninstallTaskType();

    /**
     * Tell if a package is in {@link PackageState#STARTED} state.
     *
     * @since 1.4
     * @return false if not started, including not existing (downloaded) at all.
     */
    boolean isStarted(String pkgId);

    /**
     * @since 1.4
     * @return Packages registry file
     */
    File getRegistry();

    /**
     * @since 1.4
     * @return Packages backup directory
     */
    File getBackupDir();

    /**
     * @return Last modification time of the given package; null if not installed
     * @since 1.4.11
     */
    FileTime getInstallDate(String id);

}
