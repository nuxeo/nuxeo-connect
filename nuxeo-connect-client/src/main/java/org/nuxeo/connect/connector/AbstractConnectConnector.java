/*
 * (C) Copyright 2006-2015 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     bstefanescu, tdelprat, mguillaume, jcarsique
 */

package org.nuxeo.connect.connector;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.connector.http.ConnectUrlConfig;
import org.nuxeo.connect.data.AbstractJSONSerializableData;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.data.DownloadingPackage;
import org.nuxeo.connect.data.PackageDescriptor;
import org.nuxeo.connect.data.SubscriptionStatus;
import org.nuxeo.connect.downloads.ConnectDownloadManager;
import org.nuxeo.connect.identity.LogicalInstanceIdentifier;
import org.nuxeo.connect.identity.SecurityHeaderGenerator;
import org.nuxeo.connect.packages.PackageListCache;
import org.nuxeo.connect.update.PackageType;

/**
 * Base class for {@link ConnectConnector} implementers. Provides url binding and marshaling logic.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public abstract class AbstractConnectConnector implements ConnectConnector {
    /**
     * @since 1.4.19
     */
    public static final String STUDIO_REGISTERED_CACHE_SUFFIX = "studio_registered";

    public static final String GET_DOWNLOADS_SUFFIX = "getDownloads";

    public static final String GET_DOWNLOAD_SUFFIX = "getDownload";

    public static final String GET_STATUS_SUFFIX = "status";

    public static final String NUXEO_TMP_DIR_PROPERTY = "nuxeo.tmp.dir";

    public static final String CONNECT_CONNECTOR_CACHE_MINUTES_PROPERTY = "org.nuxeo.connect.connector.cache.duration";

    /**
     * @since 1.4.19
     */
    public static final String DEFAULT_CACHE_TIME_MINUTES = "60";

    /**
     * @since 1.4
     */
    public static final long DEFAULT_CACHE_TIME_MS_STUDIO = 300 * 1000;

    public static final String CONNECT_SERVER_REACHABLE_PROPERTY = "org.nuxeo.connect.server.reachable";

    protected static Log log = LogFactory.getLog(AbstractConnectConnector.class);

    protected String getBaseUrl() {
        if (LogicalInstanceIdentifier.isRegistered()) {
            return ConnectUrlConfig.getRegistredBaseUrl();
        }
        return ConnectUrlConfig.getUnregisteredBaseUrl();
    }

    /**
     * @since 1.4
     */
    protected File getCacheFileFor(String suffix) {
        String connectUrlString = ConnectUrlConfig.getBaseUrl();
        String cacheDir = NuxeoConnectClient.getProperty(NUXEO_TMP_DIR_PROPERTY, System.getProperty("java.io.tmpdir"));
        try {
            URL connectUrl = new URL(connectUrlString);
            String cachePrefix = connectUrl.getHost() + "_";
            int port = connectUrl.getPort();
            if (port == -1) {
                port = connectUrl.getDefaultPort();
            }
            if (port == -1) {
                cachePrefix = cachePrefix + "00_";
            } else {
                cachePrefix = cachePrefix + Integer.toString(port) + "_";
            }
            cachePrefix = cachePrefix + connectUrl.getPath().replaceAll("/", "#");
            String cacheFileName = cachePrefix + "_" + suffix + ".json";
            return new File(cacheDir, cacheFileName);
        } catch (MalformedURLException e) {
            String fallbackFileName = connectUrlString + "_" + suffix + ".json";
            return new File(cacheDir, fallbackFileName);
        }
    }

    @Override
    public void flushCache() {
        for (PackageType type : PackageType.values()) {
            File cacheFile = getCacheFileFor(type.getValue());
            FileUtils.deleteQuietly(cacheFile);
        }
        FileUtils.deleteQuietly(getCacheFileFor(null));
    }

    protected ConnectServerResponse execCall(String url) throws ConnectServerError {
        return execServerCall(url, SecurityHeaderGenerator.getHeaders());
    }

    protected abstract ConnectServerResponse execServerCall(String url, Map<String, String> headers)
            throws ConnectServerError;

    @Override
    public SubscriptionStatus getConnectStatus() throws ConnectServerError {
        String url = getBaseUrl() + GET_STATUS_SUFFIX;
        ConnectServerResponse response = execCall(url);
        String json = response.getString();
        if (json == null) {
            throw new ConnectServerError("null response from server");
        }
        try {
            return AbstractJSONSerializableData.loadFromJSON(SubscriptionStatus.class, json);
        } catch (Throwable t) {
            throw new ConnectServerError("Unable to parse response: " + json, t);
        } finally {
            response.release();
        }
    }

    @Override
    public DownloadingPackage getDownload(String id) throws ConnectServerError {
        if (!isConnectServerReachable()) {
            throw new CanNotReachConnectServer("Connect server set as not reachable");
        }
        ConnectDownloadManager cdm = NuxeoConnectClient.getDownloadManager();
        DownloadingPackage downloadingPackage = cdm.getDownloadingPackage(id);
        if (downloadingPackage != null) {
            return downloadingPackage;
        }
        try {
            id = URLEncoder.encode(id, "UTF-8");
            id = id.replace("+", "%20");
            // yerk, probably not the best way :(
        } catch (UnsupportedEncodingException e) {
            log.error(e);
        }
        String url = getBaseUrl() + GET_DOWNLOAD_SUFFIX + "/" + id;
        ConnectServerResponse response = null;
        PackageDescriptor pkg = null;
        try {
            response = execCall(url);
            String json = response.getString();
            if (json == null) { // Not found
                return null;
            }
            pkg = AbstractJSONSerializableData.loadFromJSON(PackageDescriptor.class, json);
        } catch (JSONException e) {
            throw new ConnectServerError("Unable to parse response", e);
        } finally {
            if (response != null) {
                response.release();
            }
        }
        if (pkg == null || pkg.getId() == null) {
            throw new ConnectSecurityError("Unable to parse server response: package has no id");
        }
        return cdm.storeDownloadedBundle(pkg);
    }

    @Override
    public List<DownloadablePackage> getDownloads(PackageType type) throws ConnectServerError {
        return getDownloads(type.getValue(), type.getValue());
    }

    @Override
    public List<DownloadablePackage> getRegisteredStudio() throws ConnectServerError {
        return getDownloads(STUDIO_REGISTERED_CACHE_SUFFIX, PackageType.STUDIO + "?registered=true");
    }

    protected List<DownloadablePackage> getDownloads(String fileSuffix, String urlSuffix) throws ConnectServerError {
        if (!isConnectServerReachable()) {
            return new ArrayList<>();
        }

        // Try reading from the cache first
        List<DownloadablePackage> result = readCacheFile(fileSuffix);
        if (result != null) {
            return result;
        }

        // Fallback on the real source
        String url = getBaseUrl() + GET_DOWNLOADS_SUFFIX + "/" + urlSuffix;
        ConnectServerResponse response = execCall(url);
        String json = response.getString();
        response.release();
        try {
            result = new ArrayList<>();
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject ob = (JSONObject) array.get(i);
                result.add(AbstractJSONSerializableData.loadFromJSON(PackageDescriptor.class, ob));
            }
        } catch (JSONException e) {
            throw new ConnectServerError("Unable to parse response", e);
        }

        writeCacheFile(fileSuffix, json);
        return result;
    }

    /**
     * @param suffix Usually {@link PackageType#toString()}
     * @param json String JSON representation of list of {@link DownloadablePackage}
     * @since 1.4.19
     * @see PackageDescriptor
     */
    public void writeCacheFile(String type, String json) {
        try {
            FileUtils.writeStringToFile(getCacheFileFor(type), json);
        } catch (IOException e) { // Can't cache: log but don't fail
            log.error("Could not store packages list in cache", e);
        }
    }

    /**
     * @param suffix Usually {@link PackageType#toString()}
     * @return Packages list from file cache
     * @since 1.4.19
     * @see PackageListCache In-memory cache PackageListCache
     */
    public List<DownloadablePackage> readCacheFile(String suffix) {
        long cacheMaxAge = Long.parseLong(NuxeoConnectClient.getProperty(CONNECT_CONNECTOR_CACHE_MINUTES_PROPERTY,
                DEFAULT_CACHE_TIME_MINUTES)) * 60 * 1000;
        if (suffix == null || PackageType.getByValue(suffix) == PackageType.STUDIO) {
            cacheMaxAge = Math.min(cacheMaxAge, DEFAULT_CACHE_TIME_MS_STUDIO);
        }
        File cacheFile = getCacheFileFor(suffix);
        if (!cacheFile.exists() || ((new Date().getTime() - cacheFile.lastModified()) < cacheMaxAge)) {
            return null;
        }
        try {
            String json = FileUtils.readFileToString(cacheFile);
            JSONArray array = new JSONArray(json);
            List<DownloadablePackage> result = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject ob = (JSONObject) array.get(i);
                result.add(AbstractJSONSerializableData.loadFromJSON(PackageDescriptor.class, ob));
            }
            return result;
        } catch (IOException e) {
            // Issue reading the file
            log.debug(e.getMessage(), e);
        } catch (JSONException e) {
            // Issue parsing the file
            log.debug(e.getMessage(), e);
        }
        return null;
    }

    protected boolean isConnectServerReachable() {
        return Boolean.parseBoolean(NuxeoConnectClient.getProperty(CONNECT_SERVER_REACHABLE_PROPERTY, "true"));
    }

}
