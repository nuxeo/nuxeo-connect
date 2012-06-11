/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     bstefanescu, tdelprat, mguillaume
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
import org.nuxeo.connect.update.PackageType;

/**
 * Base class for {@link ConnectConnector} implementers. Provides url binding
 * and marshaling logic.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public abstract class AbstractConnectConnector implements ConnectConnector {

    public static final String GET_DOWNLOADS_SUFFIX = "getDownloads";

    public static final String GET_DOWNLOAD_SUFFIX = "getDownload";

    public static final String GET_STATUS_SUFFIX = "status";

    public static final String NUXEO_TMP_DIR_PROPERTY = "nuxeo.tmp.dir";

    public static final String CONNECT_CONNECTOR_CACHE_MINUTES_PROPERTY = "org.nuxeo.connect.connector.cache.duration";

    public static final long DEFAULT_CACHE_TIME_MS = 3600 * 1000;

    /**
     * @since 1.4
     */
    public static final long DEFAULT_CACHE_TIME_MS_STUDIO = 300 * 1000;

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
    protected File getCacheFileFor(PackageType type) {
        String connectUrlString = ConnectUrlConfig.getBaseUrl();
        String cacheDir = NuxeoConnectClient.getProperty(
                NUXEO_TMP_DIR_PROPERTY, System.getProperty("java.io.tmpdir"));
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
            cachePrefix = cachePrefix
                    + connectUrl.getPath().replaceAll("/", "#");
            String cacheFileName = cachePrefix + "_" + type.toString()
                    + ".json";
            return new File(cacheDir, cacheFileName);
        } catch (MalformedURLException e) {
            String fallbackFileName = connectUrlString + "_" + type.toString()
                    + ".json";
            return new File(cacheDir, fallbackFileName);
        }
    }

    @Override
    public void flushCache() {
        for (PackageType type : PackageType.values()) {
            File cacheFile = getCacheFileFor(type);
            FileUtils.deleteQuietly(cacheFile);
        }
    }

    protected ConnectServerResponse execCall(String url)
            throws ConnectServerError {
        Map<String, String> headers = SecurityHeaderGenerator.getHeaders();
        return execServerCall(url, headers);
    }

    protected abstract ConnectServerResponse execServerCall(String url,
            Map<String, String> headers) throws ConnectServerError;

    public SubscriptionStatus getConnectStatus() throws ConnectServerError {
        String url = getBaseUrl() + GET_STATUS_SUFFIX;
        ConnectServerResponse response = execCall(url);
        String json = response.getString();
        if (json == null) {
            throw new ConnectServerError("null response from server");
        }
        try {
            return AbstractJSONSerializableData.loadFromJSON(
                    SubscriptionStatus.class, json);
        } catch (Throwable t) {
            throw new ConnectServerError("Unable to parse response: " + json, t);
        } finally {
            response.release();
        }
    }

    public DownloadingPackage getDownload(String id) throws ConnectServerError {
        try {
            id = URLEncoder.encode(id, "UTF-8");
            id = id.replace("+", "%20");
            // yerk, probably not the best way :(
        } catch (UnsupportedEncodingException e) {
            log.error(e);
        }
        String url = getBaseUrl() + GET_DOWNLOAD_SUFFIX + "/" + id;
        ConnectServerResponse response = execCall(url);
        String json = response.getString();
        try {
            PackageDescriptor pkg = AbstractJSONSerializableData.loadFromJSON(
                    PackageDescriptor.class, json);
            if (pkg == null || pkg.getId() == null) {
                throw new ConnectSecurityError(
                        "Unable to parse server response: package has no id");
            }
            ConnectDownloadManager cdm = NuxeoConnectClient.getDownloadManager();
            return cdm.storeDownloadedBundle(pkg);
        } catch (JSONException e) {
            throw new ConnectServerError("Unable to parse response", e);
        } finally {
            response.release();
        }
    }

    public List<DownloadablePackage> getDownloads(PackageType type)
            throws ConnectServerError {
        List<DownloadablePackage> result = new ArrayList<DownloadablePackage>();
        String json = null;
        String cacheTimeString = NuxeoConnectClient.getProperty(
                CONNECT_CONNECTOR_CACHE_MINUTES_PROPERTY, null);
        long cacheMaxAge;
        if (cacheTimeString == null) {
            cacheMaxAge = DEFAULT_CACHE_TIME_MS;
        } else {
            cacheMaxAge = Long.parseLong(cacheTimeString) * 60 * 1000;
        }
        if (type == PackageType.STUDIO) {
            cacheMaxAge = Math.min(cacheMaxAge, DEFAULT_CACHE_TIME_MS_STUDIO);
        }
        File cacheFile = getCacheFileFor(type);

        // Try reading from the cache first
        Date now = new Date();
        if (cacheFile.exists()
                && ((now.getTime() - cacheFile.lastModified()) < cacheMaxAge)) {
            try {
                json = FileUtils.readFileToString(cacheFile);
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject ob = (JSONObject) array.get(i);
                    result.add(AbstractJSONSerializableData.loadFromJSON(
                            PackageDescriptor.class, ob));
                }
            } catch (IOException e) {
                // Issue reading the file (json is null)
            } catch (JSONException e) {
                // Issue parsing the file
                json = null;
                result = new ArrayList<DownloadablePackage>();
            }
        }

        if (json == null) { // Fallback on the real source
            String url = getBaseUrl() + GET_DOWNLOADS_SUFFIX + "/"
                    + type.getValue();
            ConnectServerResponse response = execCall(url);
            json = response.getString();
            response.release();
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject ob = (JSONObject) array.get(i);
                    result.add(AbstractJSONSerializableData.loadFromJSON(
                            PackageDescriptor.class, ob));
                }
            } catch (JSONException e) {
                throw new ConnectServerError("Unable to parse response", e);
            }
            // Parsing went OK, we cache the result
            try {
                FileUtils.writeStringToFile(cacheFile, json);
            } catch (IOException e) {
                // Can't cache - ignore
            }
        }
        return result;
    }

}
