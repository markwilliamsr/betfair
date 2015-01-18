package com.betfair.aping.com.betfair.aping.events.betting;

import com.betfair.aping.enums.MarketTemp;

/**
 * Created by markwilliams on 11/01/15.
 */
public class MarketClassification {
    private MarketTemp marketTemp;
    private Double homeOdds = 0d;
    private Double awayOdds = 0d;
    private Double drawOdds = 0d;

    public MarketTemp getMarketTemp() {
        return marketTemp;
    }

    public void setMarketTemp(MarketTemp marketTemp) {
        this.marketTemp = marketTemp;
    }

    public Double getHomeOdds() {
        return homeOdds;
    }

    public void setHomeOdds(Double homeOdds) {
        this.homeOdds = homeOdds;
    }

    public Double getAwayOdds() {
        return awayOdds;
    }

    public void setAwayOdds(Double awayOdds) {
        this.awayOdds = awayOdds;
    }

    public Double getDrawOdds() {
        return drawOdds;
    }

    public void setDrawOdds(Double drawOdds) {
        this.drawOdds = drawOdds;
    }
}
