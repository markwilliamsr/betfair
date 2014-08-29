package com.betfair.aping.com.betfair.aping.events.betting;

import com.betfair.aping.entities.*;

import java.util.List;

/**
 * Created by markwilliams on 23/08/2014.
 */
public class Score {

    Event event;

    public Score(Event event) {
        this.event = event;
    }

    public int getTotalGoals(RunnerCatalog r) {
        if (getHomeGoals(r) + getAwayGoals(r) >= 0) {
            return getHomeGoals(r) + getAwayGoals(r);
        } else {
            return Integer.MAX_VALUE;
        }
    }

    private int getHomeGoals(RunnerCatalog r) {
        if (!r.getRunnerName().trim().equals(ScoreEnum.ANY_UNQUOTED)) {
            String[] score = r.getRunnerName().split("-");
            return parseGoalsFromScoreFragment(score[0]);
        }
        return 0;
    }

    private int getAwayGoals(RunnerCatalog r) {
        if (!r.getRunnerName().trim().equals(ScoreEnum.ANY_UNQUOTED)) {
            String[] score = r.getRunnerName().split("-");
            return parseGoalsFromScoreFragment(score[1]);
        }
        return 0;
    }

    private int parseGoalsFromScoreFragment(String s) {
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        } catch (ArrayIndexOutOfBoundsException e) {
            return Integer.MAX_VALUE;
        }
    }

    public ScoreEnum findScoreFromMarketOdds() {
        return findCorrectScoreFromMarketOdds(event.getMarket().get(MarketType.CORRECT_SCORE).getMarketBook().getRunners(),
                event.getMarket().get(MarketType.CORRECT_SCORE));
    }

    private ScoreEnum findCorrectScoreFromMarketOdds(List<Runner> runners, MarketCatalogue m) {
        ScoreEnum correctScore = null;
        int minGoalsSoFar = Integer.MAX_VALUE;
        for (Runner r : runners) {
            RunnerCatalog runner = null;

            for (RunnerCatalog mr : m.getRunners()) {
                if (mr.getSelectionId() == r.getSelectionId()) {
                    runner = mr;
                    break;
                }
            }

            if (runner == null) {
                continue;
            }

            if (r.getEx().getAvailableToLay().size() != 0 && !runner.getRunnerName().trim().equals(ScoreEnum.ANY_UNQUOTED)) {
                if (minGoalsSoFar > getTotalGoals(runner)) {
                    minGoalsSoFar = getTotalGoals(runner);
                    correctScore = ScoreEnum.fromString(runner.getRunnerName());
                    //System.out.println("Found RunnerCatalog with possible odds " + runner.getRunnerName());
                }
            }
        }
        return correctScore;
    }

    public int getTotalGoals() {
        ScoreEnum score = findScoreFromMarketOdds();
        return score.getTotalGoals();
    }
}