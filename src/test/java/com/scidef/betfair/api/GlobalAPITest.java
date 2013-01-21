package com.scidef.betfair.api;

import com.betfair.publicapi.types.global.v3.APIErrorEnum;
import com.betfair.publicapi.types.global.v3.APIResponseHeader;
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
import com.scidef.betfair.api.exception.BetfairException;
import com.scidef.betfair.api.exception.ExceededThrottleException;
import com.scidef.betfair.api.exception.NoSessionException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.scidef.betfair.api.TestConstant.SESSION_TOKEN;
import static com.scidef.betfair.api.TestConstant.NEW_SESSION_TOKEN;
import static com.scidef.betfair.api.TestConstant.PARENT_ID;
import static com.scidef.betfair.api.TestConstant.USERNAME;
import static com.scidef.betfair.api.TestConstant.PASSWORD;

/**
 * A set of tests covering the <code>GlobalAPI</code>.
 * <p/>
 * User: tompearson
 * Date: 17/01/2013
 */
public class GlobalAPITest {

    private BFGlobalService globalService;
    private GlobalAPI globalAPI;

    @Before
    public void setupGlobalAPI() {
        globalService = mock(BFGlobalService.class);
        globalAPI = new GlobalAPI(globalService);
    }

    @Test
    public void testSuccessfulLogin() throws BetfairException {
        APIResponseHeader header = createMockHeader(APIErrorEnum.OK, SESSION_TOKEN);

        LoginResp resp = mock(LoginResp.class);
        when(resp.getErrorCode()).thenReturn(LoginErrorEnum.OK);
        when(resp.getHeader()).thenReturn(header);
        when(globalService.login((LoginReq) anyObject())).thenReturn(resp);

        globalAPI.login(USERNAME, PASSWORD, GlobalAPI.FREE_API_PRODUCT_ID);

        verify(header).getSessionToken();
        verify(resp).getErrorCode();
        verify(resp).getHeader();
        verify(globalService).login(argThat(is(aLoginReqWith(USERNAME, PASSWORD, GlobalAPI.FREE_API_PRODUCT_ID))));

        assertThat(globalAPI.getSessionToken(), is(SESSION_TOKEN));
    }

    @Test(expected = BetfairException.class)
    public void testFailedLogin() throws BetfairException {
        APIResponseHeader header = createMockHeader(APIErrorEnum.OK, SESSION_TOKEN);

        LoginResp resp = mock(LoginResp.class);
        when(resp.getErrorCode()).thenReturn(LoginErrorEnum.ACCOUNT_CLOSED);
        when(resp.getHeader()).thenReturn(header);
        when(globalService.login((LoginReq) anyObject())).thenReturn(resp);

        globalAPI.login(USERNAME, PASSWORD, GlobalAPI.FREE_API_PRODUCT_ID);
    }

    @Test
    public void testSuccessfulLogout() throws BetfairException {
        globalAPI.setSessionToken(SESSION_TOKEN);

        APIResponseHeader header = createMockHeader(APIErrorEnum.OK, NEW_SESSION_TOKEN);

        LogoutResp resp = mock(LogoutResp.class);
        when(resp.getErrorCode()).thenReturn(LogoutErrorEnum.OK);
        when(resp.getHeader()).thenReturn(header);
        when(globalService.logout((LogoutReq) anyObject())).thenReturn(resp);

        globalAPI.logout();

        verify(header).getSessionToken();
        verify(resp).getErrorCode();
        verify(resp).getHeader();
        verify(globalService).logout(argThat(is(any(LogoutReq.class))));

        assertThat(globalAPI.getSessionToken(), is(NEW_SESSION_TOKEN));
    }

    @Test(expected = BetfairException.class)
    public void testFailedLogout() throws BetfairException {
        globalAPI.setSessionToken(SESSION_TOKEN);

        APIResponseHeader header = createMockHeader(APIErrorEnum.OK, SESSION_TOKEN);

        LogoutResp resp = mock(LogoutResp.class);
        when(resp.getErrorCode()).thenReturn(LogoutErrorEnum.API_ERROR);
        when(resp.getHeader()).thenReturn(header);
        when(globalService.logout((LogoutReq) anyObject())).thenReturn(resp);

        globalAPI.logout();
    }

