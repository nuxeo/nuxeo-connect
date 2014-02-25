/*
 * (C) Copyright 2006-2014 Nuxeo SAS (http://nuxeo.com/) and contributors.
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

package org.nuxeo.connect.connector.http.proxy;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.connector.http.ConnectUrlConfig;


/**
 * The execution engine to resolve needed proxy using Mozilla Rhino.
 *
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 1.4.15
 */
public class RhinoProxyPacResolver extends ProxyPacResolver {

    private static final Log log = LogFactory.getLog(RhinoProxyPacResolver.class);

    protected static final String PAC_FUNCTIONS_FILE = "proxy_pac_functions.js";

    protected static final String EXEC_PAC_FUNC = "FindProxyForURL(\"%s\", \"%s\");";

    protected SimpleStringCache fileCache = new SimpleStringCache(5);

    @Override
    public String[] findPacProxies(String url) {
        Context ctx = ContextFactory.getGlobal().enterContext();
        try {
            ScriptableObject scope = ctx.initStandardObjects();
            // Register inets functions to dedicated inner class
            ScriptableObject.putProperty(scope, "dnsResolve",
                    new DnsResolveFunction());
            ScriptableObject.putProperty(scope, "myIpAddress",
                    new MyIpAddressFunction());

            // Register others pac methods
            ctx.evaluateReader(scope, getFileReader(PAC_FUNCTIONS_FILE),
                    PAC_FUNCTIONS_FILE, 0, null);

            // Register remote pac function
            ctx.evaluateReader(scope, getRemotePacBodyReader(), "remote pac",
                    0, null);

            // Call and return pac resolution function
            return ((String) ctx.evaluateString(scope,
                    String.format(EXEC_PAC_FUNC, url, getHost(url)),
                    "function call", 0, null)).split(";");
        } catch (IOException | RhinoException io) {
            log.warn(io, io);
        } finally {
            Context.exit();
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
            HttpClient client = new HttpClient();

            GetMethod method = new GetMethod(ConnectUrlConfig.getProxyPacUrl());
            int status = client.executeMethod(method);
            if (status == HttpStatus.SC_OK) {
                fileCache.saveValue(method.getResponseBodyAsString());
            } else {
                throw new IOException("Unable to get pac file");
            }
        }
        return new StringReader(fileCache.getValue());
    }

    protected class DnsResolveFunction extends BaseFunction {
        @Override
        public Object call(Context context, Scriptable scriptable,
                Scriptable scriptable2, Object[] objects) {
            try {
                if (objects.length > 0) {
                    return InetAddress.getByName((String) objects[0]).getHostAddress();
                }
            } catch (UnknownHostException e) {
                log.debug(e);
            }
            return null;
        }
    }

    protected class MyIpAddressFunction extends BaseFunction {
        @Override
        public Object call(Context context, Scriptable scriptable,
                Scriptable scriptable2, Object[] objects) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                return "127.0.0.1";
            }
        }
    }
}
