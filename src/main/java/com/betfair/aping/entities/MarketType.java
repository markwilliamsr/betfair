package com.betfair.aping.entities;

/**
 * Created by markwilliams on 23/08/2014.
 */
public enum MarketType {
    CORRECT_SCORE(-1),
    OVER_UNDER_05(0),
    OVER_UNDER_15(1),
    OVER_UNDER_25(2),
    OVER_UNDER_35(3),
    OVER_UNDER_45(4),
    OVER_UNDER_55(5),
    OVER_UNDER_65(6),
    OVER_UNDER_75(7);
    private int totalGoals;

    private MarketType(int totalGoals) {
        this.totalGoals = totalGoals;
    }

    public static MarketType fromTotalGoals(int totalGoals) {
        for (MarketType s : MarketType.values()) {
            if (totalGoals == s.getTotalGoals()) {
                return s;
            }
        }
        throw new IllegalArgumentException("There is no value with code '" + totalGoals + "' in ScoreEnum");
    }

    public int getTotalGoals() {
        return totalGoals;
    }
}
