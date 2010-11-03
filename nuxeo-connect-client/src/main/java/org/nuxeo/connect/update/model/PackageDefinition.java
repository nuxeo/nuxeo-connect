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
package org.nuxeo.connect.update.model;

import org.nuxeo.connect.update.NuxeoValidationState;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.PackageType;
import org.nuxeo.connect.update.ProductionState;
import org.nuxeo.connect.update.Validator;
import org.nuxeo.connect.update.Version;
import org.nuxeo.connect.update.task.Task;

/**
 * Describe a package.
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public interface PackageDefinition {

    /**
     * Get the package ID.
     *
     * The ID is a string composed from the package name and the package
     * version: <code>name-version</code>
     *
     * @return
     */
    String getId();

    /**
     * Get the package name.
     *
     * @return
     */
    String getName();

    /**
     * Set the package name.
     *
     * @param name
     */
    void setName(String name);

    /**
     * Get the package version.
     *
     * @return
     */
    Version getVersion();

    /**
     * Set the package version.
     *
     * @param version
     */
    void setVersion(Version version);

    /**
     * Get the package type.
     *
     * @return
     */
    PackageType getType();

    /**
     * Set the package type.
     *
     * @param type
     */
    void setType(PackageType type);

    /**
     * Get the package title.
     *
     * @return
     */
    String getTitle();

    /**
     * Set the package title.
     *
     * @param title
     */
    void setTitle(String title);

    /**
     * Get the package description.
     *
     * @return
     */
    String getDescription();

    /**
     * Set the package description.
     *
     * @param description
     */
    void setDescription(String description);

    /**
     * Get the package classifier.
     *
     * @return
     */
    String getClassifier();

    /**
     * Set the package classifier.
     *
     * @param classifier
     */
    void setClassifier(String classifier);

    /**
     * Get the package vendor string.
     *
     * The vendor represent the entity providing the package.
     *
     * @return
     */
    String getVendor();

    /**
     * Set the package vendor string.
     *
     * @param vendor
     */
    void setVendor(String vendor);

    /**
     * Get an URL to a web page where more information can be found about the
     * package.
     *
     * @return the package web page. may be null.
     */
    String getHomePage();

    /**
     * Set the package web page URL.
     *
     * @param homePage
     * @see #getHomePage()
     */
    void setHomePage(String homePage);

    /**
     * Get the package license name. E.g. LGPL, BSD etc.
     */
    String getLicense();

    /**
     * Set the package license name.
     *
     * @param license
     */
    void setLicense(String license);

    /**
     * Get the package license URL. If no specified the license.txt file in the
     * package is the license content.
     */
    String getLicenseUrl();

    /**
     * Set the license URL.
     *
     * @param url
     */
    void setLicenseUrl(String url);

    /**
     * Get the target platforms where this package may be installed.
     */
    String[] getPlatforms();

    /**
     * Set the target platforms of this package.
     *
     * @param platforms
     * @see #getPlatforms()
     */
    void setPlatforms(String[] platforms);

    /**
     * Get the package dependencies.
     *
     * The dependency value format is:
     * <code>package_name[:package_min_version[:package_max_version]]</code> if
     * no min and max version are specified the the last version should be used.
     *
     * @return an array of dependencies or null if no dependencies are set.
     */
    PackageDependency[] getDependencies();

    /**
     * Set the package dependencies.
     *
     * @param deps
     * @see #getDependencies()
     */
    void setDependencies(PackageDependency[] deps);

    /**
     * Get the package installer definition.
     *
     * The installer is a class implementing {@link Task}. if not specified the
     * default implementation will be used
     *
     * @return the package installer. If not set null is returned.
     */
    TaskDefinition getInstaller();

    /**
     * Set the package installer.
     *
     * @param installer
     * @see PackageDefinition#getInstaller()
     */
    void setInstaller(TaskDefinition installer);

    /**
     * Get the package uninstaller.
     *
     * The uninstaller is a class implementing {@link Task}. if not specified
     * the default implementation will be used
     *
     * @return the package uninstaller. If not set null is returned.
     */
    TaskDefinition getUninstaller();

    /**
     * Set the package uninstaller.
     *
     * @param uninstaller
     * @see #getUninstaller()
     */
    void setUninstaller(TaskDefinition uninstaller);

    /**
     * Get the package validator. Validators can be used to test that an
     * installation succeeded.
     *
     * The validator is a class implementing {@link Validator}. If not specified
     * not post install validation will be done
     *
     * @return the validator class name or null if none.
     */
    String getValidator();

    /**
     * Set the package validator class name.
     *
     * @param validator
     * @see #getValidator()
     */
    void setValidator(String validator);

    /**
     * Get an XML representation of this package definition.
     *
     * @return
     */
    public String toXML();

    /**
     * Get the production state of the package
     *
     * @return
     */
    public ProductionState getProductionState();

    /**
     * Get the validation state of the Package
     * @return
     */
    public NuxeoValidationState getValidationState();

    /**
     * Test if Package is supported by Nuxeo
     *
     * @return
     */
    public boolean isSupported();

    /**
     * Test if package supports HotReload
     *
     * @return
     */
    public boolean supportsHotReload();

    /**
     * Test if terms and conditions should be accepted by user
     *
     * @return
     */
    public boolean requireTermsAndConditionsAcceptance();

}

