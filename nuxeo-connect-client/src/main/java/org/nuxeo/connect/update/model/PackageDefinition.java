/*
 * (C) Copyright 2006-2016 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     jcarsique
 *     Yannis JULIENNE
 */
package org.nuxeo.connect.update.model;

import org.nuxeo.connect.update.NuxeoValidationState;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.PackageType;
import org.nuxeo.connect.update.PackageVisibility;
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
public interface PackageDefinition extends Package {

    /**
     * Set the package name.
     *
     * @param name
     */
    void setName(String name);

    /**
     * Set the package version.
     *
     * @param version
     */
    void setVersion(Version version);

    /**
     * Set the package type.
     *
     * @param type
     */
    void setType(PackageType type);

    /**
     * Set the package title.
     *
     * @param title
     */
    void setTitle(String title);

    /**
     * Set the package description.
     *
     * @param description
     */
    void setDescription(String description);

    /**
     * Set the package classifier.
     *
     * @param classifier
     */
    void setClassifier(String classifier);

    /**
     * Set the package vendor string.
     *
     * @param vendor
     */
    void setVendor(String vendor);

    /**
     * Set the package web page URL.
     *
     * @param homePage
     * @see #getHomePage()
     */
    void setHomePage(String homePage);

    /**
     * Get the package license name. E.g. LGPL, BSD etc.
     *
     * @deprecated Since 1.4.5. Duplicates {@link #getLicenseType()}.
     */
    @Deprecated
    String getLicense();

    /**
     * Set the package license name.
     *
     * @param license
     * @deprecated Since 1.4.5. Duplicates {@link #setLicenseType(String)}.
     */
    @Deprecated
    void setLicense(String license);

    /**
     * Set the package license name.
     *
     * @param license
     */
    void setLicenseType(String license);

    /**
     * Set the license URL.
     *
     * @param url
     */
    void setLicenseUrl(String url);

    /**
     * Get the target platforms where this package may be installed.
     *
     * @deprecated Since 1.4.5. Duplicates {@link #getTargetPlatforms()}.
     */
    @Deprecated
    String[] getPlatforms();

    /**
     * Set the target platforms of this package.
     *
     * @param platforms
     * @see #getPlatforms()
     * @deprecated Since 1.4.5. Duplicates {@link #setTargetPlatforms(String[])}
     *             .
     */
    @Deprecated
    void setPlatforms(String[] platforms);

    /**
     * Set the target platforms of this package.
     *
     * @param platforms
     * @see #getTargetPlatforms()
     */
    void setTargetPlatforms(String[] platforms);

    /**
     * Set the package dependencies.
     *
     * @param deps
     * @see #getDependencies()
     */
    void setDependencies(PackageDependency[] deps);

    /**
     * Set the package optional dependencies.
     *
     * @param deps
     * @see #getOptionalDependencies()
     * @since 1.5.2
     */
    void setOptionalDependencies(PackageDependency[] deps);

    /**
     * Set the package conflicts.
     *
     * @param deps
     * @see #getConflicts()
     */
    void setConflicts(PackageDependency[] deps);

    /**
     * Set the package provides.
     *
     * @param deps
     * @see #getProvides()
     */
    void setProvides(PackageDependency[] deps);

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
     */
    public String toXML();

    /**
     * Test if terms and conditions should be accepted by user
     *
     */
    public boolean requireTermsAndConditionsAcceptance();

    /**
     * @since 1.4
     */
    void setSupported(boolean supported);

    /**
     * @since 1.4
     */
    void setHotReloadSupport(boolean hotReloadSupport);

    /**
     * @since 1.4
     */
    void setValidationState(NuxeoValidationState validationState);

    /**
     * @since 1.4
     */
    void setProductionState(ProductionState productionState);

    /**
     * @since 1.4
     */
    void setRequireTermsAndConditionsAcceptance(
            boolean requireTermsAndConditionsAcceptance);

    /**
     * @since 1.4
     */
    void setVisibility(PackageVisibility visibility);

}
