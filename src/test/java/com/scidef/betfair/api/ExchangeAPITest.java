package com.scidef.betfair.api;

import com.betfair.publicapi.types.exchange.v5.APIErrorEnum;
import com.betfair.publicapi.types.exchange.v5.APIResponseHeader;
import com.betfair.publicapi.types.exchange.v5.BetCategoryTypeEnum;
import com.betfair.publicapi.types.exchange.v5.BetPersistenceTypeEnum;
import com.betfair.publicapi.types.exchange.v5.BetStatusEnum;
import com.betfair.publicapi.types.exchange.v5.BetTypeEnum;
import com.betfair.publicapi.types.exchange.v5.BetsOrderByEnum;
import com.betfair.publicapi.types.exchange.v5.GetAccountFundsReq;
import com.betfair.publicapi.types.exchange.v5.GetAccountFundsResp;
import com.betfair.publicapi.types.exchange.v5.GetAllMarketsReq;
import com.betfair.publicapi.types.exchange.v5.GetAllMarketsResp;
import com.betfair.publicapi.types.exchange.v5.GetBetErrorEnum;
import com.betfair.publicapi.types.exchange.v5.GetBetReq;
import com.betfair.publicapi.types.exchange.v5.GetBetResp;
import com.betfair.publicapi.types.exchange.v5.GetMUBetsErrorEnum;
import com.betfair.publicapi.types.exchange.v5.GetMUBetsReq;
import com.betfair.publicapi.types.exchange.v5.GetMUBetsResp;
import com.betfair.publicapi.types.exchange.v5.GetMarketErrorEnum;
import com.betfair.publicapi.types.exchange.v5.GetMarketPricesCompressedReq;
import com.betfair.publicapi.types.exchange.v5.GetMarketPricesCompressedResp;
import com.betfair.publicapi.types.exchange.v5.GetMarketPricesErrorEnum;
import com.betfair.publicapi.types.exchange.v5.GetMarketPricesReq;
import com.betfair.publicapi.types.exchange.v5.GetMarketPricesResp;
import com.betfair.publicapi.types.exchange.v5.GetMarketReq;
import com.betfair.publicapi.types.exchange.v5.GetMarketResp;
import com.betfair.publicapi.types.exchange.v5.PlaceBets;
import com.betfair.publicapi.types.exchange.v5.PlaceBetsErrorEnum;
import com.betfair.publicapi.types.exchange.v5.PlaceBetsReq;
import com.betfair.publicapi.types.exchange.v5.PlaceBetsResp;
import com.betfair.publicapi.types.exchange.v5.SortOrderEnum;
import com.betfair.publicapi.v5.bfexchangeservice.BFExchangeService;
import com.scidef.betfair.api.exception.BetfairException;
import com.scidef.betfair.api.exception.ExceededThrottleException;
import com.scidef.betfair.api.exception.NoSessionException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import static com.scidef.betfair.api.TestConstant.BET_ID;
import static com.scidef.betfair.api.TestConstant.MARKET_ID;
import static com.scidef.betfair.api.TestConstant.SESSION_TOKEN;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A set of tests covering the <code>ExchangeAPI</code>.
 * <p/>
 * User: tompearson
 * Date: 20/01/2013
 */
public class ExchangeAPITest {

    private BFExchangeService exchangeService;
    private GlobalAPI globalAPI;
    private ExchangeAPI exchangeAPI;

    @Before
    public void setupExchangeAPI() {
        exchangeService = mock(BFExchangeService.class);
        globalAPI = mock(GlobalAPI.class);
        when(globalAPI.getSessionToken()).thenReturn(SESSION_TOKEN);
        exchangeAPI = new ExchangeAPI(exchangeService, globalAPI);
    }

    @Test
    public void testGetAccountFunds() {
        GetAccountFundsResp resp = new GetAccountFundsResp();
        when(exchangeService.getAccountFunds((GetAccountFundsReq) anyObject())).thenReturn(resp);

        assertThat(exchangeAPI.getAccountFunds(), is(resp));

        verify(globalAPI).getSessionToken();
        verify(exchangeService).getAccountFunds(argThat(is(any(GetAccountFundsReq.class))));
    }

    @Test
    public void testGetAllMarkets() {
        GetAllMarketsResp resp = new GetAllMarketsResp();
        when(exchangeService.getAllMarkets((GetAllMarketsReq) anyObject())).thenReturn(resp);

        assertThat(exchangeAPI.getAllMarkets(), is(resp));

        verify(globalAPI).getSessionToken();
        verify(exchangeService).getAllMarkets(argThat(is(any(GetAllMarketsReq.class))));
    }

