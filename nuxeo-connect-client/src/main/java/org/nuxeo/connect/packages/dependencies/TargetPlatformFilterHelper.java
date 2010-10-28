package org.nuxeo.connect.packages.dependencies;

import org.nuxeo.connect.data.DownloadablePackage;

public class TargetPlatformFilterHelper {

    public static boolean isCompatibleWithTargetPlatform(DownloadablePackage pkg, String targetPlatform) {
        if (targetPlatform==null) {
            return true;
        }
        if (pkg.getTargetPlatforms()==null || pkg.getTargetPlatforms().length==0) {
            return true;
        }
        for (String pf : pkg.getTargetPlatforms()) {
            if (pf.equals(targetPlatform)) {
                return true;
            }
        }
        return false;
    }
}
