package com.betfair.aping.entities;

/**
 * Created by markwilliams on 23/08/2014.
 */
public enum MarketType {
    CORRECT_SCORE_NEW("Correct Score New", -1),
    CORRECT_SCORE("Correct Score", -1),
    OVER_UNDER_05("Over/Under 0.5 Goals", 0),
    OVER_UNDER_15("Over/Under 1.5 Goals", 1),
    OVER_UNDER_25("Over/Under 2.5 Goals", 2),
    OVER_UNDER_35("Over/Under 3.5 Goals", 3),
    OVER_UNDER_45("Over/Under 4.5 Goals", 4),
    OVER_UNDER_55("Over/Under 5.5 Goals", 5),
    OVER_UNDER_65("Over/Under 6.5 Goals", 6),
    OVER_UNDER_75("Over/Under 7.5 Goals", 7);
    private String marketName;
    private int totalGoals;

    private MarketType(String marketName, int totalGoals) {
        this.marketName = marketName;
        this.totalGoals = totalGoals;
    }

    public static MarketType fromTotalGoals(int totalGoals) throws IllegalArgumentException {
        for (MarketType s : MarketType.values()) {
            if (totalGoals == s.getTotalGoals()) {
                return s;
            }
        }
        throw new IllegalArgumentException("There is no value with code '" + totalGoals + "' in MarketType");
    }

    public static MarketType fromMarketName(String marketName) {
        for (MarketType s : MarketType.values()) {
            if (marketName.equals(s.getMarketName())) {
                return s;
            }
        }

        throw new IllegalArgumentException("There is no value with code '" + marketName + "' in MarketType");
    }

    public String getMarketName() {
        return marketName;
    }

    public int getTotalGoals() {
        return totalGoals;
    }
}
