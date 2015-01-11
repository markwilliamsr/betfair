package com.betfair.aping.events.betting;

import com.betfair.aping.entities.Event;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by markwilliams on 11/01/15.
 */
public class EventTest {

    private Logger logger = LoggerFactory.getLogger(EventTest.class);

    @Test
    public void loggingTest() {
        Event event = new Event();
        event.setName("Aaaa vs alskfhegbevbjdk");

        logger.info("{}; {}: Starts At: [{}], Elapsed [], Current Score: {}, Previous Score: {}",
                String.format("%1$35s", event.getName()), event.getMarketClassification(), event.getOpenDate(), event.getScore(), event.getPreviousScores().toString());
    }

    @Test
    public void eventSortingTest() {
        List<Event> events = new ArrayList<Event>();
        Event event1 = new Event();
        event1.setName("Aaaa");
        events.add(event1);

        Event event2 = new Event();
        event2.setName("Cccc");
        events.add(event2);

        Event event3 = new Event();
        event3.setName("Bbbb");
        events.add(event3);

        Comparator<Event> comp = new Comparator<Event>() {
            @Override
            public int compare(Event o1, Event o2) {
                return o1.getName().compareTo(o2.getName());
            }
        };

        Collections.sort(events, comp);

        for (Event e : events) {
            System.out.println(e.getName());
        }
    }
}
