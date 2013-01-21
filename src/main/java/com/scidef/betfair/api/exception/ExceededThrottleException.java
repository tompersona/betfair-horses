package com.scidef.betfair.api.exception;

/**
 * An exception thrown when the permitted number of Betfair API requests per minute has been
 * exceeded.
 * <p/>
 * User: tompearson
 * Date: 21/05/2010
 */
public class ExceededThrottleException extends BetfairException {

    private static final long serialVersionUID = -5729718973221965772L;

    public ExceededThrottleException() {
        super();
    }

}
