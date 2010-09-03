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

package org.nuxeo.connect.registration;

import java.io.IOException;
import java.util.List;

import org.nuxeo.connect.connector.ConnectConnector;
import org.nuxeo.connect.connector.NuxeoClientInstanceType;
import org.nuxeo.connect.data.ConnectProject;
import org.nuxeo.connect.identity.LogicalInstanceIdentifier;
import org.nuxeo.connect.identity.TechnicalInstanceIdentifier;
import org.nuxeo.connect.identity.LogicalInstanceIdentifier.InvalidCLID;

/**
*
* Interface for Nuxeo Connect Registration
*
* @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
*/
public interface ConnectRegistrationService {

    /**
     * get the Technical Identifier for the current Nuxeo Instance
     *
     * @return
     */
    TechnicalInstanceIdentifier getCTID();

    /**
     * get the Logical Instance identifier
     *
     * @return
     */
    LogicalInstanceIdentifier getCLID();

    /**
     * Register locally an instance provided a CLID
     *
     * (Means user has registred it's instance against Connect Web site and has already obtained a CLID)
     *
     * => can be used if local instance has no access to internet
     *
     * @param strCLID
     * @param description
     * @throws InvalidCLID
     * @throws IOException
     */
    void localRegisterInstance(String strCLID, String description) throws InvalidCLID, IOException;


    /**
     *
     * Ask Connect server for projects where current user is a contact
     * Returns projects that can be used to register instance
     *
     *
     * @param login
     * @param password
     * @return
     * @throws Exception
     */
    List<ConnectProject> getAvailableProjectsForRegistration(String login, String password) throws Exception ;


    /**
     * Let Nuxeo client do all the registration process
     *
     * (Requires the local instance to have internet access)
     *
     * @param login
     * @param password
     * @param prjId
     * @param type
     * @param description
     * @throws Exception
     */
    void remoteRegisterInstance(String login, String password, String prjId, NuxeoClientInstanceType type, String description) throws Exception ;


    /**
     * Gives instance registration status
     *
     * @return
     */
    boolean isInstanceRegistred();

    /**
     * return the connector to access connect remote services
     *
     * @return
     */
    ConnectConnector getConnector();
}
