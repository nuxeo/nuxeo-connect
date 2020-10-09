/*
 * (C) Copyright 2018 Nuxeo (http://nuxeo.com/).
 * This is unpublished proprietary source code of Nuxeo SA. All rights reserved.
 * Notice of copyright on this source code does not indicate publication.
 *
 * Contributors:
 *      Mincong Huang
 */
package org.nuxeo.connect.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.Test;

public class PlatformIdTest {

    @Test
    public void parse_ok() {
        assertThat(PlatformId.parse("server-9.10")).isEqualTo(PlatformId.of("server", new PlatformVersion("9.10")));
        assertThat(PlatformId.parse("server-10.10-SNAPSHOT")).isEqualTo(
                PlatformId.of("server", new PlatformVersion("10.10-SNAPSHOT")));

        assertThat(PlatformId.parse("server-9.10-I20181119_2333")).isEqualTo(
                PlatformId.of("server", new PlatformVersion("9.10-I20181119_2333")));
    }

    @Test
    public void parse_failed() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> PlatformId.parse("9.10-SNAPSHOT"));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> PlatformId.parse("server-9_10-I20181119_2333"));
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> PlatformId.parse("server"));
    }

    @Test
    public void asString() {
        assertThat(PlatformId.of("server", new PlatformVersion("9.10")).asString()).isEqualTo("server-9.10.0");
        assertThat(PlatformId.of("server", new PlatformVersion("10.10-SNAPSHOT")).asString()).isEqualTo(
                "server-10.10.0-SNAPSHOT");
    }
}
