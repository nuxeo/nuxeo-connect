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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.connector.NuxeoClientInstanceType;

/**
*
* Logical identifier for a Nuxeo Connect client subscription
*
* @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
*/
public class LogicalInstanceIdentifier {

    public static class InvalidCLID extends Exception {

        private static final long serialVersionUID = 1L;

        public InvalidCLID(String message) {
            super(message);
        }
    }

    public static class NoCLID extends Exception {
        private static final long serialVersionUID = 1L;

        public NoCLID(String message) {
            super(message);
        }

        public NoCLID(String message, Exception e) {
            super(message, e);
        }
    }

    protected static final String ID_SEP = "--";

    public static boolean USE_BASE64_SAVE = false;

    protected String instanceDescription = "";

    protected NuxeoClientInstanceType instanceType = NuxeoClientInstanceType.DEV;

    protected String CLID1 = null;

    protected String CLID2 = null;

    public LogicalInstanceIdentifier(String ID, String description) throws InvalidCLID {
        this(ID);
        this.instanceDescription = description;
    }

    public LogicalInstanceIdentifier(String ID) throws InvalidCLID {

        String[] parts = ID.split(ID_SEP);
        // XXX check on format via REGEXP

        if (parts.length!=2) {
            throw new InvalidCLID("CLID is not of the right format");
        }

        CLID1 = parts[0];
        CLID2 = parts[1];
    }

    public String getCLID() {
        return CLID1 + ID_SEP + CLID2;
    }

    public String getInstanceDescription() {
        return instanceDescription;
    }

    public void setInstanceDescription(String instanceDescription) {
        this.instanceDescription = instanceDescription;
    }

    public NuxeoClientInstanceType getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(NuxeoClientInstanceType instanceType) {
        this.instanceType = instanceType;
    }

    public String getCLID1() {
        return CLID1;
    }

    public String getCLID2() {
        return CLID2;
    }

    protected static String getSaveFileName() {
           String path = NuxeoConnectClient.getHomePath();
           return path + "instance.clid";
    }

    public static void  cleanUp() {
        instance = null;
        File file  = new File(getSaveFileName());
        if (file.exists()) {
            file.delete();
        }
    }

    public void save() throws IOException {

        String data = CLID1 + "\n" + CLID2 + "\n" + instanceDescription;

        if (USE_BASE64_SAVE) {
            data = Base64.encodeBytes(data.getBytes());
        }

        File file = new File(getSaveFileName());

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data.getBytes());
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    protected static LogicalInstanceIdentifier instance;

    public static LogicalInstanceIdentifier instance() throws NoCLID {
        if (instance==null) {
            try {
                instance = LogicalInstanceIdentifier.load();
            } catch (Exception e) {
                throw new NoCLID("can not load CLID", e);
            }
            if (instance==null) {
                throw new NoCLID("can not load CLID");
            }
        }
        return instance;
    }

    public static void unload() throws Exception {
        instance=null;
    }

    public static LogicalInstanceIdentifier load() throws Exception {


        File file = new File(getSaveFileName());
        if (!file.exists()) {
            return null;
        }

        List<String> lines = readLines(file);

        if (USE_BASE64_SAVE) {
            byte[]  data = Base64.decode(lines.get(0));
            String strData = new String(data);
            String[] parts = strData.split("\n");

            lines = new ArrayList<String>();

            for (String part : parts) {
                lines.add(part);
            }
        }

        String id = lines.get(0) + ID_SEP + lines.get(1);
        String description = lines.get(2);

        return new LogicalInstanceIdentifier(id,description);
    }


    private static List<String> readLines(File file) throws IOException {
        List<String> lines = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            InputStream in = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        return lines;
    }



}
