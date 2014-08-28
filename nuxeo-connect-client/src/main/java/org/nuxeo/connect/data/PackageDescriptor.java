/*
 * (C) Copyright 2006-2014 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *
 */

package org.nuxeo.connect.data;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.nuxeo.connect.data.marshaling.JSONExportMethod;
import org.nuxeo.connect.data.marshaling.JSONExportableField;
import org.nuxeo.connect.data.marshaling.JSONImportMethod;
import org.nuxeo.connect.update.NuxeoValidationState;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.PackageType;
import org.nuxeo.connect.update.PackageVisibility;
import org.nuxeo.connect.update.ProductionState;
import org.nuxeo.connect.update.Version;

/**
 * DTO implementation of the {@link DownloadablePackage} interface. Used to
 * transfer {@link Package} description between server and client.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class PackageDescriptor extends AbstractJSONSerializableData implements
        DownloadablePackage {

    /**
     * @deprecated Since 1.0. Use {@link #loadFromJSON(Class, JSONObject)}
     *             instead.
     */
    @Deprecated
    public static PackageDescriptor loadFromJSON(JSONObject json)
            throws JSONException {
        return loadFromJSON(PackageDescriptor.class, json);
    }

    /**
     * @deprecated Since 1.0. Use {@link #loadFromJSON(Class, String)} instead.
     */
    @Deprecated
    public static PackageDescriptor loadFromJSON(String json)
            throws JSONException {
        return loadFromJSON(new JSONObject(json));
    }

    @JSONExportableField
    protected String homePage;

    @JSONExportableField
    protected String classifier;

    @JSONExportableField
    protected String description;

    @JSONExportableField
    protected String name;

    @JSONExportableField
    protected String vendor;

    @JSONExportableField
    protected int state;

    @JSONExportableField
    protected String license;

    @JSONExportableField
    protected String licenseUrl;

    @JSONExportableField
    protected String[] targetPlatforms;

    protected PackageDependency[] dependencies;

    protected PackageDependency[] conflicts;

    protected PackageDependency[] provides;

    @JSONExportableField
    protected String title;

    @JSONExportableField
    protected PackageType type;

    @JSONExportableField
    protected Version version;

    @JSONExportableField
    protected String sourceDigest;

    @JSONExportableField
    protected String sourceUrl;

    @JSONExportableField
    protected long sourceSize;

    @JSONExportableField
    protected int commentsNumber;

    @JSONExportableField
    protected String pictureUrl;

    @JSONExportableField
    protected int downloadsCount;

    @JSONExportableField
    protected int rating;

    @JSONExportableField
    protected ProductionState productionState;

    @JSONExportableField
    protected NuxeoValidationState nuxeoValidationState;

    @JSONExportableField
    protected boolean supported;

    @JSONExportableField
    protected boolean supportsHotReload;

    @JSONExportableField
    protected PackageVisibility visibility;

    private boolean local = false;

    public PackageDescriptor() {
        super();
    }

    /**
     * @since 1.4
     */
    public PackageDescriptor(Package descriptor) {
        super();
        this.classifier = descriptor.getClassifier();
        this.dependencies = descriptor.getDependencies();
        this.conflicts = descriptor.getConflicts();
        this.description = descriptor.getDescription();
        this.homePage = descriptor.getHomePage();
        this.license = descriptor.getLicenseType();
        this.licenseUrl = descriptor.getLicenseUrl();
        this.name = descriptor.getName();
        this.productionState = descriptor.getProductionState();
        this.provides = descriptor.getProvides();
        this.state = descriptor.getState();
        this.targetPlatforms = descriptor.getTargetPlatforms();
        this.title = descriptor.getTitle();
        this.type = descriptor.getType();
        this.vendor = descriptor.getVendor();
        this.version = descriptor.getVersion();
        this.visibility = descriptor.getVisibility();
        this.nuxeoValidationState = descriptor.getValidationState();
        this.supported = descriptor.isSupported();
        this.supportsHotReload = descriptor.supportsHotReload();
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public int getCommentsNumber() {
        return commentsNumber;
    }

    @Override
    public PackageDependency[] getConflicts() {
        if (conflicts == null) {
            return new PackageDependency[0];
        }
        return conflicts;
    }

    @JSONExportMethod(name = "conflicts")
    protected JSONArray getConflictsAsJSON() {
        JSONArray deps = new JSONArray();
        for (PackageDependency dep : getConflicts()) {
            deps.put(dep.toString());
        }
        return deps;
    }

    public String getConflictsAsString() {
        StringBuilder sb = new StringBuilder();

        if (conflicts == null || conflicts.length == 0) {
            return "";
        }

        for (PackageDependency dep : getConflicts()) {
            sb.append(dep.toString());
            sb.append(",");
        }

        return sb.toString();
    }

    @Override
    public PackageDependency[] getDependencies() {
        if (dependencies == null) {
            return new PackageDependency[0];
        }
        return dependencies;
    }

    @JSONExportMethod(name = "dependencies")
    protected JSONArray getDependenciesAsJSON() {
        JSONArray deps = new JSONArray();
        for (PackageDependency dep : getDependencies()) {
            deps.put(dep.toString());
        }
        return deps;
    }

    public String getDependenciesAsString() {
        StringBuilder sb = new StringBuilder();

        if (dependencies == null || dependencies.length == 0) {
            return "";
        }

        for (PackageDependency dep : getDependencies()) {
            sb.append(dep.toString());
            sb.append(",");
        }

        return sb.toString();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getDownloadsCount() {
        return downloadsCount;
    }

    @Override
    public String getHomePage() {
        return homePage;
    }

    @Override
    public String getId() {
        if (getVersion() == null) {
            return getName();
        } else {
            return getName() + "-" + getVersion();
        }
    }

    @Override
    public String getLicenseType() {
        return license;
    }

    @Override
    public String getLicenseUrl() {
        return licenseUrl;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPictureUrl() {
        return pictureUrl;
    }

    @Override
    public ProductionState getProductionState() {
        if (productionState == null) {
            return ProductionState.TESTING;
        } else {
            return productionState;
        }
    }

    @Override
    public PackageDependency[] getProvides() {
        if (provides == null) {
            return new PackageDependency[0];
        }
        return provides;
    }

    @JSONExportMethod(name = "provides")
    protected JSONArray getProvidesAsJSON() {
        JSONArray deps = new JSONArray();
        for (PackageDependency dep : getProvides()) {
            deps.put(dep.toString());
        }
        return deps;
    }

    public String getProvidesAsString() {
        StringBuilder sb = new StringBuilder();

        if (provides == null || provides.length == 0) {
            return "";
        }

        for (PackageDependency dep : getProvides()) {
            sb.append(dep.toString());
            sb.append(",");
        }

        return sb.toString();
    }

    @Override
    public int getRating() {
        return rating;
    }

    @Override
    public String getSourceDigest() {
        return sourceDigest;
    }

    @Override
    public long getSourceSize() {
        return sourceSize;
    }

    @Override
    public String getSourceUrl() {
        return sourceUrl;
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public String[] getTargetPlatforms() {
        return targetPlatforms;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public PackageType getType() {
        return type;
    }

    @Override
    public NuxeoValidationState getValidationState() {
        if (nuxeoValidationState == null) {
            return NuxeoValidationState.NONE;
        } else {
            return nuxeoValidationState;
        }
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
    public PackageVisibility getVisibility() {
        return visibility;
    }

    @Override
    public boolean isLocal() {
        return local;
    }

    @Override
    public boolean isSupported() {
        return supported;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public void setCommentsNumber(int commentsNumber) {
        this.commentsNumber = commentsNumber;
    }

    public void setConflicts(PackageDependency[] conflicts) {
        this.conflicts = conflicts;
    }

    @JSONImportMethod(name = "conflicts")
    protected void setConflictsAsJSON(JSONArray array) throws JSONException {
        PackageDependency[] deps = new PackageDependency[array.length()];
        for (int i = 0; i < array.length(); i++) {
            deps[i] = new PackageDependency(array.getString(i));
        }
        setConflicts(deps);
    }

    public void setDependencies(PackageDependency[] dependencies) {
        this.dependencies = dependencies;
    }

    @JSONImportMethod(name = "dependencies")
    protected void setDependenciesAsJSON(JSONArray array) throws JSONException {
        PackageDependency[] deps = new PackageDependency[array.length()];
        for (int i = 0; i < array.length(); i++) {
            deps[i] = new PackageDependency(array.getString(i));
        }
        setDependencies(deps);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDownloadsCount(int downloadsCount) {
        this.downloadsCount = downloadsCount;
    }

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public void setLocal(boolean isLocal) {
        local = isLocal;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNuxeoValidationState(
            NuxeoValidationState nuxeoValidationState) {
        this.nuxeoValidationState = nuxeoValidationState;
    }

    @JSONImportMethod(name = "nuxeoValidationState")
    public void setNuxeoValidationStateAsJSON(String state) {
        nuxeoValidationState = NuxeoValidationState.getByValue(state);
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public void setProductionState(ProductionState productionState) {
        this.productionState = productionState;
    }

    @JSONImportMethod(name = "productionState")
    public void setProductionStateAsJSON(String state) {
        productionState = ProductionState.getByValue(state);
    }

    public void setProvides(PackageDependency[] provides) {
        this.provides = provides;
    }

    @JSONImportMethod(name = "provides")
    protected void setProvidesAsJSON(JSONArray array) throws JSONException {
        PackageDependency[] deps = new PackageDependency[array.length()];
        for (int i = 0; i < array.length(); i++) {
            deps[i] = new PackageDependency(array.getString(i));
        }
        setProvides(deps);
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setSourceDigest(String sourceDigest) {
        this.sourceDigest = sourceDigest;
    }

    public void setSourceSize(long sourceSize) {
        this.sourceSize = sourceSize;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    public void setSupportsHotReload(boolean supportsHotReload) {
        this.supportsHotReload = supportsHotReload;
    }

    public void setTargetPlatforms(List<String> targetPlatforms) {
        this.targetPlatforms = targetPlatforms == null ? new String[0]
                : targetPlatforms.toArray(new String[targetPlatforms.size()]);
    }

    public void setTargetPlatforms(String[] targetPlatforms) {
        this.targetPlatforms = targetPlatforms;
    }

    @JSONImportMethod(name = "targetPlatforms")
    public void setTargetPlatformsAsJSON(JSONArray array) throws JSONException {
        String[] targets = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            targets[i] = array.getString(i);
        }
        targetPlatforms = targets;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(PackageType type) {
        this.type = type;
    }

    @JSONImportMethod(name = "type")
    public void setTypeAsJSON(String strType) {
        type = PackageType.getByValue(strType);
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    @JSONImportMethod(name = "version")
    public void setVersionAsJSON(String v) {
        version = new Version(v);
    }

    public void setVisibility(PackageVisibility visibility) {
        this.visibility = visibility;
    }

    @JSONImportMethod(name = "visibility")
    public void setVisibilityAsJSON(String strVisibility) {
        visibility = PackageVisibility.valueOf(strVisibility);
    }

    @Override
    public boolean supportsHotReload() {
        return supportsHotReload;
    }

    @Override
    public String toString() {
        return getId();
    }

}
