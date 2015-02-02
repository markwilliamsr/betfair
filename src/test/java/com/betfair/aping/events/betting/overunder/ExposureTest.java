package com.betfair.aping.events.betting.overunder;

import com.betfair.aping.com.betfair.aping.events.betting.Exposure;
import com.betfair.aping.com.betfair.aping.events.betting.MatchOddsMarket;
import com.betfair.aping.com.betfair.aping.events.betting.OverUnderMarket;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.Side;
import com.google.gson.Gson;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by markwilliams on 25/08/2014.
 */
public class ExposureTest {
    final String jsonEvent = "{\"id\":\"27249641\",\"name\":\"Man City v Liverpool\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"Aug 25, 2014 8:00:00 PM\",\"market\":{\"OVER_UNDER_25\":{\"marketId\":\"1.115036531\",\"marketName\":\"Over/Under 2.5 Goals\",\"description\":{\"persistenceEnabled\":true,\"bspMarket\":false,\"marketTime\":\"Aug 25, 2014 8:00:00 PM\",\"suspendTime\":\"Aug 25, 2014 8:00:00 PM\",\"bettingType\":\"ODDS\",\"turnInPlayEnabled\":true,\"marketType\":\"OVER_UNDER_25\",\"marketBaseRate\":5.0,\"discountAllowed\":true,\"wallet\":\"UK wallet\",\"rules\":\"\",\"rulesHasDate\":true},\"runners\":[{\"selectionId\":47972,\"runnerName\":\"Under 2.5 Goals\",\"handicap\":0.0},{\"selectionId\":47973,\"runnerName\":\"Over 2.5 Goals\",\"handicap\":0.0}],\"competition\":{\"id\":\"31\",\"name\":\"Barclays Premier League\"},\"event\":{\"id\":\"27249641\",\"name\":\"Man City v Liverpool\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"Aug 25, 2014 8:00:00 PM\",\"market\":{}},\"marketBook\":{\"marketId\":\"1.115036531\",\"isMarketDataDelayed\":false,\"status\":\"OPEN\",\"betDelay\":5,\"bspReconciled\":false,\"complete\":true,\"inplay\":true,\"numberOfWinners\":1,\"numberOfRunners\":2,\"numberOfActiveRunners\":2,\"lastMatchTime\":\"Aug 25, 2014 9:06:51 PM\",\"totalMatched\":2317163.2,\"totalAvailable\":372363.45,\"crossMatching\":true,\"runnersVoidable\":false,\"version\":803963784,\"runners\":[{\"selectionId\":47972,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":1.77,\"totalMatched\":1026679.77,\"ex\":{\"availableToBack\":[{\"price\":1.76,\"size\":189.53},{\"price\":1.75,\"size\":155.93},{\"price\":1.74,\"size\":861.57}],\"availableToLay\":[{\"price\":1.77,\"size\":421.13},{\"price\":1.78,\"size\":347.01},{\"price\":1.79,\"size\":2268.39}],\"tradedVolume\":[]},\"orders\":[{\"betId\":\"40512000858\",\"orderType\":\"LIMIT\",\"status\":\"EXECUTION_COMPLETE\",\"persistenceType\":\"LAPSE\",\"side\":\"LAY\",\"price\":1.98,\"size\":2.0,\"bspLiability\":0.0,\"placedDate\":\"Aug 25, 2014 8:15:54 PM\",\"avgPriceMatched\":1.98,\"sizeMatched\":2.0,\"sizeRemaining\":0.0,\"sizeLapsed\":0.0,\"sizeCancelled\":0.0,\"sizeVoided\":0.0},{\"betId\":\"40509052837\",\"orderType\":\"LIMIT\",\"status\":\"EXECUTION_COMPLETE\",\"persistenceType\":\"LAPSE\",\"side\":\"BACK\",\"price\":2.42,\"size\":2.0,\"bspLiability\":0.0,\"placedDate\":\"Aug 25, 2014 7:21:00 PM\",\"avgPriceMatched\":2.42,\"sizeMatched\":2.0,\"sizeRemaining\":0.0,\"sizeLapsed\":0.0,\"sizeCancelled\":0.0,\"sizeVoided\":0.0},{\"betId\":\"40512132625\",\"orderType\":\"LIMIT\",\"status\":\"EXECUTION_COMPLETE\",\"persistenceType\":\"LAPSE\",\"side\":\"LAY\",\"price\":1.95,\"size\":0.46,\"bspLiability\":0.0,\"placedDate\":\"Aug 25, 2014 8:18:22 PM\",\"avgPriceMatched\":1.94,\"sizeMatched\":0.46,\"sizeRemaining\":0.0,\"sizeLapsed\":0.0,\"sizeCancelled\":0.0,\"sizeVoided\":0.0}],\"matches\":[]},{\"selectionId\":47973,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":2.3,\"totalMatched\":1290483.43,\"ex\":{\"availableToBack\":[{\"price\":2.3,\"size\":55.84},{\"price\":2.26,\"size\":42.96},{\"price\":2.24,\"size\":637.68}],\"availableToLay\":[{\"price\":2.32,\"size\":255.88},{\"price\":2.34,\"size\":754.01},{\"price\":2.36,\"size\":708.08}],\"tradedVolume\":[]}}]}},\"CORRECT_SCORE\":{\"marketId\":\"1.115036530\",\"marketName\":\"Correct Score\",\"description\":{\"persistenceEnabled\":true,\"bspMarket\":false,\"marketTime\":\"Aug 25, 2014 8:00:00 PM\",\"suspendTime\":\"Aug 25, 2014 8:00:00 PM\",\"bettingType\":\"ODDS\",\"turnInPlayEnabled\":true,\"marketType\":\"CORRECT_SCORE\",\"marketBaseRate\":5.0,\"discountAllowed\":true,\"wallet\":\"UK wallet\",\"rules\":\"\",\"rulesHasDate\":true},\"runners\":[{\"selectionId\":1,\"runnerName\":\"0 - 0\",\"handicap\":0.0},{\"selectionId\":4,\"runnerName\":\"0 - 1\",\"handicap\":0.0},{\"selectionId\":9,\"runnerName\":\"0 - 2\",\"handicap\":0.0},{\"selectionId\":16,\"runnerName\":\"0 - 3\",\"handicap\":0.0},{\"selectionId\":2,\"runnerName\":\"1 - 0\",\"handicap\":0.0},{\"selectionId\":3,\"runnerName\":\"1 - 1\",\"handicap\":0.0},{\"selectionId\":8,\"runnerName\":\"1 - 2\",\"handicap\":0.0},{\"selectionId\":15,\"runnerName\":\"1 - 3\",\"handicap\":0.0},{\"selectionId\":5,\"runnerName\":\"2 - 0\",\"handicap\":0.0},{\"selectionId\":6,\"runnerName\":\"2 - 1\",\"handicap\":0.0},{\"selectionId\":7,\"runnerName\":\"2 - 2\",\"handicap\":0.0},{\"selectionId\":14,\"runnerName\":\"2 - 3\",\"handicap\":0.0},{\"selectionId\":10,\"runnerName\":\"3 - 0\",\"handicap\":0.0},{\"selectionId\":11,\"runnerName\":\"3 - 1\",\"handicap\":0.0},{\"selectionId\":12,\"runnerName\":\"3 - 2\",\"handicap\":0.0},{\"selectionId\":13,\"runnerName\":\"3 - 3\",\"handicap\":0.0},{\"selectionId\":4506345,\"runnerName\":\"Any Unquoted \",\"handicap\":0.0}],\"competition\":{\"id\":\"31\",\"name\":\"Barclays Premier League\"},\"event\":{\"id\":\"27249641\",\"name\":\"Man City v Liverpool\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"Aug 25, 2014 8:00:00 PM\",\"market\":{}},\"marketBook\":{\"marketId\":\"1.115036530\",\"isMarketDataDelayed\":false,\"status\":\"OPEN\",\"betDelay\":5,\"bspReconciled\":false,\"complete\":true,\"inplay\":true,\"numberOfWinners\":1,\"numberOfRunners\":17,\"numberOfActiveRunners\":17,\"lastMatchTime\":\"Aug 25, 2014 9:06:50 PM\",\"totalMatched\":1056150.66,\"totalAvailable\":55756.48,\"crossMatching\":true,\"runnersVoidable\":false,\"version\":803963684,\"runners\":[{\"selectionId\":1,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":1000.0,\"totalMatched\":212854.34,\"ex\":{\"availableToBack\":[{\"price\":1000.0,\"size\":57.93},{\"price\":200.0,\"size\":15.0},{\"price\":100.0,\"size\":2.0}],\"availableToLay\":[],\"tradedVolume\":[]}},{\"selectionId\":4,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":1000.0,\"totalMatched\":45845.25,\"ex\":{\"availableToBack\":[{\"price\":1000.0,\"size\":33.57},{\"price\":200.0,\"size\":15.0},{\"price\":100.0,\"size\":2.0}],\"availableToLay\":[],\"tradedVolume\":[]}},{\"selectionId\":9,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":1000.0,\"totalMatched\":19937.85,\"ex\":{\"availableToBack\":[{\"price\":1000.0,\"size\":49.13},{\"price\":200.0,\"size\":15.0},{\"price\":100.0,\"size\":2.0}],\"availableToLay\":[],\"tradedVolume\":[]}},{\"selectionId\":16,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":1000.0,\"totalMatched\":7411.19,\"ex\":{\"availableToBack\":[{\"price\":1000.0,\"size\":18.23},{\"price\":200.0,\"size\":15.0},{\"price\":100.0,\"size\":2.0}],\"availableToLay\":[],\"tradedVolume\":[]}},{\"selectionId\":2,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":4.7,\"totalMatched\":138170.61,\"ex\":{\"availableToBack\":[{\"price\":4.7,\"size\":257.09},{\"price\":4.6,\"size\":909.9},{\"price\":4.5,\"size\":385.42}],\"availableToLay\":[{\"price\":4.8,\"size\":39.93},{\"price\":4.9,\"size\":1151.73},{\"price\":5.0,\"size\":550.18}],\"tradedVolume\":[]}},{\"selectionId\":3,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":7.0,\"totalMatched\":113601.26,\"ex\":{\"availableToBack\":[{\"price\":7.0,\"size\":604.74},{\"price\":6.8,\"size\":835.19},{\"price\":6.6,\"size\":202.35}],\"availableToLay\":[{\"price\":7.2,\"size\":127.7},{\"price\":7.4,\"size\":18.35},{\"price\":7.6,\"size\":518.28}],\"tradedVolume\":[]}},{\"selectionId\":8,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":19.0,\"totalMatched\":44039.61,\"ex\":{\"availableToBack\":[{\"price\":19.0,\"size\":110.86},{\"price\":18.5,\"size\":25.54},{\"price\":18.0,\"size\":6.36}],\"availableToLay\":[{\"price\":19.5,\"size\":2.0},{\"price\":20.0,\"size\":13.97},{\"price\":21.0,\"size\":21.54}],\"tradedVolume\":[]}},{\"selectionId\":15,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":95.0,\"totalMatched\":14892.05,\"ex\":{\"availableToBack\":[{\"price\":90.0,\"size\":15.83},{\"price\":75.0,\"size\":17.9},{\"price\":70.0,\"size\":17.29}],\"availableToLay\":[{\"price\":95.0,\"size\":27.41},{\"price\":100.0,\"size\":17.13},{\"price\":120.0,\"size\":3.4}],\"tradedVolume\":[]}},{\"selectionId\":5,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":5.0,\"totalMatched\":90201.23,\"ex\":{\"availableToBack\":[{\"price\":4.9,\"size\":876.8},{\"price\":4.8,\"size\":79.79},{\"price\":4.7,\"size\":98.86}],\"availableToLay\":[{\"price\":5.0,\"size\":94.87},{\"price\":5.1,\"size\":156.02},{\"price\":5.2,\"size\":303.75}],\"tradedVolume\":[]}},{\"selectionId\":6,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":7.8,\"totalMatched\":95347.86,\"ex\":{\"availableToBack\":[{\"price\":7.8,\"size\":5.22},{\"price\":7.6,\"size\":928.66},{\"price\":7.4,\"size\":227.76}],\"availableToLay\":[{\"price\":8.0,\"size\":43.24},{\"price\":8.2,\"size\":44.78},{\"price\":8.4,\"size\":39.78}],\"tradedVolume\":[]}},{\"selectionId\":7,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":27.0,\"totalMatched\":66994.13,\"ex\":{\"availableToBack\":[{\"price\":25.0,\"size\":4.26},{\"price\":24.0,\"size\":105.72},{\"price\":23.0,\"size\":183.28}],\"availableToLay\":[{\"price\":27.0,\"size\":33.44},{\"price\":28.0,\"size\":4.02},{\"price\":30.0,\"size\":148.32}],\"tradedVolume\":[]}},{\"selectionId\":14,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":150.0,\"totalMatched\":13540.28,\"ex\":{\"availableToBack\":[{\"price\":120.0,\"size\":17.56},{\"price\":110.0,\"size\":5.36},{\"price\":100.0,\"size\":28.87}],\"availableToLay\":[{\"price\":150.0,\"size\":18.77},{\"price\":190.0,\"size\":2.0}],\"tradedVolume\":[]}},{\"selectionId\":10,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":13.0,\"totalMatched\":40802.35,\"ex\":{\"availableToBack\":[{\"price\":11.5,\"size\":5.0},{\"price\":11.0,\"size\":133.26},{\"price\":10.5,\"size\":502.93}],\"availableToLay\":[{\"price\":12.0,\"size\":3.2},{\"price\":13.0,\"size\":552.01},{\"price\":14.0,\"size\":20.0}],\"tradedVolume\":[]}},{\"selectionId\":11,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":17.5,\"totalMatched\":48827.7,\"ex\":{\"availableToBack\":[{\"price\":17.0,\"size\":14.47},{\"price\":16.5,\"size\":269.45},{\"price\":16.0,\"size\":150.25}],\"availableToLay\":[{\"price\":19.0,\"size\":6.0},{\"price\":20.0,\"size\":93.94},{\"price\":21.0,\"size\":148.9}],\"tradedVolume\":[]}},{\"selectionId\":12,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":60.0,\"totalMatched\":23953.79,\"ex\":{\"availableToBack\":[{\"price\":55.0,\"size\":10.22},{\"price\":50.0,\"size\":33.59},{\"price\":48.0,\"size\":25.38}],\"availableToLay\":[{\"price\":65.0,\"size\":63.0},{\"price\":95.0,\"size\":3.0}],\"tradedVolume\":[]}},{\"selectionId\":13,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":310.0,\"totalMatched\":11802.6,\"ex\":{\"availableToBack\":[{\"price\":300.0,\"size\":2.0},{\"price\":290.0,\"size\":9.77},{\"price\":250.0,\"size\":3.59}],\"availableToLay\":[{\"price\":440.0,\"size\":2.65},{\"price\":450.0,\"size\":26.28},{\"price\":970.0,\"size\":3.04}],\"tradedVolume\":[]}},{\"selectionId\":4506345,\"handicap\":0.0,\"status\":\"ACTIVE\",\"lastPriceTraded\":17.0,\"totalMatched\":67928.48,\"ex\":{\"availableToBack\":[{\"price\":17.0,\"size\":105.6},{\"price\":15.5,\"size\":101.05},{\"price\":15.0,\"size\":243.2}],\"availableToLay\":[{\"price\":17.5,\"size\":19.05},{\"price\":18.0,\"size\":17.1},{\"price\":19.0,\"size\":2.1}],\"tradedVolume\":[]}}]}}}}";
    Gson gson = new Gson();
    private Logger logger = LoggerFactory.getLogger(ExposureTest.class);

