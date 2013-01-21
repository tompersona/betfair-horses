package com.scidef.betfair.api;

import com.betfair.publicapi.types.exchange.v5.BetStatusEnum;
import com.betfair.publicapi.types.exchange.v5.BetTypeEnum;
import com.betfair.publicapi.types.exchange.v5.GetBetResp;
import com.betfair.publicapi.types.exchange.v5.GetMarketResp;
import com.betfair.publicapi.types.exchange.v5.Runner;
import com.scidef.betfair.api.aop.ExceededMaxRetriesException;
import com.scidef.betfair.api.exception.BetfairException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.scidef.betfair.api.HorseRacing.HORSE_RACING_SPORT_ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.scidef.betfair.api.ResultsAPI.getPosition;

/**
 * A set of methods for enriching bets with additional data that is not available
 * at the point the bet is first returned. For example, the runner name is not
 * typically provided by the API when the bet is first returned but this can be
 * added afterwards.
 * <p/>
 * User: tompearson
 * Date: 20/01/2013
 */
public class BetEnrichment {

    private static final Logger LOG = LoggerFactory.getLogger(BetEnrichment.class);

    private final ExchangeAPI exchangeAPI;
    private final HorseRacing horseRacing;

    public BetEnrichment(ExchangeAPI exchangeAPI, HorseRacing horseRacing) {
        this.exchangeAPI = exchangeAPI;
        this.horseRacing = horseRacing;
    }

    /**
     * Enrich a list of bets with data about the name of the runner and the
     * total number of runners in that event.
     *
     * @param bets a list of <code>Bet</code>s to be enriched
     * @throws BetfairException if there is a problem with the enrichment
     */
    public void enrichBetsWithRunnerNamesAndNumberOfRunners(List<Bet> bets) throws BetfairException {
        Map<Integer, List<Bet>> betsByMarketId = mapBetsByMarketId(bets);

        // find the runners for each marketId and update each bet
        for (Integer marketId : betsByMarketId.keySet()) {
            LOG.info("Looking up runners for marketId " + marketId);
            try {
                GetMarketResp getMarketResp = exchangeAPI.getMarket(marketId);

                Map<Integer, String> runnerNamesBySelectionId = new HashMap<Integer, String>();
                final List<Runner> runnerList = getMarketResp.getMarket().getRunners().getRunner();
                for (Runner runner : runnerList) {
                    runnerNamesBySelectionId.put(runner.getSelectionId(), runner.getName());
                }
                for (Bet bet : betsByMarketId.get(marketId)) {
                    if (runnerNamesBySelectionId.containsKey(bet.getSelectionId())) {
                        bet.setRunnerName(runnerNamesBySelectionId.get(bet.getSelectionId()));
                        bet.setNumberOfRunners(runnerList.size());
                    }
                }
            } catch (ExceededMaxRetriesException e) {
                // ignore for now - enrichment can be attempted at a later stage
                LOG.warn("Exceeded max retries when attempting to look up runners for marketId " + marketId);
            }
        }
    }

    /**
     * Enrich a list of bets with data about whether or not a runner is, in fact,
     * a non-runner.
     *
     * @param bets a list of <code>Bet</code>s to be enriched
     */
    public void enrichBetsWithNonRunnerInfo(List<Bet> bets) {
        Map<Integer, List<Bet>> betsByMarketId = mapBetsByMarketId(bets);

        // find the non-runners for each marketId and update each bet
        for (Integer marketId : betsByMarketId.keySet()) {
            List<String> nonRunners;
            try {
                nonRunners = horseRacing.getNonRunners(marketId);
            } catch (BetfairException e) {
                // ignore and continue enriching other bets
                LOG.warn(e.getMessage());
                continue;
            }
            for (Bet bet : betsByMarketId.get(marketId)) {
                if (nonRunners.contains(bet.getRunnerName())) {
                    bet.setNonRunner(true);
                } else {
                    bet.setNonRunner(false);
                }
            }
        }
    }

    /**
     * Enrich a list of bets with data about the matched bet size.
     *
     * @param bets a list of <code>Bet</code>s to be enriched
     * @throws BetfairException if there is a problem retrieving the matched size data
     */
    public void enrichBetsWithMatchedSize(List<Bet> bets) throws BetfairException {
        for (Bet bet : bets) {
            if (bet.getBetId() != null) {
                GetBetResp betResp = exchangeAPI.getBet(bet.getBetId());
                if (betResp.getBet().getBetStatus().equals(BetStatusEnum.S)) {
                    bet.setAmountMatched(betResp.getBet().getMatchedSize());
                }
            }
        }
    }

    /**
     * Enrich a list of bets with data about the outcome of the event
     * (ie, where the runner placed).
     *
     * @param bets a list of <code>Bet</code>s to be enriched
     */
    public static void enrichBetsWithEventOutcomes(List<Bet> bets) {
        for (Bet bet : bets) {
            try {
                bet.setEventOutcome(getPosition(HORSE_RACING_SPORT_ID, bet.getMarketId(), bet.getRunnerName()));
            } catch (BetfairException e) {
                // ignore and continue enriching other bets
                LOG.warn(e.getMessage());
            }
        }
    }

    /**
     * Enrich a list of bets with data about the profit or loss each bet has made.
     *
     * @param bets a list of <code>Bet</code>s to be enriched
     */
    public static void enrichBetsWithProfitOrLossCalculation(List<Bet> bets) {
        for (Bet bet : bets) {
            if (bet.getNonRunner() == null || bet.getEventOutcome() == null) {
                continue;
            }
            if (bet.getBetType().equals(BetTypeEnum.L)) {
                if (bet.getNonRunner()) {
                    bet.setProfitOrLoss(0.0);
                } else if (bet.getEventOutcome().equals(1)) {
                    bet.setProfitOrLoss(-1 * (bet.getPrice() - 1));
                } else {
                    bet.setProfitOrLoss(1.0);
                }
            }
            if (bet.getBetType().equals(BetTypeEnum.B)) {
                if (bet.getNonRunner()) {
                    bet.setProfitOrLoss(0.0);
                } else if (bet.getEventOutcome().equals(1)) {
                    bet.setProfitOrLoss(bet.getPrice() - 1);
                } else {
                    bet.setProfitOrLoss(-1.0);
                }
            }

            LOG.info("Enriched bet with profit or loss calculation: " + bet);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    private static Map<Integer, List<Bet>> mapBetsByMarketId(List<Bet> bets) {
        // break down the bets by marketId
        Map<Integer, List<Bet>> betsByMarketId = new HashMap<Integer, List<Bet>>();
        for (Bet bet : bets) {
            List<Bet> bs;
            if (!betsByMarketId.containsKey(bet.getMarketId())) {
                bs = new ArrayList<Bet>();
                betsByMarketId.put(bet.getMarketId(), bs);
            } else {
                bs = betsByMarketId.get(bet.getMarketId());
            }
            bs.add(bet);
        }
        return betsByMarketId;
    }

}
