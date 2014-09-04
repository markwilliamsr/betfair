package com.betfair.aping;

import com.betfair.aping.entities.Event;
import com.betfair.aping.exceptions.APINGException;

/**
 * Created by markwilliams on 9/4/14.
 */
public interface MarketAlgo {
    void process(Event event) throws Exception, APINGException;
}
