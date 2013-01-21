package com.scidef.betfair.api.aop;

/**
 * Exception thrown when the maximum number of retry attempts for an API call
 * has been exceeded.
 * <p/>
 * User: tompearson
 * Date: 20/01/2013
 */
public class ExceededMaxRetriesException extends RuntimeException {

    private static final long serialVersionUID = 477254575076951928L;

    public ExceededMaxRetriesException() {
        super();
    }

}
