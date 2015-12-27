/*
 * (C) Copyright 2006-2009 Nuxeo SA (http://nuxeo.com/) and others.
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

package org.nuxeo.connect.data;

import java.util.EnumSet;

/**
 * Enum for status of a Connect Subscription.
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public enum SubscriptionStatusType {

    UNKNOWN("unknown"),
    OK("ok"),
    EXPIRED("expired"),
    NOT_STARTED("not_started"),
    WRONG_DATE("wrong_dates"),
    NO_CONTRACT("no_contract");

    private final String value;

    SubscriptionStatusType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static SubscriptionStatusType getByValue(String value){
        SubscriptionStatusType returnValue = null;
        for (final SubscriptionStatusType element : EnumSet.allOf(SubscriptionStatusType.class)) {
            if (element.toString().equals(value)) {
                returnValue = element;
            }
        }
        return returnValue;
    }
}
