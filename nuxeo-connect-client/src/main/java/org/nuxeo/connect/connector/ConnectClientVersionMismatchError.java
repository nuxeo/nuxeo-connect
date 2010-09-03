package org.nuxeo.connect.connector;

public class ConnectClientVersionMismatchError extends ConnectServerError {

    private static final long serialVersionUID = 1L;

    public ConnectClientVersionMismatchError(String message) {
        super(message);
    }

}
