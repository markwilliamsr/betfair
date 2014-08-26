package com.betfair.aping.com.betfair.aping.events.betting;

import com.betfair.aping.entities.MarketCatalogue;
import com.betfair.aping.entities.PriceSize;
import com.betfair.aping.entities.Runner;
import com.betfair.aping.entities.RunnerCatalog;

import java.util.List;

/**
 * Created by markwilliams on 23/08/2014.
 */
public class OverUnderMarket {
    public static final String UNDER_2_5 = "Under 2.5 Goals";
    public static final String OVER_2_5 = "Over 2.5 Goals";

    MarketCatalogue marketCatalogue;

    public OverUnderMarket(MarketCatalogue marketCatalogue) {
        this.marketCatalogue = marketCatalogue;
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
        return runner.getEx().getAvailableToBack().get(position);
    }

    public PriceSize getLay(Runner runner, int position) {
        return runner.getEx().getAvailableToLay().get(position);
    }
}
