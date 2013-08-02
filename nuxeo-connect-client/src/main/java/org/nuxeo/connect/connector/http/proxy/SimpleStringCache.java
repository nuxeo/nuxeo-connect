package org.nuxeo.connect.connector.http.proxy;

import java.util.Date;

/**
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 */
public class SimpleStringCache {

    private long duration;

    private long lastSetTime;

    private String value;

    /**
     * Instantiates a new SimpleStringCache.
     *
     * @param duration in minutes
     */
    public SimpleStringCache(int duration) {
        this.duration = duration * 60 * 1000;
    }

    public void saveValue(String value) {
        this.value = value;
        lastSetTime = new Date().getTime();
    }

    public String getValue() {
        if (new Date().getTime() - lastSetTime > duration) {
            value = null;
        }
        return value;
    }
}
