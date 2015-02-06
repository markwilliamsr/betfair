package com.betfair.aping;

import com.betfair.aping.entities.Bet;
import com.betfair.aping.entities.Event;
import com.betfair.aping.exceptions.APINGException;

import java.util.List;

/**
 * Created by markwilliams on 2/6/15.
 */
public interface IBetPlacer {
    public void placeBets(List<Bet> bets) throws APINGException;

    public boolean isSafetyOff();

    public Event getEvent();

    public void setEvent(Event event);
}
