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
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.PackageType;
import org.nuxeo.connect.update.Version;

/**
 * DTO implementation of the {@link DownloadablePackage} interface. Used to
 * transfer {@link Package} description between server and client.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class PackageDescriptor extends AbstractJSONSerializableData implements
        DownloadablePackage {

    protected String homePage;

    protected String classifier;

    protected String description;

    protected String id;

    protected String name;

    protected String vendor;

    protected int state;

    protected String license;

    protected String licenseUrl;

    protected String[] targetPlatforms;

    protected PackageDependency[] dependencies;

    protected String title;

    protected PackageType type;

    protected Version version;

    protected String sourceDigest;

    protected String sourceUrl;

    protected long sourceSize;

    protected int commentsNumber;

    protected String pictureUrl;

    protected int downloadsCount;

    protected int rating;


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
        return id;
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

    // *********************************
    // Marshaling stuffs

    @Override
    public JSONObject asJSON() {
        String[] names = { "id", "getHomePage", "sourceSize", "sourceUrl",
                "classifier", "description", "name", "state", "title",
                "version", "type", "sourceDigest", "targetPlatform",
                "commentsNumber", "rating", "pictureUrl", "downloadsCount" };
        JSONObject json = new JSONObject(this);

        String jsonString = json.toString();

        JSONArray deps = new JSONArray();
        for (PackageDependency dep : getDependencies()) {
            deps.put(dep.toString());
        }

        try {
            json.put("dependencies", deps);
        } catch (JSONException e) {
            // NOP
        }
        return json;
    }

    public static PackageDescriptor loadFromJSON(JSONObject json)
            throws JSONException {

        PackageDescriptor bundle = new PackageDescriptor();

        bundle.id = json.getString("id");
        if (json.has("homePage")) {
            bundle.homePage = json.getString("homePage");
        }

        if (json.has("classifier")) {
            bundle.classifier = json.getString("classifier");
        }

        if (json.has("description")) {
            bundle.description = json.getString("description");
        }

        if (json.has("name")) {
            bundle.name = json.getString("name");
        }

        if (json.has("state")) {
            bundle.state = json.getInt("state");
        }

        if (json.has("title")) {
            bundle.title = json.getString("title");
        }

        if (json.has("version")) {
            bundle.version = new Version(json.getString("version"));
        }

        if (json.has("type")) {
            bundle.type = PackageType.getByValue(json.getString("type"));
        }

        if (json.has("sourceUrl")) {
            bundle.sourceUrl = json.getString("sourceUrl");
        }

        if (json.has("sourceDigest")) {
            bundle.sourceDigest = json.getString("sourceDigest");
        }

        if (json.has("sourceSize")) {
            bundle.sourceSize = json.getLong("sourceSize");
        }

        if (json.has("commentsNumber")) {
            bundle.commentsNumber = json.getInt("commentsNumber");
        }
        if (json.has("rating")) {
            bundle.rating = json.getInt("rating");
        }
        if (json.has("downloadsCount")) {
            bundle.downloadsCount = json.getInt("downloadsCount");
        }

        if (json.has("pictureUrl")) {
            bundle.pictureUrl = json.getString("pictureUrl");
        }

        if (json.has("targetPlatforms")) {
            JSONArray array = json.getJSONArray("targetPlatforms");
            String[] targets = new String[array.length()];
            for (int i = 0; i < array.length(); i++) {
                targets[i] = array.getString(i);
            }
            bundle.targetPlatforms = targets;
        }

        if (json.has("dependencies")) {
            JSONArray array = json.getJSONArray("dependencies");
            PackageDependency[] deps = new PackageDependency[array.length()];
            for (int i = 0; i < array.length(); i++) {
                deps[i] = new PackageDependency(array.getString(i));
            }
            bundle.dependencies = deps;
        }

        return bundle;
    }

    public static PackageDescriptor loadFromJSON(String json)
            throws JSONException {
        return loadFromJSON(new JSONObject(json));
    }

    // *********************************
    // Setters

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
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
}
