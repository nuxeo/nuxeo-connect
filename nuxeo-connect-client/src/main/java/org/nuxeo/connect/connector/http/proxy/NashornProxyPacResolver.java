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
 *     Arnaud Kervern
 *     Florent Guillaume
 *     Yannis JULIENNE
 */
package org.nuxeo.connect.connector.http.proxy;

import static org.nuxeo.connect.HttpClientBuilderHelper.getHttpClientBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.connector.http.ConnectUrlConfig;

/**
 * The execution engine to resolve needed proxy using Nashorn.
 *
 * @since 1.4.26
 */
public class NashornProxyPacResolver extends ProxyPacResolver {

    protected static final String PAC_FUNCTIONS_FILE = "proxy_pac_functions.js";

    protected static final String EXEC_PAC_FUNC = "FindProxyForURL";

    private static final Log log = LogFactory.getLog(NashornProxyPacResolver.class);

    protected SimpleStringCache fileCache = new SimpleStringCache(5);

    public static String dnsResolve(String host) {
        try {
            return InetAddress.getByName(host).getHostAddress();
        } catch (UnknownHostException e) {
            return "";
        }
    }

    public static String myIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    @Override
    public String[] findPacProxies(String url) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        ScriptContext context = new SimpleScriptContext();
        engine.setContext(context); // set as default context, for invokeFunctino
        SimpleBindings bindings = new SimpleBindings();
        context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        try {
            // Register internet functions as Java upcalls
            engine.eval("var NashornProxyPacResolver = Java.type('" + getClass().getName() + "');"
                    + "var dnsResolve = NashornProxyPacResolver.dnsResolve;"
                    + "var myIpAddress = NashornProxyPacResolver.myIpAddress");
            // Register others pac methods
            engine.eval(getFileReader(PAC_FUNCTIONS_FILE));
            // Register remote pac function
            engine.eval(getRemotePacBodyReader());
            // Call and return pac resolution function
            String proxies = (String) ((Invocable) engine).invokeFunction(EXEC_PAC_FUNC, url, getHost(url));
            return proxies.split(";");
        } catch (IOException | ScriptException | NoSuchMethodException e) {
            log.warn(e, e);
        }
        return null;
    }

    protected Reader getFileReader(String filename) throws IOException {
        URL resource = getClass().getClassLoader().getResource(filename);
        if (resource == null) {
            throw new IOException("Unable to find file: " + filename);
        }
        return new InputStreamReader(resource.openStream());
    }

    protected Reader getRemotePacBodyReader() throws IOException {
        if (NuxeoConnectClient.isTestModeSet()) {
            return getFileReader(System.getProperty("nuxeo.test.pac.fake.remote.file"));
        }

        if (fileCache.getValue() == null) {
            String url = ConnectUrlConfig.getProxyPacUrl();
            try (CloseableHttpClient httpClient = getHttpClientBuilder(null, null, url).build();
                    CloseableHttpResponse httpResponse = httpClient.execute(new HttpGet(url))) {
                StatusLine statusLine = httpResponse.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    fileCache.saveValue(EntityUtils.toString(httpResponse.getEntity()));
                } else {
                    throw new IOException("Unable to get pac file: " + statusLine);
                }
            }
        }
        return new StringReader(fileCache.getValue());
    }

}
