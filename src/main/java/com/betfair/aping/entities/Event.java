package com.betfair.aping.entities;

import com.betfair.aping.com.betfair.aping.events.betting.ScoreEnum;
import com.betfair.aping.enums.MarketClassification;

import java.util.*;

public class Event {

    private String id;
    private String name;
    private String countryCode;
    private String timezone;
    private String venue;
    private Date openDate;
    private ScoreEnum score = ScoreEnum.NIL_NIL;
    private List<ScoreEnum> previousScores = new ArrayList<ScoreEnum>();
    private Map<MarketType, MarketCatalogue> market = new TreeMap<MarketType, MarketCatalogue>();
    private MarketClassification marketClassification;

    public MarketClassification getMarketClassification() {
        return marketClassification;
    }

    public void setMarketClassification(MarketClassification marketClassification) {
        this.marketClassification = marketClassification;
    }

    public List<ScoreEnum> getPreviousScores() {
        return previousScores;
    }

    public void setPreviousScores(List<ScoreEnum> previousScores) {
        this.previousScores = previousScores;
    }

    public ScoreEnum getScore() {
        return score;
    }

    public void setScore(ScoreEnum score) {
        this.score = score;
    }

    public Map<MarketType, MarketCatalogue> getMarket() {
        return market;
    }

    public void setMarket(Map<MarketType, MarketCatalogue> market) {
        this.market = market;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public Date getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Date openDate) {
        this.openDate = openDate;
    }

    public String toString() {
        return "{" + "" + "id=" + getId() + "," + "name=" + getName() + ","
                + "countryCode=" + getCountryCode() + "," + "timezone="
                + getTimezone() + "," + "venue=" + getVenue() + ","
                + "openDate=" + getOpenDate() + "," + "}";
    }

}
