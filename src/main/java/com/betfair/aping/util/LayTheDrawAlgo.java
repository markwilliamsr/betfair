package com.betfair.aping.util;

import com.betfair.aping.ApiNGDemo;
import com.betfair.aping.BetPlacer;
import com.betfair.aping.MarketAlgo;
import com.betfair.aping.com.betfair.aping.events.betting.*;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.MarketStatus;
import com.betfair.aping.enums.Side;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by markwilliams on 12/14/14.
 */
public class LayTheDrawAlgo implements MarketAlgo {
    private Logger logger = LoggerFactory.getLogger(LayTheDrawAlgo.class);
    private Gson gson = new Gson();
    private int MAX_PREV_SCORES = getScoreStabalizationIteration();

    private static double getSize() {
        try {
            return new Double((String) ApiNGDemo.getProp().get("LTD_BET_SIZE"));
        } catch (NumberFormatException e) {
            //returning the default value
            return new Double(0.01);
        }
    }

    @Override
    public void process(Event event) throws Exception {
        BetPlacer betPlacer = new BetPlacer();

        updateEventScore(event);
        if (isMarketStartingSoon(event)) {
            logger.info(event.getName() + ": Starts At: [" + event.getOpenDate() + "], Current Score: " + event.getScore() + ", Previous Score: " + event.getPreviousScores().toString());
        } else {
            logger.debug(event.getName() + ": Starts At: [" + event.getOpenDate() + "], Current Score: " + event.getScore() + ", Previous Score: " + event.getPreviousScores().toString());
        }

        try {
            if (event.getPreviousScores().size() == MAX_PREV_SCORES) {

                if (event.getScore().equals(ScoreEnum.NIL_NIL)) {
                    MarketCatalogue marketCatalogue = event.getMarket().get(MarketType.MATCH_ODDS);

                    if (marketCatalogue != null && isCandidateLayMarket(event, marketCatalogue)) {
                        MatchOddsMarket mom = new MatchOddsMarket(marketCatalogue);
                        Runner runner = mom.getDrawRunner();

                        Bet initialBet = getBetForMarket(marketCatalogue, runner, Side.LAY);
                        initialBet.getPriceSize().setSize(getSize());
                        List<Bet> initialLayBet = new ArrayList<Bet>();
                        initialLayBet.add(initialBet);
                        if (isSafetyOff()) {
                            logger.info("{}, {}, OPEN: Candidate Mkt Found. Placing Bet: {}", event.getName(), marketCatalogue.getMarketName(), initialBet.toString());
                            betPlacer.placeBets(initialLayBet);
                        }
                    }
                }

                //Check for Profitable trades
                MarketCatalogue marketCatalogue = event.getMarket().get(MarketType.MATCH_ODDS);

                if (isCandidateCoverMarket(event, marketCatalogue)) {
                    Exposure exposure = new Exposure(event, marketCatalogue);
                    MatchOddsMarket mom = new MatchOddsMarket(marketCatalogue);
                    Runner runner = mom.getDrawRunner();
                    Side side = Side.BACK;

                    Double cashOutBetSize = calcBackRunnerCashOutBetSize(mom, Math.abs(exposure.calcNetLtdExposure(true)));

                    if (cashOutBetSize < getMinimumBetSize()) {
                        logger.warn("{}, {}; HUM. Cash Out Bet Size Less than Minimum Bet.", event.getName(), marketCatalogue.getMarketName(), cashOutBetSize);
                        cashOutBetSize = getMinimumBetSize();
                    }

                    Bet cashOutBet = getBetForMarket(marketCatalogue, runner, side);

                    cashOutBet.getPriceSize().setSize(cashOutBetSize);

                    List<Bet> coverBet = new ArrayList<Bet>();
                    coverBet.add(cashOutBet);
                    if (isSafetyOff()) {
                        logger.info("{}, {}, WIN: Candidate Mkt Found. Placing Bet: {}", event.getName(), marketCatalogue.getMarketName(), cashOutBet.toString());
                        betPlacer.placeBets(coverBet);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            logger.info("Could not determine the MarketType. Exception: " + e.toString());
        }
    }

    private double calcOverRunnerCashOutBetSize(OverUnderMarket oum, Double netExposure) throws Exception {
        Runner runner = oum.getOverRunner();
        return roundUpToNearestFraction(netExposure / oum.getPrice(runner, 0, Side.LAY).getPrice(), 0.01);
    }

    private double calcBackRunnerCashOutBetSize(MatchOddsMarket mom, Double netExposure) throws Exception {
        Runner runner = mom.getDrawRunner();
        return roundUpToNearestFraction(netExposure / mom.getPrice(runner, 0, Side.BACK).getPrice(), 0.01);
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
        MatchOddsMarket mom = new MatchOddsMarket(marketCatalogue);
        Bet bet = new Bet();
        PriceSize priceSize = new PriceSize();

        priceSize.setPrice(mom.getPrice(runner, 0, side).getPrice());

        bet.setMarketId(marketCatalogue.getMarketId());
        bet.setPriceSize(priceSize);
        bet.setSide(side);
        bet.setSelectionId(runner.getSelectionId());

        return bet;
    }

    private boolean isCandidateLayMarket(Event event, MarketCatalogue marketCatalogue) throws Exception {

        if (marketCatalogue == null) {
            return false;
        }

        MatchOddsMarket oum = new MatchOddsMarket(marketCatalogue);
        Runner runner = oum.getDrawRunner();

        if (!marketCatalogue.getMarketBook().getStatus().equals(MarketStatus.OPEN)) {
            logger.info("{}; {}; Market is not OPEN", event.getName(), marketCatalogue.getMarketName());
            return false;
        }

        if (!isMarketStartingSoon(event)) {
            logger.debug("{}; {}; Market is not starting soon enough", event.getName(), marketCatalogue.getMarketName());
            return false;
        }

        if (!isMarketStartedTooLongAgo(event)) {
            logger.debug("{}; {}; Market started too long ago", event.getName(), marketCatalogue.getMarketName());
            return false;
        }

        if (isBetAlreadyOpen(marketCatalogue, event)) {
            return false;
        }

        if (isScoreChanging(event)) {
            logger.info("{}; Score is currently changing. Previous scores: {}", event.getName(), event.getPreviousScores());
            return false;
        }

//        if (event.getScore().getTotalGoals() >= getTotalGoalLimit()) {
//            //don't bet on some goalfest
//            logger.debug("{}; Too many goals already scored: {}. Limit is {}", event.getName(), event.getScore().getTotalGoals(), getTotalGoalLimit());
//            return false;
//        }

        try {
            if (!isBestOpeningLayPriceWithinBounds(event, oum, runner)) {
                return false;
            }
        } catch (RuntimeException ex) {
            logger.error("Error: {}", ex);
            return false;
        }

        try {
            if (!isBackLaySpreadWithinBounds(event, oum, runner)) {
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
                if (!score.equals(firstScore)) {
                    return true;
                }
            }
        } else {
            return true;
        }
        return false;
    }

    private boolean isCandidateCoverMarket(Event event, MarketCatalogue marketCatalogue) throws Exception {
        if (marketCatalogue == null) {
            return false;
        }

        MatchOddsMarket mom = new MatchOddsMarket(marketCatalogue);
        Runner runner = mom.getDrawRunner();

        if (isBasicCoverCandidate(event, marketCatalogue)) return false;

        try {
            if (!isBestCoveringLayPriceWithinBounds(event, marketCatalogue, mom)) {
                return false;
            }
        } catch (RuntimeException ex) {
            logger.info(ex.toString());
            return false;
        }

        try {
            if (!isBackLaySpreadWithinBounds(event, mom, runner)) {
                return false;
            }
        } catch (RuntimeException ex) {
            logger.info(ex.toString());
            return false;
        }

        return true;
    }

//    private boolean isCandidateLosingCoverMarket(Event event, MarketCatalogue marketCatalogue) throws Exception {
//        if (marketCatalogue == null) {
//            return false;
//        }
//
//        MatchOddsMarket mom = new MatchOddsMarket(marketCatalogue);
//        Runner runner = mom.getDrawRunner();
//
//        if (isBasicCoverCandidate(event, marketCatalogue)) {
//            return false;
//        }
//
//        try {
//            if (!isLosingCoverLayPriceWithinBounds(event, marketCatalogue, mom)) {
//                return false;
//            }
//        } catch (RuntimeException ex) {
//            logger.info(ex.toString());
//            return false;
//        }
//
//        try {
//            if (!isBackLaySpreadWithinBounds(event, mom, runner)) {
//                return false;
//            }
//        } catch (RuntimeException ex) {
//            logger.info(ex.toString());
//            return false;
//        }
//
//        return true;
//    }

    private boolean isBasicCoverCandidate(Event event, MarketCatalogue marketCatalogue) throws Exception {
        if (!marketCatalogue.getMarketBook().getStatus().equals(MarketStatus.OPEN)) {
            logger.debug("{}; {}; Market is not OPEN", event.getName(), marketCatalogue.getMarketName());
            return true;
        }

        if (!isMarketStartingSoon(event)) {
            logger.debug("{}; {}; Market is not starting soon enough", event.getName(), marketCatalogue.getMarketName());
            return true;
        }

        if (!isBetAlreadyOpen(marketCatalogue, event)) {
            logger.debug("{}; {}; No Lay Bets already open in the Market", event.getName(), marketCatalogue.getMarketName());
            return true;
        }
        return false;
    }

    private boolean isBackLaySpreadWithinBounds(Event event, MatchOddsMarket mom, Runner runner) {
        Double back = mom.getBack(runner, 0).getPrice();
        Double lay = mom.getLay(runner, 0).getPrice();
        Long spread = 0l;

        if (back != null && lay != null) {
            Double increment = PriceIncrement.getIncrement(back);
            spread = Math.round((lay - back) / increment);
            if (spread <= getMaxBackLaySpread()) {
                return true;
            }
        }
        logger.info("{}, {}; Back Lay Spread not within bounds. Lay: {}, Back: {}, Spread: {}", event.getName(), mom.getMarketType().getMarketName(), lay, back, spread);
        return false;
    }

    private boolean isBestOpeningLayPriceWithinBounds(Event event, MatchOddsMarket mom, Runner runner) {
        if (mom.getLay(runner, 0).getPrice() <= getLayTheDrawLayLimit(mom.getMarketType())) {
            logger.info("{}, {}; Lay Price within bounds. Best Price: {}; Lay Limit: {}", event.getName(), mom.getDrawRunnerName(), mom.getLay(runner, 0).toString(), getLayTheDrawLayLimit(mom.getMarketType()));
            return true;
        }
        logger.info("{}, {}; Lay Price not within bounds. Best Lay Price: {}; Lay Limit: {}", event.getName(), mom.getDrawRunnerName(), mom.getLay(runner, 0).toString(), getLayTheDrawLayLimit(mom.getMarketType()));
        return false;
    }

    private boolean isBestCoveringLayPriceWithinBounds(Event event, MarketCatalogue marketCatalogue, MatchOddsMarket mom) throws Exception {
        Double profitPercentage = calcPercentageProfit(event, marketCatalogue, mom);

        if (profitPercentage >= getCashOutProfitPercentage()) {
            //some kind of profit on the closest market, close it out
            logger.info("{}; {}; Regular Closeout. Best Lay Price: {}, {}, Profit Percentage: {}", event.getName(), mom.getMarketType().getMarketName(),
                    mom.getDrawRunnerName(), mom.getLay(mom.getDrawRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 2d));
            return true;
        }

        logger.info("{}; {}; Covering Lay Not Yet Within Bounds. Best Lay Price: {}, {}, Profit Percentage: {}", event.getName(), mom.getMarketType().getMarketName(),
                mom.getDrawRunnerName(), mom.getLay(mom.getDrawRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 2d));

        return false;
    }

//    private boolean isLosingCoverLayPriceWithinBounds(Event event, MarketCatalogue marketCatalogue, MatchOddsMarket mom) throws Exception {
//        Double profitPercentage = calcPercentageProfit(event, marketCatalogue, mom);
//
//        if (profitPercentage <= getLosingCashOutProfitPercentage()
//                && getTimeSinceMarketStart(event) > getLosingMarketTimeSinceStart()) {
//            //you lose...
//            logger.info("{}; {}; Losing Cover. Under getLosingCashOutProfitPercentage() {}. Lay Price: {}, {}, Profit Percentage: {}", event.getName(), mom.getMarketType().getMarketName(),
//                    getLosingCashOutProfitPercentage(), mom.getUnderRunnerName(), mom.getLay(mom.getUnderRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 2d));
//            return true;
//        }
//
//        if (profitPercentage <= 0
//                && getTimeSinceMarketStart(event) > getLosingMarketFinalCloseOutTime()) {
//            //you lose...
//            logger.info("{}; {}; Losing Cover. After getLosingMarketFinalCloseOutTime() {}. Lay Price: {}, {}, Profit Percentage: {}", event.getName(), mom.getMarketType().getMarketName(),
//                    getLosingMarketFinalCloseOutTime(), mom.getUnderRunnerName(), mom.getLay(mom.getUnderRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 2d));
//            return true;
//        }
//
//        logger.info("{}; {}; Not Yet Closing Losing Cover. Lay Price: {}, {}, Profit Percentage: {}", event.getName(), mom.getMarketType().getMarketName(),
//                mom.getUnderRunnerName(), mom.getLay(mom.getUnderRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 2d));
//
//        return false;
//    }

    private long getTimeSinceMarketStart(Event event) {
        Date eventStart = event.getOpenDate();
        Date now = Calendar.getInstance().getTime();

        long diffMs = now.getTime() - eventStart.getTime();
        long diffSec = diffMs / 1000;
        long min = diffSec / 60;

        return min;
    }

    private Double calcPercentageProfit(Event event, MarketCatalogue marketCatalogue, MatchOddsMarket mom) throws Exception {
        Exposure exposure = new Exposure(event, marketCatalogue);
        Double layExposure = exposure.calcNetLtdExposure(true);

        Double cashOutStake = calcBackRunnerCashOutBetSize(mom, layExposure);
        Double initialStake = getSize();

        Double profit = cashOutStake + initialStake - layExposure;

        return (profit / initialStake) * 100;
    }

    private MarketCatalogue getMarketCatalogueForTotalGoals(Event event, Integer numberOfGoals) {
        MarketType marketType = MarketType.fromTotalGoals(numberOfGoals);

        return event.getMarket().get(marketType);
    }

    private boolean isBetAlreadyOpen(MarketCatalogue marketCatalogue, Event event) throws Exception {
        Exposure exposure = new Exposure(event, marketCatalogue);
        if (exposure.calcNetLtdExposure(true) > 0.1) {
            logger.debug("{}; {}; Bet already open in the Market. Exposure: {}", event.getName(), marketCatalogue.getMarketName(), exposure.calcNetLtdExposure(true));
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

    private Double getLayTheDrawLayLimit(MarketType marketType) {
        Double limits = Double.valueOf(ApiNGDemo.getProp().getProperty("LTD_OVER_UNDER_LAY_LIMIT"));

        return limits;
    }

    private Integer getMinutesBeforeMarketStartTimeToBet() {
        return Integer.valueOf(ApiNGDemo.getProp().getProperty("LTD_MINUTES_BEFORE_MARKET_START"));
    }

    private Integer getMinutesAfterMarketStartTimeToBet() {
        return Integer.valueOf(ApiNGDemo.getProp().getProperty("LTD_MINUTES_AFTER_MARKET_START"));
    }

//    private Integer getTotalGoalLimit() {
//        return Integer.valueOf(ApiNGDemo.getProp().getProperty("LTD_TOTAL_GOAL_LIMIT"));
//    }

    private Double getCashOutProfitPercentage() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("LTD_CLOSE_OUT_PROFIT_PERCENTAGE"));
    }

//    private Double getLosingCashOutProfitPercentage() {
//        return Double.valueOf(ApiNGDemo.getProp().getProperty("LTD_LOSING_CLOSE_OUT_PROFIT_PERCENTAGE"));
//    }
//
//    private Double getLosingMarketTimeSinceStart() {
//        return Double.valueOf(ApiNGDemo.getProp().getProperty("LTD_LOSING_CLOSE_OUT_TIME_SINCE_START"));
//    }
//
//    private Double getSmallWinCloseoutMarketTimeSinceStart() {
//        return Double.valueOf(ApiNGDemo.getProp().getProperty("LTD_SMALL_WIN_CLOSE_OUT_TIME_SINCE_START"));
//    }
//
//    private Double getLosingMarketFinalCloseOutTime() {
//        return Double.valueOf(ApiNGDemo.getProp().getProperty("LTD_LOSING_CLOSE_OUT_FINAL_TIME"));
//    }
//
//    private Double getBestCaseCashOutProfitPercentage() {
//        return Double.valueOf(ApiNGDemo.getProp().getProperty("LTD_BEST_CASE_CLOSE_OUT_PROFIT_PERCENTAGE"));
//    }
//
//    private Integer getSafetyGoalMargin() {
//        return Integer.valueOf(ApiNGDemo.getProp().getProperty("SAFETY_GOAL_MARGIN", "2"));
//    }

    private Integer getMaxBackLaySpread() {
        return Integer.valueOf(ApiNGDemo.getProp().getProperty("MAX_BACK_LAY_SPREAD_INCREMENT"));
    }

    private Integer getScoreStabalizationIteration() {
        return Integer.valueOf(ApiNGDemo.getProp().getProperty("GOAL_STABALIZATION_ITERATION_COUNT"));
    }

    private Boolean isMarketStartTimeLimitOn() {
        return Boolean.valueOf(ApiNGDemo.getProp().getProperty("MARKET_START_TIME_LIMIT_ON", "true"));
    }

    private Boolean isSafetyOff() {
        return Boolean.valueOf(ApiNGDemo.getProp().getProperty("LTD_SAFETY_OFF", "false"));
    }

    private Double getMinimumBetSize() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("MINIMUM_BET_SIZE"));
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