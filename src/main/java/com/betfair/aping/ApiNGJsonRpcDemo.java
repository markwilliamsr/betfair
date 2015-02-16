package com.betfair.aping;

import com.betfair.aping.algo.BackUnderMarketAlgo;
import com.betfair.aping.algo.IMarketAlgo;
import com.betfair.aping.algo.LayAndCoverAlgo;
import com.betfair.aping.algo.LayTheDrawAlgo;
import com.betfair.aping.api.ApiNgJsonRpcOperations;
import com.betfair.aping.api.ApiNgOperations;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.*;
import com.betfair.aping.exceptions.APINGException;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This is a demonstration class to show a quick demo of the new Betfair API-NG.
 * When you execute the class will: <li>find a market (next horse race in the
 * UK)</li> <li>get prices and runners on this market</li> <li>place a bet on 1
 * runner</li> <li>handle the error</li>
 */
public class ApiNGJsonRpcDemo {
    Gson gson = new Gson();
    private Logger logger = LoggerFactory.getLogger(ApiNGJsonRpcDemo.class);
    private ApiNgOperations jsonOperations = ApiNgJsonRpcOperations.getInstance();

    private static Properties getProps() {
        return ApiNGDemo.getProp();
    }

    public void start() throws Exception {

        IMarketAlgo backUnderMarketAlgo = new BackUnderMarketAlgo();
        IMarketAlgo layAndCoverAlgo = new LayAndCoverAlgo();
        IMarketAlgo layTheDrawAlgo = new LayTheDrawAlgo();
        layTheDrawAlgo.setBetPlacer(new BetPlacer());

        List<Event> events = getCurrentEventsWithCatalogues();

        printEvents(events);

        refreshOdds(events);
        printMarketBooks(events);

        Comparator<Event> comp = new Comparator<Event>() {
            @Override
            public int compare(Event o1, Event o2) {
                     try {
                         if (o1.getMarketClassification() == null || o2.getMarketClassification() == null) {
                             return o1.getName().compareTo(o2.getName());
                         }

                         if (o1.getMarketClassification().getMarketTemp() == null || o2.getMarketClassification().getMarketTemp() == null
                                 || o1.getMarketClassification().getMarketTemp().equals(o2.getMarketClassification().getMarketTemp())) {
                             return o1.getName().compareTo(o2.getName());
                         } else {
                             return o1.getMarketClassification().getMarketTemp().compareTo(o2.getMarketClassification().getMarketTemp());
                         }
                     } catch (Exception e) {
                         logger.warn("Exception in sorting.. carrying on:", e);
                         return 0;
                     }
            }
        };

        Collections.sort(events, comp);

        for (int i = 0; i < Integer.valueOf(getProps().getProperty("LOOP_COUNT", "100")); i++) {
            try {
                Collections.sort(events, comp);
            }
            catch (IllegalArgumentException exception) {

            }
            if (isBackUnderEnabled()) {
                logger.info("--------------------Back Under Mkt Iteration " + i + " Start--------------------");
                for (Event event : events) {
                    backUnderMarketAlgo.process(event);
                }
                logger.info("--------------------Back Under Mkt Iteration " + i + " End--------------------");
            }
            if (isLayAndCoverEnabled()) {
                logger.info("--------------------Lay and Cover Iteration " + i + " Start--------------------");
                for (Event event : events) {
                    layAndCoverAlgo.process(event);
                }
                logger.info("--------------------Lay and Cover Iteration " + i + " End--------------------");
            }
            if (isLayTheDrawEnabled()) {
                logger.info("--------------------Lay The Draw Iteration " + i + " Start--------------------");
                for (Event event : events) {
                    layTheDrawAlgo.process(event);
                }
                logger.info("--------------------Lay The Draw Iteration " + i + " End--------------------");
            }
            Thread.sleep(5000);
            if (isReloadPropertiesEnabled()) {
                logger.debug("Reloading Properties");
                ApiNGDemo.loadProperties();
            }
            if (i > 0 && i % 20 == 0) {
                events = refreshEvents(events);
            }
            refreshOdds(events);
            cancelUnmatchedBets(events);
        }
    }

