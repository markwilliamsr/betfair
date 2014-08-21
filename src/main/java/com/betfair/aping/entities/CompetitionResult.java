package com.betfair.aping.entities;

/**
 * Created by markwilliams on 8/20/14.
 */
public class CompetitionResult {
    private Competition competition;
    private int marketCount;
    private String competitionRegion;

    public String getCompetitionRegion() {
        return competitionRegion;
    }

    public void setCompetitionRegion(String competitionRegion) {
        this.competitionRegion = competitionRegion;
    }

    public Competition getCompetition() {
        return competition;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public int getMarketCount() {
        return marketCount;
    }

    public void setMarketCount(int marketCount) {
        this.marketCount = marketCount;
    }

}
