package com.betfair.aping.com.betfair.aping.events.betting;

import com.betfair.aping.entities.MarketCatalogue;
import com.betfair.aping.entities.Runner;
import com.betfair.aping.entities.RunnerCatalog;

/**
 * Created by markwilliams on 23/08/2014.
 */
public class MatchOddsMarket extends BaseMarket {

    public MatchOddsMarket(MarketCatalogue marketCatalogue) {
        super(marketCatalogue);
    }

    public Runner getHomeRunner() throws Exception {
        RunnerCatalog rc = getRunnerCatalogs().get(0);
        Runner runner = getRunnerBySelectionId(rc.getSelectionId());
        return runner;
    }

    public Runner getAwayRunner() throws Exception {
        RunnerCatalog rc = getRunnerCatalogs().get(1);
        Runner runner = getRunnerBySelectionId(rc.getSelectionId());
        return runner;
    }

    public Runner getDrawRunner() throws Exception {
        RunnerCatalog rc = getRunnerCatalogs().get(2);
        Runner runner = getRunnerBySelectionId(rc.getSelectionId());
        return runner;
    }

    public String getDrawRunnerName() {
        return "The Draw";
    }

    public String getHomeRunnerName() {
        return "Home";
    }

    public String getAwayRunnerName() {
        return "Away";
    }

}
