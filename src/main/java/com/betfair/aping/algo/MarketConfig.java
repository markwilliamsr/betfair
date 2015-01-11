package com.betfair.aping.algo;

/**
 * Created by markwilliams on 11/01/15.
 */
public class MarketConfig {
    Double layLimit;
    Integer layTimeLimit;

    public Double getLayLimit() {
        return layLimit;
    }

    public void setLayLimit(Double layLimit) {
        this.layLimit = layLimit;
    }

    public Integer getLayTimeLimit() {
        return layTimeLimit;
    }

    public void setLayTimeLimit(Integer layTimeLimit) {
        this.layTimeLimit = layTimeLimit;
    }
}