    private List<Event> refreshEvents(List<Event> currentEvents) throws APINGException {
        logger.info("Refreshing Event List. Start");
        List<Event> newEvents = getCurrentEventsWithCatalogues();
        List<Event> oldEvents = new ArrayList<Event>();
        boolean found = false;

        for (Event currentEvent : currentEvents) {
            MarketCatalogue mc = currentEvent.getMarket().get(MarketType.MATCH_ODDS);
            if (mc != null) {
                MarketBook mb = mc.getMarketBook();
                if (mb != null && mb.getStatus().equals(MarketStatus.CLOSED)) {
                    oldEvents.add(currentEvent);
                }
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

        for (Event event : oldEvents) {
            currentEvents.remove(event);
            logger.debug("Event Removed " + event.getName());
            logger.info("Final Score: {}, [{}]; Score: {}", event.getName(), event.getMarketClassification().getMarketTemp(), event.getScore());
        }

        logger.info("Refreshing Event List. Complete");
        return currentEvents;
    }

    private Boolean isLayAndCoverEnabled() {
        return Boolean.valueOf(ApiNGDemo.getProp().getProperty("LNC_ENABLED", "false"));
    }

    private Boolean isLayTheDrawEnabled() {
        return Boolean.valueOf(ApiNGDemo.getProp().getProperty("LTD_ENABLED", "false"));
    }

    private Boolean isReloadPropertiesEnabled() {
        return Boolean.valueOf(ApiNGDemo.getProp().getProperty("RELOAD_PROPERTIES", "false"));
    }

    private Boolean isBackUnderEnabled() {
        return Boolean.valueOf(ApiNGDemo.getProp().getProperty("OUM_ENABLED", "false"));
    }

    private void printMarketBooks(List<Event> events) {
        logger.debug("Full MarketBook Listing Start");
        for (Event e : events) {
            logger.debug(e.getName());
            for (MarketType mt : e.getMarket().keySet()) {
                logger.debug("  " + mt + ": " + gson.toJson(e.getMarket().get(mt)));
            }
        }
        logger.debug("Full MarketBook Listing End");
    }

    protected long getTimeSinceBetPlaced(Order order) {
        Date now = Calendar.getInstance().getTime();

        long diffMs = now.getTime() - order.getPlacedDate().getTime();
        long diffSec = diffMs / 1000;

        return diffSec;
    }

    private void cancelUnmatchedBets(List<Event> events) {
        SimpleDateFormat dtf = new SimpleDateFormat("yyyyMMdd.HHmmss.SSS");
        for (Event event : events) {
            for (MarketCatalogue mc : event.getMarket().values()) {
                MarketBook marketBook = mc.getMarketBook();
                List<CancelInstruction> cancelInstructions = new ArrayList<CancelInstruction>();
                for (Runner runner : marketBook.getRunners()) {
                    if (runner.getOrders() != null) {
                        for (Order order : runner.getOrders()) {
                            if (order.getSizeRemaining() > 0) {
                                if (getTimeSinceBetPlaced(order) > 20) {
                                    CancelInstruction cancelInstruction = new CancelInstruction();
                                    cancelInstruction.setBetId(order.getBetId());
                                    cancelInstructions.add(cancelInstruction);
                                    logger.warn("{}, {}; Cancelling Bet: ID: {}, Side: {}, Rem: {}", event.getName(), mc.getMarketName(), order.getBetId(), order.getSide(), order.getSizeRemaining());
                                }
                            }
                        }
                    }
                }
                if (cancelInstructions.size() > 0) {
                    String ref = "CXL:" + dtf.format(Calendar.getInstance().getTime());
                    try {
                        CancelExecutionReport cancelExecutionReport = jsonOperations.cancelOrders(mc.getMarketId(), cancelInstructions, ref);
                        if (cancelExecutionReport.getStatus() == ExecutionReportStatus.SUCCESS) {
                            logger.info("Your cancellation has been placed. {} ", gson.toJson(cancelExecutionReport.getInstructionReports()));
                        } else if (cancelExecutionReport.getStatus() == ExecutionReportStatus.FAILURE) {
                            logger.info("Your cancellation has NOT been placed :*( ");
                            logger.info("The error is: " + cancelExecutionReport.getErrorCode() + ": " + cancelExecutionReport.getErrorCode().getMessage());
                            logger.info(gson.toJson(cancelInstructions));
                            logger.info(gson.toJson(cancelExecutionReport));
                        }
                    } catch (APINGException exception) {
                        logger.error("Exception Cancelling Unmatched Bets: {}", exception);
                    }
                }
            }
        }
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
        logger.debug("Full Event Listing Start");
        for (Event e : events) {
            logger.debug(gson.toJson(e));
        }
        logger.debug("Full Event Listing End");
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
        logger.debug("3.1 (listEvents) Get all events for " + gson.toJson(getMarketFilter().getMarketTypeCodes()) + "...");

        List<EventResult> events = jsonOperations.listEvents(getMarketFilter());

        logger.debug("3.2 (listEvents) Events Returned: " + events.size() + "\n");

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

        logger.debug("4.1 (listMarketCataloque) Get all markets for " + gson.toJson(marketFilter.getMarketTypeCodes()) + "...");

        String maxResults = getProps().getProperty("MAX_RESULTS");

        List<MarketCatalogue> mks = jsonOperations.listMarketCatalogue(marketFilter, marketProjection, MarketSort.FIRST_TO_START, maxResults);

        logger.debug("4.2. Print Event, Market Info, name and runners...\n");
        //printMarketCatalogue(mks);
        return mks;
    }

    private Set<String> getCompetitionIds() throws APINGException {
        MarketFilter marketFilter = new MarketFilter();
        Set<String> competitionIds = new HashSet<String>();
        Set<String> competitions = new HashSet<String>();
        competitions = gson.fromJson(getProps().getProperty("COMPETITIONS"), competitions.getClass());

        logger.debug("2.1.(listCompetitions) Get all Competitions...");
        List<CompetitionResult> c = jsonOperations.listCompetitions(marketFilter);
        logger.debug("2.2. Extract Competition Ids...");
        for (CompetitionResult competitionResult : c) {
            if (competitions.contains(competitionResult.getCompetition().getName())) {
                logger.debug("2.3. Competition Id for " + competitionResult.getCompetition().getName() + " is: " + competitionResult.getCompetition().getId());
                competitionIds.add(competitionResult.getCompetition().getId().toString());
            }
        }
        logger.debug("");
        return competitionIds;
    }

    private Set<String> getEventTypeIds() throws APINGException {
        MarketFilter marketFilter = new MarketFilter();
        Set<String> eventTypeIds = new HashSet<String>();
        Set<String> eventTypes = new HashSet<String>();

        eventTypes = gson.fromJson(getProps().getProperty("EVENT_TYPES"), eventTypes.getClass());

        logger.debug("1.1.(listEventTypes) Get all Event Types...");
        List<EventTypeResult> r = jsonOperations.listEventTypes(marketFilter);
        logger.debug("1.2. Extract Event Type Ids...");
        for (EventTypeResult eventTypeResult : r) {
            if (eventTypes.contains(eventTypeResult.getEventType().getName())) {
                logger.debug("1.3. EventTypeId for " + eventTypeResult.getEventType().getName() + " is: " + eventTypeResult.getEventType().getId() + "\n");
                eventTypeIds.add(eventTypeResult.getEventType().getId().toString());
            }
        }
        return eventTypeIds;
    }

    private void printMarketCatalogue(List<MarketCatalogue> mks) {
        for (MarketCatalogue mk : mks) {
            logger.debug("Event: " + mk.getEvent().getName() + ", Market Name: " + mk.getMarketName() + "; Id: " + mk.getMarketId() + "\n");
            List<RunnerCatalog> runners = mk.getRunners();
            if (runners != null) {
                for (RunnerCatalog rCat : runners) {
                    logger.debug("  Runner Name: " + rCat.getRunnerName() + "; Selection Id: " + rCat.getSelectionId());
                }
                logger.debug("");
            }
        }
    }

    public Gson getGson() {
        return gson;
    }
}
