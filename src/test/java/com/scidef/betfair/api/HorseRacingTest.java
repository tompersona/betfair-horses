package com.scidef.betfair.api;

import com.scidef.betfair.api.exception.BetfairException;
import org.junit.Before;
import org.junit.Test;

import static com.scidef.betfair.api.TestConstant.PASSWORD;
import static com.scidef.betfair.api.TestConstant.USERNAME;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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

}
