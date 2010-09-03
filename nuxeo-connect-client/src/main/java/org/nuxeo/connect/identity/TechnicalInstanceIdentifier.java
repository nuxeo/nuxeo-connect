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

package org.nuxeo.connect.identity;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

import org.nuxeo.connect.NuxeoConnectClient;

/**
 * Technical identifier for a Nuxeo Instance.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class TechnicalInstanceIdentifier {

    protected static final String HASH_METHOD="MD5";

    protected String CTID;

    protected static TechnicalInstanceIdentifier instance;

    public static TechnicalInstanceIdentifier instance() {
        if (instance == null) {
            instance = new TechnicalInstanceIdentifier();
        }
        return instance;
    }

    protected String getOrBuildCTID() throws Exception {
        if (CTID!=null) {
            return CTID;
        }

        String osName = System.getProperty("os.name"); // + "-" + System.getProperty("os.version");

        String instancePath = null;

        if (NuxeoConnectClient.isTestModeSet()) {
            instancePath ="TEST";
        } else {
            instancePath = NuxeoConnectClient.getHomePath();
        }

        String hwInfo = generateHardwareUID();

        // hash info to be sure we don't send local info in clear

        try {
            instancePath = Base64.encodeBytes(MessageDigest.getInstance(HASH_METHOD).digest(instancePath.getBytes()));
            hwInfo = Base64.encodeBytes(MessageDigest.getInstance(HASH_METHOD).digest(hwInfo.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            instancePath="***";
            hwInfo = "***";
        }

        CTID = osName + "-" + instancePath + "-" + hwInfo;

        return CTID;
    }

    public String getCTID() throws Exception {
        return getOrBuildCTID();
    }

    public static String generateHardwareUID() throws Exception {
        String hwUID = "";

        String javaVersion = System.getProperty("java.version");

        Enumeration<NetworkInterface> ifs =  NetworkInterface.getNetworkInterfaces();

        while (ifs.hasMoreElements()) {
            NetworkInterface ni = ifs.nextElement();

            if (javaVersion.contains("1.6")) {
                //ni.getHardwareAddress() only in jdk 1.6
                Method[] methods = ni.getClass().getMethods();
                for (Method method : methods) {
                    if (method.getName().equalsIgnoreCase("getHardwareAddress")) {
                        byte[] hwAddr = (byte[])method.invoke(ni);
                        if (hwAddr!=null) {
                            hwUID = hwUID + "-" + Base64.encodeBytes(hwAddr);
                        }
                        break;
                    }
                }
            } else {
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    hwUID = hwUID + "-" + Base64.encodeBytes(addrs.nextElement().getAddress());
                }
            }
        }
        return hwUID;
    }

}
