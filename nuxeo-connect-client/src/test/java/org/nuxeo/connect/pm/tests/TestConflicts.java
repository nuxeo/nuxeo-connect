/*
 * (C) Copyright 2018 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Yannis JULIENNE
 *
 */

package org.nuxeo.connect.pm.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nuxeo.connect.connector.AbstractConnectConnector.CONNECT_SERVER_REACHABLE_PROPERTY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.dependencies.CUDFHelper;
import org.nuxeo.connect.packages.dependencies.DependencyResolution;

/**
 * @since 1.7.1
 */
public class TestConflicts extends AbstractPackageManagerTestCase {

    protected DummyPackageSource remoteSource;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        List<DownloadablePackage> local = getDownloads("localConflict.json");
        List<DownloadablePackage> remote = getDownloads("remoteConflict.json");
        assertTrue(CollectionUtils.isNotEmpty(local));
        assertTrue(CollectionUtils.isNotEmpty(remote));
        pm.registerSource(new DummyPackageSource(local, "localConflict"), true);
        remoteSource = new DummyPackageSource(remote, "remoteConflict");
        pm.registerSource(remoteSource, false);
    }

    public void testUninstallOnConflict() throws Exception {
        // verify that nuxeo-jsf-ui-9.10.0 installation triggers nuxeo-9.10-HF01 uninstall
        DependencyResolution depResolution = pm.resolveDependencies(Arrays.asList("nuxeo-jsf-ui-9.10.0"), null, null,
                null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(1, depResolution.getLocalPackagesToRemove().size());
        assertTrue(depResolution.getLocalPackagesToRemove().containsKey("nuxeo-9.10-HF01"));
        assertEquals(1, depResolution.getOrderedPackageIdsToInstall().size());
        assertTrue(depResolution.getOrderedPackageIdsToInstall().contains("nuxeo-jsf-ui-9.10.0"));
    }

    public void testInstallWithoutConflict() throws Exception {
        // verify that nuxeo-jsf-ui-9.10.0-HF01 installation is ok when not precising the version of jsf-ui to install
        DependencyResolution depResolution = pm.resolveDependencies(Arrays.asList("nuxeo-jsf-ui"), null, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
        assertEquals(1, depResolution.getOrderedPackageIdsToInstall().size());
        assertTrue(depResolution.getOrderedPackageIdsToInstall().contains("nuxeo-jsf-ui-9.10.0-HF01"));

        // Change remote source to make only nuxeo-jsf-ui-9.10.0-HF01 available
        remoteSource.reset(getDownloads("remoteConflict2.json"));

        // verify that nuxeo-jsf-ui-9.10.0-HF01 installation is ok when nuxeo-jsf-ui-9.10.0 is not available (see
        // NXP-25164)
        depResolution = pm.resolveDependencies(Arrays.asList("nuxeo-jsf-ui-9.10.0-HF01"), null, null, null);
        log.info(depResolution.toString());
        assertTrue(depResolution.isValidated());
        assertEquals(0, depResolution.getLocalPackagesToRemove().size());
        assertEquals(1, depResolution.getOrderedPackageIdsToInstall().size());
        assertTrue(depResolution.getOrderedPackageIdsToInstall().contains("nuxeo-jsf-ui-9.10.0-HF01"));
    }

    // NXP-34135
    public void testNoWarningForUnknownConflictWhenOffline() throws Exception {
        // the installed nuxeo-9.10-HF01 conflicts with nuxeo-jsf-ui, which can only be found in the remote source
        remoteSource.reset(Collections.emptyList());
        assertThat(getUnknownDependencyLogs(false)).containsExactly(
                "DEBUG Unknown dependency: nuxeo-jsf-ui:9.10.0:9.10.0");
    }

    // NXP-34135
    public void testWarningForUnknownConflictWhenOnline() throws Exception {
        remoteSource.reset(Collections.emptyList());
        assertThat(getUnknownDependencyLogs(true)).containsExactly(
                "WARN Unknown dependency: nuxeo-jsf-ui:9.10.0:9.10.0");
    }

    protected List<String> getUnknownDependencyLogs(boolean connectServerReachable) throws Exception {
        List<String> logs = new ArrayList<>();
        AbstractAppender appender = new AbstractAppender("TestConflicts", null, null, false, null) {
            @Override
            public void append(LogEvent event) {
                String message = event.getMessage().getFormattedMessage();
                if (message.startsWith("Unknown dependency")) {
                    logs.add(event.getLevel() + " " + message);
                }
            }
        };
        appender.start();
        Logger logger = (Logger) LogManager.getLogger(CUDFHelper.class);
        Level previousLevel = logger.getLevel();
        logger.addAppender(appender);
        // set the level after adding the appender, as adding it might reset the logger level from its configuration
        logger.setLevel(Level.DEBUG);
        String previousReachable = System.setProperty(CONNECT_SERVER_REACHABLE_PROPERTY,
                String.valueOf(connectServerReachable));
        try {
            DependencyResolution depResolution = pm.resolveDependencies(Arrays.asList("nuxeo-9.10-HF01"), null, null,
                    null);
            assertTrue(depResolution.isValidated());
        } finally {
            if (previousReachable == null) {
                System.clearProperty(CONNECT_SERVER_REACHABLE_PROPERTY);
            } else {
                System.setProperty(CONNECT_SERVER_REACHABLE_PROPERTY, previousReachable);
            }
            logger.removeAppender(appender);
            logger.setLevel(previousLevel);
            appender.stop();
        }
        return logs;
    }

}
