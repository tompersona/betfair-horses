package com.scidef.betfair.api;

import com.betfair.publicapi.types.exchange.v5.GetMarketPricesCompressedResp;
import com.betfair.publicapi.types.exchange.v5.GetMarketPricesResp;
import com.betfair.publicapi.types.exchange.v5.Price;
import com.betfair.publicapi.types.global.v3.BFEvent;
import com.betfair.publicapi.types.global.v3.GetEventsResp;
import com.betfair.publicapi.types.global.v3.MarketSummary;
import com.scidef.betfair.api.aop.ExceededMaxRetriesException;
import com.scidef.betfair.api.exception.BetfairException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.scidef.betfair.api.MessageUtil.getMarketNameWithTime;
import static com.scidef.betfair.api.MessageUtil.parseCompressedMarketPrices;

/**
 * A collection of methods specifically designed to deal with betting on horse races
 * via the Betfair APIs.
 * <p/>
 * User: tompearson
 * Date: 20/01/2013
 */
public class HorseRacing {

    private static final Logger LOG = LoggerFactory.getLogger(HorseRacing.class);

    public static final int GB_HORSE_RACING_EVENT_ID = 298251;
    public static final int IRE_HORSE_RACING_EVENT_ID = 298252;
    public static final int HORSE_RACING_SPORT_ID = 7;

    private static final Pattern SEMICOLON_DELIMITER = Pattern.compile("(?<!\\\\);");
    private static final Pattern COMMA_DELIMITER = Pattern.compile("(?<!\\\\),");

    private final String username;
    private final String password;
    private boolean loggedIn = false;

    private final GlobalAPI globalAPI;
    private final ExchangeAPI exchangeAPI;

    private List<Pattern> eventInclusionPatterns;
    private List<Pattern> marketInclusionPatterns;  // if inclusion patterns are set, they will be used
    private List<Pattern> marketExclusionPatterns;  // otherwise, exclusion patterns are used

    private boolean mockBets = true; // set this to false to place bets

    public HorseRacing(String username, String password,
                       GlobalAPI globalAPI, ExchangeAPI exchangeAPI) {
        this.username = username;
        this.password = password;
        this.globalAPI = globalAPI;
        this.exchangeAPI = exchangeAPI;
    }

    /**
     * Login to the Betfair Free API using the configured username and password.
     */
    public void login() {
        try {
            globalAPI.login(username, password, GlobalAPI.FREE_API_PRODUCT_ID);
        } catch (BetfairException e) {
            LOG.error("Failed to login.", e);
            throw new RuntimeException(e);
        }
        loggedIn = true;
    }

    /**
     * Retrieve all GB and IRE horse racing events.
     *
     * @return a list of <code>BFEvent</code>s
     * @throws BetfairException if there is a problem retrieving the events
     */
    public List<BFEvent> getEvents() throws BetfairException {
        if (!loggedIn) {
            login();
        }

        List<BFEvent> events = new ArrayList<BFEvent>();

        addEventsForEventId(GB_HORSE_RACING_EVENT_ID, events);
        addEventsForEventId(IRE_HORSE_RACING_EVENT_ID, events);

        return events;
    }

    /**
     * Retrieve all the runners for a given market id.
     *
     * @param marketId the market id for which the runners should be retrieved
     * @return a list of <code>RunnerPricesWrapper</code> with all the runners and prices or
     *         null if the request fails and the maximum number of retries is exceeded
     * @throws BetfairException if there is a problem retrieving the runners
     */
    public List<RunnerPricesWrapper> getRunners(int marketId) throws BetfairException {
        try {
            GetMarketPricesCompressedResp psc = exchangeAPI.getMarketPricesCompressed(marketId);
            return parseCompressedMarketPrices(psc.getMarketPrices());
        } catch (ExceededMaxRetriesException e) {
            LOG.error("Failed to get runners for marketId " + marketId, e);
        }
        return null;
    }

