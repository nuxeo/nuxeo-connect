package org.nuxeo.connect.packages;

import java.util.Comparator;

import org.nuxeo.connect.data.DownloadablePackage;

public class VersionPackageComparator implements Comparator<DownloadablePackage>{

    @Override
    public int compare(DownloadablePackage p1, DownloadablePackage p2) {
        return p1.getId().compareToIgnoreCase(p2.getId());
    }

}
