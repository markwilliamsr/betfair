package com.betfair.aping.algo;

import com.betfair.aping.IBetPlacer;
import com.betfair.aping.entities.Event;
import com.betfair.aping.exceptions.APINGException;

/**
 * Created by markwilliams on 9/4/14.
 */
public interface IMarketAlgo {
    public void process(Event event) throws Exception, APINGException;
    public void setBetPlacer(IBetPlacer betPlacer);
}
