package com.scidef.betfair.api.aop;

import com.scidef.betfair.api.HorseRacing;
import com.scidef.betfair.api.exception.ExceededThrottleException;
import com.scidef.betfair.api.exception.NoSessionException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This aspect handles expired API sessions, exceeded throttle exceptions and
 * other API errors by intercepting the exception and retrying the method in question.
 * <p/>
 * If the session has expired, then a new login will be attempted before retrying the
 * method.
 * If the exception is due to exceeding the throttle for a given API method then
 * the method will only be retried after <code>exceededThrottleSleepTime</code> ms.
 * For all other exceptions, the method will be retried after
 * <code>standardSleepTime</code> ms in case the problem was intermittent.
 * In all cases, only <code>maxRetries</code> retries will be attempted before
 * an <code>ExceededMaxRetriesException</code> is thrown.
 * <p/>
 * User: tompearson
 * Date: 25/06/2010
 */
@Aspect
public class RetryMethodAspect {

    private final static Logger LOG = LoggerFactory.getLogger(RetryMethodAspect.class);

    private int maxRetries = 3;
    private long exceededThrottleSleepTime = 60000L;
    private long standardSleepTime = 10000L;

    private HorseRacing horseRacing;

    public RetryMethodAspect(HorseRacing horseRacing) {
        this.horseRacing = horseRacing;
    }

    @Pointcut("execution(* com.scidef.betfair.api.*API.*(..))")
    public void retry() {
    }

    @Around("retry()")
    public Object retryMethod(ProceedingJoinPoint pjp) {
        return retryMethod(pjp, maxRetries);
    }

    public Object retryMethod(ProceedingJoinPoint pjp, int retries) {
        if (retries == 0) {
            throw new ExceededMaxRetriesException();
        }
        try {
            return pjp.proceed();
        } catch (Throwable t) {
            if (t instanceof NoSessionException) {
                horseRacing.login();
            } else {
                try {
                    Thread.sleep((t instanceof ExceededThrottleException) ?
                            exceededThrottleSleepTime : standardSleepTime);
                } catch (InterruptedException e) {
                    // restore interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            LOG.warn("Method [" + pjp.getSignature().toShortString() + "] failed with exception:", t);
            return retryMethod(pjp, retries - 1);
        }
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setExceededThrottleSleepTime(long exceededThrottleSleepTime) {
        this.exceededThrottleSleepTime = exceededThrottleSleepTime;
    }

    public void setStandardSleepTime(long standardSleepTime) {
        this.standardSleepTime = standardSleepTime;
    }

}