    @Test
    public void exposureTest() throws Exception {
        Event event = gson.fromJson(jsonEvent, Event.class);
        OverUnderMarket ouc = new OverUnderMarket(event.getMarket().get(MarketType.OVER_UNDER_25));

        MarketCatalogue mc = event.getMarket().get(MarketType.OVER_UNDER_25);
        MarketBook mb = mc.getMarketBook();

        Runner r = ouc.getUnderRunner();

        Exposure exposure = new Exposure(event, mc);

        PriceSize priceSize = new PriceSize();
        priceSize.setPrice(4.1d);
        priceSize.setSize(2d);

        Bet placedBet = new Bet();
        placedBet.setPriceSize(priceSize);
        placedBet.setSide(Side.BACK);
        placedBet.setMarketId(mb.getMarketId());
        placedBet.setSelectionId(r.getSelectionId());

//
//        Bet cob = .calcCashOutBet(placedBet, 10d);
//        assertEquals(3.7d, cob.getPriceSize().getPrice(), 0);
//        assertEquals(2.21d, cob.getPriceSize().getSize(), 0);
//        assertEquals(Side.LAY, cob.getSide());
//        assertEquals(mc.getMarketId(), cob.getMarketId());
//        assertEquals(r.getSelectionId(), cob.getSelectionId());
    }

