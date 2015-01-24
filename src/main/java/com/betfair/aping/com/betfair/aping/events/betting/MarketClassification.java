package com.betfair.aping.com.betfair.aping.events.betting;

import com.betfair.aping.enums.MarketTemp;
import com.betfair.aping.enums.OddsClassification;

/**
 * Created by markwilliams on 11/01/15.
 */
public class MarketClassification {
    private MarketTemp marketTemp;
    private Double homeOdds = 0d;
    private Double awayOdds = 0d;
    private Double drawOdds = 0d;
    private OddsClassification homeOddsClassification;
    private OddsClassification awayOddsClassification;
    private OddsClassification drawOddsClassification;

    public OddsClassification getAwayOddsClassification() {
        return awayOddsClassification;
    }

    public void setAwayOddsClassification(OddsClassification awayOddsClassification) {
        this.awayOddsClassification = awayOddsClassification;
    }

    public OddsClassification getDrawOddsClassification() {
        return drawOddsClassification;
    }

    public void setDrawOddsClassification(OddsClassification drawOddsClassification) {
        this.drawOddsClassification = drawOddsClassification;
    }

    public OddsClassification getHomeOddsClassification() {
        return homeOddsClassification;
    }

    public void setHomeOddsClassification(OddsClassification homeOddsClassification) {
        this.homeOddsClassification = homeOddsClassification;
    }

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

    public boolean isHomeFavourite(){
        if (homeOddsClassification.equals(OddsClassification.LOW)) {
            return true;
        }
        return false;
    }

    public boolean isAwayFavourite(){
        if (awayOddsClassification.equals(OddsClassification.LOW)) {
            return true;
        }
        return false;
    }
}
