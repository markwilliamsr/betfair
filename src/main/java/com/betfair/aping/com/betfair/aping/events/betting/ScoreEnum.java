package com.betfair.aping.com.betfair.aping.events.betting;

/**
 * Created by markwilliams on 8/25/14.
 */
public enum ScoreEnum {
    NIL_NIL("0 - 0", 0, 0),
    ONE_NIL("1 - 0", 1, 0),
    TWO_NIL("2 - 0", 2, 0),
    THREE_NIL("3 - 0", 3, 0),
    NIL_ONE("0 - 1", 0, 1),
    ONE_ONE("1 - 1", 1, 1),
    TWO_ONE("2 - 1", 2, 1),
    THREE_ONE("3 - 1", 3, 1),
    NIL_TWO("0 - 2", 0, 2),
    ONE_TWO("1 - 2", 1, 2),
    TWO_TWO("2 - 2", 2, 2),
    THREE_TWO("3 - 2", 3, 2),
    NIL_THREE("0 - 3", 0, 3),
    ONE_THREE("1 - 3", 1, 3),
    TWO_THREE("2 - 3", 2, 3),
    THREE_THREE("3 - 3", 3, 3),
    ANY_UNQUOTED("Any Unquoted", 7, 0);
    private String score;
    private int homeGoals;
    private int awayGoals;

    private ScoreEnum(String score, int homeGoals, int awayGoals) {
        this.score = score;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
    }

    public static ScoreEnum fromString(String name) {
        for (ScoreEnum s : ScoreEnum.values()) {
            if (name.equalsIgnoreCase(s.score)) {
                return s;
            }
        }
        throw new IllegalArgumentException("There is no value with code '" + name + "' in ScoreEnum");
    }

    public String getName() {
        return name();
    }
    public int getTotalGoals() {
        return homeGoals + awayGoals;
    }
    public int getHomeGoals() {
        return homeGoals;
    }
    public int getAwayGoals() {
        return awayGoals;
    }
    public String toString() {
        return score;
    }
    public boolean isHomeTeamWinning() {
        return homeGoals > awayGoals;
    }

    public boolean isAwayTeamWinning() {
        return homeGoals < awayGoals;
    }

    public int goalDifference() {
        return Math.abs(homeGoals - awayGoals);
    }

}
