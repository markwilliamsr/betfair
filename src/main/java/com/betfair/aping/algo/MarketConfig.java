package com.betfair.aping.algo;

/**
 * Created by markwilliams on 11/01/15.
 */
public class MarketConfig {
    Double layLimit;
    Integer layTimeLimit;
    Double backLimit;
    Double backLowerLimit;
    Double backUpperLimit;
    Integer backTimeLimit;
    Double expLossLimit;
    Double stakeLossLimit;
    Double cashOutProfitPercentage;
    Integer cashOutTimeLimit;
    Integer losingTimeLimit;

    public Double getBackUpperLimit() {
        return backUpperLimit;
    }

    public void setBackUpperLimit(Double backUpperLimit) {
        this.backUpperLimit = backUpperLimit;
    }

    public Double getBackLowerLimit() {
        return backLowerLimit;
    }

    public void setBackLowerLimit(Double backLowerLimit) {
        this.backLowerLimit = backLowerLimit;
    }

    public Integer getLosingTimeLimit() {
        return losingTimeLimit;
    }

    public void setLosingTimeLimit(Integer losingTimeLimit) {
        this.losingTimeLimit = losingTimeLimit;
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

    public Integer getCashOutTimeLimit() {
        return cashOutTimeLimit;
    }

    public void setCashOutTimeLimit(Integer cashOutTimeLimit) {
        this.cashOutTimeLimit = cashOutTimeLimit;
    }

    public void setSingleGoalTimeLimit(Double singleGoalTimeLimit) {
        if (singleGoalTimeLimit != null) {
            this.cashOutTimeLimit = singleGoalTimeLimit.intValue();
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
