package org.nuxeo.connect.packages.dependencies;

public class DependencyException extends Exception {

    private static final long serialVersionUID = 1L;

    public DependencyException(String message) {
        super(message);
    }

    public DependencyException(String message, Throwable t) {
        super(message,t);
    }
}
