package com.betfair.aping.events.betting;

import com.betfair.aping.JsonRpcTestBase;
import com.betfair.aping.algo.LayAndCoverAlgo;
import com.betfair.aping.algo.MarketConfig;
import com.betfair.aping.entities.MarketType;
import com.betfair.aping.enums.MarketTemp;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by markwilliams on 11/01/15.
 */
public class PropertiesTest extends JsonRpcTestBase {

    @Test
    public void lncMarketPropertiesTest() {
        LayAndCoverAlgo algo = new LayAndCoverAlgo();
        Map<MarketTemp, Map<MarketType, MarketConfig>> marketConfigurations = algo.getMarketConfigs();

        Double layLimit = marketConfigurations.get(MarketTemp.HOT).get(MarketType.OVER_UNDER_05).getLayLimit();

        assertTrue(layLimit > 0);

    }
}
