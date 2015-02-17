package com.betfair.aping.algo;

/**
 * Created by markwilliams on 11/01/15.
 */
public class MarketConfig {
    Double layLimit;
    Integer layTimeLimit;
    Boolean eligibleMarket;
    Double backLimit;
    Integer backTimeLimit;
    Double expLossLimit;
    Double stakeLossLimit;
    Double cashOutProfitPercentage;
    Integer singleGoalTimeLimit;

    public Boolean getEligibleMarket() {
        return eligibleMarket;
    }

    public void setEligibleMarket(Boolean eligibleMarket) {
        this.eligibleMarket = eligibleMarket;
    }

    public Double getBackLimit() {
        return backLimit;
    }

    public void setBackLimit(Double backLimit) {
        this.backLimit = backLimit;
    }

    public Integer getBackTimeLimit() {
        return backTimeLimit;
    }

    public void setBackTimeLimit(Integer backTimeLimit) {
        this.backTimeLimit = backTimeLimit;
    }

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

    public void setSingleGoalTimeLimit(Double singleGoalTimeLimit) {
        if (singleGoalTimeLimit != null) {
            this.singleGoalTimeLimit = singleGoalTimeLimit.intValue();
        }
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
