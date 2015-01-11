package com.betfair.aping.algo;

import com.betfair.aping.ApiNGDemo;
import com.betfair.aping.BetPlacer;
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

public class BackUnderMarketAlgo implements MarketAlgo {
    private Logger logger = LoggerFactory.getLogger(BackUnderMarketAlgo.class);
    private Gson gson = new Gson();
    private int MAX_PREV_SCORES = 6;

    private static double getSize() {
        try {
            return new Double((String) ApiNGDemo.getProp().get("BET_SIZE"));
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
                mc = getMarketCatalogueForTotalGoals(event);

                if (mc != null) {
                    if (isCandidateMarket(event)) {
                        logger.info("OPEN: Candidate Mkt Found: " + mc.getMarketName() + " " + gson.toJson(event));
                        Exposure exposure = new Exposure(event, mc);
                        OverUnderMarket oum = new OverUnderMarket(mc);
                        Runner runner = oum.getUnderRunner();

                        Bet initialBet = getBet(mc, runner, Side.BACK);
                        Bet cashOutBet = exposure.calcCashOutBet(initialBet, getCashOutProfitPercentage());
                        List<Bet> bets = new ArrayList<Bet>();
                        bets.add(initialBet);
                        bets.add(cashOutBet);
                        betPlacer.placeBets(bets);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            logger.info("Could not determine the MarketType. Exception: " + e.toString());
        }
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
                for (int i = 1; i < MAX_PREV_SCORES; i++) {
                    ScoreEnum loopScore = event.getPreviousScores().get(i);
                    if (!loopScore.equals(firstScore)) {
                        continue;
                    }
                    updateScore = true;
                }
                if (updateScore) {
                    event.setScore(firstScore);
                }
            }
        } catch (NullPointerException e) {
            event.setScore(ScoreEnum.ANY_UNQUOTED);
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
        bet.setSide(side);
        bet.setSelectionId(runner.getSelectionId());

        return bet;
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

    private Double getOverUnderBackLimit() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("OVER_UNDER_BACK_LIMIT"));
    }

    private Integer getMinutesBeforeMarketStartTimeToBet() {
        return Integer.valueOf(ApiNGDemo.getProp().getProperty("MINUTES_BEFORE_MARKET_START"));
    }

    private Integer getTotalGoalLimit() {
        return Integer.valueOf(ApiNGDemo.getProp().getProperty("TOTAL_GOAL_LIMIT"));
    }

    private Double getCashOutProfitPercentage() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("CLOSE_OUT_PROFIT_PERCENTAGE"));
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
}