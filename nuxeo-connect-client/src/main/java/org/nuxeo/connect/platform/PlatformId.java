/*
 * (C) Copyright 2018 Nuxeo (http://nuxeo.com/).
 * This is unpublished proprietary source code of Nuxeo SA. All rights reserved.
 * Notice of copyright on this source code does not indicate publication.
 *
 * Contributors:
 *      Mincong Huang
 */
package org.nuxeo.connect.platform;

import com.google.auto.value.AutoValue;

/**
 * Identifier for a platform
 */
@AutoValue
public abstract class PlatformId { // NOSONAR

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
        return new AutoValue_PlatformId(name, version);
    }

    public abstract String name();

    public abstract PlatformVersion version();

    public String asString() {
        return name() + '-' + version().asString();
    }

}
