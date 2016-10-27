/*
 * (C) Copyright 2012-2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Julien Carsique
 *     Yannis JULIENNE
 *
 */

package org.nuxeo.connect.connector.fake;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.update.NuxeoValidationState;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.PackageState;
import org.nuxeo.connect.update.PackageType;
import org.nuxeo.connect.update.PackageVisibility;
import org.nuxeo.connect.update.ProductionState;
import org.nuxeo.connect.update.Version;

/**
 * @since 1.4
 */
public class FakeDownloadablePackage implements DownloadablePackage {

    private String name;

    private Version version;

    private String classifier;

    private List<PackageDependency> dependencies = new ArrayList<>();

    private List<PackageDependency> optionalDependencies = new ArrayList<>();

    private List<PackageDependency> conflicts = new ArrayList<>();

    public String id;

    public String title;

    public String description;

    public PackageType type;

    public String vendor;

    public List<String> targetPlatforms = new ArrayList<>();

    public PackageState packageState = PackageState.UNKNOWN;

    public String homePage;

    public String licenseType;

    public String licenseUrl;

    public ProductionState productionState;

    public NuxeoValidationState validationState;

    public PackageVisibility visibility;

    public FakeDownloadablePackage(String name, Version version) {
        this.name = name;
        this.version = version;
        classifier = version.classifier();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public PackageType getType() {
        return type;
    }

    @Override
    public String getVendor() {
        return vendor;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public String[] getTargetPlatforms() {
        return targetPlatforms.toArray(new String[targetPlatforms.size()]);
    }

    @Override
    public PackageDependency[] getDependencies() {
        return dependencies.toArray(new PackageDependency[dependencies.size()]);
    }

    @Override
    public PackageDependency[] getOptionalDependencies() {
        return optionalDependencies.toArray(new PackageDependency[optionalDependencies.size()]);
    }

    @Override
    public PackageDependency[] getConflicts() {
        return conflicts.toArray(new PackageDependency[conflicts.size()]);
    }

    @Override
    public PackageDependency[] getProvides() {
        return null;
    }

    @Deprecated
    @Override
    public int getState() {
        return packageState.getValue();
    }

    @Override
    public PackageState getPackageState() {
        return packageState;
    }

    @Override
    public String getHomePage() {
        return homePage;
    }

    @Override
    public String getLicenseType() {
        return licenseType;
    }

    @Override
    public String getLicenseUrl() {
        return licenseUrl;
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public boolean isLocal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProductionState getProductionState() {
        return productionState;
    }

    @Override
    public NuxeoValidationState getValidationState() {
        return validationState;
    }

    @Override
    public boolean supportsHotReload() {
        return false;
    }

    @Override
    public boolean isSupported() {
        return false;
    }

    @Override
    public String getSourceDigest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSourceUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getSourceSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRating() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCommentsNumber() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPictureUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDownloadsCount() {
        throw new UnsupportedOperationException();
    }

    public void addDependency(PackageDependency dependency) {
        dependencies.add(dependency);
    }

    public void addConflict(PackageDependency conflict) {
        conflicts.add(conflict);
    }

    @Override
    public PackageVisibility getVisibility() {
        return visibility;
    }

}
