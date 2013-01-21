package com.scidef.betfair.api.exception;

/**
 * An exception thrown when there is currently no active Betfair session.
 * <p/>
 * User: tompearson
 * Date: 21/05/2010
 */
public class NoSessionException extends BetfairException {

    private static final long serialVersionUID = 2599572616217129820L;

    public NoSessionException() {
        super();
    }

}
