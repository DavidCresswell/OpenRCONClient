package uk.cresswell.rcon.net;

/*
  Modified from https://github.com/t9t/minecraft-rcon-client to be compatible with non standard-compliant RCON servers
  Licensed under The Apache Software License, Version 2.0
*/

/**
 * Exception thrown by {@link RconClientException} when the specified password is incorrect.
 */
public class AuthFailureException extends RconClientException {
    public AuthFailureException() {
        super("Authentication failure");
    }

    @Override
    public String toString() {
        return getMessage();
    }
}