    /**
     * Build a map of runner prices for each market in a given event.
     *
     * @param bfEvent the event to get the map for
     * @param date    the date for the markets to be found; if null then all markets are returned
     * @return a map of runner prices for a set of markets
     * @throws BetfairException if there is a problem building the map of prices
     */
    public Map<MarketSummary, List<RunnerPricesWrapper>> getMarketsMapForEvent(BFEvent bfEvent,
                                                                               GregorianCalendar date)
            throws BetfairException {
        if (!loggedIn) {
            login();
        }

        Map<MarketSummary, List<RunnerPricesWrapper>> marketsMap =
                new HashMap<MarketSummary, List<RunnerPricesWrapper>>();

        // populate marketsMap
        GetEventsResp er = globalAPI.getEvents(bfEvent.getEventId());
        for (MarketSummary marketSummary : er.getMarketItems().getMarketSummary()) {

            // check the market should be included
            boolean marketMatch = true;
            if (date != null) {
                if (marketSummary.getStartTime().getDay() != date.get(Calendar.DAY_OF_MONTH) ||
                        marketSummary.getStartTime().getMonth() != date.get(Calendar.MONTH) + 1 ||
                        marketSummary.getStartTime().getYear() != date.get(Calendar.YEAR)) {
                    LOG.info("Excluding market [" + bfEvent.getEventName() + ": " +
                            getMarketNameWithTime(marketSummary) + "] because of its " +
                            "start time: " + new SimpleDateFormat("dd/MM/yyyy").format(
                            marketSummary.getStartTime().toGregorianCalendar().getTime()));
                    marketMatch = false;
                }
            }
            if (marketMatch) {
                if (marketInclusionPatterns != null && marketInclusionPatterns.size() != 0) {
                    marketMatch = false;
                    for (Pattern marketPattern : marketInclusionPatterns) {
                        if (marketPattern.matcher(marketSummary.getMarketName()).find()) {
                            LOG.info("Including market [" + bfEvent.getEventName() + ": " +
                                    getMarketNameWithTime(marketSummary) + "] because of pattern: " +
                                    marketPattern.pattern());
                            marketMatch = true;
                        }
                    }
                } else {
                    for (Pattern marketPattern : marketExclusionPatterns) {
                        if (marketPattern.matcher(marketSummary.getMarketName()).find()) {
                            LOG.info("Excluding market [" + bfEvent.getEventName() + ": " +
                                    getMarketNameWithTime(marketSummary) + "] because of pattern: " +
                                    marketPattern.pattern());
                            marketMatch = false;
                        }
                    }
                }
            }

            if (!marketMatch) {
                continue;
            }

            try {
                List<RunnerPricesWrapper> runnerPricesWrappers = getRunners(marketSummary.getMarketId());
                marketsMap.put(marketSummary, runnerPricesWrappers);
            } catch (ExceededMaxRetriesException e) {
                LOG.error("Failed to get compressed market prices for marketId " + marketSummary.getMarketId(), e);
            }
        }

        return marketsMap;
    }

    /**
     * Build a map of events to market maps.
     *
     * @param date the date for the markets to be found; if null then all markets are returned
     * @return a map of events to market maps
     * @throws BetfairException if there is a problem building the map
     */
    public Map<BFEvent, Map<MarketSummary, List<RunnerPricesWrapper>>> getEventsMap(GregorianCalendar date)
            throws BetfairException {

        Map<BFEvent, Map<MarketSummary, List<RunnerPricesWrapper>>> eventsMap =
                new HashMap<BFEvent, Map<MarketSummary, List<RunnerPricesWrapper>>>();

        // populate eventsMap
        List<BFEvent> events = getEvents();
        for (BFEvent bfEvent : events) {
            Map<MarketSummary, List<RunnerPricesWrapper>> marketsMap = getMarketsMapForEvent(bfEvent, date);
            eventsMap.put(bfEvent, marketsMap);
        }

        return eventsMap;
    }

    /**
     * Retrieve a list of all the non-runners for a given market id.
     *
     * @param marketId the market id to retrieve non-runners for
     * @return a list of <code>String</code>s representing the non-runners in this market
     * @throws BetfairException if there is a problem retrieving the non-runners
     */
    public List<String> getNonRunners(int marketId) throws BetfairException {
        if (!loggedIn) {
            login();
        }

        GetMarketPricesResp getMarketPricesResp = null;
        try {
            getMarketPricesResp = exchangeAPI.getMarketPrices(marketId);
        } catch (ExceededMaxRetriesException e) {
            throw new BetfairException("Exceeded max retries", e);
        }
        String removedRunners = getMarketPricesResp.getMarketPrices().getRemovedRunners();
        String[] splitOnSemicolon = SEMICOLON_DELIMITER.split(removedRunners);
        List<String> nonRunners = new ArrayList<String>();
        for (String nonRunner : splitOnSemicolon) {
            String[] splitOnComma = COMMA_DELIMITER.split(nonRunner);
            if (splitOnComma.length > 0) {
                nonRunners.add(splitOnComma[0]);
            }
        }
        return nonRunners;
    }

