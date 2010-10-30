package org.nuxeo.connect.packages;

import java.util.List;

import org.nuxeo.connect.data.DownloadablePackage;

public interface BasePackageManager {

    DownloadablePackage getPackage(String pkgId);

    List<DownloadablePackage> listInstalledPackages();

}
