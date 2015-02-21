package com.betfair.aping.algo;

import com.betfair.aping.BetPlacer;
import com.betfair.aping.com.betfair.aping.events.betting.Exposure;
import com.betfair.aping.com.betfair.aping.events.betting.OverUnderMarket;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.MarketStatus;
import com.betfair.aping.enums.MarketTemp;
import com.betfair.aping.enums.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by markwilliams on 12/14/14.
 */
public class BackAndCoverAlgo extends LayAndCoverAlgo implements IMarketAlgo {

    private static final String ALGO_TYPE = "BNC";
    protected Logger logger = LoggerFactory.getLogger(BackAndCoverAlgo.class);

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

                    if (marketCatalogue != null && isCandidateBackMarket(event, marketCatalogue)) {
                        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
                        Runner runner = oum.getOverRunner();

                        Bet initialBet = getBetForMarket(marketCatalogue, runner, Side.BACK);
                        initialBet.getPriceSize().setSize(getSize());
                        List<Bet> initialBackBet = new ArrayList<Bet>();
                        initialBackBet.add(initialBet);
                        if (isSafetyOff()) {
                            logger.info("{}, {}, {}, OPEN: Candidate Mkt Found. Placing Bet: {}", event.getName(), event.getMarketClassification().getMarketTemp(), marketCatalogue.getMarketName(), initialBet.toString());
                            betPlacer.placeBets(initialBackBet);
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
                            logger.info("{}, {}, {}, WIN: Candidate Mkt Found. Placing Bet: {}", event.getName(), event.getMarketClassification().getMarketTemp(), marketCatalogue.getMarketName(), cashOutBet.toString());
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
                            logger.info("{}, {}, {}, LOSE: Candidate Mkt Found. Placing Bet: {}", event.getName(), event.getMarketClassification().getMarketTemp(), marketCatalogue.getMarketName(), cashOutBet.toString());
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

    protected Integer getMinutesAfterMarketStartTimeToBet(Event event, MarketType marketType) {
        MarketConfig marketConfig = getMarketConfigs().get(event.getMarketClassification().getMarketTemp()).get(marketType);
        if (marketConfig == null) {
            return 0;
        }
        return marketConfig.getBackTimeLimit();
    }

    private boolean isCandidateBackMarket(Event event, MarketCatalogue marketCatalogue) throws Exception {

        if (marketCatalogue == null) {
            return false;
        }

        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
        Runner runner = oum.getOverRunner();

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
            if (!isBestOpeningBackPriceWithinBounds(event, oum, runner)) {
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

        if (isScoreChanging(event)) {
            logger.info("{}; Score is currently changing. Previous scores: {}", event.getName(), event.getPreviousScores());
            return false;
        }
        return false;
    }

    private boolean isBestOpeningBackPriceWithinBounds(Event event, OverUnderMarket oum, Runner runner) {
        Double backLimit = getMarketConfig(event.getMarketClassification().getMarketTemp(), oum.getMarketType()).getBackLimit();
        if (oum.getBack(runner, 0).getPrice() >= backLimit) {
            logger.info("{}, {}; Back Price within bounds. Best Price: {}; Limit: {}", event.getName(), oum.getOverRunnerName(), oum.getBack(runner, 0).toString(), backLimit);
            return true;
        }
        logger.info("{}, {}; Back Price not within bounds. Best Price: {}; Limit: {}; Time Limit: {}", event.getName(), oum.getOverRunnerName(), oum.getLay(runner, 0).toString(), backLimit, getMinutesAfterMarketStartTimeToBet(event, oum.getMarketType()));
        return false;
    }

    private boolean isBestCoveringLayPriceWithinBounds(Event event, MarketCatalogue marketCatalogue, OverUnderMarket oum) throws Exception {
        Double profitPercentage = calcPercentageProfit(event, marketCatalogue, oum);

        int goalDifference = oum.getMarketType().getTotalGoals() - event.getScore().getTotalGoals();

        if (goalDifference == 0 && profitPercentage >= getCashOutProfitPercentage(event.getMarketClassification().getMarketTemp(), oum.getMarketType())) {
            //some kind of profit on the closest market, close it out
            logger.info("{}; {}; Regular Next Mkt Closeout. Goal Difference:{}, Best Lay Price: {}, {}, Profit Percentage: {}", event.getName(), oum.getMarketType().getMarketName(),
                    goalDifference, oum.getOverRunnerName(), oum.getLay(oum.getOverRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 2d));
            return true;
        }

        //If we have any profit and there is more than one goal needed to close it then cash out
        if (goalDifference >= 1 && profitPercentage >= 0.0
                && getTimeSinceMarketStart(event) > getCashOutTimeLimit(event.getMarketClassification().getMarketTemp(), oum.getMarketType())) {
            logger.info("{}; {}; Sm. Win: GD:{}, Best Lay: {}, {}, Profit %: {}", event.getName(), oum.getMarketType().getMarketName(),
                    goalDifference, oum.getOverRunnerName(), oum.getLay(oum.getOverRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 2d));
            return true;
        }

        logger.info("{}; {}; Covering Lay not yet within bounds. GD:{}, Best Lay: {}, {}, Profit %: {}", event.getName(), oum.getMarketType().getMarketName(),
                goalDifference, oum.getOverRunnerName(), oum.getLay(oum.getOverRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 2d));

        return false;
    }

    private boolean isLosingCoverLayPriceWithinBounds(Event event, MarketCatalogue marketCatalogue, OverUnderMarket oum) throws Exception {
        Double profitPercentage = calcPercentageProfit(event, marketCatalogue, oum);
        Double losingCashOutProfitPercentage = getLosingCashOutProfitPercentage(event.getMarketClassification().getMarketTemp(), oum.getMarketType());
        Integer losingMarketTimeSinceStart = getLosingMarketTimeSinceStart(event.getMarketClassification().getMarketTemp(), oum.getMarketType());
        if (profitPercentage <= losingCashOutProfitPercentage
                && getTimeSinceMarketStart(event) > losingMarketTimeSinceStart) {
            //you lose...
            logger.info("{}; {}; Losing Cover. Under getLosingCashOutProfitPercentage() {}. Lay Price: {}, {}, Profit Percentage: {}", event.getName(), oum.getMarketType().getMarketName(),
                    losingCashOutProfitPercentage, oum.getOverRunner(), oum.getLay(oum.getOverRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 2d));
            return true;
        }

        logger.debug("{}; {}; Not Yet Closing Losing Cover. Lay Price: {}, {}, Profit Percentage: {}", event.getName(), oum.getMarketType().getMarketName(),
                oum.getOverRunner(), oum.getLay(oum.getOverRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 2d));

        return false;
    }

    private Double calcPercentageProfit(Event event, MarketCatalogue marketCatalogue, OverUnderMarket oum) throws Exception {
        Exposure exposure = new Exposure(event, marketCatalogue);
        Double netExposure = exposure.calcNetExposure(true);

        Double cashOutStake = calcUnderRunnerCashOutBetSize(oum, netExposure);
        Double initialStake = getSize();

        Double profit = netExposure - cashOutStake - initialStake;

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

    protected Integer getCashOutTimeLimit(MarketTemp marketTemp, MarketType marketType) {
        return getMarketConfigs().get(marketTemp).get(marketType).getCashOutTimeLimit();
    }

    private Double getLosingCashOutProfitPercentage(MarketTemp marketTemp, MarketType marketType) {
        return getMarketConfigs().get(marketTemp).get(marketType).getStakeLossLimit();
    }

    private Integer getLosingMarketTimeSinceStart(MarketTemp marketTemp, MarketType marketType) {
        return getMarketConfigs().get(marketTemp).get(marketType).getLosingTimeLimit();
    }

    @Override
    protected String getAlgoType() {
        return ALGO_TYPE;
    }
}