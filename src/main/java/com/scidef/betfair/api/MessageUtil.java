package com.scidef.betfair.api;

import com.betfair.publicapi.types.exchange.v5.BetTypeEnum;
import com.betfair.publicapi.types.exchange.v5.Price;
import com.betfair.publicapi.types.exchange.v5.RunnerPrices;
import com.betfair.publicapi.types.global.v3.MarketSummary;
import com.scidef.betfair.api.exception.BetfairException;
import com.scidef.betfair.api.exception.ExceededThrottleException;
import com.scidef.betfair.api.exception.NoSessionException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Useful methods for handling messages to and from the Betfair APIs.
 * <p/>
 * User: tompearson
 * Date: 21/05/2010
 */
public class MessageUtil {

    private static final Pattern COLON_DELIMITER = Pattern.compile("(?<!\\\\):");
    private static final Pattern PIPE_DELIMITER = Pattern.compile("(?<!\\\\)\\|");
    private static final Pattern TILDA_DELIMTER = Pattern.compile("(?<!\\\\)~");

    /**
     * Check a response from the Global API for errors.
     *
     * @param resp the response to check
     */
    public static void checkResponseForAPIError(com.betfair.publicapi.types.global.v3.APIResponse resp)
            throws BetfairException {
        com.betfair.publicapi.types.global.v3.APIErrorEnum errorCode = resp.getHeader().getErrorCode();
        if (errorCode.equals(com.betfair.publicapi.types.global.v3.APIErrorEnum.NO_SESSION)) {
            throw new NoSessionException();
        }
        if (errorCode.equals(com.betfair.publicapi.types.global.v3.APIErrorEnum.EXCEEDED_THROTTLE)) {
            throw new ExceededThrottleException();
        }
    }

    /**
     * Check a response from the Exchange API for errors.
     *
     * @param resp the response to check
     */
    public static void checkResponseForAPIError(com.betfair.publicapi.types.exchange.v5.APIResponse resp)
            throws BetfairException {
        com.betfair.publicapi.types.exchange.v5.APIErrorEnum errorCode = resp.getHeader().getErrorCode();
        if (errorCode.equals(com.betfair.publicapi.types.exchange.v5.APIErrorEnum.NO_SESSION)) {
            throw new NoSessionException();
        }
        if (errorCode.equals(com.betfair.publicapi.types.exchange.v5.APIErrorEnum.EXCEEDED_THROTTLE)) {
            throw new ExceededThrottleException();
        }
    }

    /**
     * Create a string representing the name and time of the market event based on its summary.
     *
     * @param summary the <code>MarketSummary</code> from which to create the market name and time
     * @return a <code>String</code> representing the market name and time
     */
    public static String getMarketNameWithTime(MarketSummary summary) {
        return summary.getMarketName() + " " +
                new SimpleDateFormat("HH:mm").format(
                        summary.getStartTime().toGregorianCalendar().getTime());
    }

    /**
     * Parse the market prices data in compressed form, returning a list of runner
     * prices.
     *
     * @param compressed the compressed market prices data
     * @return a list of <code>RunnerPricesWrapper</code>s containing the parsed data
     */
    public static List<RunnerPricesWrapper> parseCompressedMarketPrices(String compressed) {

        List<RunnerPricesWrapper> listOfRunnerPrices = new ArrayList<RunnerPricesWrapper>();

        String[] splitOnColon = COLON_DELIMITER.split(compressed);

        for (int i = 1; i < splitOnColon.length; i++) {
            RunnerPrices runnerPrices = new RunnerPrices();
            String[] splitOnPipe = PIPE_DELIMITER.split(splitOnColon[i]);

            // details
            String[] runnerDetails = TILDA_DELIMTER.split(splitOnPipe[0], -1);
            runnerPrices.setSelectionId(Integer.valueOf(runnerDetails[0]));
            runnerPrices.setSortOrder(Integer.valueOf(runnerDetails[1]));
            runnerPrices.setTotalAmountMatched(Double.valueOf(runnerDetails[2]));
            runnerPrices.setLastPriceMatched(!runnerDetails[3].equals("") ?
                    Double.valueOf(runnerDetails[3]) : 0);
            runnerPrices.setHandicap(!runnerDetails[4].equals("") ?
                    Double.valueOf(runnerDetails[4]) : null);
            runnerPrices.setReductionFactor(Double.valueOf(runnerDetails[5]));
            runnerPrices.setVacant(!runnerDetails[5].equals("") ?
                    Boolean.valueOf(runnerDetails[6]) : null);
            runnerPrices.setFarBSP(!runnerDetails[7].equals("") ?
                    Double.valueOf(runnerDetails[7]) : null);
            runnerPrices.setNearBSP(!runnerDetails[8].equals("") ?
                    Double.valueOf(runnerDetails[8]) : null);
            runnerPrices.setActualBSP(!runnerDetails[9].equals("") ?
                    Double.valueOf(runnerDetails[9]) : null);

            List<Price> bestPricesToBack = new ArrayList<Price>();
            if (splitOnPipe.length > 1) {
                // back prices
                populatePriceListFromInput(splitOnPipe[1], bestPricesToBack);
            }

            List<Price> bestPricesToLay = new ArrayList<Price>();
            if (splitOnPipe.length > 2) {
                // lay prices
                populatePriceListFromInput(splitOnPipe[2], bestPricesToLay);
            }

            listOfRunnerPrices.add(new RunnerPricesWrapper(runnerPrices, bestPricesToBack, bestPricesToLay));
        }

        return listOfRunnerPrices;
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    private static void populatePriceListFromInput(String input, List<Price> bestPrices) {
        String[] backPrices = TILDA_DELIMTER.split(input);
        if (backPrices.length >= 4) {
            for (int j = 0; j < backPrices.length; j += 4) {
                Price price = new Price();
                price.setPrice(Double.valueOf(backPrices[j]));
                price.setAmountAvailable(Double.valueOf(backPrices[j + 1]));
                price.setBetType(BetTypeEnum.valueOf(backPrices[j + 2]));
                price.setDepth(Integer.valueOf(backPrices[j + 3]));
                bestPrices.add(price);
            }
        }
    }

}
