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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.nuxeo.connect.update.model.PackageDefinition;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public interface PackageUpdateService {

    /**
     * Initialize the service. This is usually doing file system initialization
     * and loading the list of local packages.
     * 
     * @throws Exception
     */
    void initialize() throws PackageException;

    /**
     * Close any resource held by this service. The service instance will no
     * more be available.
     * 
     * @throws Exception
     */
    void shutdown() throws PackageException;

    /**
     * Set the state for the given package.
     * 
     * @param id
     * @param state
     */
    void setPackageState(LocalPackage pkg, int state) throws PackageException;

    /**
     * Add a new package to the packages registry given the package file (a zip
     * or directory). The package will be added and put in
     * {@link PackageState#DOWNLOADED} state.
     * 
     * @param file the package file.
     * @return the package object.
     * @throws Exception
     */
    LocalPackage addPackage(File file) throws PackageException;

    /**
     * Removes a package from the packages registry given its ID.
     * The package has to be in {@link PackageState#DOWNLOADED} state.
     *
     * @param id
     * @throws Exception
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
     * Get a list with all local packages (that were already registered - i.e.
     * downloaded).
     * 
     * @return
     */
    List<LocalPackage> getPackages() throws PackageException;

    /**
     * Get the local package having the given name and which is in either one of
     * the following states:
     * <ul>
     * <li> {@link PackageState#INSTALLING}
     * <li> {@link PackageState#INSTALLED}
     * <li> {@link PackageState#STARTED}
     * </ul>
     * 
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
     * @return
     */
    public PackageDefinition loadPackageFromZip(File zip)
            throws PackageException;

    /**
     * Load the package definition from the given XML file.
     * 
     * @param file
     * @return
     */
    public PackageDefinition loadPackage(File file) throws PackageException;

    /**
     * Load the package definition for the given XML input stream.
     * 
     * @param in
     * @return
     * @throws IOException
     */
    public PackageDefinition loadPackage(InputStream in)
            throws PackageException;

    /**
     * Reset the index file -> mark all packages as downloaded.
     * Do not modify package data. This method should be used after a system upgrade 
     * to reset packages that were previously installed.
     * 
     * This is usually invoked from command line tools by the administrator after 
     * an upgrade to reset the downloaded package states.  
     * 
     * @throws PackageException
     */
    public void reset() throws PackageException;

}