    @Test
    public void testGetAllEventTypes() {
        globalAPI.setSessionToken(SESSION_TOKEN);

        GetEventTypesResp resp = new GetEventTypesResp();
        when(globalService.getAllEventTypes((GetEventTypesReq) anyObject())).thenReturn(resp);

        assertThat(globalAPI.getAllEventTypes(), is(resp));

        verify(globalService).getAllEventTypes(argThat(is(any(GetEventTypesReq.class))));
    }

    @Test
    public void testGetEvents() throws BetfairException {
        globalAPI.setSessionToken(SESSION_TOKEN);

        GetEventsResp resp = mock(GetEventsResp.class);
        when(resp.getErrorCode()).thenReturn(GetEventsErrorEnum.OK);
        when(globalService.getEvents((GetEventsReq) anyObject())).thenReturn(resp);

        assertThat(globalAPI.getEvents(PARENT_ID), is(resp));

        verify(resp).getErrorCode();
        verify(globalService).getEvents(argThat(is(aGetEventsReqWithParentId(PARENT_ID))));
    }

    @Test(expected = NoSessionException.class)
    public void testGetEventsWithNoSession() throws BetfairException {
        globalAPI.setSessionToken(SESSION_TOKEN);

        APIResponseHeader header = createMockHeader(APIErrorEnum.NO_SESSION, SESSION_TOKEN);
        GetEventsResp resp = mock(GetEventsResp.class);
        when(resp.getErrorCode()).thenReturn(GetEventsErrorEnum.API_ERROR);
        when(resp.getHeader()).thenReturn(header);
        when(globalService.getEvents((GetEventsReq) anyObject())).thenReturn(resp);

        globalAPI.getEvents(PARENT_ID);
    }

    @Test(expected = ExceededThrottleException.class)
    public void testGetEventsExceededThrottle() throws BetfairException {
        globalAPI.setSessionToken(SESSION_TOKEN);

        APIResponseHeader header = createMockHeader(APIErrorEnum.EXCEEDED_THROTTLE, SESSION_TOKEN);
        GetEventsResp resp = mock(GetEventsResp.class);
        when(resp.getErrorCode()).thenReturn(GetEventsErrorEnum.API_ERROR);
        when(resp.getHeader()).thenReturn(header);
        when(globalService.getEvents((GetEventsReq) anyObject())).thenReturn(resp);

        globalAPI.getEvents(PARENT_ID);
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    private static APIResponseHeader createMockHeader(APIErrorEnum errorCode, String sessionToken) {
        APIResponseHeader header = mock(APIResponseHeader.class);
        when(header.getErrorCode()).thenReturn(errorCode);
        when(header.getSessionToken()).thenReturn(sessionToken);

        return header;
    }

    private static Matcher<LoginReq> aLoginReqWith(final String username,
                                                   final String password,
                                                   final int productId) {
        return new TypeSafeMatcher<LoginReq>() {
            @Override
            protected boolean matchesSafely(LoginReq loginReq) {
                return username.equals(loginReq.getUsername()) &&
                        password.equals(loginReq.getPassword()) &&
                        productId == loginReq.getProductId();
            }

            public void describeTo(Description description) {
                description.appendText(
                        String.format("a LoginReq with username %s, password %s and productId %d",
                                username, password, productId)
                );
            }
        };
    }

    private static Matcher<GetEventsReq> aGetEventsReqWithParentId(final int parentId) {
        return new TypeSafeMatcher<GetEventsReq>() {
            @Override
            protected boolean matchesSafely(GetEventsReq getEventsReq) {
                return parentId == getEventsReq.getEventParentId();
            }

            public void describeTo(Description description) {
                description.appendText(
                        String.format("a GetEventsReq with eventParentId %d", parentId)
                );
            }
        };
    }

}
