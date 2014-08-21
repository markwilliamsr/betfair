package com.betfair.aping;

import com.betfair.aping.api.ApiNgJsonRpcOperations;
import com.betfair.aping.api.ApiNgOperations;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.*;
import com.betfair.aping.exceptions.APINGException;
import com.google.gson.Gson;

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

    public void start(String appKey, String ssoid) {

        this.applicationKey = appKey;
        this.sessionToken = ssoid;

        try {

            /**
             * ListEventTypes: Search for the event types and then for the "Horse Racing" in the returned list to finally get
             * the listEventTypeId
             */

            MarketFilter marketFilter;
            marketFilter = new MarketFilter();
            Set<String> eventTypeIds = new HashSet<String>();
            Set<String> competitionIds = new HashSet<String>();
            Set<String> eventTypes = new HashSet<String>();
            Set<String> competitions = new HashSet<String>();
            marketFilter = new MarketFilter();

            eventTypeIds = getEventTypeIds();

            marketFilter.setEventTypeIds(eventTypeIds);

            competitionIds = getCompetitionIds();

            marketFilter.setCompetitionIds(competitionIds);
            List<EventResult> events = getEvents(marketFilter);
            printEvents(events);
            List<MarketCatalogue> marketCatalogueResult = getMarketCatalogues(marketFilter);


            System.out.println("5. Print static marketId, name and runners....\n");
            printMarketCatalogue(marketCatalogueResult.get(0));
            /**
             * ListMarketBook: get list of runners in the market, parameters:
             * marketId:  the market we want to list runners
             *
             */
            System.out.println("6.(listMarketBook) Get volatile info for Market including best 3 exchange prices available...\n");
            String marketIdChosen = marketCatalogueResult.get(0).getMarketId();

            PriceProjection priceProjection = new PriceProjection();
            Set<PriceData> priceData = new HashSet<PriceData>();
            priceData.add(PriceData.EX_BEST_OFFERS);
            priceProjection.setPriceData(priceData);

            //In this case we don't need these objects so they are declared null
            OrderProjection orderProjection = null;
            MatchProjection matchProjection = null;
            String currencyCode = null;

            List<String> marketIds = new ArrayList<String>();
            marketIds.add(marketIdChosen);

            List<MarketBook> marketBookReturn = jsonOperations.listMarketBook(marketIds, priceProjection,
                    orderProjection, matchProjection, currencyCode, applicationKey, sessionToken);

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

            long selectionId = 0;
            if (marketBookReturn.size() != 0) {
                Runner runner = marketBookReturn.get(0).getRunners().get(0);
                selectionId = runner.getSelectionId();
                System.out.println("7. Place a bet below minimum stake to prevent the bet actually " +
                        "being placed for marketId: " + marketIdChosen + " with selectionId: " + selectionId + "...\n\n");
                List<PlaceInstruction> instructions = new ArrayList<PlaceInstruction>();
                PlaceInstruction instruction = new PlaceInstruction();
                instruction.setHandicap(0);
                instruction.setSide(Side.BACK);
                instruction.setOrderType(OrderType.LIMIT);

                LimitOrder limitOrder = new LimitOrder();
                limitOrder.setPersistenceType(PersistenceType.LAPSE);
                //API-NG will return an error with the default size=0.01. This is an expected behaviour.
                //You can adjust the size and price value in the "apingdemo.properties" file
                limitOrder.setPrice(getPrice());
                limitOrder.setSize(getSize());

                instruction.setLimitOrder(limitOrder);
                instruction.setSelectionId(selectionId);
                instructions.add(instruction);

                String customerRef = "1";

                PlaceExecutionReport placeBetResult = jsonOperations.placeOrders(marketIdChosen, instructions, customerRef, applicationKey, sessionToken);

                // Handling the operation result
                if (placeBetResult.getStatus() == ExecutionReportStatus.SUCCESS) {
                    System.out.println("Your bet has been placed!!");
                    System.out.println(placeBetResult.getInstructionReports());
                } else if (placeBetResult.getStatus() == ExecutionReportStatus.FAILURE) {
                    System.out.println("Your bet has NOT been placed :*( ");
                    System.out.println("The error is: " + placeBetResult.getErrorCode() + ": " + placeBetResult.getErrorCode().getMessage());
                }
            } else {
                System.out.println("Sorry, no runners found\n\n");
            }

        } catch (APINGException apiExc) {
            System.out.println(apiExc.toString());
        }
    }

    private void printEvents(List<EventResult> events) {
        System.out.println("Events, dear boy...");
        for (EventResult e : events){
            System.out.println(e.getEvent().toString());
        }
    }

    private List<EventResult> getEvents(MarketFilter marketFilter) throws APINGException {
        TimeRange time = new TimeRange();
        time.setFrom(new Date());
        Set<String> countries = new HashSet<String>();
        Set<String> typesCode = new HashSet<String>();
        typesCode = gson.fromJson(getProps().getProperty("MARKET_TYPES"), typesCode.getClass());

        marketFilter.setMarketStartTime(time);
        marketFilter.setMarketCountries(countries);
        marketFilter.setMarketTypeCodes(typesCode);

        System.out.println("4.1 (listMarketCataloque) Get all events for " + gson.toJson(typesCode) + "...");

        String maxResults = getProps().getProperty("MAX_RESULTS");

        return jsonOperations.listEvents(marketFilter, applicationKey, sessionToken);
    }

    private List<MarketCatalogue> getMarketCatalogues(MarketFilter marketFilter) throws APINGException {
        /**
         * ListMarketCatalogue: Get next available horse races, parameters:
         * eventTypeIds : 7 - get all available horse races for event id 7 (horse racing)
         * maxResults: 1 - specify number of results returned (narrowed to 1 to get first race)
         * marketStartTime: specify date (must be in this format: yyyy-mm-ddTHH:MM:SSZ)
         * sort: FIRST_TO_START - specify sort order to first to start race
         */
        TimeRange time = new TimeRange();
        time.setFrom(new Date());
        Set<String> countries = new HashSet<String>();
        Set<String> typesCode = new HashSet<String>();
        typesCode = gson.fromJson(getProps().getProperty("MARKET_TYPES"), typesCode.getClass());

        marketFilter.setMarketStartTime(time);
        marketFilter.setMarketCountries(countries);
        marketFilter.setMarketTypeCodes(typesCode);

        Set<MarketProjection> marketProjection = new HashSet<MarketProjection>();
        marketProjection.add(MarketProjection.RUNNER_DESCRIPTION);

        System.out.println("4.1 (listMarketCataloque) Get all markets for " + gson.toJson(typesCode) + "...");

        String maxResults = getProps().getProperty("MAX_RESULTS");

        return jsonOperations.listMarketCatalogue(marketFilter, marketProjection, MarketSort.FIRST_TO_START, maxResults,
                applicationKey, sessionToken);
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

    private void printMarketCatalogue(MarketCatalogue mk) {
        System.out.println("Market Name: " + mk.getMarketName() + "; Id: " + mk.getMarketId() + "\n");
        List<RunnerCatalog> runners = mk.getRunners();
        if (runners != null) {
            for (RunnerCatalog rCat : runners) {
                System.out.println("Runner Name: " + rCat.getRunnerName() + "; Selection Id: " + rCat.getSelectionId() + "\n");
            }
        }
    }
}
