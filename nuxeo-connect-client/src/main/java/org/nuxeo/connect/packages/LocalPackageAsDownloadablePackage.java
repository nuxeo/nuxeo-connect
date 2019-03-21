/*
 * (C) Copyright 2010-2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Nuxeo - initial API and implementation
 *     Yannis JULIENNE
 */

package org.nuxeo.connect.packages;

import java.io.File;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.update.LocalPackage;
import org.nuxeo.connect.update.NuxeoValidationState;
import org.nuxeo.connect.update.PackageData;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.PackageException;
import org.nuxeo.connect.update.PackageState;
import org.nuxeo.connect.update.PackageType;
import org.nuxeo.connect.update.ProductionState;
import org.nuxeo.connect.update.Validator;
import org.nuxeo.connect.update.Version;
import org.nuxeo.connect.update.model.Form;
import org.nuxeo.connect.update.task.Task;

public class LocalPackageAsDownloadablePackage implements LocalPackage, DownloadablePackage {

    protected LocalPackage localPackage;

    public LocalPackageAsDownloadablePackage(LocalPackage localPackage) {
        this.localPackage = localPackage;
    }

    @Override
    public PackageData getData() {
        return localPackage.getData();
    }

    @Override
    public File getInstallFile() throws PackageException {
        return localPackage.getInstallFile();
    }

    @Override
    public Form[] getInstallForms() throws PackageException {
        return localPackage.getInstallForms();
    }

    @Override
    public Task getInstallTask() throws PackageException {
        return localPackage.getInstallTask();
    }

    @Override
    public String getLicenseType() {
        return localPackage.getLicenseType();
    }

    @Override
    public String getLicenseUrl() {
        return localPackage.getLicenseUrl();
    }

    @Override
    public String getLicenseContent() throws PackageException {
        return localPackage.getLicenseContent();
    }

    @Override
    public File getUninstallFile() throws PackageException {
        return localPackage.getUninstallFile();
    }

    @Override
    public Form[] getUninstallForms() throws PackageException {
        return localPackage.getUninstallForms();
    }

    @Override
    public Task getUninstallTask() throws PackageException {
        return localPackage.getUninstallTask();
    }

    @Override
    public Form[] getValidationForms() throws PackageException {
        return localPackage.getValidationForms();
    }

    @Override
    public Validator getValidator() throws PackageException {
        return localPackage.getValidator();
    }

    @Override
    @Deprecated
    public void setState(int state) {
        localPackage.setState(state);
    }

    @Override
    public void setState(PackageState state) {
        localPackage.setState(state);
    }

    @Override
    public String getClassifier() {
        return localPackage.getClassifier();
    }

    @Override
    public PackageDependency[] getDependencies() {
        return localPackage.getDependencies();
    }

    @Override
    public PackageDependency[] getOptionalDependencies() {
        return localPackage.getOptionalDependencies();
    }

    @Override
    public PackageDependency[] getConflicts() {
        return localPackage.getConflicts();
    }

    @Override
    public PackageDependency[] getProvides() {
        return localPackage.getProvides();
    }

    @Override
    public String getDescription() {
        return localPackage.getDescription();
    }

    @Override
    public String getHomePage() {
        return localPackage.getHomePage();
    }

    @Override
    public String getId() {
        return localPackage.getId();
    }

    @Override
    public String getName() {
        return localPackage.getName();
    }

    @Deprecated
    @Override
    public int getState() {
        return localPackage.getState();
    }

    @Override
    public PackageState getPackageState() {
        return localPackage.getPackageState();
    }

    @Override
    public String[] getTargetPlatforms() {
        return localPackage.getTargetPlatforms();
    }

    @Override
    public String getTitle() {
        return localPackage.getTitle();
    }

    @Override
    public PackageType getType() {
        return localPackage.getType();
    }

    @Override
    public String getVendor() {
        return localPackage.getVendor();
    }

    @Override
    public Version getVersion() {
        return localPackage.getVersion();
    }

    @Override
    public boolean requireTermsAndConditionsAcceptance() {
        return localPackage.requireTermsAndConditionsAcceptance();
    }

    @Override
    public String getTermsAndConditionsContent() throws PackageException {
        return localPackage.getTermsAndConditionsContent();
    }

    @Override
    public ProductionState getProductionState() {
        return localPackage.getProductionState();
    }

    @Override
    public NuxeoValidationState getValidationState() {
        return localPackage.getValidationState();
    }

    @Override
    public boolean supportsHotReload() {
        return localPackage.supportsHotReload();
    }

    @Override
    public boolean isSupported() {
        return localPackage.isSupported();
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public int getCommentsNumber() {
        return 0;
    }

    @Override
    public int getDownloadsCount() {
        return 0;
    }

    @Override
    public String getPictureUrl() {
        return null;
    }

    @Override
    public int getRating() {
        return 0;
    }

    @Override
    public String getSourceDigest() {
        return null;
    }

    @Override
    public long getSourceSize() {
        return 0;
    }

    @Override
    public String getSourceUrl() {
        return null;
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public boolean hasSubscriptionRequired() {
        return false;
    }

    @Override
    public String getOwner() {
        return null;
    }

}
