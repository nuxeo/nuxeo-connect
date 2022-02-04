/*
 * (C) Copyright 2006-2018 Nuxeo (http://nuxeo.com/) and others.
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
 *
 */

package org.nuxeo.connect.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.connect.data.marshaling.JSONExportMethod;
import org.nuxeo.connect.data.marshaling.JSONExportableField;
import org.nuxeo.connect.data.marshaling.JSONImportMethod;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.PackageState;
import org.nuxeo.connect.update.PackageType;
import org.nuxeo.connect.update.Version;

/**
 * DTO implementation of the {@link DownloadablePackage} interface. Used to transfer {@link Package} description between
 * server and client.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class PackageDescriptor extends AbstractJSONSerializableData implements DownloadablePackage {

    public static final String NUXEO_CAP_TARGET_PLATFORM_COMPAT = "nuxeo.cap.target.platform.compat";

    private static final String CAP_PREFIX = "cap-";

    private static final String SERVER_PREFIX = "server-";

    private static final String NUXEO_JSF_UI = "nuxeo-jsf-ui";

    @JSONExportableField
    protected String classifier;

    @JSONExportableField
    protected String description;

    @JSONExportableField
    protected String name;

    @JSONExportableField
    protected String vendor;

    protected PackageState packageState;

    @JSONExportableField
    protected String license;

    @JSONExportableField
    protected String licenseUrl;

    @JSONExportableField
    protected String[] targetPlatforms;

    @JSONExportableField
    protected String targetPlatformRange;

    @JSONExportableField
    protected String targetPlatformName;

    protected PackageDependency[] dependencies;

    protected PackageDependency[] optionalDependencies;

    protected PackageDependency[] conflicts;

    protected PackageDependency[] provides;

    @JSONExportableField
    protected String title;

    protected PackageType type;

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
    protected int downloadsCount;

    @JSONExportableField
    protected int rating;

    @JSONExportableField
    protected boolean supportsHotReload;

    @JSONExportableField
    protected boolean subscriptionRequired;

    @JSONExportableField
    protected String owner;

    private boolean local = false;

    public PackageDescriptor() {
        super();
    }

    /**
     * @since 1.4
     */
    public PackageDescriptor(DownloadablePackage descriptor) {
        super();
        classifier = descriptor.getClassifier();
        dependencies = descriptor.getDependencies();
        optionalDependencies = descriptor.getOptionalDependencies();
        conflicts = descriptor.getConflicts();
        description = descriptor.getDescription();
        license = descriptor.getLicenseType();
        licenseUrl = descriptor.getLicenseUrl();
        name = descriptor.getName();
        provides = descriptor.getProvides();
        packageState = descriptor.getPackageState();
        targetPlatforms = descriptor.getTargetPlatforms();
        targetPlatformName = descriptor.getTargetPlatformName();
        targetPlatformRange = descriptor.getTargetPlatformRange();
        title = descriptor.getTitle();
        type = descriptor.getType();
        vendor = descriptor.getVendor();
        version = descriptor.getVersion();
        supportsHotReload = descriptor.supportsHotReload();
        subscriptionRequired = descriptor.hasSubscriptionRequired();
        owner = descriptor.getOwner();
    }

    /**
     * @deprecated Since 1.0. Use {@link #loadFromJSON(Class, JSONObject)} instead.
     */
    @Deprecated
    public static PackageDescriptor loadFromJSON(JSONObject json) throws JSONException {
        return loadFromJSON(PackageDescriptor.class, json);
    }

    /**
     * @deprecated Since 1.0. Use {@link #loadFromJSON(Class, String)} instead.
     */
    @Deprecated
    public static PackageDescriptor loadFromJSON(String json) throws JSONException {
        return loadFromJSON(new JSONObject(json));
    }

    /**
     * Merges two arrays of package dependencies.
     *
     * @since 8.3
     */
    public static PackageDependency[] addPackageDependencies(PackageDependency[] dep1, PackageDependency[] dep2) {
        if (dep1 == null) {
            return dep2;
        }
        if (dep2 == null) {
            return dep1;
        }
        // merge without duplicates
        Set<PackageDependency> set = new LinkedHashSet<>();
        set.addAll(Arrays.asList(dep1));
        set.addAll(Arrays.asList(dep2));
        return set.toArray(new PackageDependency[0]);
    }

    /**
     * If additional dependencies have already been set, check that we don't have ourselves in them.
     *
     * @since 8.3
     */
    public static PackageDependency[] fixDependencies(String name, PackageDependency[] dependencies) {
        if (dependencies == null) {
            return null;
        }
        for (PackageDependency dep : dependencies) {
            if (dep.getName().equals(name)) {
                // we have a dependency for ourselves. remove it.
                List<PackageDependency> list = new ArrayList<>(Arrays.asList(dependencies));
                list.remove(dep);
                return list.toArray(new PackageDependency[0]);
            }
        }
        return dependencies;
    }

    /**
     * Returns a fixed list of target platforms and dependencies, to deal with backward compatibility. Can be turned off
     * by setting the nuxeo.cap.target.platform.compat system property to false (useful for Connect/Studio).
     *
     * @param packageDependencies a PackageDependency[] return value
     * @since 8.3
     */
    public static String[] fixTargetPlatforms(String name, String[] targets, MutableObject packageDependencies) {
        if (Boolean.parseBoolean(System.getProperty(NUXEO_CAP_TARGET_PLATFORM_COMPAT, "true"))) {
            List<String> serverTargets = new ArrayList<>();
            List<String> newServerTargets = new ArrayList<>();
            for (String target : targets) {
                if (target.startsWith(SERVER_PREFIX)) {
                    serverTargets.add(target);
                } else if (target.startsWith(CAP_PREFIX)) {
                    String newServerTarget = SERVER_PREFIX + target.substring(CAP_PREFIX.length());
                    newServerTargets.add(newServerTarget);
                }
            }
            newServerTargets.removeAll(serverTargets);
            if (!newServerTargets.isEmpty()) {
                // BBB: if we have "cap" declared as a target platform,
                // then also declare "server" with an added dependency on "nuxeo-jsf-ui"
                List<String> list = new ArrayList<>(targets.length + newServerTargets.size());
                list.addAll(Arrays.asList(targets));
                if (!NUXEO_JSF_UI.equals(name)) {
                    // don't do if it's for nuxeo-jsf-ui itself, otherwise the cap compatibility version
                    // would be a candidate even on a server
                    list.addAll(newServerTargets);
                }
                targets = list.toArray(new String[0]);
                // set additional dependency if it's not ourselves
                // name may be null (not yet set), in which case the setter for name will clean up dependencies
                if (NUXEO_JSF_UI.equals(name)) {
                    packageDependencies.setValue(null);
                } else {
                    packageDependencies.setValue(new PackageDependency[] { new PackageDependency(NUXEO_JSF_UI) });
                }
            }
        }
        return targets;
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    @Override
    public int getCommentsNumber() {
        return commentsNumber;
    }

    public void setCommentsNumber(int commentsNumber) {
        this.commentsNumber = commentsNumber;
    }

    @Override
    public PackageDependency[] getConflicts() {
        if (conflicts == null) {
            conflicts = new PackageDependency[0];
        }
        return conflicts;
    }

    public void setConflicts(PackageDependency[] conflicts) {
        this.conflicts = conflicts;
    }

    @JSONExportMethod(name = "conflicts")
    protected JSONArray getConflictsAsJSON() {
        JSONArray deps = new JSONArray();
        for (PackageDependency dep : getConflicts()) {
            deps.put(dep.toString());
        }
        return deps;
    }

    @JSONImportMethod(name = "conflicts")
    protected void setConflictsAsJSON(JSONArray array) throws JSONException {
        PackageDependency[] deps = new PackageDependency[array.length()];
        for (int i = 0; i < array.length(); i++) {
            deps[i] = new PackageDependency(array.getString(i));
        }
        setConflicts(deps);
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
            dependencies = new PackageDependency[0];
        }
        return dependencies;
    }

    public void setDependencies(PackageDependency[] dependencies) {
        this.dependencies = addPackageDependencies(this.dependencies, dependencies);
    }

    @JSONExportMethod(name = "dependencies")
    protected JSONArray getDependenciesAsJSON() {
        JSONArray deps = new JSONArray();
        for (PackageDependency dep : getDependencies()) {
            deps.put(dep.toString());
        }
        return deps;
    }

    @JSONImportMethod(name = "dependencies")
    protected void setDependenciesAsJSON(JSONArray array) throws JSONException {
        PackageDependency[] deps = new PackageDependency[array.length()];
        for (int i = 0; i < array.length(); i++) {
            deps[i] = new PackageDependency(array.getString(i));
        }
        setDependencies(deps);
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
    public PackageDependency[] getOptionalDependencies() {
        if (optionalDependencies == null) {
            optionalDependencies = new PackageDependency[0];
        }
        return optionalDependencies;
    }

    /**
     * @since 1.4.26
     */
    public void setOptionalDependencies(PackageDependency[] optionalDependencies) {
        this.optionalDependencies = addPackageDependencies(this.optionalDependencies, optionalDependencies);
    }

    /**
     * @since 1.5.2
     */
    @JSONExportMethod(name = "optionalDependencies")
    protected JSONArray getOptionalDependenciesAsJSON() {
        JSONArray deps = new JSONArray();
        for (PackageDependency dep : getOptionalDependencies()) {
            deps.put(dep.toString());
        }
        return deps;
    }

    /**
     * @since 1.4.26
     */
    @JSONImportMethod(name = "optionalDependencies")
    protected void setOptionalDependenciesAsJSON(JSONArray array) throws JSONException {
        PackageDependency[] deps = new PackageDependency[array.length()];
        for (int i = 0; i < array.length(); i++) {
            deps[i] = new PackageDependency(array.getString(i));
        }
        setOptionalDependencies(deps);
    }

    /**
     * @since 1.5.2
     */
    public String getOptionalDependenciesAsString() {
        StringBuilder sb = new StringBuilder();
        if (optionalDependencies == null || optionalDependencies.length == 0) {
            return "";
        }
        for (PackageDependency dep : getOptionalDependencies()) {
            sb.append(dep.toString());
            sb.append(",");
        }
        return sb.toString();
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int getDownloadsCount() {
        return downloadsCount;
    }

    public void setDownloadsCount(int downloadsCount) {
        this.downloadsCount = downloadsCount;
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

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    @Override
    public String getName() {
        return name;
    }

    @JSONImportMethod(name = "name")
    public void setName(String name) {
        this.name = name;
        dependencies = fixDependencies(name, dependencies);
    }

    /**
     * @since 1.4.17
     */
    @Override
    public PackageState getPackageState() {
        if (packageState == null) {
            packageState = PackageState.UNKNOWN;
        }
        return packageState;
    }

    /**
     * @since 1.4.17
     */
    public void setPackageState(PackageState state) {
        packageState = state;
    }

    @JSONExportMethod(name = "packageState")
    public String getPackageStateAsJson() {
        return getPackageState().getLabel();
    }

    @JSONImportMethod(name = "packageState")
    public void setPackageStateAsJson(String packageState) {
        setPackageState(PackageState.getByLabel(packageState));
    }

    /**
     * @since 1.4.17
     */
    @JSONImportMethod(name = "state")
    public void setPackageState(int state) {
        packageState = PackageState.getByValue(state);
    }

    @Override
    public PackageDependency[] getProvides() {
        if (provides == null) {
            provides = new PackageDependency[0];
        }
        return provides;
    }

    public void setProvides(PackageDependency[] provides) {
        this.provides = provides;
    }

    @JSONExportMethod(name = "provides")
    protected JSONArray getProvidesAsJSON() {
        JSONArray deps = new JSONArray();
        for (PackageDependency dep : getProvides()) {
            deps.put(dep.toString());
        }
        return deps;
    }

    @JSONImportMethod(name = "provides")
    protected void setProvidesAsJSON(JSONArray array) throws JSONException {
        PackageDependency[] deps = new PackageDependency[array.length()];
        for (int i = 0; i < array.length(); i++) {
            deps[i] = new PackageDependency(array.getString(i));
        }
        setProvides(deps);
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

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public String getSourceDigest() {
        return sourceDigest;
    }

    public void setSourceDigest(String sourceDigest) {
        this.sourceDigest = sourceDigest;
    }

    @Override
    public long getSourceSize() {
        return sourceSize;
    }

    public void setSourceSize(long sourceSize) {
        this.sourceSize = sourceSize;
    }

    @Override
    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    @Deprecated
    @Override
    public int getState() {
        return packageState.getValue();
    }

    /**
     * @deprecated Since 1.4.17. Use {@link #setPackageState(PackageState)} instead.
     */
    @Deprecated
    public void setState(int state) {
        packageState = PackageState.getByValue(state);
    }

    @Override
    public String[] getTargetPlatforms() {
        return targetPlatforms;
    }

    public void setTargetPlatforms(List<String> targetPlatforms) {
        this.targetPlatforms = targetPlatforms == null ? new String[0]
                : targetPlatforms.toArray(new String[targetPlatforms.size()]);
    }

    public void setTargetPlatforms(String[] targetPlatforms) {
        this.targetPlatforms = targetPlatforms;
    }

    @Override
    public String getTargetPlatformRange() {
        return targetPlatformRange;
    }

    public void setTargetPlatformRange(String targetPlatformRange) {
        this.targetPlatformRange = targetPlatformRange;
    }

    @Override
    public String getTargetPlatformName() {
        return targetPlatformName;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public PackageType getType() {
        return type;
    }

    @JSONExportMethod(name = "type")
    public String getTypeAsJson() {
        return type.getValue();
    }

    public void setType(PackageType type) {
        this.type = type;
    }

    @Override
    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    @JSONExportMethod(name = "version")
    public String getVersionAsJSON() {
        return version.toString();
    }

    @JSONImportMethod(name = "version")
    public void setVersionAsJSON(String v) {
        version = new Version(v);
    }

    @Override
    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean isLocal) {
        local = isLocal;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    /**
     * @since 1.4.17
     */
    @JSONImportMethod(name = "packageState")
    public void setPackageStateAsJSON(String state) {
        setPackageState(PackageState.getByValue(state));
    }

    public void setSupportsHotReload(boolean supportsHotReload) {
        this.supportsHotReload = supportsHotReload;
    }

    @JSONImportMethod(name = "targetPlatforms")
    public void setTargetPlatformsAsJSON(JSONArray array) throws JSONException {
        String[] targets = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            targets[i] = array.getString(i);
        }
        MutableObject packageDependencies = new MutableObject();
        targetPlatforms = fixTargetPlatforms(name, targets, packageDependencies);
        setDependencies((PackageDependency[]) packageDependencies.getValue());
    }

    @JSONImportMethod(name = "type")
    public void setTypeAsJSON(String strType) {
        type = PackageType.getByValue(strType);
    }

    @Override
    public boolean supportsHotReload() {
        return supportsHotReload;
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public boolean hasSubscriptionRequired() {
        return subscriptionRequired;
    }

    @Override
    public String getOwner() {
        return owner;
    }

}
