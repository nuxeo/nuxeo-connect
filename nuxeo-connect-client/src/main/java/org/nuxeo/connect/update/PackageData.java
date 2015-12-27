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
package org.nuxeo.connect.update;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A package data is the representation of the package data and configuration.
 * The package data is usually wrapping a folder that represent the unziped
 * package. The package data is able to locate embedded files and java/groovy
 * classes and provides all package parametrization details.
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public interface PackageData {

    /**
     * Get the package manifest - an XML file.
     *
     */
    File getManifest();

    /**
     * Get the package bundle root.
     *
     */
    File getRoot();

    /**
     * Get a package entry given a relative path to the package root.
     *
     * @param path
     */
    File getEntry(String path);

    /**
     * Get a package entry as a stream given a relative path.
     *
     * @param path
     *
     * @throws IOException
     */
    InputStream getEntryAsStream(String path) throws IOException;

    /**
     * Load a class from that package (can be Groovy) given the class name.
     *
     * @param name
     */
    Class<?> loadClass(String name) throws PackageException;

    /**
     * Get the class loader used to load classes from this package
     *
     * @return the class loader cannot be null
     */
    ClassLoader getLoader();

}
