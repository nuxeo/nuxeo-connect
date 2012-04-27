/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Julien Carsique
 *
 */

package org.nuxeo.connect.connector.fake;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.update.NuxeoValidationState;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.PackageType;
import org.nuxeo.connect.update.ProductionState;
import org.nuxeo.connect.update.Version;

/**
 *
 *
 * @since 5.6
 */
public class FakeDownloadablePackage implements DownloadablePackage {

    private String name;

    private Version version;

    private String classifier;

    private List<PackageDependency> dependencies = new ArrayList<PackageDependency>();

    private List<PackageDependency> conflicts = new ArrayList<PackageDependency>();

    public FakeDownloadablePackage(String name, Version version) {
        this.name = name;
        this.version = version;
        this.classifier = version.classifier();
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTitle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PackageType getType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVendor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public String[] getTargetPlatforms() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PackageDependency[] getDependencies() {
        return dependencies.toArray(new PackageDependency[dependencies.size()]);
    }

    @Override
    public PackageDependency[] getConflicts() {
        return conflicts.toArray(new PackageDependency[conflicts.size()]);
    }

    @Override
    public PackageDependency[] getProvides() {
        return null;
    }

    @Override
    public int getState() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHomePage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLicenseType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLicenseUrl() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public NuxeoValidationState getValidationState() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsHotReload() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSupported() {
        throw new UnsupportedOperationException();
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

}
