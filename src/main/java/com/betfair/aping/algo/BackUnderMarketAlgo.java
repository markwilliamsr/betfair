package com.betfair.aping.algo;

import com.betfair.aping.ApiNGDemo;
import com.betfair.aping.BetPlacer;
import com.betfair.aping.com.betfair.aping.events.betting.Exposure;
import com.betfair.aping.com.betfair.aping.events.betting.OverUnderMarket;
import com.betfair.aping.com.betfair.aping.events.betting.PriceIncrement;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.MarketStatus;
import com.betfair.aping.enums.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BackUnderMarketAlgo extends MarketAlgo implements IMarketAlgo {
    private static final String ALGO_TYPE = "OUM";
    protected Logger logger = LoggerFactory.getLogger(BackUnderMarketAlgo.class);


    public Bet calcCashOutBet(Bet placedBet, Integer ticks) throws Exception {
        Bet cashOutBet = new Bet();

        Double cashOutBetPrice = placedBet.getPriceSize().getPrice();
        Double tickSize = PriceIncrement.getIncrement(cashOutBetPrice);
        cashOutBetPrice = cashOutBetPrice - (ticks * tickSize);
        Double newTick = PriceIncrement.getIncrement(cashOutBetPrice);
        cashOutBetPrice = roundDownToNearestFraction(cashOutBetPrice, newTick);

        PriceSize priceSize = new PriceSize();
        priceSize.setPrice(cashOutBetPrice);
        cashOutBet.setPriceSize(priceSize);
        cashOutBet.setSide(Side.LAY);

        Double totalExposure = placedBet.getPriceSize().getPrice() * placedBet.getPriceSize().getSize();

        Double cashOutBetSize = totalExposure / cashOutBetPrice;
        cashOutBetSize = roundUpToNearestFraction(cashOutBetSize, 0.01);

        cashOutBet.getPriceSize().setSize(cashOutBetSize);

        cashOutBet.setSelectionId(placedBet.getSelectionId());
        cashOutBet.setMarketId(placedBet.getMarketId());

        return cashOutBet;
    }

    @Override
    public void process(Event event) throws Exception {
        BetPlacer betPlacer = new BetPlacer();
        MarketCatalogue marketCatalogue = new MarketCatalogue();

        updateEventScore(event);
        logEventName(event);

        try {
            if (event.getPreviousScores().size() == MAX_PREV_SCORES) {
                marketCatalogue = getMarketCatalogueForTotalGoals(event);

                if (marketCatalogue != null) {
                    if (isCandidateMarket(event)) {
                        logger.info("OPEN: Candidate Mkt Found: " + marketCatalogue.getMarketName() + " " + gson.toJson(event));
                        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
                        Runner runner = oum.getUnderRunner();

                        Bet initialBet = getBetForMarket(marketCatalogue, runner, Side.BACK);
                        initialBet.getPriceSize().setSize(getSize());
                        Bet cashOutBet = calcCashOutBet(initialBet, getCashOutTickNumber());
                        List<Bet> bets = new ArrayList<Bet>();
                        bets.add(initialBet);
                        bets.add(cashOutBet);
                        if (isSafetyOff()) {
                            logger.info("{}, {}, OPEN: Candidate Mkt Found. Placing Initial Bet: {}, CashOut Bet: {}", event.getName(), marketCatalogue.getMarketName(), initialBet.toString(), cashOutBet.toString());
                            betPlacer.placeBets(bets);
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            logger.info("Could not determine the MarketType. Exception: " + e.toString());
        }
    }

    private boolean isCandidateMarket(Event event) throws Exception {
        MarketCatalogue marketCatalogue = getMarketCatalogueForTotalGoals(event);

        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
        Runner runner = oum.getUnderRunner();

        if (!marketCatalogue.getMarketBook().getStatus().equals(MarketStatus.OPEN)) {
            logger.info("Market is not OPEN");
            return false;
        }

        if (!isMarketStartingSoon(event)) {
            logger.info("Market is not starting soon enough");
            return false;
        }

        if (isBetAlreadyOpen(marketCatalogue, event)) {
            logger.info("Bet already open in the Market");
            return false;
        }

        if (event.getScore().getTotalGoals() >= getTotalGoalLimit()) {
            //don't bet on some goalfest
            logger.info("Too many goals already scored: " + event.getScore().getTotalGoals());
            return false;
        }

        try {
            if (!isBestBackPriceWithinBounds(oum, runner)) {
                logger.info("Back Price not within Bounds");
                return false;
            }
        } catch (RuntimeException ex) {
            logger.info(ex.toString());
            return false;
        }

        try {
            if (!isBackLaySpreadWithinBounds(event, oum, runner)) {
                logger.info("Back Lay Spread not within bounds");
                return false;
            }
        } catch (RuntimeException ex) {
            logger.info(ex.toString());
            return false;
        }

        return true;
    }

    private boolean isBestBackPriceWithinBounds(OverUnderMarket oum, Runner runner) {
        if (oum.getBack(runner, 0).getPrice() >= getOverUnderBackLimit()) {
            logger.info("Best Back Price: " + oum.getUnderRunnerName() + " : " + oum.getBack(runner, 0).toString());
            return true;
        }
        logger.info("Best Back Price: " + oum.getUnderRunnerName() + " : " + oum.getBack(runner, 0).toString());
        return false;
    }

    private MarketCatalogue getMarketCatalogueForTotalGoals(Event event) {
        Integer totalGoalsForMarket = event.getScore().getTotalGoals() + getSafetyGoalMargin();
        MarketType marketType = MarketType.fromTotalGoals(totalGoalsForMarket);

        return event.getMarket().get(marketType);
    }

    private boolean isBetAlreadyOpen(MarketCatalogue marketCatalogue, Event event) throws Exception {
        Exposure exposure = new Exposure(event, marketCatalogue);
        if (exposure.calcNetExposure() > 0.1) {
            //already bet on this market
            return true;
        }
        return false;
    }

    @Override
    protected String getAlgoType() {
        return ALGO_TYPE;
    }

    private Double getOverUnderBackLimit() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty(getAlgoType() + "_OVER_UNDER_BACK_LIMIT"));
    }

    private Integer getSafetyGoalMargin() {
        return Integer.valueOf(ApiNGDemo.getProp().getProperty(getAlgoType() + "_SAFETY_GOAL_MARGIN", "2"));
    }

    private Integer getCashOutTickNumber() {
        return Integer.valueOf(ApiNGDemo.getProp().getProperty(getAlgoType() + "_CASH_OUT_TICK_NUMBER"));
    }
}