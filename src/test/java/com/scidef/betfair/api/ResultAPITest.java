package com.scidef.betfair.api;

import com.scidef.betfair.api.exception.BetfairException;
import org.junit.Test;

import java.util.List;

import static com.scidef.betfair.api.ResultsAPI.parseWinners;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for the <code>ResultAPI</code> class.
 * <p/>
 * User: tompearson
 * Date: 20/01/2013
 */
public class ResultAPITest {

    @Test
    public void testOneWinnerExpression() throws BetfairException {
        String s = "Winner(s): Blah";
        List<String> winners = parseWinners(s);
        assertThat(winners.size(), is(1));
        assertThat(winners.get(0), is("Blah"));
    }

    @Test
    public void testThreeWinnersExpression() throws BetfairException {
        String s = "Winner(s): Blah, Foo, Wobble";
        List<String> winners = parseWinners(s);
        assertThat(winners.size(), is(3));
        assertThat(winners.get(0), is("Blah"));
        assertThat(winners.get(1), is("Foo"));
        assertThat(winners.get(2), is("Wobble"));
    }

}
