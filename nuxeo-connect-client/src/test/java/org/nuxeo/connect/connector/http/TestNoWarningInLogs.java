/*
 * (C) Copyright 2017 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Damien Metzler
 */
package org.nuxeo.connect.connector.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.connect.data.SubscriptionStatus;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class TestNoWarningInLogs {

    private TestAppender appender;

    private Logger logger;

    @Test
    public void call_with_set_cookie_with_expires_should_not_raise_a_warning() throws Exception {

        try (MockWebServer server = new MockWebServer()) {

            // Given a Connect server that returns a Set-Cookie with Expires value
            MockResponse response = new MockResponse()//
                                                      .setResponseCode(200)
                                                      .setBody("{}")
                                                      .setHeader("Set-Cookie",
                                                              "Path=/;Max-Age=31536000;Secure;Expires=Wed, 17 Jun 2020 16:00:18 GMT;HttpOnly");
            server.enqueue(response);
            server.start();

            // When I do a simple call on the Connect API
            ConnectHttpConnector httpConnector = new ConnectHttpConnector();
            httpConnector.overrideUrl = server.url("/").url().toString();
            SubscriptionStatus status = httpConnector.getConnectStatus();
            assertNotNull(status);

            List<LoggingEvent> warnings = appender.getLog()
                                                  .stream()
                                                  .filter(l -> l.getLevel().equals(Level.WARN))
                                                  .collect(Collectors.toList());

            // Then I should not get any warnings in the logs
            warnings.forEach(l -> System.out.println(String.format("%s %s", l.getLevel(), l.getMessage())));
            assertEquals(0, warnings.size());

        }
    }

    @Before
    public void doBefore() {
        appender = new TestAppender();
        logger = Logger.getRootLogger();
        logger.addAppender(appender);
    }

    @After
    public void doAfter() {
        logger.removeAppender(appender);
    }

    class TestAppender extends AppenderSkeleton {
        private final List<LoggingEvent> log = new ArrayList<LoggingEvent>();

        @Override
        public boolean requiresLayout() {
            return false;
        }

        @Override
        protected void append(final LoggingEvent loggingEvent) {
            log.add(loggingEvent);
        }

        @Override
        public void close() {
        }

        public List<LoggingEvent> getLog() {
            return new ArrayList<LoggingEvent>(log);
        }
    }
}