    @Test
    public void roundingTest() {
        Double val = 1.9090909090d;
        val = 21.4823;

        logger.info(roundDownToNearestFraction(val, 0.01).toString());
        logger.info(roundDownToNearestFraction(val, 0.02).toString());
        logger.info(roundDownToNearestFraction(val, 0.05).toString());
        logger.info(roundDownToNearestFraction(val, 0.1).toString());
        logger.info(roundDownToNearestFraction(val, 0.2).toString());
        logger.info(roundDownToNearestFraction(val, 0.5).toString());
        logger.info(roundDownToNearestFraction(val, 1.0).toString());
        logger.info(roundDownToNearestFraction(val, 2.0).toString());

        logger.info("");

        logger.info(roundUpToNearestFraction(val, 0.01).toString());
        logger.info(roundUpToNearestFraction(val, 0.02).toString());
        logger.info(roundUpToNearestFraction(val, 0.05).toString());
        logger.info(roundUpToNearestFraction(val, 0.1).toString());
        logger.info(roundUpToNearestFraction(val, 0.2).toString());
        logger.info(roundUpToNearestFraction(val, 0.5).toString());
        logger.info(roundUpToNearestFraction(val, 1.0).toString());
        logger.info(roundUpToNearestFraction(val, 2.0).toString());
    }

