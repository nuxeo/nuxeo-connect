/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and others.
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

package org.nuxeo.connect.connector.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.connector.ConnectConnector;
import org.nuxeo.connect.connector.NuxeoClientInstanceType;
import org.nuxeo.connect.connector.http.ConnectHttpConnector;
import org.nuxeo.connect.data.ConnectProject;
import org.nuxeo.connect.downloads.ConnectDownloadManager;
import org.nuxeo.connect.downloads.ConnectDownloadManagerImpl;
import org.nuxeo.connect.identity.LogicalInstanceIdentifier;
import org.nuxeo.connect.identity.LogicalInstanceIdentifier.InvalidCLID;
import org.nuxeo.connect.identity.LogicalInstanceIdentifier.NoCLID;
import org.nuxeo.connect.identity.TechnicalInstanceIdentifier;
import org.nuxeo.connect.registration.ConnectRegistrationService;
import org.nuxeo.connect.registration.RegistrationException;
import org.nuxeo.connect.registration.RegistrationHelper;
import org.nuxeo.connect.registration.response.TrialErrorResponse;
import org.nuxeo.connect.registration.response.TrialRegistrationResponse;
import org.nuxeo.connect.registration.response.TrialSuccessResponse;

/**
 * Implementation of the {@link ConnectRegistrationService} Also provides access to {@link ConnectConnector} and
 * {@link ConnectDownloadManager} services
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class ConnectGatewayComponent implements ConnectRegistrationService {

    protected ConnectConnector connector = null;

    protected ConnectDownloadManager downloadManager = null;

    protected ConnectConnector testConnector = null;

    public ConnectConnector getTestConnector() {
        return testConnector;
    }

    public void setTestConnector(ConnectConnector testConnector) {
        this.testConnector = testConnector;
    }

    protected TechnicalInstanceIdentifier ctid = new TechnicalInstanceIdentifier();

    @Override
    public TechnicalInstanceIdentifier getCTID() {
        return ctid;
    }

    @Override
    public LogicalInstanceIdentifier getCLID() {
        try {
            return LogicalInstanceIdentifier.instance();
        } catch (NoCLID e) {
            return null;
        }
    }

    @Override
    public void localRegisterInstance(String strCLID, String description) throws InvalidCLID, IOException {
        LogicalInstanceIdentifier CLID = new LogicalInstanceIdentifier(strCLID, description);
        CLID.save();
        connector = null;
    }

    @Override
    public boolean isInstanceRegistred() {
        return LogicalInstanceIdentifier.isRegistered();
    }

    @Override
    public ConnectConnector getConnector() {
        // for Unit tests
        if (NuxeoConnectClient.isTestModeSet() && testConnector != null) {
            return testConnector;
        }

        if (connector == null) {
            connector = new ConnectHttpConnector();
        }
        return connector;
    }

    public ConnectDownloadManager getDownloadManager() {
        if (downloadManager == null) {
            downloadManager = new ConnectDownloadManagerImpl();
        }
        return downloadManager;
    }

    @Override
    public List<ConnectProject> getAvailableProjectsForRegistration(String login, String password) {
        return RegistrationHelper.getAvailableProjectsForRegistration(login, password);
    }

    @Override
    public void remoteRegisterInstance(String login, String password, String prjId, NuxeoClientInstanceType type,
            String description) throws IOException, InvalidCLID {
        String strCLID = RegistrationHelper.remoteRegisterInstance(login, password, prjId, type, description);
        if (strCLID != null) {
            localRegisterInstance(strCLID, description);
        }
    }

    @Override
    public void remoteTrialInstanceRegistration(Map<String, String> parameters) throws RegistrationException,
            IOException {
        // If no login; replace it with email
        if (!parameters.containsKey("login")) {
            parameters.put("login", parameters.get("email"));
        }

        TrialRegistrationResponse res = RegistrationHelper.remoteTrialInstanceRegistration(parameters);
        if (res.isError()) {
            throw new RegistrationException((TrialErrorResponse) res);
        }

        TrialSuccessResponse ss = (TrialSuccessResponse) res;
        try {
            localRegisterInstance(ss.getToken().get("CLID"), parameters.get("description"));
        } catch (InvalidCLID e) {
            throw new RegistrationException(e);
        }
    }

    @Override
    public void remoteRenewRegistration() throws Exception {
        String newCLID = getConnector().remoteRenewRegistration();
        if (newCLID != null) {
            LogicalInstanceIdentifier oldCLID = getCLID();
            String description = oldCLID == null ? "" : oldCLID.getInstanceDescription();
            localRegisterInstance(newCLID, description);
        }
    }

}
