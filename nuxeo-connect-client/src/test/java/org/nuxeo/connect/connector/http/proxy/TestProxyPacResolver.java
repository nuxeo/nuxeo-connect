/*
 * (C) Copyright 2006-2014 Nuxeo SA (http://nuxeo.com/) and contributors.
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

package org.nuxeo.connect.connector.http.proxy;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.nuxeo.connect.connector.http.proxy.ProxyPacResolver.parseProxyInfos;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 1.4.15
 */
public class TestProxyPacResolver {

    static final String PAC_FILE_KEY = "nuxeo.test.pac.fake.remote.file";

    RhinoProxyPacResolver solver;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("org.nuxeo.connect.client.testMode", "true");
    }

    @Before
    public void before() {
        System.clearProperty(PAC_FILE_KEY);
        solver = new RhinoProxyPacResolver();
    }

    @Test
    public void testGetSimplePac() {
        changePacFile("simple_pac.js");
        String[] proxies = solver.findPacProxies("http://www.dimdamdom.fr/page.php");
        assertEquals(1, proxies.length);
        assertEquals("DIRECT", proxies[0]);
    }

    @Test
    public void testMissingPac() {
        changePacFile("");
        String[] proxies = solver.findPacProxies("http://www.dimdamdom.fr/page.php");
        assertNull(proxies);
    }

    @Test
    public void testNuxeocom() {
        changePacFile("test1_pac.js");
        String[] proxies = solver.findPacProxies("https://www.nuxeo.com");
        assertEquals(1, proxies.length);
        assertEquals("DIRECT", proxies[0]);

        proxies = solver.findPacProxies("http://localhost:8080/nuxeo/");
        assertEquals(1, proxies.length);
        assertEquals("PROXY 127.0.0.1", proxies[0]);
    }

    @Test
    public void testFunctions() {
        changePacFile("test2_pac.js");
        String[] proxies = solver.findPacProxies("ftp://www.something.com");
        assertEquals(1, proxies.length);
        assertEquals("DIRECT", proxies[0]);

        proxies = solver.findPacProxies("http://intranet.domain.com/blabla");
        assertEquals(1, proxies.length);
        assertEquals("DIRECT", proxies[0]);

        proxies = solver.findPacProxies("http://abcdomain.com/folder/something");
        assertEquals(1, proxies.length);
        assertEquals("DIRECT", proxies[0]);

        proxies = solver.findPacProxies("http://www.abcdomain.com/blabla");
        assertEquals(1, proxies.length);
        assertEquals("DIRECT", proxies[0]);

        proxies = solver.findPacProxies("http://www.domain.com/blabla");
        assertEquals(2, proxies.length);
    }

    @Test
    public void testProxyRegex() {
        String[] res = parseProxyInfos("PROXY 127.0.0.1");
        Assert.assertEquals("127.0.0.1", res[0]);
        Assert.assertEquals("-1", res[1]);

        res = parseProxyInfos(" PROXY 127.0.0.1:8080");
        Assert.assertEquals("127.0.0.1", res[0]);
        Assert.assertEquals("8080", res[1]);

        res = parseProxyInfos(" PROXY myproxy.com:0");
        Assert.assertEquals("myproxy.com", res[0]);
        Assert.assertEquals("0", res[1]);

        res = parseProxyInfos(" PROXY myproxy.com:");
        assertNull(res[0]);
        assertNull(res[1]);

        res = parseProxyInfos(" PROXY my43proxy.com");
        Assert.assertEquals("my43proxy.com", res[0]);
        Assert.assertEquals("-1", res[1]);
    }

    protected void changePacFile(String name) {
        System.setProperty(PAC_FILE_KEY, "test-pac/" + name);
    }
}
