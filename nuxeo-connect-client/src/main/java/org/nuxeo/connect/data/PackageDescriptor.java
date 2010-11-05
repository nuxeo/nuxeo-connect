/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *
 * $Id$
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
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.PackageType;
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

    @JSONExportMethod(name="dependencies")
    protected JSONArray getDependenciesAsJSON() {
        JSONArray deps = new JSONArray();
        for (PackageDependency dep : getDependencies()) {
            deps.put(dep.toString());
        }
        return deps;
    }

    @JSONImportMethod(name="dependencies")
    protected void setDependenciesAsJSON(JSONArray array) throws JSONException {
        PackageDependency[] deps = new PackageDependency[array.length()];
        for (int i = 0; i < array.length(); i++) {
            deps[i] = new PackageDependency(array.getString(i));
        }
        setDependencies(deps);
    }

    @JSONImportMethod(name="targetPlatforms")
    public void setTargetPlatformsAsJSON(JSONArray array) throws JSONException {
        String[] targets = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            targets[i] = array.getString(i);
        }
        targetPlatforms = targets;
    }

    @JSONImportMethod(name = "type")
    public void setTypeAsJSON(String strType) {
        type=PackageType.getByValue(strType);
    }

    @JSONImportMethod(name = "version")
    public void setVersionAsJSON(String v) {
        version = new Version(v);
    }

    @JSONImportMethod(name = "productionState")
    public void setProductionStateAsJSON(String state) {
       productionState = ProductionState.getByValue(state);
    }

    @JSONImportMethod(name = "nuxeoValidationState")
    public void setNuxeoValidationStateAsJSON(String state) {
        nuxeoValidationState = NuxeoValidationState.getByValue(state);
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setCommentsNumber(int commentsNumber) {
        this.commentsNumber = commentsNumber;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public void setDownloadsCount(int downloadsCount) {
        this.downloadsCount = downloadsCount;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getHomePage() {
        return homePage;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        if (getVersion()==null) {
            return getName();
        } else {
            return getName() + "-" + getVersion();
        }
    }

    public String getName() {
        return name;
    }

    public String getVendor() {
        return vendor;
    }

    public int getState() {
        return state;
    }

    public String getLicenseType() {
        return license;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public String[] getTargetPlatforms() {
        return targetPlatforms;
    }

    public PackageDependency[] getDependencies() {
        if (dependencies == null) {
            return new PackageDependency[0];
        }
        return dependencies;
    }

    public String getTitle() {
        return title;
    }

    public PackageType getType() {
        return type;
    }

    public Version getVersion() {
        return version;
    }

    public boolean isLocal() {
        return false;
    }

    public String getSourceDigest() {
        return sourceDigest;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public long getSourceSize() {
        return sourceSize;
    }

    @Deprecated
    public static PackageDescriptor loadFromJSON(JSONObject json)
            throws JSONException {
        return PackageDescriptor.loadFromJSON(PackageDescriptor.class, json);
    }

    @Deprecated
    public static PackageDescriptor loadFromJSON(String json)
            throws JSONException {
        return loadFromJSON(new JSONObject(json));
    }

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTargetPlatforms(List<String> targetPlatforms) {
        this.targetPlatforms = targetPlatforms == null ? new String[0]
                : targetPlatforms.toArray(new String[targetPlatforms.size()]);
    }

    public void setTargetPlatforms(String[] targetPlatforms) {
        this.targetPlatforms = targetPlatforms;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(PackageType type) {
        this.type = type;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public void setSourceDigest(String sourceDigest) {
        this.sourceDigest = sourceDigest;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public void setSourceSize(long sourceSize) {
        this.sourceSize = sourceSize;
    }

    public void setDependencies(PackageDependency[] dependencies) {
        this.dependencies = dependencies;
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

    public int getCommentsNumber() {
        return commentsNumber;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public int getRating() {
        return rating;
    }

    public int getDownloadsCount() {
        return downloadsCount;
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public ProductionState getProductionState() {
        if (productionState==null) {
            return ProductionState.TESTING;
        } else {
            return productionState;
        }
    }

    @Override
    public NuxeoValidationState getValidationState() {
        if (nuxeoValidationState==null) {
            return NuxeoValidationState.NONE;
        } else {
            return nuxeoValidationState;
        }
    }

    @Override
    public boolean isSupported() {
        return supported;
    }

    @Override
    public boolean supportsHotReload() {
        return supportsHotReload;
    }

    public void setProductionState(ProductionState productionState) {
        this.productionState = productionState;
    }

    public void setNuxeoValidationState(NuxeoValidationState nuxeoValidationState) {
        this.nuxeoValidationState = nuxeoValidationState;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    public void setSupportsHotReload(boolean supportsHotReload) {
        this.supportsHotReload = supportsHotReload;
    }

}
