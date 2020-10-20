/*
 * (C) Copyright 2010-2020 Nuxeo SA (http://nuxeo.com/) and others.
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
 */
package org.nuxeo.connect.platform;

/**
 * Identifier for a platform
 */
public class PlatformId {

    private final String name;

    private final PlatformVersion version;

    PlatformId(String name, PlatformVersion version) {
        if (name == null) {
            throw new NullPointerException("Null name");
        }
        this.name = name;
        if (version == null) {
            throw new NullPointerException("Null version");
        }
        this.version = version;
    }

    public String name() {
        return name;
    }

    public PlatformVersion version() {
        return version;
    }

    @Override
    public String toString() {
        return "PlatformId{" + "name=" + name + ", " + "version=" + version + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof PlatformId) {
            PlatformId that = (PlatformId) o;
            return (this.name.equals(that.name())) && (this.version.equals(that.version()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h$ = 1;
        h$ *= 1000003;
        h$ ^= name.hashCode();
        h$ *= 1000003;
        h$ ^= version.hashCode();
        return h$;
    }

    public static PlatformId parse(String id) {
        return parseWithHyphen(id, id.lastIndexOf('-'));
    }

    private static PlatformId parseWithHyphen(String id, int lastHyphenIdx) {
        if (lastHyphenIdx == -1) {
            throw new IllegalArgumentException("Bad platform id: " + id);
        }
        String name = id.substring(0, lastHyphenIdx);
        String version = id.substring(lastHyphenIdx + 1);
        try {
            return parse(name, version);
        } catch (RuntimeException e) {
            return parseWithHyphen(id, id.substring(0, lastHyphenIdx).lastIndexOf('-'));
        }
    }

    public static PlatformId parse(String name, String version) {
        return of(name, new PlatformVersion(version));
    }

    public static PlatformId of(String name, PlatformVersion version) {
        return new PlatformId(name, version);
    }

    public String asString() {
        return name() + "-" + version().asString();
    }

}
