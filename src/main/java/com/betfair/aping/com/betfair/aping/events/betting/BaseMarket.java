package com.betfair.aping.com.betfair.aping.events.betting;

import com.betfair.aping.entities.*;
import com.betfair.aping.enums.Side;

import java.util.List;

/**
 * Created by markwilliams on 1/14/15.
 */
public class BaseMarket {
    MarketCatalogue marketCatalogue;
    MarketType marketType;

    public BaseMarket(MarketCatalogue marketCatalogue) {
        this.marketType = MarketType.fromMarketName(marketCatalogue.getMarketName());
        this.marketCatalogue = marketCatalogue;
    }

    public MarketType getMarketType() {
        return marketType;
    }

    public List<RunnerCatalog> getRunnerCatalogs() {
        return marketCatalogue.getRunners();
    }

    public RunnerCatalog getRunnerCatalogueByName(String runnerName) throws Exception {
        for (RunnerCatalog r : this.getRunnerCatalogs()) {
            if (r.getRunnerName().equals(runnerName)) {
                return r;
            }
        }
        throw new Exception("Runner not found: " + runnerName);
    }

    public Runner getRunnerByName(String runnerName) throws Exception {
        RunnerCatalog rc = getRunnerCatalogueByName(runnerName);
        Runner runner = getRunnerBySelectionId(rc.getSelectionId());
        return runner;
    }

    public Runner getRunnerBySelectionId(long selectionId) throws Exception {
        for (Runner r : this.getRunners()) {
            if (r.getSelectionId() == selectionId) {
                return r;
            }
        }
        throw new Exception("Runner not found: " + selectionId);
    }

    public List<Runner> getRunners() {
        return marketCatalogue.getMarketBook().getRunners();
    }

    public PriceSize getBack(Runner runner, int position) {
        if (runner.getEx().getAvailableToBack().size() <= position) {
            throw new RuntimeException("Not Enough Depth for Position: " + position);
        }
        return runner.getEx().getAvailableToBack().get(position);
    }

    public PriceSize getLay(Runner runner, int position) {
        if (runner.getEx().getAvailableToLay().size() <= position) {
            throw new RuntimeException("Not Enough Depth for Position: " + position);
        }
        return runner.getEx().getAvailableToLay().get(position);
    }

    public PriceSize getPrice(Runner runner, int position, Side side) {
        if (side.equals(Side.BACK)) {
            return getBack(runner, position);
        } else {
            return getLay(runner, position);
        }
    }
}
