package com.scidef.betfair.api;

import com.betfair.publicapi.types.exchange.v5.APIRequest;
import com.betfair.publicapi.types.exchange.v5.APIRequestHeader;
import com.betfair.publicapi.types.exchange.v5.ArrayOfPlaceBets;
import com.betfair.publicapi.types.exchange.v5.BetCategoryTypeEnum;
import com.betfair.publicapi.types.exchange.v5.BetPersistenceTypeEnum;
import com.betfair.publicapi.types.exchange.v5.BetStatusEnum;
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
import com.betfair.publicapi.v5.bfexchangeservice.BFExchangeService_Service;
import com.scidef.betfair.api.exception.BetfairException;

import java.util.List;

/**
 * This class provides methods wrapping a subset of Betfair's exchange services.
 * <p/>
 * User: tompearson
 * Date: 21/05/2010
 */
public class ExchangeAPI {

    private BFExchangeService exchangeService;
    private GlobalAPI globalAPI;

    /**
     * Primary constructor
     *
     * @param globalAPI an instance of the <code>GlobalAPI</code>, used to retrieve session details
     */
    public ExchangeAPI(GlobalAPI globalAPI) {
        this.globalAPI = globalAPI;
        exchangeService = new BFExchangeService_Service().getBFExchangeService();
    }

    /**
     * Get the available funds for the current user.
     *
     * @return a <code>GetAccountFundsResp</code> object containing details of available funds
     */
    public GetAccountFundsResp getAccountFunds() {
        GetAccountFundsReq req = new GetAccountFundsReq();
        addHeader(req);
        return exchangeService.getAccountFunds(req);
    }

    /**
     * Retrieve information about all of the markets that are currently active or suspended.
     *
     * @return a <code>GetAllMarketsResp</code> object containing details of the markets
     */
    public GetAllMarketsResp getAllMarkets() {
        GetAllMarketsReq req = new GetAllMarketsReq();
        addHeader(req);
        return exchangeService.getAllMarkets(req);
    }

    /**
     * Retrieve all static market data for the market requested.
     * <p/>
     * The <code>marketId</code> can be found using <code>GlobalAPI.getEvents(int eventParentId)</code>.
     *
     * @param marketId the id of the market
     * @return a <code>GetMarketResp</code> object with details of the market requested
     * @throws BetfairException if there is a problem retrieving the market
     */
    public GetMarketResp getMarket(int marketId) throws BetfairException {
        GetMarketReq req = new GetMarketReq();
        addHeader(req);
        req.setMarketId(marketId);
        GetMarketResp resp = exchangeService.getMarket(req);
        if (resp.getErrorCode().equals(GetMarketErrorEnum.OK)) {
            return resp;
        }
        MessageUtil.checkResponseForAPIError(resp);
        throw new BetfairException("getMarket error. Error code: " + resp.getErrorCode() + "; API code: "
                + resp.getHeader().getErrorCode());
    }

    /**
     * Retrieve dynamic market data for a given market.
     * <p/>
     * The <code>marketId</code> can be found using <code>GlobalAPI.getEvents(int eventParentId)</code>.
     *
     * @param marketId the id of the market
     * @return a <code>GetMarketPricesResp</code> object with details of the market requested
     * @throws BetfairException if there is a problem retrieving the market prices
     */
    public GetMarketPricesResp getMarketPrices(int marketId) throws BetfairException {
        GetMarketPricesReq req = new GetMarketPricesReq();
        addHeader(req);
        req.setMarketId(marketId);
        GetMarketPricesResp resp = exchangeService.getMarketPrices(req);
        if (resp.getErrorCode().equals(GetMarketPricesErrorEnum.OK)) {
            return resp;
        }
        MessageUtil.checkResponseForAPIError(resp);
        throw new BetfairException("getMarketPrices error. Error code: " + resp.getErrorCode() + "; API code: "
                + resp.getHeader().getErrorCode());
    }

    /**
     * Retrieve dynamic market data for a given market in a tilde-delimited string.
     * <p/>
     * The <code>marketId</code> can be found using <code>GlobalAPI.getEvents(int eventParentId)</code>.
     *
     * @param marketId the id of the market
     * @return a <code>GetMarketPricesCompressedResp</code> object with details of the market requested
     * @throws BetfairException if there is a problem retrieving the market prices
     */
    public GetMarketPricesCompressedResp getMarketPricesCompressed(int marketId) throws BetfairException {
        GetMarketPricesCompressedReq req = new GetMarketPricesCompressedReq();
        addHeader(req);
        req.setMarketId(marketId);
        GetMarketPricesCompressedResp resp = exchangeService.getMarketPricesCompressed(req);
        if (resp.getErrorCode().equals(GetMarketPricesErrorEnum.OK)) {
            return resp;
        }
        MessageUtil.checkResponseForAPIError(resp);
        throw new BetfairException("getMarketPricesCompressed error. Error code: " + resp.getErrorCode() + "; API code: "
                + resp.getHeader().getErrorCode());
    }

