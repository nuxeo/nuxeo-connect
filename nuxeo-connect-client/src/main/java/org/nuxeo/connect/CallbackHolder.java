package org.nuxeo.connect;

import org.nuxeo.connect.update.PackageUpdateService;

public interface CallbackHolder {

    PackageUpdateService getUpdateService();

    boolean isTestModeSet();

    String getProperty(String key, String defaultValue);

    String getHomePath();
}
