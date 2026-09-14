/*
 * (C) Copyright 2006-2026 Nuxeo (http://nuxeo.com/) and others.
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

import org.nuxeo.connect.update.model.Form;
import org.nuxeo.connect.update.task.Task;

/**
 * A package that is stored in the local persistence area.
 * <p/>
 * This package contains more information than a remote package like install wizards, files to install etc.
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public interface LocalPackage extends Package {

    /**
     * The package descriptor file.
     */
    String MANIFEST = "package.xml";

    /**
     * A text file containing the package license.
     */
    String LICENSE = "license.txt";

    /**
     * A text file containing terms and conditions.
     */
    String TERMSANDCONDITIONS = "terms-and-conditions.txt";

    /**
     * The install commands file name
     */
    String INSTALL = "install.xml";

    /**
     * The generated install directory
     */
    String BACKUP_DIR = "backup";

    String INSTALL_PROPERTIES = "install.properties";

    String UNINSTALL = "uninstall.xml";

    /**
     * The custom install wizard forms file
     */
    String INSTALL_FORMS = "forms/install.xml";

    /**
     * The custom uninstall wizard forms file
     */
    String UNINSTALL_FORMS = "forms/uninstall.xml";

    /**
     * The custom validation wizard forms file
     */
    String VALIDATION_FORMS = "forms/validation.xml";

    /**
     * Set the package state. This method is not updating the persistence area - you must use
     * {@link PackageUpdateService#setPackageState(LocalPackage, PackageState)} instead.
     *
     * @since 1.4.5
     */
    void setState(PackageState state);

    /**
     * Get the content of the license file.
     */
    String getLicenseContent() throws PackageException;

    /**
     * Get the content of the terms and conditions file.
     */
    String getTermsAndConditionsContent() throws PackageException;

    /**
     * Get the attached bundle if any. Remote packages has no attached bundle and must return null. All local packages
     * have an attached bundle and must return non null.
     */
    PackageData getData();

    /**
     * Get the install commands file to execute when installing the package
     */
    File getInstallFile() throws PackageException;

    /**
     * Get the install log commands file to execute when uninstalling.
     */
    File getUninstallFile() throws PackageException;

    /**
     * Get the install task instance.
     */
    Task getInstallTask() throws PackageException;

    /**
     * Get the uninstall task instance.
     */
    Task getUninstallTask() throws PackageException;

    /**
     * Get the validator for this package. If no validator is specified for the package then null is returned.
     * <p/>
     * To register a custom installer use the <code>validator</code> element having as value the validator class name in
     * the package XML descriptor.
     *
     * @return the validator or null.
     */
    Validator getValidator() throws PackageException;

    /**
     * Get the list of the custom forms to be used in the install wizard to gather user input. If no custom forms are
     * provided null is returned.
     *
     * @return the array of forms or null
     */
    Form[] getInstallForms() throws PackageException;

    /**
     * Get the list of custom forms to be used in the uninstall wizard to gather the user input. If no custom forms are
     * provided null is returned.
     *
     * @return the list to forms or null
     */
    Form[] getUninstallForms() throws PackageException;

    /**
     * Get the list of custom forms to be used in the validation wizard to gather the user input. If no custom forms are
     * provided null is returned.
     *
     * @return the list to forms or null
     */
    Form[] getValidationForms() throws PackageException;

    /**
     * Indicates if terms and conditions must be accepted bu user before installation
     */
    boolean requireTermsAndConditionsAcceptance();

}
