package com.betfair.aping;

import com.betfair.aping.api.ApiNgJsonRpcOperations;
import com.betfair.aping.api.ApiNgOperations;
import com.betfair.aping.com.betfair.aping.events.betting.Exposure;
import com.betfair.aping.com.betfair.aping.events.betting.OverUnderMarket;
import com.betfair.aping.com.betfair.aping.events.betting.Score;
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
    DecimalFormat df = new DecimalFormat("0.00");
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
            MarketFilter marketFilter;
            Set<String> eventIds = new HashSet<String>();

            marketFilter = getMarketFilter();

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

            for (Event event : events) {
                Score score = new Score(event);
                System.out.println(event.getName() + ": " + score.findScoreFromMarketOdds());
                MarketCatalogue mc = event.getMarket().get(MarketType.OVER_UNDER_25);

                if (mc == null) {
                    continue;
                }

                if (isCandidateMarket(event)) {
                    System.out.println("OPEN: Candidate Mkt Found:" + gson.toJson(event));
                    Exposure exposure = new Exposure(mc);
                    OverUnderMarket oum = new OverUnderMarket(mc);
                    Runner runner = oum.getRunnerByName(OverUnderMarket.UNDER_2_5);

                    Bet initialBet = getBet(mc, runner, Side.BACK);
                    Bet cashOutBet = exposure.calcCashOutBet(initialBet, getCashOutProfitPercentage());
                    List<Bet> bets = new ArrayList<Bet>();
                    bets.add(initialBet);
                    bets.add(cashOutBet);
                    placeBets(bets);
                }
            }
        } catch (APINGException apiExc) {
            System.out.println(apiExc.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bet getBet(MarketCatalogue marketCatalogue, Runner runner, Side side) {
        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
        Bet bet = new Bet();
        PriceSize priceSize = new PriceSize();

        priceSize.setSize(getSize());

        priceSize.setPrice(oum.getPrice(runner, 0, side).getPrice());

        bet.setMarketId(marketCatalogue.getMarketId());
        bet.setPriceSize(priceSize);
        bet.setSide(Side.BACK);
        bet.setSelectionId(runner.getSelectionId());

        return bet;
    }

    private Double getCashOutProfitPercentage() {
        return Double.valueOf(getProps().getProperty("CLOSE_OUT_PROFIT_PERCENTAGE"));
    }

    private boolean isCandidateMarket(Event event) throws Exception {
        MarketType marketType = MarketType.OVER_UNDER_25;
        MarketCatalogue marketCatalogue = event.getMarket().get(marketType);
        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
        Exposure exposure = new Exposure(marketCatalogue);
        Runner runner = oum.getRunnerByName(OverUnderMarket.UNDER_2_5);

        if (exposure.calcExposureForSide(runner, Side.BACK) > 0) {
            //already bet on this market
            return false;
        }

        try {
            if (oum.getBack(runner, 0).getPrice() >= getOverUnderBackLimit()) {
                Score score = new Score(event);
                ScoreEnum correctScore = score.findScoreFromMarketOdds();
                if (correctScore.getTotalGoals() <= (marketType.getTotalGoals() - getSafetyGoalMargin())) {
                    System.out.println("Best Back Price: " + oum.getBack(runner, 0).toString());
                    return true;
                }
            }
        } catch (RuntimeException ex) {
            System.out.println(ex);
            return false;
        }
        return false;
    }

    private Integer getSafetyGoalMargin() {
        return Integer.valueOf(getProps().getProperty("SAFETY_GOAL_MARGIN", "2"));
    }

    private Double getOverUnderBackLimit() {
        return Double.valueOf(getProps().getProperty("OVER_UNDER_BACK_LIMIT"));
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

    private void placeBets(List<Bet> bets) throws APINGException {
        List<PlaceInstruction> instructions = new ArrayList<PlaceInstruction>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd.HHmmss");
        String marketId = "";

        for (Bet bet : bets) {
            if (marketId.equals("")) {
                marketId = bet.getMarketId();
            } else if (!marketId.equals(bet.getMarketId())) {
                throw new IllegalArgumentException("Cannot mix markets in Bet submission list: MarketId1: " + marketId + ", MarketId2:" + bet.getMarketId());
            }

            LimitOrder limitOrder = new LimitOrder();
            limitOrder.setPersistenceType(PersistenceType.LAPSE);
            limitOrder.setPrice(bet.getPriceSize().getPrice());
            limitOrder.setSize(bet.getPriceSize().getSize());

            PlaceInstruction instruction = new PlaceInstruction();
            instruction.setHandicap(0);
            instruction.setOrderType(OrderType.LIMIT);
            instruction.setSide(bet.getSide());
            instruction.setLimitOrder(limitOrder);
            instruction.setSelectionId(bet.getSelectionId());
            instructions.add(instruction);
        }
        String customerRef = "OU25:" + df.format(cal.getTime());

        if (isSafetyOff()) {
            PlaceExecutionReport placeBetResult = jsonOperations.placeOrders(marketId, instructions, customerRef, applicationKey, sessionToken);
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
