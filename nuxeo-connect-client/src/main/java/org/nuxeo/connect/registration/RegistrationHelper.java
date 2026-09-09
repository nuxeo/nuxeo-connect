/*
 * (C) Copyright 2006-2026 Nuxeo (http://nuxeo.com/) and others.
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.utils.URIUtils;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.util.Timeout;
import org.nuxeo.connect.connector.NuxeoClientInstanceType;
import org.nuxeo.connect.connector.http.ConnectUrlConfig;
import org.nuxeo.connect.connector.http.ProxyHelper;
import org.nuxeo.connect.data.AbstractJSONSerializableData;
import org.nuxeo.connect.data.ConnectProject;
import org.nuxeo.connect.data.JSONHelper;
import org.nuxeo.connect.identity.TechnicalInstanceIdentifier;
import org.nuxeo.connect.registration.response.TrialErrorResponse;
import org.nuxeo.connect.registration.response.TrialRegistrationResponse;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

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
        CredentialsStore credentialsProvider = new BasicCredentialsProvider();
        Credentials credentials = null;
        if (login != null) {
            credentials = new UsernamePasswordCredentials(login, password == null ? null : password.toCharArray());
            credentialsProvider.setCredentials(new AuthScope(null, null, -1, null, null), credentials);
        }
        context.setCredentialsProvider(credentialsProvider);

        // Create AuthCache instance for preemptive authentication
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        try {
            BasicScheme basicAuth = new BasicScheme();
            if (credentials != null) {
                basicAuth.initPreemptive(credentials);
            }
            authCache.put(URIUtils.extractHost(new URI(url)), basicAuth);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        context.setAuthCache(authCache);

        // Create request configuration
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                                                                  .setConnectTimeout(Timeout.ofMilliseconds(10000))
                                                                  .setCookieSpec(StandardCookieSpec.RELAXED);

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
            int rc = httpResponse.getCode();
            if (rc == HttpStatus.SC_OK) {
                HttpEntity responseEntity = httpResponse.getEntity();
                if (responseEntity != null) {
                    String json = EntityUtils.toString(responseEntity);
                    ArrayNode array = JSONHelper.readArray(json);
                    for (int i = 0; i < array.size(); i++) {
                        ObjectNode ob = JSONHelper.toObjectNode(array.get(i));
                        result.add(AbstractJSONSerializableData.loadFromJSON(ConnectProject.class, ob));
                    }
                }
            } else {
                log.error("Unhandled response code: " + rc);
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        } catch (JacksonException | IllegalArgumentException e) {
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
        method.setEntity(new UrlEncodedFormEntity(nvps));
        try (CloseableHttpClient httpClient = getHttpClientBuilder(null, null, url).build();
                CloseableHttpResponse httpResponse = httpClient.execute(method,
                        getHttpClientContext(url, login, password))) {
            int rc = httpResponse.getCode();
            if (rc == HttpStatus.SC_OK) {
                HttpEntity responseEntity = httpResponse.getEntity();
                return responseEntity == null ? null : EntityUtils.toString(responseEntity);
            } else {
                log.error("Unhandled response code: " + rc);
            }
        } catch (IOException | ParseException e) {
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
        method.setEntity(new UrlEncodedFormEntity(nvps));
        try (CloseableHttpClient httpClient = getHttpClientBuilder(null, null, url).build();
                CloseableHttpResponse httpResponse = httpClient.execute(method,
                        getHttpClientContext(url, null, null))) {
            int rc = httpResponse.getCode();
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
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
        return TrialErrorResponse.UNKNOWN();
    }
}
