package com.betfair.aping.algo;

/**
 * Created by markwilliams on 11/01/15.
 */
public class MarketConfig {
    Double layLimit;
    Integer layTimeLimit;
    Double expLossLimit;
    Double stakeLossLimit;
    Double cashOutProfitPercentage;
    Integer singleGoalTimeLimit;

    public Double getCashOutProfitPercentage() {
        return cashOutProfitPercentage;
    }

    public void setCashOutProfitPercentage(Double cashOutProfitPercentage) {
        this.cashOutProfitPercentage = cashOutProfitPercentage;
    }

    public Integer getSingleGoalTimeLimit() {
        return singleGoalTimeLimit;
    }

    public void setSingleGoalTimeLimit(Integer singleGoalTimeLimit) {
        this.singleGoalTimeLimit = singleGoalTimeLimit;
    }

    public Double getExpLossLimit() {
        return expLossLimit;
    }

    public void setExpLossLimit(Double expLossLimit) {
        this.expLossLimit = expLossLimit;
    }

    public Double getStakeLossLimit() {
        return stakeLossLimit;
    }

    public void setStakeLossLimit(Double stakeLossLimit) {
        this.stakeLossLimit = stakeLossLimit;
    }

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
