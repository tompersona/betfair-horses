package com.scidef.betfair.api;


import com.betfair.publicapi.types.global.v3.APIRequest;
import com.betfair.publicapi.types.global.v3.APIRequestHeader;
import com.betfair.publicapi.types.global.v3.GetEventTypesReq;
import com.betfair.publicapi.types.global.v3.GetEventTypesResp;
import com.betfair.publicapi.types.global.v3.GetEventsErrorEnum;
import com.betfair.publicapi.types.global.v3.GetEventsReq;
import com.betfair.publicapi.types.global.v3.GetEventsResp;
import com.betfair.publicapi.types.global.v3.LoginErrorEnum;
import com.betfair.publicapi.types.global.v3.LoginReq;
import com.betfair.publicapi.types.global.v3.LoginResp;
import com.betfair.publicapi.types.global.v3.LogoutErrorEnum;
import com.betfair.publicapi.types.global.v3.LogoutReq;
import com.betfair.publicapi.types.global.v3.LogoutResp;
import com.betfair.publicapi.v3.bfglobalservice.BFGlobalService;
import com.betfair.publicapi.v3.bfglobalservice.BFGlobalService_Service;
import com.scidef.betfair.api.exception.BetfairException;

/**
 * This class provides methods wrapping some of Betfair's high-level global services such as login,
 * logout and event type retrieval.
 * <p/>
 * User: tompearson
 * Date: 21/05/2010
 */
public class GlobalAPI {

    public static final int FREE_API_PRODUCT_ID = 82;

    private BFGlobalService globalService;
    private String sessionToken;

    /**
     * Primary constructor
     */
    public GlobalAPI() {
        this.globalService = new BFGlobalService_Service().getBFGlobalService();
    }

    /**
     * Login to a Betfair account for a specific product.
     * <p/>
     * For the Free API, use product id <code>FREE_API_PRODUCT_ID</code>.
     *
     * @param username  a Betfair username
     * @param password  the password associated with the Betfair username
     * @param productId a Betfair product id
     * @throws BetfairException if there is a problem logging in
     */
    public void login(String username, String password, int productId) throws BetfairException {
        LoginReq req = new LoginReq();
        req.setUsername(username);
        req.setPassword(password);
        req.setProductId(productId);
        req.setLocationId(0);
        req.setVendorSoftwareId(0);

        LoginResp resp = globalService.login(req);
        if (resp.getErrorCode().equals(LoginErrorEnum.OK)) {
            sessionToken = resp.getHeader().getSessionToken();
            return;
        }
        MessageUtil.checkResponseForAPIError(resp);
        throw new BetfairException("Login error. Error code: " + resp.getErrorCode() + "; API code: "
                + resp.getHeader().getErrorCode());
    }

    /**
     * Logout of the current Betfair session.
     *
     * @throws BetfairException if there is a problem logging out
     */
    public void logout() throws BetfairException {
        LogoutReq req = new LogoutReq();
        addHeader(req);
        LogoutResp resp = globalService.logout(req);
        if (resp.getErrorCode().equals(LogoutErrorEnum.OK)) {
            sessionToken = resp.getHeader().getSessionToken();
            return;
        }
        MessageUtil.checkResponseForAPIError(resp);
        throw new BetfairException("Logout error. Error code: " + resp.getErrorCode() + "; API code: "
                + resp.getHeader().getErrorCode());
    }

    /**
     * Retrieve all the top-level Betfair event types for the current user and product.
     *
     * @return a <code>GetEventTypesResp</code> object containing details of the available event types
     */
    public GetEventTypesResp getAllEventTypes() {
        GetEventTypesReq req = new GetEventTypesReq();
        addHeader(req);
        return globalService.getAllEventTypes(req);
    }

    /**
     * Having retrieved all the top-level Betfair event types using <code>getAllEventTypes()</code>,
     * this method can be used to drill down into the hierarchy by querying for events by a parent
     * id.
     *
     * @param eventParentId a parent event id in the event types hierarchy
     * @return a <code>GetEventsResp</code> object containing details of the events beneath the given parent event
     * @throws BetfairException if there is a problem retrieving the events
     */
    public GetEventsResp getEvents(int eventParentId) throws BetfairException {
        GetEventsReq req = new GetEventsReq();
        addHeader(req);
        req.setEventParentId(eventParentId);
        GetEventsResp resp = globalService.getEvents(req);
        if (resp.getErrorCode().equals(GetEventsErrorEnum.OK)) {
            return resp;
        }
        MessageUtil.checkResponseForAPIError(resp);
        throw new BetfairException("getEvents error. Error code: " + resp.getErrorCode() + "; API code: "
                + resp.getHeader().getErrorCode());

    }

    /**
     * Provides the session token for the current user and product.
     *
     * @return a session token if a user is logged in or null otherwise
     */
    public String getSessionToken() {
        return sessionToken;
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    // simple helper method to set the current session token on a request
    private void addHeader(APIRequest request) {
        APIRequestHeader header = new APIRequestHeader();
        header.setSessionToken(sessionToken);
        request.setHeader(header);
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    // for tests
    GlobalAPI(BFGlobalService globalService) {
        this.globalService = globalService;
    }

    // for tests
    void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

}
