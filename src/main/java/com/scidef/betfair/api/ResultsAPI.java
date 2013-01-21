package com.scidef.betfair.api;

import com.scidef.betfair.api.exception.BetfairException;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A class to handle results retrieval from the Betfair RSS feed.
 * <p/>
 * User: tompearson
 * Date: 11/06/2010
 */
public class ResultsAPI {

    private static final String BETFAIR_RSS_URL = "http://rss.betfair.com/RSS.aspx";

    private static final Pattern COLON_DELIMITER = Pattern.compile(":");
    private static final Pattern COMMA_DELIMITER = Pattern.compile(",");

    /**
     * Get the winners of an event as defined by the given sport and market ids.
     *
     * @param sportId  the id of the sport
     * @param marketId the id of the market
     * @return a list of strings representing the winners in the order they placed
     * @throws BetfairException if there is a problem retrieving the winners
     */
    public static List<String> getWinners(int sportId, int marketId) throws BetfairException {
        String url = BETFAIR_RSS_URL + "?format=rss" +
                "&sportID=" + sportId +
                "&marketID=" + marketId;
        SyndFeed feed = fetchFeed(url);

        if (feed != null && feed.getEntries() != null && feed.getEntries().size() > 0) {
            SyndEntry entry = (SyndEntry) feed.getEntries().get(0);
            return parseWinners(entry.getDescription().getValue().trim());
        }
        throw new BetfairException("Problem fetching results from URL " +
                url);
    }

    /**
     * Utility method to work out the position of a given runner.
     * <p/>
     * The answer could be first (1), second (2), etc – or 0 if the runner did not place.
     *
     * @param sportId    the id of the sport
     * @param marketId   the id of the market
     * @param runnerName the name of the runner
     * @return an <code>int</code> representing the position the runner placed or 0 if the runner did not place
     * @throws BetfairException if there is a problem getting the position
     */
    public static int getPosition(int sportId, int marketId, String runnerName) throws BetfairException {
        List<String> winners = getWinners(sportId, marketId);
        int position = 1;
        for (String winner : winners) {
            if (winner.equals(runnerName)) {
                return position;
            }
            position++;
        }
        return 0;
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    static List<String> parseWinners(String winnersString) throws BetfairException {
        String[] splitOnColon = COLON_DELIMITER.split(winnersString);
        if (splitOnColon.length != 2) {
            throw new BetfairException("Winners string [" + winnersString + "] is incorrectly formatted");
        }
        String[] splitOnComma = COMMA_DELIMITER.split(splitOnColon[1]);
        List<String> winners = new ArrayList<String>();
        for (String s : splitOnComma) {
            winners.add(s.trim());
        }
        return winners;
    }

    private static SyndFeed fetchFeed(String url) throws BetfairException {
        SyndFeed feed;
        try {
            URL feedSource = new URL(url);
            SyndFeedInput input = new SyndFeedInput();
            feed = input.build(new XmlReader(feedSource));
        } catch (Exception e) {
            throw new BetfairException("Problem fetching results from URL " +
                    url, e);
        }
        return feed;
    }


}
