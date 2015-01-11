package com.betfair.aping.events.betting;

import com.betfair.aping.ApiNGDemo;
import com.betfair.aping.JsonRpcTestBase;
import com.betfair.aping.algo.LayAndCoverAlgo;
import com.betfair.aping.algo.MarketConfig;
import com.betfair.aping.entities.MarketType;
import com.betfair.aping.enums.MarketClassification;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by markwilliams on 11/01/15.
 */
public class PropertiesTest extends JsonRpcTestBase {

    @Test
    public void lncMarketPropertiesTest() {
        LayAndCoverAlgo algo = new LayAndCoverAlgo();
        Map<MarketClassification, Map<MarketType, MarketConfig>> marketConfigurations = algo.getMarketConfigurations();

        Double layLimit = marketConfigurations.get(MarketClassification.HOT).get(MarketType.OVER_UNDER_05).getLayLimit();

        assertTrue(layLimit > 0);

    }
}
