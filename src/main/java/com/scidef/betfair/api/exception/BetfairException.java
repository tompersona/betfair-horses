package com.scidef.betfair.api.exception;

/**
 * A general exception for problems with the Betfair APIs.
 * <p/>
 * User: tompearson
 * Date: 21/05/2010
 */
public class BetfairException extends Exception {

    private static final long serialVersionUID = -1490984894267153508L;

    public BetfairException() {
    }

    public BetfairException(String message) {
        super(message);
    }

    public BetfairException(String message, Throwable cause) {
        super(message, cause);
    }
}