    @Test
    public void testGetMarket() throws BetfairException {
        GetMarketResp resp = mock(GetMarketResp.class);
        when(resp.getErrorCode()).thenReturn(GetMarketErrorEnum.OK);
        when(exchangeService.getMarket((GetMarketReq) anyObject())).thenReturn(resp);

        assertThat(exchangeAPI.getMarket(MARKET_ID), is(resp));

        verify(globalAPI).getSessionToken();
        verify(exchangeService).getMarket(argThat(is(aGetMarketReqWithMarketId(MARKET_ID))));
    }

    @Test(expected = BetfairException.class)
    public void testGetMarketFailure() throws BetfairException {
        APIResponseHeader header = createMockHeader(APIErrorEnum.OK, SESSION_TOKEN);
        GetMarketResp resp = mock(GetMarketResp.class);
        when(resp.getErrorCode()).thenReturn(GetMarketErrorEnum.INVALID_MARKET);
        when(resp.getHeader()).thenReturn(header);
        when(exchangeService.getMarket((GetMarketReq) anyObject())).thenReturn(resp);

        exchangeAPI.getMarket(MARKET_ID);
    }

    @Test(expected = NoSessionException.class)
    public void testGetMarketWithNoSession() throws BetfairException {
        APIResponseHeader header = createMockHeader(APIErrorEnum.NO_SESSION, SESSION_TOKEN);
        GetMarketResp resp = mock(GetMarketResp.class);
        when(resp.getErrorCode()).thenReturn(GetMarketErrorEnum.API_ERROR);
        when(resp.getHeader()).thenReturn(header);
        when(exchangeService.getMarket((GetMarketReq) anyObject())).thenReturn(resp);

        exchangeAPI.getMarket(MARKET_ID);
    }

    @Test(expected = ExceededThrottleException.class)
    public void testGetMarketExceededThrottle() throws BetfairException {
        APIResponseHeader header = createMockHeader(APIErrorEnum.EXCEEDED_THROTTLE, SESSION_TOKEN);
        GetMarketResp resp = mock(GetMarketResp.class);
        when(resp.getErrorCode()).thenReturn(GetMarketErrorEnum.API_ERROR);
        when(resp.getHeader()).thenReturn(header);
        when(exchangeService.getMarket((GetMarketReq) anyObject())).thenReturn(resp);

        exchangeAPI.getMarket(MARKET_ID);
    }

    @Test
    public void testGetMarketPrices() throws BetfairException {
        GetMarketPricesResp resp = mock(GetMarketPricesResp.class);
        when(resp.getErrorCode()).thenReturn(GetMarketPricesErrorEnum.OK);
        when(exchangeService.getMarketPrices((GetMarketPricesReq) anyObject())).thenReturn(resp);

        assertThat(exchangeAPI.getMarketPrices(MARKET_ID), is(resp));

        verify(globalAPI).getSessionToken();
        verify(exchangeService).getMarketPrices(argThat(is(aGetMarketPricesReqWithMarketId(MARKET_ID))));
    }

    @Test(expected = BetfairException.class)
    public void testGetMarketPricesFailure() throws BetfairException {
        APIResponseHeader header = createMockHeader(APIErrorEnum.OK, SESSION_TOKEN);
        GetMarketPricesResp resp = mock(GetMarketPricesResp.class);
        when(resp.getErrorCode()).thenReturn(GetMarketPricesErrorEnum.INVALID_MARKET);
        when(resp.getHeader()).thenReturn(header);
        when(exchangeService.getMarketPrices((GetMarketPricesReq) anyObject())).thenReturn(resp);

        exchangeAPI.getMarketPrices(MARKET_ID);
    }

    @Test
    public void testGetMarketPricesCompressed() throws BetfairException {
        GetMarketPricesCompressedResp resp = mock(GetMarketPricesCompressedResp.class);
        when(resp.getErrorCode()).thenReturn(GetMarketPricesErrorEnum.OK);
        when(exchangeService.getMarketPricesCompressed((GetMarketPricesCompressedReq) anyObject())).thenReturn(resp);

        assertThat(exchangeAPI.getMarketPricesCompressed(MARKET_ID), is(resp));

        verify(globalAPI).getSessionToken();
        verify(exchangeService).getMarketPricesCompressed(argThat(is(
                aGetMarketPricesCompressedReqWithMarketId(MARKET_ID)
        )));
    }

