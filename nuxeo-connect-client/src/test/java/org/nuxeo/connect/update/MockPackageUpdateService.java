/*
 * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Julien Carsique
 *
 */

package org.nuxeo.connect.update;

import java.io.File;
import java.io.InputStream;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

import org.nuxeo.connect.packages.PackageManager;
import org.nuxeo.connect.update.model.PackageDefinition;
import org.nuxeo.connect.update.task.Command;

/**
 * @since 1.4.13
 */
public class MockPackageUpdateService implements PackageUpdateService {

    private PackageManager pm;

    public MockPackageUpdateService(PackageManager pm) {
        this.pm = pm;
    }

    @Override
    public File getDataDir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initialize() throws PackageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shutdown() throws PackageException {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void setPackageState(LocalPackage pkg, int state)
            throws PackageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPackageState(LocalPackage pkg, PackageState state)
            throws PackageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalPackage addPackage(File file) throws PackageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removePackage(String id) throws PackageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalPackage getPackage(String id) throws PackageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<LocalPackage> getPackages() throws PackageException {
        return new ArrayList<LocalPackage>();
    }

    @Override
    public LocalPackage getActivePackage(String name) throws PackageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void restart() throws PackageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PackageDefinition loadPackageFromZip(File zip)
            throws PackageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PackageDefinition loadPackage(File file) throws PackageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PackageDefinition loadPackage(InputStream in)
            throws PackageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() throws PackageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Command getCommand(String id) throws PackageException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDefaultInstallTaskType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDefaultUninstallTaskType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isStarted(String pkgId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getRegistry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getBackupDir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileTime getInstallDate(String id) {
        throw new UnsupportedOperationException();
    }

}
