package com.betfair.aping.events.betting;

import com.betfair.aping.ApiNGDemo;
import com.betfair.aping.JsonRpcTestBase;
import com.betfair.aping.entities.EventTypeResult;
import com.betfair.aping.entities.MarketFilter;
import com.betfair.aping.exceptions.APINGException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by markwilliams on 25/08/2014.
 */
public class EventTypeIdsOperationTest extends JsonRpcTestBase {

    private Logger logger = LoggerFactory.getLogger(EventTypeIdsOperationTest.class);

    @Test
    public void getEventIdsTest() throws APINGException, Exception {
        ApiNGDemo.loadProperties();
        Set<String> eventTypeIds = getEventTypeIds();
        assertEquals(1, eventTypeIds.size());
        for (String id : eventTypeIds) {
            assertEquals("1", id);
        }
    }

    private Set<String> getEventTypeIds() throws APINGException {
        MarketFilter marketFilter = new MarketFilter();
        Set<String> eventTypeIds = new HashSet<String>();
        Set<String> eventTypes = new HashSet<String>();

        eventTypes = gson.fromJson(api.getProp().getProperty("EVENT_TYPES"), eventTypes.getClass());

        List<EventTypeResult> r = jsonOperations.listEventTypes(marketFilter);
        for (EventTypeResult eventTypeResult : r) {
            if (eventTypes.contains(eventTypeResult.getEventType().getName())) {
                logger.info("EventTypeId for " + eventTypeResult.getEventType().getName() + " is: " + eventTypeResult.getEventType().getId() + "\n");
                eventTypeIds.add(eventTypeResult.getEventType().getId().toString());
            }
        }
        return eventTypeIds;
    }
}
