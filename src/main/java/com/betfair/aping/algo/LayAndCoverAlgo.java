package com.betfair.aping.algo;

import com.betfair.aping.ApiNGDemo;
import com.betfair.aping.BetPlacer;
import com.betfair.aping.com.betfair.aping.events.betting.Exposure;
import com.betfair.aping.com.betfair.aping.events.betting.OverUnderMarket;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.MarketStatus;
import com.betfair.aping.enums.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by markwilliams on 12/14/14.
 */
public class LayAndCoverAlgo extends MarketAlgo implements IMarketAlgo {

    protected Logger logger = LoggerFactory.getLogger(LayAndCoverAlgo.class);
    private static final String ALGO_TYPE = "LNC";

    @Override
    public void process(Event event) throws Exception {
        BetPlacer betPlacer = new BetPlacer();
        Integer maxGoals = getTotalGoalLimit();

        updateEventScore(event);
        classifyMarket(event);
        logEventName(event);

        try {
            if (event.getPreviousScores().size() == MAX_PREV_SCORES) {

                int initialGoals = event.getScore().getTotalGoals();
                for (int numberOfGoals = initialGoals; numberOfGoals <= maxGoals; numberOfGoals++) {
                    MarketCatalogue marketCatalogue = getMarketCatalogueForTotalGoals(event, numberOfGoals);

                    if (marketCatalogue != null && isCandidateLayMarket(event, marketCatalogue)) {
                        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
                        Runner runner = oum.getUnderRunner();

                        Bet initialBet = getBetForMarket(marketCatalogue, runner, Side.LAY);
                        initialBet.getPriceSize().setSize(getSize());
                        List<Bet> initialLayBet = new ArrayList<Bet>();
                        initialLayBet.add(initialBet);
                        if (isSafetyOff()) {
                            logger.info("{}, {}, {}, OPEN: Candidate Mkt Found. Placing Bet: {}", event.getName(), event.getMarketClassification(), marketCatalogue.getMarketName(), initialBet.toString());
                            betPlacer.placeBets(initialLayBet);
                        }
                    }
                }
                //Check for Profitable trades
                for (int numberOfGoals = 0; numberOfGoals <= maxGoals; numberOfGoals++) {
                    MarketCatalogue marketCatalogue = getMarketCatalogueForTotalGoals(event, numberOfGoals);

                    if (isCandidateCoverMarket(event, marketCatalogue)) {
                        Exposure exposure = new Exposure(event, marketCatalogue);
                        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
                        Runner runner = oum.getOverRunner();
                        Side side;

                        Double cashOutBetSize = calcOverRunnerCashOutBetSize(oum, Math.abs(exposure.calcNetExposure(true)));

                        if (cashOutBetSize > getMinimumBetSize()) {
                            side = Side.LAY;
                        } else {
                            cashOutBetSize = calcUnderRunnerCashOutBetSize(oum, Math.abs(exposure.calcNetExposure(true)));
                            side = Side.BACK;
                        }

                        Bet cashOutBet = getBetForMarket(marketCatalogue, runner, side);

                        cashOutBet.getPriceSize().setSize(cashOutBetSize);

                        List<Bet> coverBet = new ArrayList<Bet>();
                        coverBet.add(cashOutBet);
                        if (isSafetyOff()) {
                            logger.info("{}, {}, {}, WIN: Candidate Mkt Found. Placing Bet: {}", event.getName(), event.getMarketClassification(), marketCatalogue.getMarketName(), cashOutBet.toString());
                            betPlacer.placeBets(coverBet);
                        }
                    }
                }
                //Check for and close out the losers
                for (int numberOfGoals = 0; numberOfGoals <= maxGoals; numberOfGoals++) {
                    MarketCatalogue marketCatalogue = getMarketCatalogueForTotalGoals(event, numberOfGoals);

                    if (isCandidateLosingCoverMarket(event, marketCatalogue)) {
                        Exposure exposure = new Exposure(event, marketCatalogue);
                        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
                        Runner runner = oum.getUnderRunner();
                        Bet cashOutBet = getBetForMarket(marketCatalogue, runner, Side.BACK);

                        Double cashOutBetSize = calcUnderRunnerCashOutBetSize(oum, Math.abs(exposure.calcNetExposure(true)));
                        cashOutBet.getPriceSize().setSize(cashOutBetSize);

                        List<Bet> coverBet = new ArrayList<Bet>();
                        coverBet.add(cashOutBet);
                        if (isSafetyOff()) {
                            logger.info("{}, {}, {}, LOSE: Candidate Mkt Found. Placing Bet: {}", event.getName(), event.getMarketClassification(), marketCatalogue.getMarketName(), cashOutBet.toString());
                            betPlacer.placeBets(coverBet);
                        }
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

    private double calcUnderRunnerCashOutBetSize(OverUnderMarket oum, Double netExposure) throws Exception {
        Runner runner = oum.getUnderRunner();
        return roundUpToNearestFraction(netExposure / oum.getPrice(runner, 0, Side.BACK).getPrice(), 0.01);
    }

    private boolean isCandidateLayMarket(Event event, MarketCatalogue marketCatalogue) throws Exception {

        if (marketCatalogue == null) {
            return false;
        }

        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
        Runner runner = oum.getUnderRunner();

        if (!marketCatalogue.getMarketBook().getStatus().equals(MarketStatus.OPEN)) {
            logger.info("{}; {}; Market is not OPEN", event.getName(), marketCatalogue.getMarketName());
            return false;
        }

        if (!isMarketStartingSoon(event)) {
            logger.debug("{}; {}; Market is not starting soon enough", event.getName(), marketCatalogue.getMarketName());
            return false;
        }

        if (!isMarketStartedTooLongAgo(event, oum)) {
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

        if (event.getScore().getTotalGoals() >= getTotalGoalLimit()) {
            //don't bet on some goalfest
            logger.debug("{}; Too many goals already scored: {}. Limit is {}", event.getName(), event.getScore().getTotalGoals(), getTotalGoalLimit());
            return false;
        }

        try {
            if (!isBestOpeningLayPriceWithinBounds(event, oum, runner)) {
                return false;
            }
        } catch (RuntimeException ex) {
            logger.info(ex.toString());
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

    private boolean isCandidateCoverMarket(Event event, MarketCatalogue marketCatalogue) throws Exception {
        if (marketCatalogue == null) {
            return false;
        }

        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
        Runner runner = oum.getUnderRunner();

        if (isBasicCoverCandidate(event, marketCatalogue)) return false;

        try {
            if (!isBestCoveringLayPriceWithinBounds(event, marketCatalogue, oum)) {
                return false;
            }
        } catch (RuntimeException ex) {
            logger.info(ex.toString());
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

    private boolean isCandidateLosingCoverMarket(Event event, MarketCatalogue marketCatalogue) throws Exception {
        if (marketCatalogue == null) {
            return false;
        }

        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
        Runner runner = oum.getUnderRunner();

        if (isBasicCoverCandidate(event, marketCatalogue)) {
            return false;
        }

        try {
            if (!isLosingCoverLayPriceWithinBounds(event, marketCatalogue, oum)) {
                return false;
            }
        } catch (RuntimeException ex) {
            logger.info(ex.toString());
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

    private boolean isBestOpeningLayPriceWithinBounds(Event event, OverUnderMarket oum, Runner runner) {
        Double layLimit = getMarketConfig(event.getMarketClassification(), oum.getMarketType()).getLayLimit();
        if (oum.getLay(runner, 0).getPrice() <= layLimit) {
            logger.info("{}, {}; Lay Price within bounds. Best Price: {}; Lay Limit: {}", event.getName(), oum.getUnderRunnerName(), oum.getLay(runner, 0).toString(), layLimit);
            return true;
        }
        logger.info("{}, {}; Lay Price not within bounds. Best Lay Price: {}; Lay Limit: {}; Time Limit: {}", event.getName(), oum.getUnderRunnerName(), oum.getLay(runner, 0).toString(), layLimit, getMinutesAfterMarketStartTimeToBet(event, oum.getMarketType()));
        return false;
    }

    private boolean isBestCoveringLayPriceWithinBounds(Event event, MarketCatalogue marketCatalogue, OverUnderMarket oum) throws Exception {
        Double profitPercentage = calcPercentageProfit(event, marketCatalogue, oum);

        int goalDifference = oum.getMarketType().getTotalGoals() - event.getScore().getTotalGoals();

        if (goalDifference == 0 && profitPercentage >= getCashOutProfitPercentage()) {
            //some kind of profit on the closest market, close it out
            logger.info("{}; {}; Regular Next Mkt Closeout. Goal Difference:{}, Best Lay Price: {}, {}, Profit Percentage: {}", event.getName(), oum.getMarketType().getMarketName(),
                    goalDifference, oum.getOverRunnerName(), oum.getLay(oum.getOverRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 2d));
            return true;
        }

        if (goalDifference == 1 && profitPercentage >= 0.0
                && getTimeSinceMarketStart(event) > getSmallWinCloseoutMarketTimeSinceStart()) {
            //only close the next market up if we have some kind of gangbuster profit right off the bat
            logger.info("{}; {}; Small Win Cover: Goal Difference:{}, Best Lay Price: {}, {}, Profit Percentage: {}", event.getName(), oum.getMarketType().getMarketName(),
                    goalDifference, oum.getOverRunnerName(), oum.getLay(oum.getOverRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 2d));
            return true;
        }

        if (goalDifference == 1 && profitPercentage >= getBestCaseCashOutProfitPercentage()) {
            //only close the next market up if we have some kind of gangbuster profit right off the bat
            logger.info("{}; {}; Large Profit Skip Mkt Closeout. Goal Difference:{}, Best Lay Price: {}, {}, Profit Percentage: {}", event.getName(), oum.getMarketType().getMarketName(),
                    goalDifference, oum.getOverRunnerName(), oum.getLay(oum.getOverRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 2d));
            return true;
        }

        logger.info("{}; {}; Covering Lay not yet within bounds. Goal Difference:{}, Best Lay Price: {}, {}, Profit Percentage: {}", event.getName(), oum.getMarketType().getMarketName(),
                goalDifference, oum.getOverRunnerName(), oum.getLay(oum.getOverRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 2d));

        return false;
    }

    private boolean isLosingCoverLayPriceWithinBounds(Event event, MarketCatalogue marketCatalogue, OverUnderMarket oum) throws Exception {
        Double profitPercentage = calcPercentageProfit(event, marketCatalogue, oum);

        if (profitPercentage <= getLosingCashOutProfitPercentage()
                && getTimeSinceMarketStart(event) > getLosingMarketTimeSinceStart()) {
            //you lose...
            logger.info("{}; {}; Losing Cover. Under getLosingCashOutProfitPercentage() {}. Lay Price: {}, {}, Profit Percentage: {}", event.getName(), oum.getMarketType().getMarketName(),
                    getLosingCashOutProfitPercentage(), oum.getUnderRunnerName(), oum.getLay(oum.getUnderRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 2d));
            return true;
        }

        if (profitPercentage <= 0
                && getTimeSinceMarketStart(event) > getLosingMarketFinalCloseOutTime()) {
            //you lose...
            logger.info("{}; {}; Losing Cover. After getLosingMarketFinalCloseOutTime() {}. Lay Price: {}, {}, Profit Percentage: {}", event.getName(), oum.getMarketType().getMarketName(),
                    getLosingMarketFinalCloseOutTime(), oum.getUnderRunnerName(), oum.getLay(oum.getUnderRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 2d));
            return true;
        }

        logger.info("{}; {}; Not Yet Closing Losing Cover. Lay Price: {}, {}, Profit Percentage: {}", event.getName(), oum.getMarketType().getMarketName(),
                oum.getUnderRunnerName(), oum.getLay(oum.getUnderRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 2d));

        return false;
    }

    private Double calcPercentageProfit(Event event, MarketCatalogue marketCatalogue, OverUnderMarket oum) throws Exception {
        Exposure exposure = new Exposure(event, marketCatalogue);
        Double layExposure = exposure.calcNetExposure(true);

        Double cashOutStake = calcOverRunnerCashOutBetSize(oum, layExposure);
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
        if (exposure.calcNetExposure(true) > 0.1) {
            logger.debug("{}; {}; Bet already open in the Market. Exposure: {}", event.getName(), marketCatalogue.getMarketName(), exposure.calcNetExposure(true));
            return true;
        }
        return false;
    }

    private Double getCashOutProfitPercentage() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("LNC_CLOSE_OUT_PROFIT_PERCENTAGE"));
    }

    private Double getLosingCashOutProfitPercentage() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("LNC_LOSING_CLOSE_OUT_PROFIT_PERCENTAGE"));
    }

    private Double getLosingMarketTimeSinceStart() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("LNC_LOSING_CLOSE_OUT_TIME_SINCE_START"));
    }

    private Double getSmallWinCloseoutMarketTimeSinceStart() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("LNC_SMALL_WIN_CLOSE_OUT_TIME_SINCE_START"));
    }

    private Double getLosingMarketFinalCloseOutTime() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("LNC_LOSING_CLOSE_OUT_FINAL_TIME"));
    }

    private Double getBestCaseCashOutProfitPercentage() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("LNC_BEST_CASE_CLOSE_OUT_PROFIT_PERCENTAGE"));
    }

    @Override
    protected String getAlgoType() {
        return ALGO_TYPE;
    }
}