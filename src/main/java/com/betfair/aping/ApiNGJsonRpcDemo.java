package com.betfair.aping;

import com.betfair.aping.api.ApiNgJsonRpcOperations;
import com.betfair.aping.api.ApiNgOperations;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.*;
import com.betfair.aping.exceptions.APINGException;
import com.betfair.aping.util.LayAndCoverAlgo;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This is a demonstration class to show a quick demo of the new Betfair API-NG.
 * When you execute the class will: <li>find a market (next horse race in the
 * UK)</li> <li>get prices and runners on this market</li> <li>place a bet on 1
 * runner</li> <li>handle the error</li>
 */
public class ApiNGJsonRpcDemo {
    private Logger logger = LoggerFactory.getLogger(ApiNGJsonRpcDemo.class);
            Gson gson = new Gson();
    private ApiNgOperations jsonOperations = ApiNgJsonRpcOperations.getInstance();

    private static Properties getProps() {
        return ApiNGDemo.getProp();
    }

    public void start() {
        try {
            MarketFilter marketFilter;

            MarketAlgo marketAlgo1 = new BackUnderMarketAlgo();
            MarketAlgo marketAlgo2 = new LayAndCoverAlgo();


            List<Event> events = getCurrentEventsWithCatalogues();

            printEvents(events);

            refreshOdds(events);
            printMarketBooks(events);
            for (int i = 0; i < Integer.valueOf(getProps().getProperty("LOOP_COUNT", "100")); i++) {
                if (isBackUnderEnabled()) {
                    logger.info("--------------------Back Under Mkt Iteration " + i + " Start--------------------");
                    for (Event event : events) {
                        marketAlgo1.process(event);
                    }
                    logger.info("--------------------Back Under Mkt Iteration " + i + " End--------------------");
                }
                if (isLayAndCoverEnabled()) {
                    logger.info("--------------------Lay and Cover Iteration " + i + " Start--------------------");
                    for (Event event : events) {
                        marketAlgo2.process(event);
                    }
                    logger.info("--------------------Lay and Cover Iteration " + i + " End--------------------");
                }
                Thread.sleep(5000);
                if (i > 0 && i % 300 == 0) {
                    events = refreshEvents(events);
                }
                refreshOdds(events);
            }
        } catch (APINGException apiExc) {
            logger.info(apiExc.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Event> refreshEvents(List<Event> currentEvents) throws APINGException {
        logger.info("Refreshing Event List. Start");
        List<Event> newEvents = getCurrentEventsWithCatalogues();
        boolean found = false;
        for (Event currentEvent : currentEvents) {
            for (Event newEvent : newEvents) {
                if (currentEvent.getId().equals(newEvent.getId())) {
                    found = true;
                }
            }
            if (!found) {
                currentEvents.remove(currentEvent);
                logger.info("Event Removed " + currentEvent.getName());
            }
        }

        for (Event newEvent : newEvents) {
            for (Event currentEvent : currentEvents) {
                if (currentEvent.getId().equals(newEvent.getId())) {
                    found = true;
                }
            }
            if (!found) {
                currentEvents.add(newEvent);
                logger.info("Event Added " + newEvent.getName());
            }
        }

        logger.info("Refreshing Event List. Complete");
        return currentEvents;
    }

    private Boolean isLayAndCoverEnabled() {
        return Boolean.valueOf(ApiNGDemo.getProp().getProperty("LNC_ENABLED", "false"));
    }

    private Boolean isBackUnderEnabled() {
        return Boolean.valueOf(ApiNGDemo.getProp().getProperty("BU_ENABLED", "false"));
    }

    private void printMarketBooks(List<Event> events) {
        logger.info("Full MarketBook Listing Start");
        for (Event e : events) {
            logger.info(e.getName());
            for (MarketType mt : e.getMarket().keySet()) {
                logger.info("  " + mt + ": " + gson.toJson(e.getMarket().get(mt)));
            }
        }
        logger.info("Full MarketBook Listing End");
    }

    private void refreshOdds(List<Event> events) throws APINGException {
        PriceProjection priceProjection = new PriceProjection();
        Set<PriceData> priceData = new HashSet<PriceData>();
        priceData.add(PriceData.EX_BEST_OFFERS);
        priceProjection.setPriceData(priceData);
        OrderProjection orderProjection = OrderProjection.ALL;
        MatchProjection matchProjection = null;
        String currencyCode = null;
        int batchRequestCost = 0;
        int totalRequests = 0;
        final int DATA_LIMIT = 200;
        final int QUERY_COST = 5;

        List<String> marketIds = new ArrayList<String>();
        List<String> marketIdsBatch = new ArrayList<String>();

        for (Event event : events) {
            for (MarketCatalogue mc : event.getMarket().values()) {
                marketIds.add(mc.getMarketId());
            }
        }

        for (String id : marketIds) {
            marketIdsBatch.add(id);
            batchRequestCost += QUERY_COST;
            totalRequests++;
            if ((batchRequestCost + QUERY_COST >= DATA_LIMIT) || totalRequests == marketIds.size()) {
                List<MarketBook> marketBookReturn = jsonOperations.listMarketBook(marketIdsBatch, priceProjection,
                        orderProjection, matchProjection, currencyCode);
                for (Event event : events) {
                    for (MarketCatalogue mc : event.getMarket().values()) {
                        for (MarketBook mb : marketBookReturn) {
                            if (mc.getMarketId().equals(mb.getMarketId())) {
                                mc.setMarketBook(mb);
                            }
                        }
                    }
                }
                marketIdsBatch.clear();
                batchRequestCost = 0;
            }
        }
    }

    private void printEvents(List<Event> events) {
        logger.info("Full Event Listing Start");
        for (Event e : events) {
            logger.info(gson.toJson(e));
        }
        logger.info("Full Event Listing End");
    }

    private List<Event> getCurrentEventsWithCatalogues() throws APINGException {
        List<EventResult> eventResults = getEvents();
        List<MarketCatalogue> marketCatalogues = getMarketCatalogues();
        List<Event> events = new ArrayList<Event>();
        for (MarketCatalogue mc : marketCatalogues) {
            for (EventResult er : eventResults) {
                if (mc.getEvent().getId().equals(er.getEvent().getId())) {
                    er.getEvent().getMarket().put(mc.getDescription().getMarketType(), mc);
                    mc.getDescription().setRules("");
                }
            }
        }
        for (EventResult er : eventResults) {
            events.add(er.getEvent());
        }
        return events;
    }

    private List<EventResult> getEvents() throws APINGException {
        logger.info("3.1 (listEvents) Get all events for " + gson.toJson(getMarketFilter().getMarketTypeCodes()) + "...");

        List<EventResult> events = jsonOperations.listEvents(getMarketFilter());

        logger.info("3.2 (listEvents) Events Returned: " + events.size() + "\n");

        return events;
    }

    private MarketFilter getMarketFilter() throws APINGException {
        MarketFilter marketFilter = new MarketFilter();
        Calendar cal = null;
        TimeRange time = new TimeRange();
        Set<String> countries = new HashSet<String>();
        Set<String> typesCode = new HashSet<String>();
        typesCode = gson.fromJson(getProps().getProperty("MARKET_TYPES"), typesCode.getClass());

        cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -120);
        time.setFrom(cal.getTime());

        cal = Calendar.getInstance();
        String timeBeforeStart = getProps().getProperty("TIME_BEFORE_START");
        if (timeBeforeStart.length() > 0) {
            cal.add(Calendar.MINUTE, Integer.valueOf(timeBeforeStart));
            time.setTo(cal.getTime());
        }

        marketFilter.setMarketStartTime(time);
        marketFilter.setMarketCountries(countries);
        marketFilter.setMarketTypeCodes(typesCode);
        marketFilter.setTurnInPlayEnabled(true);
        marketFilter.setEventTypeIds(getEventTypeIds());
        marketFilter.setCompetitionIds(getCompetitionIds());

        return marketFilter;
    }

    private List<MarketCatalogue> getMarketCatalogues() throws APINGException {

        Set<MarketProjection> marketProjection = new HashSet<MarketProjection>();
        Set<String> eventIds = new HashSet<String>();
        marketProjection.add(MarketProjection.COMPETITION);
        marketProjection.add(MarketProjection.EVENT);
        marketProjection.add(MarketProjection.MARKET_DESCRIPTION);
        marketProjection.add(MarketProjection.RUNNER_DESCRIPTION);
        marketProjection.add(MarketProjection.MARKET_START_TIME);

        List<EventResult> eventResults = getEvents();
        for (EventResult er : eventResults) {
            eventIds.add(er.getEvent().getId());
        }

        MarketFilter marketFilter = getMarketFilter();
        marketFilter.setEventIds(eventIds);

        logger.info("4.1 (listMarketCataloque) Get all markets for " + gson.toJson(marketFilter.getMarketTypeCodes()) + "...");

        String maxResults = getProps().getProperty("MAX_RESULTS");

        List<MarketCatalogue> mks = jsonOperations.listMarketCatalogue(marketFilter, marketProjection, MarketSort.FIRST_TO_START, maxResults);

        logger.info("4.2. Print Event, Market Info, name and runners...\n");
        //printMarketCatalogue(mks);
        return mks;
    }

    private Set<String> getCompetitionIds() throws APINGException {
        MarketFilter marketFilter = new MarketFilter();
        Set<String> competitionIds = new HashSet<String>();
        Set<String> competitions = new HashSet<String>();
        competitions = gson.fromJson(getProps().getProperty("COMPETITIONS"), competitions.getClass());

        logger.info("2.1.(listCompetitions) Get all Competitions...");
        List<CompetitionResult> c = jsonOperations.listCompetitions(marketFilter);
        logger.info("2.2. Extract Competition Ids...");
        for (CompetitionResult competitionResult : c) {
            if (competitions.contains(competitionResult.getCompetition().getName())) {
                logger.info("2.3. Competition Id for " + competitionResult.getCompetition().getName() + " is: " + competitionResult.getCompetition().getId());
                competitionIds.add(competitionResult.getCompetition().getId().toString());
            }
        }
        logger.info("");
        return competitionIds;
    }

    private Set<String> getEventTypeIds() throws APINGException {
        MarketFilter marketFilter = new MarketFilter();
        Set<String> eventTypeIds = new HashSet<String>();
        Set<String> eventTypes = new HashSet<String>();

        eventTypes = gson.fromJson(getProps().getProperty("EVENT_TYPES"), eventTypes.getClass());

        logger.info("1.1.(listEventTypes) Get all Event Types...");
        List<EventTypeResult> r = jsonOperations.listEventTypes(marketFilter);
        logger.info("1.2. Extract Event Type Ids...");
        for (EventTypeResult eventTypeResult : r) {
            if (eventTypes.contains(eventTypeResult.getEventType().getName())) {
                logger.info("1.3. EventTypeId for " + eventTypeResult.getEventType().getName() + " is: " + eventTypeResult.getEventType().getId() + "\n");
                eventTypeIds.add(eventTypeResult.getEventType().getId().toString());
            }
        }
        return eventTypeIds;
    }

    private void printMarketCatalogue(List<MarketCatalogue> mks) {
        for (MarketCatalogue mk : mks) {
            logger.info("Event: " + mk.getEvent().getName() + ", Market Name: " + mk.getMarketName() + "; Id: " + mk.getMarketId() + "\n");
            List<RunnerCatalog> runners = mk.getRunners();
            if (runners != null) {
                for (RunnerCatalog rCat : runners) {
                    logger.info("  Runner Name: " + rCat.getRunnerName() + "; Selection Id: " + rCat.getSelectionId());
                }
                logger.info("");
            }
        }
    }

    public Gson getGson() {
        return gson;
    }
}
