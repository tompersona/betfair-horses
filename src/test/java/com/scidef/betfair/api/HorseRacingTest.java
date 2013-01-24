package com.scidef.betfair.api;

import com.betfair.publicapi.types.exchange.v5.GetMarketPricesCompressedResp;
import com.betfair.publicapi.types.exchange.v5.GetMarketPricesResp;
import com.betfair.publicapi.types.exchange.v5.MarketPrices;
import com.betfair.publicapi.types.global.v3.ArrayOfBFEvent;
import com.betfair.publicapi.types.global.v3.BFEvent;
import com.betfair.publicapi.types.global.v3.GetEventsResp;
import com.scidef.betfair.api.exception.BetfairException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static com.scidef.betfair.api.HorseRacing.GB_HORSE_RACING_EVENT_ID;
import static com.scidef.betfair.api.HorseRacing.IRE_HORSE_RACING_EVENT_ID;
import static com.scidef.betfair.api.TestConstant.MARKET_ID;
import static com.scidef.betfair.api.TestConstant.PASSWORD;
import static com.scidef.betfair.api.TestConstant.USERNAME;
import static com.scidef.betfair.api.TestConstant.COMPRESSED_MARKET_DATA;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the <code>HorseRacing</code> class.
 * <p/>
 * User: tompearson
 * Date: 20/01/2013
 */
public class HorseRacingTest {

    private GlobalAPI globalAPI;
    private ExchangeAPI exchangeAPI;
    private HorseRacing horseRacing;

    @Before
    public void setupHorseRacing() {
        globalAPI = mock(GlobalAPI.class);
        exchangeAPI = mock(ExchangeAPI.class);
        horseRacing = new HorseRacing(USERNAME, PASSWORD, globalAPI, exchangeAPI);
    }

    @Test
    public void testSuccessfulLogin() throws BetfairException {
        assertThat(horseRacing.isLoggedIn(), is(false));

        horseRacing.login();

        assertThat(horseRacing.isLoggedIn(), is(true));

        verify(globalAPI).login(USERNAME, PASSWORD, GlobalAPI.FREE_API_PRODUCT_ID);
    }

    @Test(expected = RuntimeException.class)
    public void testFailedLogin() throws BetfairException {
        doThrow(new BetfairException()).when(globalAPI).login(USERNAME, PASSWORD, GlobalAPI.FREE_API_PRODUCT_ID);

        horseRacing.login();
    }

    @Test
    public void testSuccessfulLogout() throws BetfairException {
        horseRacing.login();

        assertThat(horseRacing.isLoggedIn(), is(true));

        horseRacing.logout();

        assertThat(horseRacing.isLoggedIn(), is(false));

        verify(globalAPI).login(USERNAME, PASSWORD, GlobalAPI.FREE_API_PRODUCT_ID);
        verify(globalAPI).logout();
    }

    @Test(expected = RuntimeException.class)
    public void testFailedLogout() throws BetfairException {
        doThrow(new BetfairException()).when(globalAPI).logout();

        horseRacing.login();
        horseRacing.logout();
    }

    @Test
    public void testGetEvents() throws BetfairException {
        horseRacing.setEventInclusionPatterns(
                Collections.singletonList(Pattern.compile("^[A-Za-z]*? [0-9]*?[a-z][a-z] [A-Za-z]*?$"))
        );

        GetEventsResp gbResp = new GetEventsResp();
        ArrayOfBFEvent gbEvents = new ArrayOfBFEvent();
        BFEvent gbEvent1 = new BFEvent();
        gbEvent1.setEventName("Ling 22nd Jan");
        BFEvent gbEvent2 = new BFEvent();
        gbEvent2.setEventName("Non-matching event");
        gbEvents.getBFEvent().add(gbEvent1);
        gbEvents.getBFEvent().add(gbEvent2);
        gbResp.setEventItems(gbEvents);
        when(globalAPI.getEvents(GB_HORSE_RACING_EVENT_ID)).thenReturn(gbResp);

        GetEventsResp ireResp = new GetEventsResp();
        ArrayOfBFEvent ireEvents = new ArrayOfBFEvent();
        BFEvent ireEvent1 = new BFEvent();
        ireEvent1.setEventName("Punch 1st Mar");
        BFEvent ireEvent2 = new BFEvent();
        ireEvent2.setEventName("Another non-matching event");
        ireEvents.getBFEvent().add(ireEvent1);
        ireEvents.getBFEvent().add(ireEvent2);
        ireResp.setEventItems(ireEvents);
        when(globalAPI.getEvents(IRE_HORSE_RACING_EVENT_ID)).thenReturn(ireResp);

        List<BFEvent> events = horseRacing.getEvents();

        assertThat(events.size(), is(2));
        assertThat(events, hasItem(aBFEventWithEventName("Ling 22nd Jan")));
        assertThat(events, hasItem(aBFEventWithEventName("Punch 1st Mar")));
    }

    @Test
    public void testGetRunners() throws BetfairException {
        GetMarketPricesCompressedResp resp = new GetMarketPricesCompressedResp();
        resp.setMarketPrices(COMPRESSED_MARKET_DATA);
        when(exchangeAPI.getMarketPricesCompressed(MARKET_ID)).thenReturn(resp);

        List<RunnerPricesWrapper> runners = horseRacing.getRunners(MARKET_ID);

        assertThat(runners.size(), is(13));
    }

    @Test
    public void testGetNonRunners() throws BetfairException {
        GetMarketPricesResp resp = new GetMarketPricesResp();
        MarketPrices prices = new MarketPrices();
        prices.setRemovedRunners(
                "Kheskianto,10.18,2.0;True Pleasure,9.28,6.7;Teth,10.04,11.1;" +
                        "Leitrim King,12.01,13.2;Sleepy Lucy,11.43,2.3;"
        );
        resp.setMarketPrices(prices);
        when(exchangeAPI.getMarketPrices(MARKET_ID)).thenReturn(resp);

        List<String> nonRunners = horseRacing.getNonRunners(MARKET_ID);

        assertThat(nonRunners.size(), is(5));
        assertThat(nonRunners, hasItem(is("Kheskianto")));
        assertThat(nonRunners, hasItem(is("True Pleasure")));
        assertThat(nonRunners, hasItem(is("Teth")));
        assertThat(nonRunners, hasItem(is("Leitrim King")));
        assertThat(nonRunners, hasItem(is("Sleepy Lucy")));
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    private Matcher<BFEvent> aBFEventWithEventName(final String eventName) {
        return new TypeSafeMatcher<BFEvent>() {
            @Override
            protected boolean matchesSafely(BFEvent bfEvent) {
                return eventName.equals(bfEvent.getEventName());
            }

            public void describeTo(Description description) {
                description.appendText(
                        String.format("a BFEvent with eventName %s", eventName)
                );
            }
        };
    }

}