    @Test(expected = BetfairException.class)
    public void testGetMarketPricesCompressedFailure() throws BetfairException {
        APIResponseHeader header = createMockHeader(APIErrorEnum.OK, SESSION_TOKEN);
        GetMarketPricesCompressedResp resp = mock(GetMarketPricesCompressedResp.class);
        when(resp.getErrorCode()).thenReturn(GetMarketPricesErrorEnum.INVALID_MARKET);
        when(resp.getHeader()).thenReturn(header);
        when(exchangeService.getMarketPricesCompressed((GetMarketPricesCompressedReq) anyObject())).thenReturn(resp);

        exchangeAPI.getMarketPricesCompressed(MARKET_ID);
    }

    @Test
    public void testGetBet() throws BetfairException {
        GetBetResp resp = mock(GetBetResp.class);
        when(resp.getErrorCode()).thenReturn(GetBetErrorEnum.OK);
        when(exchangeService.getBet((GetBetReq) anyObject())).thenReturn(resp);

        assertThat(exchangeAPI.getBet(BET_ID), is(resp));

        verify(globalAPI).getSessionToken();
        verify(exchangeService).getBet(argThat(is(aGetBetReqWithBetId(BET_ID))));
    }

    @Test(expected = BetfairException.class)
    public void testGetBetFailure() throws BetfairException {
        APIResponseHeader header = createMockHeader(APIErrorEnum.OK, SESSION_TOKEN);
        GetBetResp resp = mock(GetBetResp.class);
        when(resp.getErrorCode()).thenReturn(GetBetErrorEnum.API_ERROR);
        when(resp.getHeader()).thenReturn(header);
        when(exchangeService.getBet((GetBetReq) anyObject())).thenReturn(resp);

        exchangeAPI.getBet(BET_ID);
    }

    @Test
    public void testGetMatchedAndUnmatchedBets() throws BetfairException {
        GetMUBetsResp resp = mock(GetMUBetsResp.class);
        when(resp.getErrorCode()).thenReturn(GetMUBetsErrorEnum.OK);
        when(exchangeService.getMUBets((GetMUBetsReq) anyObject())).thenReturn(resp);

        assertThat(exchangeAPI.getMatchedAndUnmatchedBets(MARKET_ID, BetStatusEnum.M), is(resp));

        verify(globalAPI).getSessionToken();
        verify(exchangeService).getMUBets(argThat(is(
                aGetMUBetsReqWith(MARKET_ID, BetStatusEnum.M, BetsOrderByEnum.NONE, 200, SortOrderEnum.ASC, 0)
        )));
    }

    @Test(expected = BetfairException.class)
    public void testGetMatchedAndUnmatchedBetsFailure() throws BetfairException {
        APIResponseHeader header = createMockHeader(APIErrorEnum.OK, SESSION_TOKEN);
        GetMUBetsResp resp = mock(GetMUBetsResp.class);
        when(resp.getErrorCode()).thenReturn(GetMUBetsErrorEnum.API_ERROR);
        when(resp.getHeader()).thenReturn(header);
        when(exchangeService.getMUBets((GetMUBetsReq) anyObject())).thenReturn(resp);

        exchangeAPI.getMatchedAndUnmatchedBets(MARKET_ID, BetStatusEnum.M);
    }

    @Test
    public void testPlaceBets() throws BetfairException {
        PlaceBetsResp resp = mock(PlaceBetsResp.class);
        when(resp.getErrorCode()).thenReturn(PlaceBetsErrorEnum.OK);
        when(exchangeService.placeBets((PlaceBetsReq) anyObject())).thenReturn(resp);

        Bet bet = new Bet(MARKET_ID, 123, BetTypeEnum.B, 2.0, 4.0,
                new GregorianCalendar().getTime(), "Somewhere", "Description");
        bet.setSize(40.0);
        List<Bet> bets = Collections.singletonList(bet);

        assertThat(exchangeAPI.placeBets(bets), is(resp));

        verify(exchangeService).placeBets(argThat(is(
                aPlaceBetsReqWith(0, BetCategoryTypeEnum.E, BetPersistenceTypeEnum.NONE,
                        BetTypeEnum.B, 0.0, MARKET_ID, 2.0, 123, 40.0)
        )));
    }

