package com.betfair.aping;

import com.betfair.aping.api.ApiNgJsonRpcOperations;
import com.betfair.aping.api.ApiNgOperations;
import com.betfair.aping.com.betfair.aping.events.betting.CorrectScore;
import com.betfair.aping.com.betfair.aping.events.betting.OverUnderCandidate;
import com.betfair.aping.com.betfair.aping.events.betting.ScoreEnum;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.*;
import com.betfair.aping.exceptions.APINGException;
import com.google.gson.Gson;

import java.text.DecimalFormat;
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
    private ApiNgOperations jsonOperations = ApiNgJsonRpcOperations.getInstance();
    private String applicationKey;
    private String sessionToken;

    private static double getPrice() {
        try {
            return new Double((String) ApiNGDemo.getProp().get("BET_PRICE"));
        } catch (NumberFormatException e) {
            //returning the default value
            return new Double(1000);
        }
    }

    private static Properties getProps() {
        return ApiNGDemo.getProp();
    }

    private static double getSize() {
        try {
            return new Double((String) ApiNGDemo.getProp().get("BET_SIZE"));
        } catch (NumberFormatException e) {
            //returning the default value
            return new Double(0.01);
        }
    }

    private boolean isSafetyOff() {
        try {
            return Boolean.valueOf(ApiNGDemo.getProp().getProperty("SAFETY_OFF"));
        } catch (Exception e) {
            //returning the default value
            return true;
        }
    }

    public void start(String appKey, String ssoid) {

        this.applicationKey = appKey;
        this.sessionToken = ssoid;

        try {

            /**
             * ListEventTypes: Search for the event types and then for the "Horse Racing" in the returned list to finally get
             * the listEventTypeId
             */

            MarketFilter marketFilter;
            Set<String> eventTypeIds = new HashSet<String>();
            Set<String> competitionIds = new HashSet<String>();
            Set<String> eventTypes = new HashSet<String>();
            Set<String> eventIds = new HashSet<String>();
            Set<String> competitions = new HashSet<String>();

            marketFilter = getMarketFilter();

            eventTypeIds = getEventTypeIds();
            marketFilter.setEventTypeIds(eventTypeIds);

            competitionIds = getCompetitionIds();
            marketFilter.setCompetitionIds(competitionIds);

            List<EventResult> eventResults = getEvents(marketFilter);
            for (EventResult er : eventResults) {
                eventIds.add(er.getEvent().getId());
            }
            marketFilter.setEventIds(eventIds);

            List<MarketCatalogue> marketCatalogueResult = getMarketCatalogues(marketFilter);

            List<Event> events = assignMarketsToEvents(eventResults, marketCatalogueResult);

            printEvents(events);

            getMarketBooks(marketCatalogueResult);
            printMarketBooks(events);

            for (Event e : events) {
                System.out.println(gson.toJson(e));
            }

            for (Event event : events) {
                System.out.println(event.getName() + ": " + CorrectScore.findCorrectScoreFromMarketOdds(event));
                if (isCandidateMarket(event)) {
                    System.out.println("OPEN: Candidate Mkt Found:" + gson.toJson(event));
                    placeBets(event.getMarket().get(MarketType.OVER_UNDER_25), true);
                }
                if (isMarketCashOut(event)) {
                    System.out.println("CLOSE: Candidate Mkt Found:" + gson.toJson(event));
                    placeBets(event.getMarket().get(MarketType.OVER_UNDER_25), false);
                }
            }

            //placeBets(marketIdChosen, marketBookReturn);

        } catch (APINGException apiExc) {
            System.out.println(apiExc.toString());
        }
    }

    private boolean isMarketCashOut(Event event) {
        MarketCatalogue mc = event.getMarket().get(MarketType.OVER_UNDER_25);

        Double amount = mc.getMarketBook().getRunners().get(0).getOrders().get(0).getSize();
        Double backedPrice = mc.getMarketBook().getRunners().get(0).getOrders().get(0).getPrice();
        Double bestLayPrice = mc.getMarketBook().getRunners().get(0).getEx().getAvailableToLay().get(0).getPrice();

        Double profit = (backedPrice - bestLayPrice) * amount;
        Double percentageProfit = ((backedPrice - bestLayPrice) * amount * 100.0) / amount;
        DecimalFormat df = new DecimalFormat("0.00");
        System.out.println("Amt: " + amount + ", Backed @ " + backedPrice + ", bestLay: " + bestLayPrice);
        System.out.println("Profit: " + df.format(profit) + ", Percentage PnL: " + df.format(percentageProfit));
        if (percentageProfit >= 10.0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isCandidateMarket(Event event) {
        OverUnderCandidate ouc = new OverUnderCandidate();
        MarketCatalogue mk = event.getMarket().get(MarketType.OVER_UNDER_25);
        MarketBook mb = event.getMarket().get(MarketType.OVER_UNDER_25).getMarketBook();

        if (event.getMarket().get(MarketType.OVER_UNDER_25).getMarketBook().getRunners().get(0).getOrders().size() > 0) {
            return false;
        }

        try {
            RunnerCatalog rc = ouc.getRunnerByName(mk.getRunners(), ouc.under25Goals);
            Runner r = ouc.getRunnerBySelectionId(mb.getRunners(), rc.getSelectionId());
            if (ouc.getBack(r, 0).getPrice() >= Double.valueOf(getProps().getProperty("OVER_UNDER_25_BACK_LIMIT"))) {
                if (CorrectScore.findCorrectScoreFromMarketOdds(event).equals(ScoreEnum.NIL_NIL)) {
                    System.out.println("Best Back Price: " + ouc.getBack(r, 0).toString());
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void printMarketBooks(List<Event> events) {
        System.out.println("Full MarketBook Listing Start");
        for (Event e : events) {
            System.out.println(e.getName());
            for (MarketType mt : e.getMarket().keySet()) {
                System.out.println("  " + mt + ": " + gson.toJson(e.getMarket().get(mt)));
            }
        }
        System.out.println("Full MarketBook Listing End");
    }

    private void getMarketBooks(List<MarketCatalogue> marketCatalogueResult) throws APINGException {
        System.out.println("6.(listMarketBook) Get volatile info for Market including best 3 exchange prices available...\n");

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

        for (MarketCatalogue mc : marketCatalogueResult) {
            marketIds.add(mc.getMarketId());
        }

        for (String id : marketIds) {
            marketIdsBatch.add(id);
            batchRequestCost += QUERY_COST;
            totalRequests++;
            if ((batchRequestCost + QUERY_COST >= DATA_LIMIT) || totalRequests == marketIds.size()) {
                List<MarketBook> marketBookReturn = jsonOperations.listMarketBook(marketIdsBatch, priceProjection,
                        orderProjection, matchProjection, currencyCode, applicationKey, sessionToken);
                for (MarketCatalogue mc : marketCatalogueResult) {
                    for (MarketBook mb : marketBookReturn) {
                        if (mc.getMarketId().equals(mb.getMarketId())) {
                            mc.setMarketBook(mb);
                        }
                    }
                }
                marketIdsBatch.clear();
                batchRequestCost = 0;
            }
        }
    }

    private void placeBets(MarketCatalogue marketCatalogue, boolean isOpen) throws APINGException {
        /**
         * PlaceOrders: we try to place a bet, based on the previous request we provide the following:
         * marketId: the market id
         * selectionId: the runner selection id we want to place the bet on
         * side: BACK - specify side, can be Back or Lay
         * orderType: LIMIT - specify order type
         * size: the size of the bet
         * price: the price of the bet
         * customerRef: 1 - unique reference for a transaction specified by user, must be different for each request
         *
         */
        final String under25Goals = "Under 2.5 Goals";
        final String over25Goals = "Over 2.5 Goals";
        RunnerCatalog selectedRunnerCatalog = null;
        Runner selectedRunner = null;

        for (RunnerCatalog rc : marketCatalogue.getRunners()) {
            if (rc.getRunnerName().equals(under25Goals)) {
                selectedRunnerCatalog = rc;
                break;
            }
        }

        for (Runner r : marketCatalogue.getMarketBook().getRunners()) {
            if (r.getSelectionId().equals(selectedRunnerCatalog.getSelectionId())) {
                selectedRunner = r;
                break;
            }
        }

        List<PlaceInstruction> instructions = new ArrayList<PlaceInstruction>();
        PlaceInstruction instruction = new PlaceInstruction();
        instruction.setHandicap(0);

        LimitOrder limitOrder = new LimitOrder();
        limitOrder.setPersistenceType(PersistenceType.LAPSE);
        instruction.setOrderType(OrderType.LIMIT);

        if (isOpen) {
            instruction.setSide(Side.BACK);
            limitOrder.setPrice(selectedRunner.getEx().getAvailableToBack().get(0).getPrice());
        } else {
            instruction.setSide(Side.LAY);
            limitOrder.setPrice(selectedRunner.getEx().getAvailableToLay().get(0).getPrice());
        }

        limitOrder.setSize(getSize());

        instruction.setLimitOrder(limitOrder);
        instruction.setSelectionId(selectedRunner.getSelectionId());
        instructions.add(instruction);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd.HHmmss");

        String customerRef = "OU25:" + df.format(cal.getTime());

        if (isSafetyOff()) {
            PlaceExecutionReport placeBetResult = jsonOperations.placeOrders(marketCatalogue.getMarketId(), instructions, customerRef, applicationKey, sessionToken);

            // Handling the operation result
            if (placeBetResult.getStatus() == ExecutionReportStatus.SUCCESS) {
                System.out.println("Your bet has been placed!!");
                System.out.println(placeBetResult.getInstructionReports());
            } else if (placeBetResult.getStatus() == ExecutionReportStatus.FAILURE) {
                System.out.println("Your bet has NOT been placed :*( ");
                System.out.println("The error is: " + placeBetResult.getErrorCode() + ": " + placeBetResult.getErrorCode().getMessage());
            }
        }
    }

    private void printEvents(List<Event> events) {
        System.out.println("Full Event Listing Start");
        for (Event e : events) {
            System.out.println(gson.toJson(e));
        }
        System.out.println("Full Event Listing End");
    }

    private List<Event> assignMarketsToEvents(List<EventResult> eventResults, List<MarketCatalogue> mks) {
        List<Event> events = new ArrayList<Event>();
        for (MarketCatalogue mk : mks) {
            for (EventResult er : eventResults) {
                if (mk.getEvent().getId().equals(er.getEvent().getId())) {
                    er.getEvent().getMarket().put(mk.getDescription().getMarketType(), mk);
                    mk.getDescription().setRules("");
                }
            }
        }
        for (EventResult er : eventResults) {
            events.add(er.getEvent());
        }
        return events;
    }

    private List<EventResult> getEvents(MarketFilter marketFilter) throws APINGException {
        System.out.println("3.1 (listEvents) Get all events for " + gson.toJson(marketFilter.getMarketTypeCodes()) + "...");

        List<EventResult> events = jsonOperations.listEvents(marketFilter, applicationKey, sessionToken);

        System.out.println("3.2 (listEvents) Events Returned: " + events.size() + "\n");

        return events;
    }

    private MarketFilter getMarketFilter() {
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

        return marketFilter;
    }

    private List<MarketCatalogue> getMarketCatalogues(MarketFilter marketFilter) throws APINGException {

        Set<MarketProjection> marketProjection = new HashSet<MarketProjection>();
        marketProjection.add(MarketProjection.COMPETITION);
        marketProjection.add(MarketProjection.EVENT);
        marketProjection.add(MarketProjection.MARKET_DESCRIPTION);
        marketProjection.add(MarketProjection.RUNNER_DESCRIPTION);
        marketProjection.add(MarketProjection.MARKET_START_TIME);

        System.out.println("4.1 (listMarketCataloque) Get all markets for " + gson.toJson(marketFilter.getMarketTypeCodes()) + "...");

        String maxResults = getProps().getProperty("MAX_RESULTS");

        List<MarketCatalogue> mks = jsonOperations.listMarketCatalogue(marketFilter, marketProjection, MarketSort.FIRST_TO_START, maxResults,
                applicationKey, sessionToken);

        System.out.println("4.2. Print Event, Market Info, name and runners...\n");
        printMarketCatalogue(mks);
        return mks;
    }

    private Set<String> getCompetitionIds() throws APINGException {
        MarketFilter marketFilter = new MarketFilter();
        Set<String> competitionIds = new HashSet<String>();
        Set<String> competitions = new HashSet<String>();
        competitions = gson.fromJson(getProps().getProperty("COMPETITIONS"), competitions.getClass());

        System.out.println("2.1.(listCompetitions) Get all Competitions...");
        List<CompetitionResult> c = jsonOperations.listCompetitions(marketFilter, applicationKey, sessionToken);
        System.out.println("2.2. Extract Competition Ids...");
        for (CompetitionResult competitionResult : c) {
            if (competitions.contains(competitionResult.getCompetition().getName())) {
                System.out.println("2.3. Competition Id for " + competitionResult.getCompetition().getName() + " is: " + competitionResult.getCompetition().getId());
                competitionIds.add(competitionResult.getCompetition().getId().toString());
            }
        }
        System.out.println();
        return competitionIds;
    }

    private Set<String> getEventTypeIds() throws APINGException {
        MarketFilter marketFilter = new MarketFilter();
        Set<String> eventTypeIds = new HashSet<String>();
        Set<String> eventTypes = new HashSet<String>();

        eventTypes = gson.fromJson(getProps().getProperty("EVENT_TYPES"), eventTypes.getClass());

        System.out.println("1.1.(listEventTypes) Get all Event Types...");
        List<EventTypeResult> r = jsonOperations.listEventTypes(marketFilter, applicationKey, sessionToken);
        System.out.println("1.2. Extract Event Type Ids...");
        for (EventTypeResult eventTypeResult : r) {
            if (eventTypes.contains(eventTypeResult.getEventType().getName())) {
                System.out.println("1.3. EventTypeId for " + eventTypeResult.getEventType().getName() + " is: " + eventTypeResult.getEventType().getId() + "\n");
                eventTypeIds.add(eventTypeResult.getEventType().getId().toString());
            }
        }
        return eventTypeIds;
    }

    private void printMarketCatalogue(List<MarketCatalogue> mks) {
        for (MarketCatalogue mk : mks) {
            System.out.println("Event: " + mk.getEvent().getName() + ", Market Name: " + mk.getMarketName() + "; Id: " + mk.getMarketId() + "\n");
            List<RunnerCatalog> runners = mk.getRunners();
            if (runners != null) {
                for (RunnerCatalog rCat : runners) {
                    System.out.println("  Runner Name: " + rCat.getRunnerName() + "; Selection Id: " + rCat.getSelectionId());
                }
                System.out.println();
            }
        }
    }
}