    /**
     * Get the winners for a given horse racing market.
     *
     * @param marketId the market id to get the winners for
     * @return a list of strings representing the winners in the order they placed
     * @throws BetfairException if there is a problem retrieving the winners
     */
    public static List<String> getWinners(int marketId) throws BetfairException {
        return ResultsAPI.getWinners(HORSE_RACING_SPORT_ID, marketId);
    }

    /**
     * Pretty-prints a given event map to the log.
     *
     * @param eventsMap the events map to print
     */
    public static void prettyPrintEventsMapToLog(
            Map<BFEvent, Map<MarketSummary, List<RunnerPricesWrapper>>> eventsMap
    ) {
        LOG.info(buildEventsMapString(eventsMap));
    }

    /**
     * Pretty-prints a given event map to standard output.
     *
     * @param eventsMap the events map to print
     */
    public static void prettyPrintEventsMapToStdout(
            Map<BFEvent, Map<MarketSummary, List<RunnerPricesWrapper>>> eventsMap
    ) {
        System.out.println(buildEventsMapString(eventsMap));
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    public boolean isMockBets() {
        return mockBets;
    }

    public void setMockBets(boolean mockBets) {
        this.mockBets = mockBets;
    }

    public List<Pattern> getEventInclusionPatterns() {
        return eventInclusionPatterns;
    }

    public void setEventInclusionPatterns(List<Pattern> eventInclusionPatterns) {
        this.eventInclusionPatterns = eventInclusionPatterns;
    }

    public List<Pattern> getMarketInclusionPatterns() {
        return marketInclusionPatterns;
    }

    public void setMarketInclusionPatterns(List<Pattern> marketInclusionPatterns) {
        this.marketInclusionPatterns = marketInclusionPatterns;
    }

    public List<Pattern> getMarketExclusionPatterns() {
        return marketExclusionPatterns;
    }

    public void setMarketExclusionPatterns(List<Pattern> marketExclusionPatterns) {
        this.marketExclusionPatterns = marketExclusionPatterns;
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    private void addEventsForEventId(int eventId, List<BFEvent> events) throws BetfairException {
        GetEventsResp eventsResp = globalAPI.getEvents(eventId);
        for (BFEvent bfEvent : eventsResp.getEventItems().getBFEvent()) {
            // check the event should be included
            boolean eventMatch = false;
            for (Pattern eventPattern : eventInclusionPatterns) {
                if (eventPattern.matcher(bfEvent.getEventName()).find()) {
                    eventMatch = true;
                }
            }
            if (eventMatch) {
                events.add(bfEvent);
            }
        }
    }

    private static String buildEventsMapString(Map<BFEvent, Map<MarketSummary, List<RunnerPricesWrapper>>> eventsMap) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<BFEvent, Map<MarketSummary, List<RunnerPricesWrapper>>> eventsMapEntry : eventsMap.entrySet()) {
            sb.append("Event: ").append(eventsMapEntry.getKey().getEventName()).append("\n");
            Map<MarketSummary, List<RunnerPricesWrapper>> marketsMap = eventsMapEntry.getValue();
            for (Map.Entry<MarketSummary, List<RunnerPricesWrapper>> marketsMapEntry : marketsMap.entrySet()) {
                MarketSummary summary = marketsMapEntry.getKey();
                sb.append("  Market: ").append(getMarketNameWithTime(summary)).append("\n");
                List<RunnerPricesWrapper> runnerPricesWrappers = marketsMapEntry.getValue();
                for (RunnerPricesWrapper runnerPricesWrapper : runnerPricesWrappers) {
                    sb.append("    Selection ID: ").append(runnerPricesWrapper.getRunnerPrices().getSelectionId())
                            .append("\n");
                    List<Price> bestPricesToBack = runnerPricesWrapper.getBestPricesToBack();
                    sb.append("    > Best prices to back <\n");
                    for (Price price : bestPricesToBack) {
                        sb.append("      P: ")
                                .append(price.getPrice()).append("; A: ").append(price.getAmountAvailable())
                                .append("\n");
                    }
                    List<Price> bestPricesToLay = runnerPricesWrapper.getBestPricesToLay();
                    sb.append("    > Best prices to lay <\n");
                    for (Price price : bestPricesToLay) {
                        sb.append("      P: ")
                                .append(price.getPrice()).append("; A: ").append(price.getAmountAvailable())
                                .append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }

    // for tests
    boolean isLoggedIn() {
        return loggedIn;
    }
}
