package com.scidef.betfair.api;

import com.betfair.publicapi.types.exchange.v5.Price;
import com.betfair.publicapi.types.exchange.v5.RunnerPrices;

import java.util.List;

/**
 * A wrapper class around Betfair's runner prices, populated by parsing the
 * compressed market data.
 * <p/>
 * User: tompearson
 * Date: 26/05/2010
 */
public class RunnerPricesWrapper {

    private RunnerPrices runnerPrices;
    private List<Price> bestPricesToBack;
    private List<Price> bestPricesToLay;

    public RunnerPricesWrapper(RunnerPrices runnerPrices,
                               List<Price> bestPricesToBack,
                               List<Price> bestPricesToLay) {
        this.runnerPrices = runnerPrices;
        this.bestPricesToBack = bestPricesToBack;
        this.bestPricesToLay = bestPricesToLay;
    }

    public RunnerPrices getRunnerPrices() {
        return runnerPrices;
    }

    public List<Price> getBestPricesToBack() {
        return bestPricesToBack;
    }

    public List<Price> getBestPricesToLay() {
        return bestPricesToLay;
    }
}
