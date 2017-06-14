/*
 * (C) Copyright 2006-2017 Nuxeo SA (http://nuxeo.com/) and others.
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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.nuxeo.connect.connector.ConnectConnector;
import org.nuxeo.connect.connector.NuxeoClientInstanceType;
import org.nuxeo.connect.data.ConnectProject;
import org.nuxeo.connect.identity.LogicalInstanceIdentifier;
import org.nuxeo.connect.identity.LogicalInstanceIdentifier.InvalidCLID;
import org.nuxeo.connect.identity.TechnicalInstanceIdentifier;

/**
 * Interface for Nuxeo Connect Registration
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public interface ConnectRegistrationService {

    /**
     * get the Technical Identifier for the current Nuxeo Instance
     */
    TechnicalInstanceIdentifier getCTID();

    /**
     * get the Logical Instance identifier
     */
    LogicalInstanceIdentifier getCLID();

    /**
     * Register locally an instance provided a CLID (means user has registered his instance against NOS Web site and has
     * already obtained a CLID): can be used if local instance has no access to Internet
     *
     * @param strCLID
     * @param description
     * @throws InvalidCLID
     * @throws IOException
     */
    void localRegisterInstance(String strCLID, String description) throws InvalidCLID, IOException;

    /**
     * Ask Connect server for projects where current user is a contact Returns projects that can be used to register
     * instance
     *
     * @param login
     * @param password
     */
    List<ConnectProject> getAvailableProjectsForRegistration(String login, String password);

    /**
     * Let Nuxeo client do all the registration process (Requires the local instance to have Internet access)
     *
     * @param login
     * @param password
     * @param prjId
     * @param type
     * @param description
     */
    void remoteRegisterInstance(String login, String password, String prjId, NuxeoClientInstanceType type,
            String description) throws IOException, InvalidCLID;

    /**
     * Gives instance registration status
     */
    boolean isInstanceRegistered();

    /**
     * Call Connect to register a new instance with a new trial user. The CLID is pregenerated from Connect.
     *
     * @param properties needed properties: termsAndConditions, company, firstName, lastName, email, login and
     *            connectreg:projectName
     * @throws RegistrationException In case something went wrong, a RegistrationException is thrown with information
     *             returned from the server
     * @since 1.4.25
     */
    void remoteTrialInstanceRegistration(Map<String, String> properties) throws RegistrationException, IOException, InvalidCLID;

    /**
     * return the connector to access connect remote services
     */
    ConnectConnector getConnector();
}