    @Test
    public void layTheDrawTest() throws Exception {
        Event event = new Event();
        event.setName("Test Event");
        MarketCatalogue marketCatalogue = new MarketCatalogue();
        marketCatalogue.setMarketName(MarketType.MATCH_ODDS.getMarketName());
        MarketBook marketBook = new MarketBook();
        Runner home = createRunner(1l);
        Runner away = createRunner(2l);
        Runner draw = createRunner(3l);

        draw.setOrders(getOrder(2.0, 3.55, Side.LAY));
        home.setOrders(getOrder(5.51, 1.29, Side.LAY));

        List<Runner> runners = new ArrayList<Runner>(Arrays.asList(home, away, draw));
        List<RunnerCatalog> runnerCatalogs = new ArrayList<RunnerCatalog>(Arrays.asList(
                createRunnerCatalog(1l, "Home"),
                createRunnerCatalog(2l, "Away"),
                createRunnerCatalog(3l, "Draw")));
        marketCatalogue.setRunners(runnerCatalogs);
        marketBook.setRunners(runners);
        marketCatalogue.setMarketBook(marketBook);
        event.getMarket().put(MarketType.MATCH_ODDS, marketCatalogue);
        Exposure exposure = new Exposure(event, event.getMarket().get(MarketType.MATCH_ODDS));
        logger.info("Exposure: {}", exposure.calcWorstCastMatchOddsExposure().toString());
        logger.info("Exposure: {}", exposure.calcBestCastMatchOddsExposure().toString());
    }