    /**
     * Retrieve information about a particular bet. Each request will retrieve all
     * components of the desired bet.
     *
     * @param betId the unique bet identifier
     * @return a <code>GetBetResp</code> object containing details of the bet
     * @throws BetfairException if there is a problem retrieving the bet
     */
    public GetBetResp getBet(long betId) throws BetfairException {
        GetBetReq req = new GetBetReq();
        addHeader(req);
        req.setBetId(betId);
        GetBetResp resp = exchangeService.getBet(req);
        if (resp.getErrorCode().equals(GetBetErrorEnum.OK)) {
            return resp;
        }
        MessageUtil.checkResponseForAPIError(resp);
        throw new BetfairException("getBet error. Error code: " + resp.getErrorCode() + "; API code: "
                + resp.getHeader().getErrorCode());
    }

    /**
     * Retrieve information about all matched and unmatched bets for the current user.
     *
     * @param marketId  the id of the market
     * @param betStatus the status of the bets to return (matched, unmatched, or both)
     * @return a <code>GetMUBetsResp</code> object containing details of the bets
     * @throws BetfairException if there is a problem retrieving the bets
     */
    public GetMUBetsResp getMatchedAndUnmatchedBets(int marketId, BetStatusEnum betStatus) throws BetfairException {
        GetMUBetsReq req = new GetMUBetsReq();
        addHeader(req);
        req.setMarketId(marketId);
        req.setBetStatus(betStatus);
        req.setOrderBy(BetsOrderByEnum.NONE);
        req.setRecordCount(200);
        req.setSortOrder(SortOrderEnum.ASC);
        req.setStartRecord(0);
        GetMUBetsResp resp = exchangeService.getMUBets(req);
        if (resp.getErrorCode().equals(GetMUBetsErrorEnum.OK)) {
            return resp;
        }
        MessageUtil.checkResponseForAPIError(resp);
        throw new BetfairException("getMatchedAndUnmatchedBets error. Error code: " + resp.getErrorCode() + "; API code: "
                + resp.getHeader().getErrorCode());
    }

    /**
     * Place bets (1 to 60) on a single market.
     *
     * @param bets a list of <code>Bet</code>s to place
     * @return a <code>PlaceBetsResp</code> object containing the outcome of placing the bets
     * @throws BetfairException if there is a problem placing the bets
     */
    public PlaceBetsResp placeBets(List<Bet> bets) throws BetfairException {
        PlaceBetsReq req = new PlaceBetsReq();
        addHeader(req);
        ArrayOfPlaceBets arrayOfPlaceBets = new ArrayOfPlaceBets();
        for (Bet bet : bets) {
            PlaceBets placeBets = new PlaceBets();
            placeBets.setAsianLineId(0);
            placeBets.setBetCategoryType(BetCategoryTypeEnum.E);
            placeBets.setBetPersistenceType(BetPersistenceTypeEnum.NONE);
            placeBets.setBetType(bet.getBetType());
            placeBets.setBspLiability(0.0);
            placeBets.setMarketId(bet.getMarketId());
            placeBets.setPrice(bet.getPrice());
            placeBets.setSelectionId(bet.getSelectionId());
            placeBets.setSize(bet.getSize());
            arrayOfPlaceBets.getPlaceBets().add(placeBets);
        }
        req.setBets(arrayOfPlaceBets);
        PlaceBetsResp resp = exchangeService.placeBets(req);
        if (resp.getErrorCode().equals(PlaceBetsErrorEnum.OK)) {
            return resp;
        }
        MessageUtil.checkResponseForAPIError(resp);
        throw new BetfairException("placeBets error. Error code: " + resp.getErrorCode() + "; API code: "
                + resp.getHeader().getErrorCode());
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    // simple helper method to set the current session token on a request
    private void addHeader(APIRequest req) {
        APIRequestHeader header = new APIRequestHeader();
        header.setSessionToken(globalAPI.getSessionToken());
        req.setHeader(header);
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    // for tests
    ExchangeAPI(BFExchangeService exchangeService, GlobalAPI globalAPI) {
        this.exchangeService = exchangeService;
        this.globalAPI = globalAPI;
    }
}
