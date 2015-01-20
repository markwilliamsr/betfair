package com.betfair.aping.entities;

/**
 * Created by markwilliams on 1/19/15.
 */
public class CancelInstruction {
    String betId;
    Double sizeReduction;

    public String getBetId() {
        return betId;
    }

    public void setBetId(String betId) {
        this.betId = betId;
    }

    public Double getSizeReduction() {
        return sizeReduction;
    }

    public void setSizeReduction(Double sizeReduction) {
        this.sizeReduction = sizeReduction;
    }
}
