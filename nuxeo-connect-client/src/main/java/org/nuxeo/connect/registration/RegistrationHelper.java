/*
 * (C) Copyright 2006-2017 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Nuxeo
 *     Yannis JULIENNE
 */
package org.nuxeo.connect.registration;

import static org.nuxeo.connect.HttpClientBuilderHelper.getHttpClientBuilder;
import static org.nuxeo.connect.connector.http.ConnectUrlConfig.getTrialRegistrationBaseUrl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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

    protected static List<String> ALLOWED_TRIAL_FIELDS = Arrays.asList("termsAndConditions", "company", "email",
            "login", "connectreg:projectName", "firstName", "lastName");

    protected static String getBaseUrl() {
        return ConnectUrlConfig.getRegistrationBaseUrl();
    }

    protected static HttpClientContext getHttpClientContext(String url, String login, String password) {
        HttpClientContext context = HttpClientContext.create();

        // Set credentials provider
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (login != null) {
            Credentials ba = new UsernamePasswordCredentials(login, password);
            credentialsProvider.setCredentials(AuthScope.ANY, ba);
        }
        context.setCredentialsProvider(credentialsProvider);

        // Create AuthCache instance for preemptive authentication
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        try {
            authCache.put(URIUtils.extractHost(new URI(url)), basicAuth);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        context.setAuthCache(authCache);

        // Create request configuration
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                                                                  .setConnectTimeout(10000)
                                                                  .setCookieSpec(CookieSpecs.STANDARD);

        // Configure the http proxy if needed
        ProxyHelper.configureProxyIfNeeded(requestConfigBuilder, credentialsProvider, url);

        context.setRequestConfig(requestConfigBuilder.build());
        return context;
    }

    public static List<ConnectProject> getAvailableProjectsForRegistration(String login, String password) {
        String url = getBaseUrl() + GET_PROJECTS_SUFFIX;
        List<ConnectProject> result = new ArrayList<>();

        try (CloseableHttpClient httpClient = getHttpClientBuilder(null, null, url).build();
                CloseableHttpResponse httpResponse = httpClient.execute(new HttpGet(url),
                        getHttpClientContext(url, login, password))) {
            int rc = httpResponse.getStatusLine().getStatusCode();
            if (rc == HttpStatus.SC_OK) {
                HttpEntity responseEntity = httpResponse.getEntity();
                if (responseEntity != null) {
                    String json = EntityUtils.toString(responseEntity);
                    JSONArray array = new JSONArray(json);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject ob = (JSONObject) array.get(i);
                        result.add(AbstractJSONSerializableData.loadFromJSON(ConnectProject.class, ob));
                    }
                }
            } else {
                log.error("Unhandled response code: " + rc);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            log.debug(e, e);
        }
        return result;
    }

    public static String remoteRegisterInstance(String login, String password, String prjId,
            NuxeoClientInstanceType type, String description) {
        String url = getBaseUrl() + POST_REGISTER_SUFFIX;
        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("projectId", prjId));
        nvps.add(new BasicNameValuePair("description", description));
        nvps.add(new BasicNameValuePair("type", type.getValue()));
        nvps.add(new BasicNameValuePair("CTID", TechnicalInstanceIdentifier.instance().getCTID()));
        HttpPost method = new HttpPost(url);
        try {
            method.setEntity(new UrlEncodedFormEntity(nvps));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        try (CloseableHttpClient httpClient = getHttpClientBuilder(null, null, url).build();
                CloseableHttpResponse httpResponse = httpClient.execute(method,
                        getHttpClientContext(url, login, password))) {
            int rc = httpResponse.getStatusLine().getStatusCode();
            if (rc == HttpStatus.SC_OK) {
                HttpEntity responseEntity = httpResponse.getEntity();
                return responseEntity == null ? null : EntityUtils.toString(responseEntity);
            } else {
                log.error("Unhandled response code: " + rc);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * @since 1.4.25
     */
    public static TrialRegistrationResponse remoteTrialInstanceRegistration(Map<String, String> parameters) {
        String url = getTrialRegistrationBaseUrl() + "submit?embedded=true";
        List<NameValuePair> nvps = new ArrayList<>();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (!ALLOWED_TRIAL_FIELDS.contains(entry.getKey())) {
                log.debug("Skipped field: " + entry.getKey() + " (" + entry.getValue() + ")");
                continue;
            }
            nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        HttpPost method = new HttpPost(url);
        try {
            method.setEntity(new UrlEncodedFormEntity(nvps));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        try (CloseableHttpClient httpClient = getHttpClientBuilder(null, null, url).build();
                CloseableHttpResponse httpResponse = httpClient.execute(method,
                        getHttpClientContext(url, null, null))) {
            int rc = httpResponse.getStatusLine().getStatusCode();
            log.debug("Registration response code: " + rc);
            HttpEntity responseEntity = httpResponse.getEntity();
            if (responseEntity != null) {
                String body = EntityUtils.toString(responseEntity);
                if (rc == HttpStatus.SC_OK) {
                    return TrialRegistrationResponse.read(body);
                } else if (rc == HttpStatus.SC_BAD_REQUEST) {
                    return TrialRegistrationResponse.read(body);
                } else {
                    log.error("Unhandled response code: " + rc);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return TrialErrorResponse.UNKNOWN();
    }
}
