package com.scidef.betfair.api;

import com.betfair.publicapi.types.exchange.v5.ArrayOfRunner;
import com.betfair.publicapi.types.exchange.v5.BetStatusEnum;
import com.betfair.publicapi.types.exchange.v5.BetTypeEnum;
import com.betfair.publicapi.types.exchange.v5.GetBetResp;
import com.betfair.publicapi.types.exchange.v5.GetMarketResp;
import com.betfair.publicapi.types.exchange.v5.Market;
import com.betfair.publicapi.types.exchange.v5.Runner;
import com.scidef.betfair.api.exception.BetfairException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import static com.scidef.betfair.api.BetEnrichment.enrichBetsWithProfitOrLossCalculation;
import static com.scidef.betfair.api.TestConstant.BET_ID_1;
import static com.scidef.betfair.api.TestConstant.BET_ID_2;
import static com.scidef.betfair.api.TestConstant.MARKET_ID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the <code>BetEnrichment</code> class.
 * <p/>
 * User: tompearson
 * Date: 21/01/2013
 */
public class BetEnrichmentTest {

    private ExchangeAPI exchangeAPI;
    private HorseRacing horseRacing;
    private BetEnrichment betEnrichment;

    @Before
    public void setupBetEnrichment() {
        exchangeAPI = mock(ExchangeAPI.class);
        horseRacing = mock(HorseRacing.class);
        betEnrichment = new BetEnrichment(exchangeAPI, horseRacing);
    }

    @Test
    public void testEnrichBetsWithRunnerNamesAndNumberOfRunners() throws BetfairException {
        GetMarketResp resp = new GetMarketResp();
        Market market = new Market();
        ArrayOfRunner runners = new ArrayOfRunner();
        Runner runner1 = new Runner();
        runner1.setSelectionId(10);
        runner1.setName("Dingbat");
        runners.getRunner().add(runner1);
        Runner runner2 = new Runner();
        runner2.setSelectionId(11);
        runner2.setName("Bilbo Baggins");
        runners.getRunner().add(runner2);
        Runner runner3 = new Runner();
        runner3.setSelectionId(12);
        runner3.setName("Fat Chance");
        runners.getRunner().add(runner3);
        market.setRunners(runners);
        resp.setMarket(market);
        when(exchangeAPI.getMarket(MARKET_ID)).thenReturn(resp);

        List<Bet> bets = generateBets();

        betEnrichment.enrichBetsWithRunnerNamesAndNumberOfRunners(bets);

        assertThat(bets.size(), is(2));
        assertThat(bets.get(0).getRunnerName(), is("Dingbat"));
        assertThat(bets.get(0).getNumberOfRunners(), is(3));
        assertThat(bets.get(1).getRunnerName(), is("Bilbo Baggins"));
        assertThat(bets.get(1).getNumberOfRunners(), is(3));
    }

    @Test
    public void testEnrichBetsWithNonRunnerInfo() throws BetfairException {
        when(horseRacing.getNonRunners(MARKET_ID)).thenReturn(
                Collections.singletonList("Bilbo Baggins")
        );

        List<Bet> bets = generateBets();
        bets.get(0).setRunnerName("Dingbat");
        bets.get(1).setRunnerName("Bilbo Baggins");

        betEnrichment.enrichBetsWithNonRunnerInfo(bets);

        assertThat(bets.size(), is(2));
        assertThat(bets.get(0).getNonRunner(), is(false));
        assertThat(bets.get(1).getNonRunner(), is(true));
    }

    @Test
    public void testEnrichBetsWithMatchedSize() throws BetfairException {
        GetBetResp resp1 = new GetBetResp();
        com.betfair.publicapi.types.exchange.v5.Bet bet1 =
                new com.betfair.publicapi.types.exchange.v5.Bet();
        bet1.setBetStatus(BetStatusEnum.S);
        bet1.setMatchedSize(20.0);
        resp1.setBet(bet1);
        when(exchangeAPI.getBet(BET_ID_1)).thenReturn(resp1);
        GetBetResp resp2 = new GetBetResp();
        com.betfair.publicapi.types.exchange.v5.Bet bet2 =
                new com.betfair.publicapi.types.exchange.v5.Bet();
        bet2.setBetStatus(BetStatusEnum.S);
        bet2.setMatchedSize(401.35);
        resp2.setBet(bet2);
        when(exchangeAPI.getBet(BET_ID_2)).thenReturn(resp2);

        List<Bet> bets = generateBets();

        betEnrichment.enrichBetsWithMatchedSize(bets);

        assertThat(bets.size(), is(2));
        assertThat(bets.get(0).getAmountMatched(), is(20.0));
        assertThat(bets.get(1).getAmountMatched(), is(401.35));
    }

    @Test
    public void testEnrichBetsWithProfitOrLossCalculationLay() {
        List<Bet> bets = generateBets();
        bets.get(0).setBetType(BetTypeEnum.L);
        bets.get(0).setEventOutcome(0);
        bets.get(0).setNonRunner(false);
        bets.get(1).setBetType(BetTypeEnum.L);
        bets.get(1).setEventOutcome(1);
        bets.get(1).setNonRunner(false);

        enrichBetsWithProfitOrLossCalculation(bets);

        assertThat(bets.size(), is(2));
        assertThat(bets.get(0).getProfitOrLoss(), is(1.0));
        assertThat(bets.get(1).getProfitOrLoss(), is(-3.0));
    }

    @Test
    public void testEnrichBetsWithProfitOrLossCalculationBack() {
        List<Bet> bets = generateBets();
        bets.get(0).setBetType(BetTypeEnum.B);
        bets.get(0).setEventOutcome(0);
        bets.get(0).setNonRunner(false);
        bets.get(1).setBetType(BetTypeEnum.B);
        bets.get(1).setEventOutcome(1);
        bets.get(1).setNonRunner(false);

        enrichBetsWithProfitOrLossCalculation(bets);

        assertThat(bets.size(), is(2));
        assertThat(bets.get(0).getProfitOrLoss(), is(-1.0));
        assertThat(bets.get(1).getProfitOrLoss(), is(3.0));
    }

    @Test
    public void testEnrichBetsWithProfitOrLossCalculationNonRunners() {
        List<Bet> bets = generateBets();
        bets.get(0).setBetType(BetTypeEnum.L);
        bets.get(0).setEventOutcome(0);
        bets.get(0).setNonRunner(true);
        bets.get(1).setBetType(BetTypeEnum.B);
        bets.get(1).setEventOutcome(1);
        bets.get(1).setNonRunner(true);

        enrichBetsWithProfitOrLossCalculation(bets);

        assertThat(bets.size(), is(2));
        assertThat(bets.get(0).getProfitOrLoss(), is(0.0));
        assertThat(bets.get(1).getProfitOrLoss(), is(0.0));
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    private static List<Bet> generateBets() {
        List<Bet> bets = new ArrayList<Bet>();
        Bet bet1 = new Bet(
                MARKET_ID, 10, BetTypeEnum.B, 13.0, 100.0, new GregorianCalendar().getTime(), "Punch", "Desc"
        );
        bet1.setBetId(BET_ID_1);
        bets.add(bet1);
        Bet bet2 = new Bet(
                MARKET_ID, 11, BetTypeEnum.B, 4.0, 20.0, new GregorianCalendar().getTime(), "Punch", "Desc"
        );
        bet2.setBetId(BET_ID_2);
        bets.add(bet2);
        return bets;
    }

}
