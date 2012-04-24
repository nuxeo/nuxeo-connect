/*
 * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Nuxeo - initial API and implementation
 */

package org.nuxeo.connect.packages;

import java.io.File;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.update.LocalPackage;
import org.nuxeo.connect.update.NuxeoValidationState;
import org.nuxeo.connect.update.PackageData;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.PackageException;
import org.nuxeo.connect.update.PackageType;
import org.nuxeo.connect.update.ProductionState;
import org.nuxeo.connect.update.Validator;
import org.nuxeo.connect.update.Version;
import org.nuxeo.connect.update.model.Form;
import org.nuxeo.connect.update.task.Task;

public class LocalPackageAsDownloadablePackage implements LocalPackage,
        DownloadablePackage {

    protected LocalPackage localPackage;

    public LocalPackageAsDownloadablePackage(LocalPackage localPackage) {
        this.localPackage = localPackage;
    }

    public PackageData getData() {
        return localPackage.getData();
    }

    public File getInstallFile() throws PackageException {
        return localPackage.getInstallFile();
    }

    public Form[] getInstallForms() throws PackageException {
        return localPackage.getInstallForms();
    }

    public Task getInstallTask() throws PackageException {
        return localPackage.getInstallTask();
    }

    public String getLicenseType() {
        return localPackage.getLicenseType();
    }

    public String getLicenseUrl() {
        return localPackage.getLicenseUrl();
    }

    public String getLicenseContent() throws PackageException {
        return localPackage.getLicenseContent();
    }

    public File getUninstallFile() throws PackageException {
        return localPackage.getUninstallFile();
    }

    public Form[] getUninstallForms() throws PackageException {
        return localPackage.getUninstallForms();
    }

    public Task getUninstallTask() throws PackageException {
        return localPackage.getUninstallTask();
    }

    public Form[] getValidationForms() throws PackageException {
        return localPackage.getValidationForms();
    }

    public Validator getValidator() throws PackageException {
        return localPackage.getValidator();
    }

    public void setState(int state) {
        localPackage.setState(state);
    }

    public String getClassifier() {
        return localPackage.getClassifier();
    }

    public PackageDependency[] getDependencies() {
        return localPackage.getDependencies();
    }

    public PackageDependency[] getConflicts() {
        return localPackage.getConflicts();
    }

    public PackageDependency[] getProvides() {
        return localPackage.getProvides();
    }

    public String getDescription() {
        return localPackage.getDescription();
    }

    public String getHomePage() {
        return localPackage.getHomePage();
    }

    public String getId() {
        return localPackage.getId();
    }

    public String getName() {
        return localPackage.getName();
    }

    public int getState() {
        return localPackage.getState();
    }

    public String[] getTargetPlatforms() {
        return localPackage.getTargetPlatforms();
    }

    public String getTitle() {
        return localPackage.getTitle();
    }

    public PackageType getType() {
        return localPackage.getType();
    }

    public String getVendor() {
        return localPackage.getVendor();
    }

    public Version getVersion() {
        return localPackage.getVersion();
    }

    public boolean requireTermsAndConditionsAcceptance() {
        return localPackage.requireTermsAndConditionsAcceptance();
    }

    public String getTermsAndConditionsContent() throws PackageException {
        return localPackage.getTermsAndConditionsContent();
    }

    public ProductionState getProductionState() {
        return localPackage.getProductionState();
    }

    public NuxeoValidationState getValidationState() {
        return localPackage.getValidationState();
    }

    public boolean supportsHotReload() {
        return localPackage.supportsHotReload();
    }

    public boolean isSupported() {
        return localPackage.isSupported();
    }

    public boolean isLocal() {
        return true;
    }

    public int getCommentsNumber() {
        return 0;
    }

    public int getDownloadsCount() {
        return 0;
    }

    public String getPictureUrl() {
        return null;
    }

    public int getRating() {
        return 0;
    }

    public String getSourceDigest() {
        return null;
    }

    public long getSourceSize() {
        return 0;
    }

    public String getSourceUrl() {
        return null;
    }

    @Override
    public String toString() {
       return getId();
    }
}
