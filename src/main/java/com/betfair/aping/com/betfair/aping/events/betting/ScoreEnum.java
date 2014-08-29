package com.betfair.aping.com.betfair.aping.events.betting;

/**
 * Created by markwilliams on 8/25/14.
 */
public enum ScoreEnum {
    NIL_NIL("0 - 0", 0),
    ONE_NIL("1 - 0", 1),
    TWO_NIL("2 - 0", 2),
    THREE_NIL("3 - 0", 3),
    NIL_ONE("0 - 1", 1),
    ONE_ONE("1 - 1", 2),
    TWO_ONE("2 - 1", 3),
    THREE_ONE("3 - 1", 4),
    NIL_TWO("0 - 2", 2),
    ONE_TWO("1 - 2", 3),
    TWO_TWO("2 - 2", 4),
    THREE_TWO("3 - 2", 5),
    NIL_THREE("0 - 3", 3),
    ONE_THREE("1 - 3", 4),
    TWO_THREE("2 - 3", 5),
    THREE_THREE("3 - 3", 6),
    ANY_UNQUOTED("Any Unquoted", 7);
    private String score;
    private int totalGoals;

    private ScoreEnum(String score, int totalGoals) {
        this.score = score;
        this.totalGoals = totalGoals;
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
        return totalGoals;
    }
    public String toString() {
        return score;
    }
}
