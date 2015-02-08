package com.betfair.aping.events.betting.overunder;

import com.betfair.aping.ApiNGDemo;
import com.betfair.aping.algo.IMarketAlgo;
import com.betfair.aping.algo.LayTheDrawAlgo;
import com.betfair.aping.com.betfair.aping.events.betting.Exposure;
import com.betfair.aping.com.betfair.aping.events.betting.ScoreEnum;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.MarketStatus;
import com.betfair.aping.enums.Side;
import com.betfair.aping.events.betting.TestBetPlacer;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by markwilliams on 25/08/2014.
 */
public class LayTheDrawTest {
    private static final long NIL_NIL = 0l;
    private static final long ONE_NIL = 1l;
    private static final long NIL_ONE = 2l;
    private static final long ONE_ONE = 3l;
    private final int HOME_INDEX = 0;
    private final int AWAY_INDEX = 1;
    private final int DRAW_INDEX = 2;
    private Logger logger = LoggerFactory.getLogger(LayTheDrawTest.class);

    @Before
    public void setupSession() throws Exception {
        ApiNGDemo.setPropertiesPath("/Users/markwilliams/GitHub/betfair/src/test/resources/test.properties");
        ApiNGDemo.loadProperties();
    }

    @Test
    public void layTheDrawLayTheWinnerTest() throws Exception {
        IMarketAlgo layTheDrawAlgo = new LayTheDrawAlgo();
        TestBetPlacer betPlacer = new TestBetPlacer();
        Calendar calendar = Calendar.getInstance();
        Event event = getBasicEvent(ScoreEnum.NIL_NIL);

        betPlacer.setEvent(event);
        layTheDrawAlgo.setBetPlacer(betPlacer);

        //getMatchOddsRunner(event, DRAW_INDEX).setOrders(getOrder(2.0, 3.55, Side.LAY));
        //getMatchOddsRunner(event, HOME_INDEX).setOrders(getOrder(5.51, 1.29, Side.LAY));

        getMatchOddsRunner(event, HOME_INDEX).setEx(getExchangePrices(1.3, 1.4));
        getMatchOddsRunner(event, AWAY_INDEX).setEx(getExchangePrices(6.3, 6.4));
        getMatchOddsRunner(event, DRAW_INDEX).setEx(getExchangePrices(4.2, 4.3));

        layTheDrawAlgo.process(event);

        getMatchOddsRunner(event, HOME_INDEX).setEx(getExchangePrices(1.2, 1.21));
        getMatchOddsRunner(event, AWAY_INDEX).setEx(getExchangePrices(28, 32));
        getMatchOddsRunner(event, DRAW_INDEX).setEx(getExchangePrices(7.0, 7.6));

        updateScore(event, ScoreEnum.ONE_NIL);
        event.getMarket().get(MarketType.CORRECT_SCORE).getMarketBook().getRunners().get((int) NIL_NIL)
                .getEx().setAvailableToLay(new ArrayList<PriceSize>());
        event.getMarket().get(MarketType.CORRECT_SCORE).getMarketBook().getRunners().get((int) NIL_ONE)
                .getEx().setAvailableToLay(new ArrayList<PriceSize>());

        calendar.add(Calendar.MINUTE, -5);
        event.setOpenDate(calendar.getTime());

        layTheDrawAlgo.process(event);

        getMatchOddsRunner(event, HOME_INDEX).setEx(getExchangePrices(1.3, 1.4));
        getMatchOddsRunner(event, AWAY_INDEX).setEx(getExchangePrices(6.3, 6.4));
        getMatchOddsRunner(event, DRAW_INDEX).setEx(getExchangePrices(2.3, 2.4));

        calendar.add(Calendar.MINUTE, -60);
        event.setOpenDate(calendar.getTime());

        layTheDrawAlgo.process(event);
        layTheDrawAlgo.process(event);
        layTheDrawAlgo.process(event);

        Exposure exposure = new Exposure(event, event.getMarket().get(MarketType.MATCH_ODDS));
        logger.info("Worst Case Exposure: {}", exposure.calcWorstCaseMatchOddsLiability().toString());
        logger.info("Best Case Exposure: {}", exposure.calcBestCaseMatchOddsLiability().toString());
        //Assert.assertEquals(true, Math.abs(exposure.calcWorstCaseMatchOddsLiability()) < 0.1);
        //Assert.assertEquals(true, Math.abs(exposure.calcBestCaseMatchOddsLiability()) > 14.0);
    }

    private void updateScore(Event event, ScoreEnum score) {
        event.setScore(score);
        event.getPreviousScores().clear();
        event.getPreviousScores().addAll(Arrays.asList(score, score));
    }

    private ExchangePrices getExchangePrices(double back, double lay) {
        ExchangePrices homePrices = new ExchangePrices();
        homePrices.setAvailableToBack(getPriceSizes(back, 0d));
        homePrices.setAvailableToLay(getPriceSizes(lay, 100d));
        return homePrices;
    }

    private Runner getMatchOddsRunner(Event event, int runner) {
        return event.getMarket().get(MarketType.MATCH_ODDS).getMarketBook().getRunners()
                .get(runner);
    }

