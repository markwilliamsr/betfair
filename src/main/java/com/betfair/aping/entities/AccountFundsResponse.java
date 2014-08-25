package com.betfair.aping.entities;

/**
 * Created by markwilliams on 25/08/2014.
 */
public class AccountFundsResponse {
    private double availableToBetBalance;
    private double exposure;
    private double retainedCommission;
    private double exposureLimit;
    private double discountRate;
    private int pointsBalance;

    public double getAvailableToBetBalance() {
        return availableToBetBalance;
    }

    public void setAvailableToBetBalance(double availableToBetBalance) {
        this.availableToBetBalance = availableToBetBalance;
    }

    public double getExposure() {
        return exposure;
    }

    public void setExposure(double exposure) {
        this.exposure = exposure;
    }

    public double getRetainedCommission() {
        return retainedCommission;
    }

    public void setRetainedCommission(double retainedCommission) {
        this.retainedCommission = retainedCommission;
    }

    public double getExposureLimit() {
        return exposureLimit;
    }

    public void setExposureLimit(double exposureLimit) {
        this.exposureLimit = exposureLimit;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    public int getPointsBalance() {
        return pointsBalance;
    }

    public void setPointsBalance(int pointsBalance) {
        this.pointsBalance = pointsBalance;
    }
}
