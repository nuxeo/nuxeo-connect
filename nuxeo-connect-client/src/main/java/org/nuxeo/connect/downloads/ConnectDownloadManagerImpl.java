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
package org.nuxeo.connect.downloads;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.nuxeo.connect.NuxeoConnectClient;
import org.nuxeo.connect.connector.service.ConnectGatewayComponent;
import org.nuxeo.connect.data.DownloadingPackage;
import org.nuxeo.connect.data.PackageDescriptor;

/**
 * 
 * Implementation of the {@link ConnectDownloadManager} interface. This
 * implementation is accessed via {@link ConnectGatewayComponent}
 * 
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public class ConnectDownloadManagerImpl implements ConnectDownloadManager {

    protected BlockingQueue<Runnable> pendingDownloadTasks = new ArrayBlockingQueue<Runnable>(
            25);

    protected ThreadPoolExecutor tpexec = new ThreadPoolExecutor(1, 5, 300,
            TimeUnit.SECONDS, pendingDownloadTasks, new DaemonThreadFactory());

    protected Map<String, DownloadingPackage> downloadingPackages = new HashMap<String, DownloadingPackage>();

    public List<DownloadingPackage> listDownloadingPackages() {

        List<DownloadingPackage> result = new ArrayList<DownloadingPackage>();
        result.addAll(downloadingPackages.values());
        return result;
    }

    public DownloadingPackage storeDownloadedBundle(String sourceId,
            PackageDescriptor descriptor) {

        LocalDownloadingPackage localPackage = new LocalDownloadingPackage(
                sourceId, descriptor);
        tpexec.execute(localPackage);
        downloadingPackages.put(localPackage.getId(), localPackage);
        return localPackage;
    }

    public String getDownloadedBundleLocalStorage() {

        String path = NuxeoConnectClient.getHomePath();
        path = path + "ConnectDownloads/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return path;
    }

    public void removeDownloadingPackage(String packageId) {
        downloadingPackages.remove(packageId);
    }

    protected static class DaemonThreadFactory implements ThreadFactory {

        private final ThreadGroup group;

        private final String namePrefix;

        private static final AtomicInteger poolNumber = new AtomicInteger();

        private final AtomicInteger threadNumber = new AtomicInteger();

        public DaemonThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup()
                    : Thread.currentThread().getThreadGroup();
            namePrefix = "ConnectDownloadThread-"
                    + poolNumber.incrementAndGet() + '-';
        }

        @Override
        public Thread newThread(Runnable r) {
            String name = namePrefix + threadNumber.incrementAndGet();
            Thread t = new Thread(group, r, name);
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }

    }
}
