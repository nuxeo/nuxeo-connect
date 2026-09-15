/*
 * (C) Copyright 2006-2026 Nuxeo (http://nuxeo.com/) and contributors.
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

import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.PackageType;
import org.nuxeo.connect.update.Validator;
import org.nuxeo.connect.update.Version;
import org.nuxeo.connect.update.task.Task;

/**
 * Describe the APIs to write the minimal package properties and the getters not exposed from {@link Package}.
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public interface PackageDefinition extends Package {

    /**
     * Set the package name.
     */
    void setName(String name);

    /**
     * Set the package version.
     */
    void setVersion(Version version);

    /**
     * Set the package type.
     */
    void setType(PackageType type);

    /**
     * Set the package title.
     */
    void setTitle(String title);

    /**
     * Set the package description.
     */
    void setDescription(String description);

    /**
     * Set the package classifier.
     */
    void setClassifier(String classifier);

    /**
     * Set the package vendor string.
     */
    void setVendor(String vendor);

    /**
     * Set the package license name.
     */
    void setLicenseType(String license);

    /**
     * Set the license URL.
     */
    void setLicenseUrl(String url);

    /**
     * Set the target platforms of this package.
     *
     * @see #getTargetPlatforms()
     */
    void setTargetPlatforms(String[] platforms);

    /**
     * Set the target platform range of this package.
     * 
     * @see #getTargetPlatformRange()
     */
    void setTargetPlatformRange(String targetPlatformRange);

    /**
     * Set the target platform name of this package.
     * 
     * @see #getTargetPlatformRange()
     */
    void setTargetPlatformName(String targetPlatformName);

    /**
     * Set the package dependencies.
     *
     * @see #getDependencies()
     */
    void setDependencies(PackageDependency[] deps);

    /**
     * Set the package optional dependencies.
     *
     * @see #getOptionalDependencies()
     * @since 1.5.2
     */
    void setOptionalDependencies(PackageDependency[] deps);

    /**
     * Set the package conflicts.
     *
     * @see #getConflicts()
     */
    void setConflicts(PackageDependency[] deps);

    /**
     * Set the package provides.
     *
     * @see #getProvides()
     */
    void setProvides(PackageDependency[] deps);

    /**
     * Get the package installer definition. The installer is a class implementing {@link Task}. if not specified the
     * default implementation will be used
     *
     * @return the package installer. If not set null is returned.
     */
    TaskDefinition getInstaller();

    /**
     * Set the package installer.
     *
     * @see PackageDefinition#getInstaller()
     */
    void setInstaller(TaskDefinition installer);

    /**
     * Get the package uninstaller. The uninstaller is a class implementing {@link Task}. if not specified the default
     * implementation will be used
     *
     * @return the package uninstaller. If not set null is returned.
     */
    TaskDefinition getUninstaller();

    /**
     * Set the package uninstaller.
     *
     * @see #getUninstaller()
     */
    void setUninstaller(TaskDefinition uninstaller);

    /**
     * Get the package validator. Validators can be used to test that an installation succeeded. The validator is a
     * class implementing {@link Validator}. If not specified not post install validation will be done
     *
     * @return the validator class name or null if none.
     */
    String getValidator();

    /**
     * Set the package validator class name.
     *
     * @see #getValidator()
     */
    void setValidator(String validator);

    /**
     * Get an XML representation of this package definition.
     */
    String toXML();

    /**
     * Test if terms and conditions should be accepted by user
     */
    boolean requireTermsAndConditionsAcceptance();

    /**
     * @since 1.4
     */
    void setHotReloadSupport(boolean hotReloadSupport);

    /**
     * @since 1.4
     */
    void setRequireTermsAndConditionsAcceptance(boolean requireTermsAndConditionsAcceptance);

}