    private Event getBasicEvent(ScoreEnum score) {
        Event event = new Event();
        event.setName("Test Event");
        event.setScore(score);
        event.getPreviousScores().addAll(Arrays.asList(score, score));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -5);
        event.setOpenDate(calendar.getTime());
        MarketCatalogue matchOdds = getMatchOddsMarketCatalogue();
        event.getMarket().put(MarketType.MATCH_ODDS, matchOdds);
        MarketCatalogue correctScore = getCorrectScoreMarketCatalogue();
        event.getMarket().put(MarketType.CORRECT_SCORE, correctScore);
        return event;
    }

    private MarketCatalogue getMatchOddsMarketCatalogue() {
        MarketCatalogue marketCatalogue = new MarketCatalogue();
        marketCatalogue.setMarketName(MarketType.MATCH_ODDS.getMarketName());
        marketCatalogue.setMarketId("MatchOdds");
        MarketBook marketBook = new MarketBook();
        marketBook.setMarketId("MatchOdds");
        setMatchOddsRunners(marketCatalogue, marketBook);
        marketBook.setStatus(MarketStatus.OPEN);
        marketCatalogue.setMarketBook(marketBook);
        return marketCatalogue;
    }

    private MarketCatalogue getCorrectScoreMarketCatalogue() {
        MarketCatalogue marketCatalogue = new MarketCatalogue();
        marketCatalogue.setMarketName(MarketType.CORRECT_SCORE.getMarketName());
        marketCatalogue.setMarketId("CorrectScore");
        MarketBook marketBook = new MarketBook();
        marketBook.setMarketId("CorrectScore");
        setCorrectScoreRunners(marketCatalogue, marketBook);
        marketBook.setStatus(MarketStatus.OPEN);
        marketCatalogue.setMarketBook(marketBook);
        return marketCatalogue;
    }

    private void setMatchOddsRunners(MarketCatalogue marketCatalogue, MarketBook marketBook) {
        Runner home = createRunner(HOME_INDEX);
        Runner away = createRunner(AWAY_INDEX);
        Runner draw = createRunner(DRAW_INDEX);

        List<Runner> runners = new ArrayList<Runner>(Arrays.asList(home, away, draw));
        marketBook.setRunners(runners);

        List<RunnerCatalog> runnerCatalogs = new ArrayList<RunnerCatalog>(Arrays.asList(
                createRunnerCatalog(HOME_INDEX, "Home"),
                createRunnerCatalog(AWAY_INDEX, "Away"),
                createRunnerCatalog(DRAW_INDEX, "Draw")));
        marketCatalogue.setRunners(runnerCatalogs);
    }

    private void setCorrectScoreRunners(MarketCatalogue marketCatalogue, MarketBook marketBook) {
        Runner nilNil = createRunner(NIL_NIL);
        Runner oneNil = createRunner(ONE_NIL);
        Runner nilOne = createRunner(NIL_ONE);
        Runner oneOne = createRunner(ONE_ONE);

        List<Runner> runners = new ArrayList<Runner>();
        runners.add((int) NIL_NIL, nilNil);
        runners.add((int) ONE_NIL, oneNil);
        runners.add((int) NIL_ONE, nilOne);
        runners.add((int) ONE_ONE, oneOne);

        marketBook.setRunners(runners);

        List<RunnerCatalog> runnerCatalogs = new ArrayList<RunnerCatalog>();
        runnerCatalogs.add((int) NIL_NIL, createRunnerCatalog(NIL_NIL, ScoreEnum.NIL_NIL.getScore()));
        runnerCatalogs.add((int) ONE_NIL, createRunnerCatalog(ONE_NIL, ScoreEnum.ONE_NIL.getScore()));
        runnerCatalogs.add((int) NIL_ONE, createRunnerCatalog(NIL_ONE, ScoreEnum.NIL_ONE.getScore()));
        runnerCatalogs.add((int) ONE_ONE, createRunnerCatalog(ONE_ONE, ScoreEnum.ONE_ONE.getScore()));
        marketCatalogue.setRunners(runnerCatalogs);
    }

    private Runner createRunner(long selectionId) {
        Runner runner = new Runner();
        runner.setSelectionId(selectionId);
        runner.setEx(getExchangePrices(1d, 1d));
        return runner;
    }

    private List<Order> getOrder(double sizeMatched, double price, Side side) {
        Order order = new Order();
        order.setSizeMatched(sizeMatched);
        order.setSizeRemaining(0d);
        order.setSide(side);
        order.setPrice(price);
        List<Order> orders = new ArrayList<Order>();
        orders.add(order);
        return orders;
    }

    private List<PriceSize> getPriceSizes(double price, double size) {
        PriceSize priceSize = new PriceSize();
        priceSize.setPrice(price);
        priceSize.setSize(size);
        List<PriceSize> exchangePrices = new ArrayList<PriceSize>();
        exchangePrices.add(priceSize);
        return exchangePrices;
    }

    private RunnerCatalog createRunnerCatalog(long selectionId, String name) {
        RunnerCatalog runnerCatalog = new RunnerCatalog();
        runnerCatalog.setSelectionId(selectionId);
        runnerCatalog.setRunnerName(name);
        return runnerCatalog;
    }
}
