package com.betfair.aping.util;

import com.betfair.aping.ApiNGDemo;
import com.betfair.aping.BetPlacer;
import com.betfair.aping.MarketAlgo;
import com.betfair.aping.com.betfair.aping.events.betting.*;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.MarketStatus;
import com.betfair.aping.enums.Side;
import com.betfair.aping.exceptions.APINGException;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by markwilliams on 12/14/14.
 */
public class LayAndCoverAlgo implements MarketAlgo {
    private Logger logger = LoggerFactory.getLogger(LayAndCoverAlgo.class);
    private Gson gson = new Gson();
    private int MAX_PREV_SCORES = 6;

    private static double getSize() {
        try {
            return new Double((String) ApiNGDemo.getProp().get("LNC_BET_SIZE"));
        } catch (NumberFormatException e) {
            //returning the default value
            return new Double(0.01);
        }
    }

    @Override
    public void process(Event event) throws Exception, APINGException {
        BetPlacer betPlacer = new BetPlacer();
        MarketCatalogue mc = new MarketCatalogue();

        updateEventScore(event);
        logger.info(event.getName() + ": Starts At: [" + event.getOpenDate() + "], Current Score: " + event.getScore() + ", Previous Score: " + event.getPreviousScores().toString());

        try {
            if (event.getPreviousScores().size() == MAX_PREV_SCORES) {
                mc = getMarketCatalogueForTotalGoals(event, 1);

                if (mc != null) {
                    if (isCandidateLayMarket(event)) {
                        logger.info("OPEN: Candidate Mkt Found: " + mc.getMarketName() + " " + gson.toJson(event));
                        OverUnderMarket oum = new OverUnderMarket(mc);
                        Runner runner = oum.getUnderRunner();

                        Bet initialBet = getBetForMarket(mc, runner, Side.LAY);
                        initialBet.getPriceSize().setSize(getSize());
                        List<Bet> initialLayBet = new ArrayList<Bet>();
                        initialLayBet.add(initialBet);
                        if (isSafetyOff()) {
                            betPlacer.placeBets(initialLayBet);
                        }
                    }
                }
                mc = getMarketCatalogueForTotalGoals(event, 0);
                if (mc != null) {
                    if (isCandidateCoverMarket(event)) {
                        Exposure exposure = new Exposure(mc);
                        OverUnderMarket oum = new OverUnderMarket(mc);
                        Runner runner = oum.getOverRunner();
                        Bet cashOutBet = getBetForMarket(mc, runner, Side.LAY);

                        Double cashOutBetSize = calcCashOutBetSize(oum, Math.abs(exposure.calcNetExposure(true)));
                        cashOutBet.getPriceSize().setSize(cashOutBetSize);

                        List<Bet> coverBet = new ArrayList<Bet>();
                        coverBet.add(cashOutBet);
                        if (isSafetyOff()) {
                            betPlacer.placeBets(coverBet);
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            logger.info("Could not determine the MarketType. Exception: " + e.toString());
        }
    }

    private double calcCashOutBetSize(OverUnderMarket oum, Double netExposure) throws Exception {
        Runner runner = oum.getOverRunner();
        return roundUpToNearestFraction(netExposure / oum.getPrice(runner, 0, Side.LAY).getPrice(), 0.01);
    }

    private void updateEventScore(Event event) {
        boolean updateScore = false;
        boolean processUnquoted = false;
        try {
            Score score = new Score(event);
            ScoreEnum currentScore = score.findScoreFromMarketOdds();

            if (event.getPreviousScores().size() == MAX_PREV_SCORES) {
                for (int i = 0; i < MAX_PREV_SCORES - 1; i++) {
                    //shuffle them down one
                    event.getPreviousScores().set(i, event.getPreviousScores().get(i + 1));
                }

                if (currentScore.equals(ScoreEnum.ANY_UNQUOTED)) {
                    int goalsFromPrevScore = event.getPreviousScores().get(MAX_PREV_SCORES - 1).getTotalGoals();
                    if ((goalsFromPrevScore + 1) == ScoreEnum.ANY_UNQUOTED.getTotalGoals()) {
                        processUnquoted = true;
                    }
                } else {
                    event.getPreviousScores().remove(MAX_PREV_SCORES - 1);
                }
            }

            if (!currentScore.equals(ScoreEnum.ANY_UNQUOTED) || processUnquoted) {
                event.getPreviousScores().add(currentScore);
            }

            if (event.getPreviousScores().size() == MAX_PREV_SCORES) {
                ScoreEnum firstScore = event.getPreviousScores().get(0);
                updateScore = true;
                for (int i = 1; i < MAX_PREV_SCORES; i++) {
                    ScoreEnum loopScore = event.getPreviousScores().get(i);
                    if (!loopScore.equals(firstScore)) {
                        updateScore = false;
                        break;
                    }
                }
                if (updateScore) {
                    event.setScore(firstScore);
                }
            }
        } catch (NullPointerException e) {
            event.setScore(ScoreEnum.ANY_UNQUOTED);
        }
    }

    private Bet getBetForMarket(MarketCatalogue marketCatalogue, Runner runner, Side side) {
        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
        Bet bet = new Bet();
        PriceSize priceSize = new PriceSize();

        priceSize.setPrice(oum.getPrice(runner, 0, side).getPrice());

        bet.setMarketId(marketCatalogue.getMarketId());
        bet.setPriceSize(priceSize);
        bet.setSide(side);
        bet.setSelectionId(runner.getSelectionId());

        return bet;
    }

    private boolean isCandidateLayMarket(Event event) throws Exception {
        MarketCatalogue marketCatalogue = getMarketCatalogueForTotalGoals(event, 1);

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

        if (!isMarketStartedTooLongAgo(event)) {
            logger.info("Market started too long ago");
            return false;
        }

        if (isBetAlreadyOpen(marketCatalogue, oum)) {
            logger.info("Bet already open in the Market");
            return false;
        }

        if (isScoreChanging(event)) {
            logger.info("Score is currently changing. Previous scores: " + event.getPreviousScores());
            return false;
        }

        if (event.getScore().getTotalGoals() > getTotalGoalLimit()) {
            //don't bet on some goalfest
            logger.info("Too many goals already scored: " + event.getScore().getTotalGoals() + ". Limit is " + getTotalGoalLimit());
            return false;
        }

        try {
            if (!isBestOpeningLayPriceWithinBounds(oum, runner)) {
                logger.info("Lay Price not within Bounds");
                return false;
            }
        } catch (RuntimeException ex) {
            logger.info(ex.toString());
            return false;
        }

        try {
            if (!isBackLaySpreadWithinBounds(oum, runner)) {
                logger.info("Back Lay Spread not within bounds");
                return false;
            }
        } catch (RuntimeException ex) {
            logger.info(ex.toString());
            return false;
        }

        return true;
    }

    private boolean isScoreChanging(Event event) {
        if (event.getPreviousScores().size() == MAX_PREV_SCORES) {
            ScoreEnum firstScore = event.getPreviousScores().get(0);
            for (ScoreEnum score : event.getPreviousScores()) {
                if (!score.equals(firstScore)){
                    return true;
                }
            }
        } else {
            return true;
        }
        return false;
    }

    private boolean isCandidateCoverMarket(Event event) throws Exception {
        MarketCatalogue marketCatalogue = getMarketCatalogueForTotalGoals(event, 0);

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

        if (!isBetAlreadyOpen(marketCatalogue, oum)) {
            logger.info("No Lay Bets already open in the Market");
            return false;
        }

        try {
            if (!isBestCoveringLayPriceWithinBounds(marketCatalogue, oum)) {
                logger.info("Best Covering Lay Price not within Bounds");
                return false;
            }
        } catch (RuntimeException ex) {
            logger.info(ex.toString());
            return false;
        }

        try {
            if (!isBackLaySpreadWithinBounds(oum, runner)) {
                logger.info("Back Lay Spread not within bounds");
                return false;
            }
        } catch (RuntimeException ex) {
            logger.info(ex.toString());
            return false;
        }

        return true;
    }

    private boolean isBackLaySpreadWithinBounds(OverUnderMarket oum, Runner runner) {
        Double back = oum.getBack(runner, 0).getPrice();
        Double lay = oum.getLay(runner, 0).getPrice();

        if (back != null && lay != null) {
            Double increment = PriceIncrement.getIncrement(back);
            Long spread = Math.round((lay - back) / increment);
            if (spread <= getMaxBackLaySpread()) {
                return true;
            }
        }
        return false;
    }

    private boolean isBestOpeningLayPriceWithinBounds(OverUnderMarket oum, Runner runner) {
        if (oum.getLay(runner, 0).getPrice() <= getOverUnderLayLimit()) {
            logger.info("Best Lay Price: " + oum.getUnderRunnerName() + " : " + oum.getLay(runner, 0).toString());
            return true;
        }
        logger.info("Best Lay Price: " + oum.getUnderRunnerName() + " : " + oum.getLay(runner, 0).toString());
        return false;
    }

    private boolean isBestCoveringLayPriceWithinBounds(MarketCatalogue marketCatalogue, OverUnderMarket oum) throws Exception {
        Exposure exposure = new Exposure(marketCatalogue);
        Double layExposure = exposure.calcNetExposure(true);

        Double cashOutStake = calcCashOutBetSize(oum, layExposure);
        Double initialStake = getSize();

        Double profit = cashOutStake + initialStake - layExposure;

        Double profitPercentage = (profit / initialStake) * 100;

        if (profitPercentage >= getCashOutProfitPercentage()) {
            logger.info("Best Lay Price: " + oum.getOverRunnerName() + " : " + oum.getLay(oum.getOverRunner(), 0).toString() + ". Profit Percentage: " + roundUpToNearestFraction(profitPercentage, 2d));
            return true;
        }

        logger.info("Best Lay Price: " + oum.getOverRunnerName() + " : " + oum.getLay(oum.getOverRunner(), 0).toString() + ". Profit Percentage: " + roundUpToNearestFraction(profitPercentage, 2d));
        return false;
    }

    private MarketCatalogue getMarketCatalogueForTotalGoals(Event event, Integer offset) {
        //Just need the next Mkt up from where we're already at
        Integer totalGoalsForMarket = event.getScore().getTotalGoals() + offset;
        MarketType marketType = MarketType.fromTotalGoals(totalGoalsForMarket);

        if (marketType.equals(MarketType.OVER_UNDER_05)) {
            logger.info("Request for " + marketType + ". Returning null.");
        }

        return event.getMarket().get(marketType);
    }

    private boolean isBetAlreadyOpen(MarketCatalogue marketCatalogue, OverUnderMarket oum) throws Exception {
        Exposure exposure = new Exposure(marketCatalogue);
        if (exposure.calcNetExposure(true) > 0.1) {
            //already bet on this market
            return true;
        }
        return false;
    }

    private boolean isMarketStartingSoon(Event event) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, getMinutesBeforeMarketStartTimeToBet());
        if (!isMarketStartTimeLimitOn()) {
            return true;
        }
        if (event.getOpenDate().before(calendar.getTime())) {
            //bet on something that is starting in the next few mins
            return true;
        }
        return false;
    }

    private boolean isMarketStartedTooLongAgo(Event event) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1 * getMinutesAfterMarketStartTimeToBet());
        if (!isMarketStartTimeLimitOn()) {
            return true;
        }
        if (event.getOpenDate().after(calendar.getTime())) {
            //bet on something that has been in  play for only up to the required time.
            //need to leave some time for goals to be scored :)
            return true;
        }
        return false;
    }