    private Runner createRunner(long selectionId) {
        Runner runner = new Runner();
        runner.setSelectionId(selectionId);
        return runner;
    }

    private List<Order> getOrder(double sizeMatched, double price, Side lay) {
        Order drawOrder = new Order();
        drawOrder.setSizeMatched(sizeMatched);
        drawOrder.setSizeRemaining(0d);
        drawOrder.setSide(lay);
        drawOrder.setPrice(price);
        return Arrays.asList(drawOrder);
    }


    private RunnerCatalog createRunnerCatalog(long selectionId, String name) {
        RunnerCatalog runnerCatalog = new RunnerCatalog();
        runnerCatalog.setSelectionId(selectionId);
        runnerCatalog.setRunnerName(name);
        return runnerCatalog;
    }

    private Double calcPercentageProfit(Event event, MarketCatalogue marketCatalogue, MatchOddsMarket mom) throws Exception {
        Exposure exposure = new Exposure(event, marketCatalogue);
        Double layExposure = exposure.calcNetLtdExposure(true);

        Double cashOutStake = calcBackRunnerCashOutBetSize(mom, layExposure);
        Double initialStake = 2.0;

        Double profit = cashOutStake + initialStake - layExposure;

        return (profit / initialStake) * 100;
    }

    private double calcBackRunnerCashOutBetSize(MatchOddsMarket mom, Double netExposure) throws Exception {
        Runner runner = mom.getDrawRunner();
        return roundUpToNearestFraction(netExposure / mom.getPrice(runner, 0, Side.BACK).getPrice(), 0.01);
    }

    private Double roundDownToNearestFraction(Double number, Double fractionAsDecimal) {
        Double factor = 1 / fractionAsDecimal;
        return Math.round((number - (fractionAsDecimal / 2)) * factor) / factor;
    }

    private Double roundUpToNearestFraction(Double number, Double fractionAsDecimal) {
        Double factor = 1 / fractionAsDecimal;
        return Math.round((number + (fractionAsDecimal / 2)) * factor) / factor;
    }
}
