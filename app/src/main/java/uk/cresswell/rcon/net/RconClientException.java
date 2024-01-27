package uk.cresswell.rcon.net;

/*
  Modified from https://github.com/t9t/minecraft-rcon-client to be compatible with non standard-compliant RCON servers
  Licensed under The Apache Software License, Version 2.0
*/

/**
 * Generic exception thrown by {@link RconClientException} when any exception occurs.
 */
public class RconClientException extends RuntimeException {
    public RconClientException(String message) {
        super(message);
    }

    public RconClientException(String message, Throwable cause) {
        super(message, cause);
    }
}