    @Test(expected = BetfairException.class)
    public void testPlaceBetsFailure() throws BetfairException {
        APIResponseHeader header = createMockHeader(APIErrorEnum.OK, SESSION_TOKEN);
        PlaceBetsResp resp = mock(PlaceBetsResp.class);
        when(resp.getErrorCode()).thenReturn(PlaceBetsErrorEnum.API_ERROR);
        when(resp.getHeader()).thenReturn(header);
        when(exchangeService.placeBets((PlaceBetsReq) anyObject())).thenReturn(resp);

        exchangeAPI.placeBets(new ArrayList<Bet>());
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    private static APIResponseHeader createMockHeader(APIErrorEnum errorCode, String sessionToken) {
        APIResponseHeader header = mock(APIResponseHeader.class);
        when(header.getErrorCode()).thenReturn(errorCode);
        when(header.getSessionToken()).thenReturn(sessionToken);

        return header;
    }

    private static Matcher<GetMarketReq> aGetMarketReqWithMarketId(final int marketId) {
        return new TypeSafeMatcher<GetMarketReq>() {
            @Override
            protected boolean matchesSafely(GetMarketReq req) {
                return marketId == req.getMarketId();
            }

            public void describeTo(Description description) {
                description.appendText(
                        String.format("a GetMarketReq with marketId %d", marketId)
                );
            }
        };
    }

    private static Matcher<GetMarketPricesReq> aGetMarketPricesReqWithMarketId(final int marketId) {
        return new TypeSafeMatcher<GetMarketPricesReq>() {
            @Override
            protected boolean matchesSafely(GetMarketPricesReq req) {
                return marketId == req.getMarketId();
            }

            public void describeTo(Description description) {
                description.appendText(
                        String.format("a GetMarketPricesReq with marketId %d", marketId)
                );
            }
        };
    }

    private static Matcher<GetMarketPricesCompressedReq> aGetMarketPricesCompressedReqWithMarketId(
            final int marketId) {
        return new TypeSafeMatcher<GetMarketPricesCompressedReq>() {
            @Override
            protected boolean matchesSafely(GetMarketPricesCompressedReq req) {
                return marketId == req.getMarketId();
            }

            public void describeTo(Description description) {
                description.appendText(
                        String.format("a GetMarketPricesCompressedReq with marketId %d", marketId)
                );
            }
        };
    }

    private static Matcher<GetBetReq> aGetBetReqWithBetId(final long betId) {
        return new TypeSafeMatcher<GetBetReq>() {
            @Override
            protected boolean matchesSafely(GetBetReq req) {
                return betId == req.getBetId();
            }

            public void describeTo(Description description) {
                description.appendText(
                        String.format("a GetBetReq with betId %d", betId)
                );
            }
        };
    }

    private static Matcher<GetMUBetsReq> aGetMUBetsReqWith(
            final int marketId,
            final BetStatusEnum betStatus,
            final BetsOrderByEnum orderBy,
            final int recordCount,
            final SortOrderEnum sortOrder,
            final int startRecord
    ) {
        return new TypeSafeMatcher<GetMUBetsReq>() {
            @Override
            protected boolean matchesSafely(GetMUBetsReq req) {
                return marketId == req.getMarketId()
                        && betStatus.equals(req.getBetStatus())
                        && orderBy.equals(req.getOrderBy())
                        && recordCount == req.getRecordCount()
                        && sortOrder.equals(req.getSortOrder())
                        && startRecord == req.getStartRecord();
            }

            public void describeTo(Description description) {
                description.appendText(
                        String.format("a GetMUBetsReq with marketId %d, betStatus %s, " +
                                "orderBy %s, recordCount %d, sortOrder %s, startRecord %d",
                                marketId, betStatus, orderBy, recordCount, sortOrder, startRecord)
                );
            }
        };
    }

    private static Matcher<PlaceBetsReq> aPlaceBetsReqWith(
            final int asianLineId,
            final BetCategoryTypeEnum betCategoryType,
            final BetPersistenceTypeEnum betPersistenceType,
            final BetTypeEnum betType,
            final double bspLiability,
            final int marketId,
            final double price,
            final int selectionId,
            final double size
    ) {
        return new TypeSafeMatcher<PlaceBetsReq>() {
            @Override
            protected boolean matchesSafely(PlaceBetsReq req) {
                List<PlaceBets> placeBets = req.getBets().getPlaceBets();
                if (placeBets.size() < 1) {
                    return false;
                }
                PlaceBets bet = placeBets.get(0);
                return asianLineId == bet.getAsianLineId()
                        && betCategoryType.equals(bet.getBetCategoryType())
                        && betPersistenceType.equals(bet.getBetPersistenceType())
                        && betType.equals(bet.getBetType())
                        && bspLiability == bet.getBspLiability()
                        && marketId == bet.getMarketId()
                        && price == bet.getPrice()
                        && selectionId == bet.getSelectionId()
                        && size == bet.getSize();
            }

            public void describeTo(Description description) {
                description.appendText(
                        String.format("a PlaceBetsReq with asianLineId %d, betCategoryType %s, " +
                                "betPersistenceType %s, betType %s, bspLiability %f, marketId %d, " +
                                "price %f, selectionId %d, size %f", asianLineId,
                                betCategoryType, betPersistenceType, betType, bspLiability, marketId,
                                price, selectionId, size)
                );
            }
        };
    }

}
