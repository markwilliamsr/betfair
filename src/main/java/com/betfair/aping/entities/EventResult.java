package com.betfair.aping.entities;

/**
 * Created by markwilliams on 8/20/14.
 */
public class EventResult {
    private Event event;
    private int marketCount;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public int getMarketCount() {
        return marketCount;
    }

    public void setMarketCount(int marketCount) {
        this.marketCount = marketCount;
    }

}
