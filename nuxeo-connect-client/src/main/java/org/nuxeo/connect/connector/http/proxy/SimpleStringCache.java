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

import java.util.Date;

/**
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 1.4.15
 */
public class SimpleStringCache {

    private long duration;

    private long lastSetTime;

    private String value;

    /**
     * Instantiates a new SimpleStringCache.
     *
     * @param duration in minutes
     */
    public SimpleStringCache(int duration) {
        this.duration = duration * 60 * 1000;
    }

    public void saveValue(@SuppressWarnings("hiding") String value) {
        this.value = value;
        lastSetTime = new Date().getTime();
    }

    public String getValue() {
        if (new Date().getTime() - lastSetTime > duration) {
            value = null;
        }
        return value;
    }
}
