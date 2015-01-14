package com.betfair.aping.com.betfair.aping.events.betting;

import com.betfair.aping.entities.MarketCatalogue;
import com.betfair.aping.entities.Runner;

/**
 * Created by markwilliams on 23/08/2014.
 */
public class OverUnderMarket extends BaseMarket {
    public OverUnderMarket(MarketCatalogue marketCatalogue) {
        super(marketCatalogue);
    }

    public Runner getUnderRunner() throws Exception {
        return getRunnerByName(this.getUnderRunnerName());
    }

    public Runner getOverRunner() throws Exception {
        return getRunnerByName(this.getOverRunnerName());
    }

    public String getUnderRunnerName() {
        return "Under " + marketType.getTotalGoals() + ".5 Goals";
    }

    public String getOverRunnerName() {
        return "Over " + marketType.getTotalGoals() + ".5 Goals";
    }

}
