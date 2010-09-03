package org.nuxeo.connect;

import org.nuxeo.connect.update.PackageUpdateService;

public class DefaultCallbackHolder implements CallbackHolder {

    public String getHomePath() {
        return getProperty("java.io.tmpdir", null) + "/";
    }

    public String getProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }

    public boolean isTestModeSet() {
        return Boolean.parseBoolean(getProperty(
                "org.nuxeo.connect.client.testMode", "false"));
    }

    @Override
    public PackageUpdateService getUpdateService() {
        return null;
    }
}
