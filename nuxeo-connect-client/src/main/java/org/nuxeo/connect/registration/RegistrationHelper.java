/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
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
package org.nuxeo.connect.registration;

import static org.nuxeo.connect.connector.http.ConnectUrlConfig.getTrialRegistrationBaseUrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.connect.connector.NuxeoClientInstanceType;
import org.nuxeo.connect.connector.http.ConnectUrlConfig;
import org.nuxeo.connect.connector.http.ProxyHelper;
import org.nuxeo.connect.data.AbstractJSONSerializableData;
import org.nuxeo.connect.data.ConnectProject;
import org.nuxeo.connect.identity.TechnicalInstanceIdentifier;
import org.nuxeo.connect.registration.response.TrialErrorResponse;
import org.nuxeo.connect.registration.response.TrialRegistrationResponse;

/**
 * Helper to manage Registration to Nuxeo Connect.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class RegistrationHelper {

    public static final String GET_PROJECTS_SUFFIX = "getAvailableProjectsForRegistration";

    public static final String POST_REGISTER_SUFFIX = "remoteRegisterInstance";

    protected static final Log log = LogFactory.getLog(RegistrationHelper.class);

    protected static String getBaseUrl() {
        return ConnectUrlConfig.getRegistrationBaseUrl();
    }

    protected static List<String> ALLOWED_TRIAL_FIELDS = Arrays.asList("termsAndConditions", "company", "password",
            "password_verif", "email", "login", "connectreg:projectName", "description");

    protected static void configureHttpClient(HttpClient httpClient,
            String url, String login, String password) {
        // Configure BA to access Connect for registration
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(
                10000);
        if (login != null) {
            httpClient.getParams().setAuthenticationPreemptive(true);
            Credentials ba = new UsernamePasswordCredentials(login, password);
            httpClient.getState().setCredentials(
                    new AuthScope(null, -1, AuthScope.ANY_REALM), ba);
        }
        // Configure the http proxy if needed
        ProxyHelper.configureProxyIfNeeded(httpClient, url);
    }

    protected static HttpClient newHttpClient(String url, String login, String password) {
        HttpClient httpClient = new HttpClient();
        configureHttpClient(httpClient, url, login, password);
        return httpClient;
    }

    public static List<ConnectProject> getAvailableProjectsForRegistration(
            String login, String password) {
        String url = getBaseUrl() + GET_PROJECTS_SUFFIX;
        HttpClient httpClient = newHttpClient(url, login, password);
        HttpMethod method = new GetMethod(url);
        List<ConnectProject> result = new ArrayList<>();
        try {
            int rc = httpClient.executeMethod(method);
            if (rc == HttpStatus.SC_OK) {
                String json = method.getResponseBodyAsString();
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject ob = (JSONObject) array.get(i);
                    result.add(AbstractJSONSerializableData.loadFromJSON(
                            ConnectProject.class, ob));
                }
            }
        } catch (IOException | JSONException e) {
            log.debug(e, e);
        } finally {
            method.releaseConnection();
        }
        return result;
    }

    public static String remoteRegisterInstance(String login, String password,
            String prjId, NuxeoClientInstanceType type, String description) {
        String url = getBaseUrl() + POST_REGISTER_SUFFIX;
        HttpClient httpClient = newHttpClient(url, login, password);
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
        } catch (IOException e) {
            log.debug(e, e);
        } finally {
            method.releaseConnection();
        }
        return null;
    }

    /**
     * @since 1.4.25
     */
    public static TrialRegistrationResponse remoteTrialInstanceRegistration(Map<String, String> parameters) {
        String url = getTrialRegistrationBaseUrl() + "submit?embedded=true";
        PostMethod method = new PostMethod(url);
        List<NameValuePair> nvp = new ArrayList<>();

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (!ALLOWED_TRIAL_FIELDS.contains(entry.getKey())) {
                log.debug("Skipped field: " + entry.getKey() + " (" + entry.getValue() + ")");
                continue;
            }
            nvp.add(new NameValuePair(entry.getKey(), entry.getValue()));
        }

        method.setRequestBody(nvp.toArray(new NameValuePair[nvp.size()]));
        HttpClient httpClient = newHttpClient(url, null, null);
        try {
            int rc = httpClient.executeMethod(method);
            log.debug("Registration response code: " + rc);

            String body = method.getResponseBodyAsString();
            if (rc == HttpStatus.SC_OK) {
                return TrialRegistrationResponse.read(body);
            } else if (rc == HttpStatus.SC_BAD_REQUEST) {
                return TrialRegistrationResponse.read(body);
            } else {
                log.error("Unhandled response code: " + rc);
            }
        } catch (IOException e) {
            log.debug(e, e);
        } finally {
            method.releaseConnection();
        }
        return TrialErrorResponse.UNKNOWN();
    }
}