    private Double getOverUnderLayLimit() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("LNC_OVER_UNDER_LAY_LIMIT"));
    }

    private Integer getMinutesBeforeMarketStartTimeToBet() {
        return Integer.valueOf(ApiNGDemo.getProp().getProperty("LNC_MINUTES_BEFORE_MARKET_START"));
    }

    private Integer getMinutesAfterMarketStartTimeToBet() {
        return Integer.valueOf(ApiNGDemo.getProp().getProperty("LNC_MINUTES_AFTER_MARKET_START"));
    }

    private Integer getTotalGoalLimit() {
        return Integer.valueOf(ApiNGDemo.getProp().getProperty("LNC_TOTAL_GOAL_LIMIT"));
    }

    private Double getCashOutProfitPercentage() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("LNC_CLOSE_OUT_PROFIT_PERCENTAGE"));
    }

    private Integer getSafetyGoalMargin() {
        return Integer.valueOf(ApiNGDemo.getProp().getProperty("SAFETY_GOAL_MARGIN", "2"));
    }

    private Integer getMaxBackLaySpread() {
        return Integer.valueOf(ApiNGDemo.getProp().getProperty("MAX_BACK_LAY_SPREAD_INCREMENT"));
    }

    private Boolean isMarketStartTimeLimitOn() {
        return Boolean.valueOf(ApiNGDemo.getProp().getProperty("MARKET_START_TIME_LIMIT_ON", "true"));
    }

    private Boolean isSafetyOff() {
        return Boolean.valueOf(ApiNGDemo.getProp().getProperty("LNC_SAFETY_OFF", "false"));
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