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
 * $Id$
 */

package org.nuxeo.connect.connector.fake;

import org.nuxeo.connect.update.PackageType;

/**
 * Fake implementation
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 * @deprecated since 1.4
 */
@Deprecated
public class UnregistredFakeConnector extends AbstractFakeConnector {

    @Override
    protected String getJSONDataForDownloads(String type) {
        String data = null;
        if (PackageType.HOT_FIX.getValue().equals(type)) {
            data = "[ ]";
        } else if (PackageType.STUDIO.getValue().equals(type)) {
            data = "[ ]";
        } else if (PackageType.ADDON.getValue().equals(type)) {
            // get feed for advertised addons
            data = "[ ]";
        }

        return data;
    }

    @Override
    protected String getJSONDataForStatus() {
        return "{ contractStatus : 'unregistered', endDate : ''}";
    }

    @Override
    protected String getJSONDataForDownload(String pkgId) {
        throw new UnsupportedOperationException();
    }

}
