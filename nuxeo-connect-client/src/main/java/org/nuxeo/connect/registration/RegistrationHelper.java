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
 *
 */

package org.nuxeo.connect.registration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.connect.connector.NuxeoClientInstanceType;
import org.nuxeo.connect.connector.http.ConnectUrlConfig;
import org.nuxeo.connect.connector.http.ProxyHelper;
import org.nuxeo.connect.data.AbstractJSONSerializableData;
import org.nuxeo.connect.data.ConnectProject;
import org.nuxeo.connect.identity.TechnicalInstanceIdentifier;

/**
 * Helper to manage Registration to Nuxeo Connect.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class RegistrationHelper {

    public static final String GET_PROJECTS_SUFFIX = "getAvailableProjectsForRegistration";

    public static final String POST_REGISTER_SUFFIX = "remoteRegisterInstance";

    protected static String getBaseUrl() {
        return ConnectUrlConfig.getRegistrationBaseUrl();
    }

    protected static void configureHttpClient(HttpClient httpClient,
            String login, String password) {
        // Configure BA to access Connect for registration
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(
                10000);
        httpClient.getParams().setAuthenticationPreemptive(true);
        Credentials ba = new UsernamePasswordCredentials(login, password);
        httpClient.getState().setCredentials(
                new AuthScope(null, -1, AuthScope.ANY_REALM), ba);
        // Configure the http proxy if needed
        ProxyHelper.configureProxyIfNeeded(httpClient);
    }

    public static List<ConnectProject> getAvailableProjectsForRegistration(
            String login, String password) {
        String url = getBaseUrl() + GET_PROJECTS_SUFFIX;
        HttpClient httpClient = new HttpClient();
        configureHttpClient(httpClient, login, password);
        HttpMethod method = new GetMethod(url);
        List<ConnectProject> result = new ArrayList<ConnectProject>();
        try {
            int rc = httpClient.executeMethod(method);
            if (rc == HttpStatus.SC_OK) {
                String json = method.getResponseBodyAsString();
                try {
                    JSONArray array = new JSONArray(json);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject ob = (JSONObject) array.get(i);
                        result.add(AbstractJSONSerializableData.loadFromJSON(
                                ConnectProject.class, ob));
                    }
                } catch (JSONException e) {
                }
            }
        } catch (Exception e) {
        } finally {
            method.releaseConnection();
        }
        return result;
    }

    public static String remoteRegisterInstance(String login, String password,
            String prjId, NuxeoClientInstanceType type, String description)
            throws Exception {
        String url = getBaseUrl() + POST_REGISTER_SUFFIX;
        HttpClient httpClient = new HttpClient();
        configureHttpClient(httpClient, login, password);
        PostMethod method = new PostMethod(url);
        NameValuePair project = new NameValuePair("projectId", prjId);
        NameValuePair desc = new NameValuePair("description", description);
        NameValuePair strType = new NameValuePair("type", type.getValue());
        NameValuePair ctid = new NameValuePair("CTID",
                TechnicalInstanceIdentifier.instance().getCTID());
        method.setRequestBody(new NameValuePair[] { project, desc, strType,
                ctid });
        try {
            int rc = httpClient.executeMethod(method);
            if (rc == HttpStatus.SC_OK) {
                return method.getResponseBodyAsString();
            }
        } catch (Exception e) {
        } finally {
            method.releaseConnection();
        }
        return null;
    }
}
