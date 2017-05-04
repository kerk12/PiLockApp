package com.kerk12.pilock;

/**
 * Enum used to indicate possible connection errors.
 * INVALID_CERTIFICATE: Invalid certificate. Connection aborted.
 * CONNECTION_ERROR: An error occurred while trying to reach the server.
 */
public enum HttpsConnectionError {
    INVALID_CERTIFICATE,
    CONNECTION_ERROR,
    NOT_CONNECTED_TO_WIFI,
}
