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

package org.nuxeo.connect.connector;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.nuxeo.connect.identity.SecurityHeaderGenerator;
import org.nuxeo.connect.update.PackageType;

/**
 * Base class for {@link ConnectConnector} implementers.
 * Provides url binding and marshaling logic.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public abstract class AbstractConnectConnector implements ConnectConnector {

    public static final String GET_DOWNLOADS_SUFFIX = "getDownloads";
    public static final String GET_DOWNLOAD_SUFFIX = "getDownload";
    public static final String GET_STATUS_SUFFIX = "status";

    protected String getBaseUrl() {
        return ConnectUrlConfig.getRegistredBaseUrl();
    }

    protected ConnectServerResponse execCall(String url) throws ConnectServerError {
        Map<String, String> headers = SecurityHeaderGenerator.getHeaders();
        return execServerCall(url, headers);
    }

    protected abstract  ConnectServerResponse execServerCall(String url, Map<String, String> headers) throws ConnectServerError;

    public SubscriptionStatus getConnectStatus() throws ConnectServerError {

        String url = getBaseUrl() + GET_STATUS_SUFFIX;

        ConnectServerResponse response = execCall(url);

        String json = response.getString();

        try {
            return AbstractJSONSerializableData.loadFromJSON(SubscriptionStatus.class, json);
        } catch (JSONException e) {
            throw new ConnectServerError("Unable to parse response", e);

        }finally {
            response.release();
        }
    }

    public DownloadingPackage getDownload(String id) throws ConnectServerError {
        try {
            id = URLEncoder.encode(id, "UTF-8");
            id = id.replace("+", "%20");
            // yerk, probably not the best way :(
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = getBaseUrl() + GET_DOWNLOAD_SUFFIX + "/" + id;

        ConnectServerResponse response = execCall(url);

        String json = response.getString();

        try {
            PackageDescriptor pkg = AbstractJSONSerializableData.loadFromJSON(PackageDescriptor.class, json);
            if (pkg==null || pkg.getId()==null) {
                throw new ConnectSecurityError("Unable to parse server response : package has no id");
            }
            ConnectDownloadManager cdm = NuxeoConnectClient.getDownloadManager();
            return cdm.storeDownloadedBundle(pkg);
        } catch (Exception e) {
            throw new ConnectServerError("Unable to parse response", e);
        }finally {
            response.release();
        }
    }

    public List<DownloadablePackage> getDownloads(PackageType type)
            throws ConnectServerError {

        List<DownloadablePackage> result = new ArrayList<DownloadablePackage>();

        String url = getBaseUrl() + GET_DOWNLOADS_SUFFIX + "/" + type.getValue();
        ConnectServerResponse response = execCall(url);

        String json = response.getString();

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i< array.length(); i++) {
                JSONObject ob = (JSONObject) array.get(i);
                result.add(AbstractJSONSerializableData.loadFromJSON(PackageDescriptor.class, ob));
            }
            return result;
        }
        catch (JSONException e) {
            throw new ConnectServerError("Unable to parse response", e);
        }
        finally {
            response.release();
        }

    }

}
