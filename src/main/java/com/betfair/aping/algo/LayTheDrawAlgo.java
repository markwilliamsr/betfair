package com.betfair.aping.algo;

import com.betfair.aping.ApiNGDemo;
import com.betfair.aping.BetPlacer;
import com.betfair.aping.com.betfair.aping.events.betting.Exposure;
import com.betfair.aping.com.betfair.aping.events.betting.MatchOddsMarket;
import com.betfair.aping.com.betfair.aping.events.betting.ScoreEnum;
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
public class LayTheDrawAlgo extends MarketAlgo implements IMarketAlgo {
    private static final String ALGO_TYPE = "LTD";
    private Logger logger = LoggerFactory.getLogger(LayTheDrawAlgo.class);

    @Override
    public void process(Event event) throws Exception {
        BetPlacer betPlacer = new BetPlacer();

        updateEventScore(event);
        classifyMarket(event);
        logEventName(event);

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

                MarketCatalogue marketCatalogue = event.getMarket().get(MarketType.MATCH_ODDS);
                if (marketCatalogue != null) {
                    MatchOddsMarket mom = new MatchOddsMarket(marketCatalogue);
                    Exposure exposure = new Exposure(event, marketCatalogue);

                    //Check for Profitable Trades
                    if (isCandidateCoverMarket(event, marketCatalogue)) {
                        if (calcPercentageProfitStake(event, marketCatalogue, mom) > 0) {
                            Bet cashOutBet = getCashOutBetForMarket(marketCatalogue, Math.abs(exposure.calcNetLtdExposure(true)));
                            List<Bet> coverBet = new ArrayList<Bet>();
                            coverBet.add(cashOutBet);
                            if (isSafetyOff()) {
                                logger.info("{}, {}, WIN: Candidate Mkt Found. Placing Bet: {}", event.getName(), marketCatalogue.getMarketName(), cashOutBet.toString());
                                betPlacer.placeBets(coverBet);
                            }
                        }
                    }
                    //Check for Losing Trades
                    if (isCandidateLosingCoverMarket(event, marketCatalogue)) {
                        if (calcPercentageProfitStake(event, marketCatalogue, mom) < 0) {
                            Bet cashOutBet = getCashOutBetForMarket(marketCatalogue, Math.abs(exposure.calcNetLtdExposure(true)));

                            if (cashOutBet.getPriceSize().getSize() >= getMinimumBetSize()) {
                                List<Bet> coverBet = new ArrayList<Bet>();
                                coverBet.add(cashOutBet);
                                if (isSafetyOff()) {
                                    logger.info("{}, {}, LOSE: Candidate Mkt Found. Placing Bet: {}", event.getName(), marketCatalogue.getMarketName(), cashOutBet.toString());
                                    betPlacer.placeBets(coverBet);
                                }
                            } else {
                                logger.warn("{}, {}; HUM. Losing Cash Out Bet Size Less than Minimum Bet. {}", event.getName(), marketCatalogue.getMarketName(), cashOutBet.getPriceSize().getSize());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception in processing loop: ", e);
        }
    }

    protected Bet getCashOutBetForMarket(MarketCatalogue marketCatalogue, Double exposure) throws Exception {
        MatchOddsMarket mom = new MatchOddsMarket(marketCatalogue);

        Double cashOutBetSize = calcBackRunnerCashOutBetSize(mom, exposure);
        Runner runner = mom.getDrawRunner();
        Side side = Side.BACK;

        if (cashOutBetSize < getMinimumBetSize()) {
            cashOutBetSize = calcLayHomeRunnerCashOutBetSize(mom, exposure);
            runner = mom.getHomeRunner();
            side = Side.LAY;
        }

        if (cashOutBetSize < getMinimumBetSize()) {
            cashOutBetSize = calcLayAwayRunnerCashOutBetSize(mom, exposure);
            runner = mom.getAwayRunner();
            side = Side.LAY;
        }

        Bet cashOutBet = getBetForMarket(marketCatalogue, runner, side);

        cashOutBet.getPriceSize().setSize(cashOutBetSize);
        cashOutBet.setSide(side);

        return cashOutBet;
    }

    private double calcBackRunnerCashOutBetSize(MatchOddsMarket mom, Double netExposure) throws Exception {
        Runner runner = mom.getDrawRunner();
        return roundUpToNearestFraction(netExposure / mom.getPrice(runner, 0, Side.BACK).getPrice(), 0.01);
    }

    private double calcLayHomeRunnerCashOutBetSize(MatchOddsMarket mom, Double netExposure) throws Exception {
        Runner runner = mom.getHomeRunner();
        return roundUpToNearestFraction(netExposure / mom.getPrice(runner, 0, Side.LAY).getPrice(), 0.01);
    }

    private double calcLayAwayRunnerCashOutBetSize(MatchOddsMarket mom, Double netExposure) throws Exception {
        Runner runner = mom.getAwayRunner();
        return roundUpToNearestFraction(netExposure / mom.getPrice(runner, 0, Side.LAY).getPrice(), 0.01);
    }

    @Override
    protected String getAlgoType() {
        return ALGO_TYPE;
    }

    private boolean isCandidateLayMarket(Event event, MarketCatalogue marketCatalogue) throws Exception {

        if (marketCatalogue == null) {
            return false;
        }

        MatchOddsMarket mom = new MatchOddsMarket(marketCatalogue);
        Runner runner = mom.getDrawRunner();

        if (!marketCatalogue.getMarketBook().getStatus().equals(MarketStatus.OPEN)) {
            logger.info("{}; {}; Market is not OPEN", event.getName(), marketCatalogue.getMarketName());
            return false;
        }

        if (!isMarketStartingSoon(event)) {
            logger.debug("{}; {}; Market is not starting soon enough", event.getName(), marketCatalogue.getMarketName());
            return false;
        }

        if (!isMarketStartedTooLongAgo(event, mom)) {
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

        try {
            if (!isBestOpeningLayPriceWithinBounds(event, mom, runner)) {
                return false;
            }
        } catch (RuntimeException ex) {
            logger.error("Error: {}", ex);
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

    private boolean isCandidateCoverMarket(Event event, MarketCatalogue marketCatalogue) throws Exception {
        if (marketCatalogue == null) {
            return false;
        }

        MatchOddsMarket mom = new MatchOddsMarket(marketCatalogue);
        Runner runner = mom.getDrawRunner();

        if (isBasicCoverCandidate(event, marketCatalogue)) {
            return false;
        }

        if (!isBetAlreadyOpen(marketCatalogue, event)) {
            return false;
        }

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

    private boolean isCandidateLosingCoverMarket(Event event, MarketCatalogue marketCatalogue) throws Exception {
        if (marketCatalogue == null) {
            return false;
        }

        MatchOddsMarket mom = new MatchOddsMarket(marketCatalogue);
        Runner runner = mom.getDrawRunner();

        if (!isBasicCoverCandidate(event, marketCatalogue)) {
            return false;
        }

        try {
            if (!isCoveringLossWithinBounds(event, marketCatalogue, mom)) {
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

    private boolean isBasicCoverCandidate(Event event, MarketCatalogue marketCatalogue) throws Exception {
        if (!marketCatalogue.getMarketBook().getStatus().equals(MarketStatus.OPEN)) {
            logger.debug("{}; {}; Market is not OPEN", event.getName(), marketCatalogue.getMarketName());
            return false;
        }

        if (!isMarketStartingSoon(event)) {
            logger.debug("{}; {}; Market is not starting soon enough", event.getName(), marketCatalogue.getMarketName());
            return false;
        }

        if (!isBetAlreadyOpen(marketCatalogue, event)) {
            logger.debug("{}; {}; No Lay Bets already open in the Market", event.getName(), marketCatalogue.getMarketName());
            return false;
        }
        return true;
    }

    private boolean isBestOpeningLayPriceWithinBounds(Event event, MatchOddsMarket mom, Runner runner) {
        Double layLimit = getLayTheDrawLayLimit(event.getMarketClassification().getMarketTemp(), mom.getMarketType());
        if (mom.getLay(runner, 0).getPrice() <= layLimit) {
            logger.info("{}, {}; Lay Price within bounds. Best Price: {}; Lay Limit: {}", event.getName(), mom.getDrawRunnerName(), mom.getLay(runner, 0).toString(), layLimit);
            return true;
        }
        logger.info("{}, {}; Lay Price not within bounds. Best Lay Price: {}; Lay Limit: {}", event.getName(), mom.getDrawRunnerName(), mom.getLay(runner, 0).toString(), layLimit);
        return false;
    }

    private boolean isCoveringLossWithinBounds(Event event, MarketCatalogue marketCatalogue, MatchOddsMarket mom) throws Exception {
        Double expProfitPercentage = calcPercentageProfitExposure(event, marketCatalogue, mom);

        if (Math.abs(expProfitPercentage) >= getLayTheDrawLossLimit(event.getMarketClassification().getMarketTemp(), mom.getMarketType())) {
            logger.info("{}; {}; Reg. Closeout. Best Lay: {}, {}, Exp Profit%: {}", event.getName(), mom.getMarketType().getMarketName(),
                    mom.getDrawRunnerName(), mom.getLay(mom.getDrawRunner(), 0).toString(), roundUpToNearestFraction(expProfitPercentage, 0.01d));
            return true;
        }

        logger.info("{}; {}; Reg. Closeout Not OK. Best Lay: {}, {}, Exp Profit%: {}", event.getName(), mom.getMarketType().getMarketName(),
                mom.getDrawRunnerName(), mom.getLay(mom.getDrawRunner(), 0).toString(), roundUpToNearestFraction(expProfitPercentage, 0.01d));

        return false;
    }

    private boolean isBestCoveringLayPriceWithinBounds(Event event, MarketCatalogue marketCatalogue, MatchOddsMarket mom) throws Exception {
        Double profitPercentage = calcPercentageProfitStake(event, marketCatalogue, mom);

        if (profitPercentage >= getCashOutProfitPercentage()) {
            //some kind of profit on the closest market, close it out
            logger.info("{}; {}; Reg. Closeout. Best Lay: {}, {}, Profit%: {}", event.getName(), mom.getMarketType().getMarketName(),
                    mom.getDrawRunnerName(), mom.getLay(mom.getDrawRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 0.01d));
            return true;
        }

        logger.info("{}; {}; Cov. Lay Not OK. Best Lay: {}, {}, Profit%: {}", event.getName(), mom.getMarketType().getMarketName(),
                mom.getDrawRunnerName(), mom.getLay(mom.getDrawRunner(), 0).toString(), roundUpToNearestFraction(profitPercentage, 0.01d));

        return false;
    }

    private Double calcPercentageProfitStake(Event event, MarketCatalogue marketCatalogue, MatchOddsMarket mom) throws Exception {
        Exposure exposure = new Exposure(event, marketCatalogue);
        Double layExposure = exposure.calcNetLtdExposure(true);
        Double cashOutStake = calcBackRunnerCashOutBetSize(mom, layExposure);
        Double initialStake = exposure.calcPlacedBetsForSide(mom.getDrawRunner(), Side.LAY, true);

        Double profit = initialStake - cashOutStake;

        profit = roundUpToNearestFraction(profit, 0.01);

        return (profit / initialStake) * 100;
    }

    private Double calcPercentageProfitExposure(Event event, MarketCatalogue marketCatalogue, MatchOddsMarket mom) throws Exception {
        Exposure exposure = new Exposure(event, marketCatalogue);
        Double layExposure = exposure.calcNetLtdExposure(true);
        Double cashOutStake = calcBackRunnerCashOutBetSize(mom, layExposure);
        Double initialStake = exposure.calcPlacedBetsForSide(mom.getDrawRunner(), Side.LAY, true);

        Double profit = initialStake - cashOutStake;

        return roundUpToNearestFraction((profit / layExposure) * 100, 0.01);

    }

    private boolean isBetAlreadyOpen(MarketCatalogue marketCatalogue, Event event) throws Exception {
        Exposure exposure = new Exposure(event, marketCatalogue);
        if (exposure.calcNetLtdExposure(true) > 0.1) {
            logger.debug("{}; {}; Bet already open in the Market. Exposure: {}", event.getName(), marketCatalogue.getMarketName(), exposure.calcNetLtdExposure(true));
            return true;
        }
        return false;
    }

    private Double getLayTheDrawLayLimit(MarketTemp marketTemp, MarketType marketType) {
        return getMarketConfigs().get(marketTemp).get(marketType).getLayLimit();
    }

    private Double getLayTheDrawLossLimit(MarketTemp marketTemp, MarketType marketType) {
        return getMarketConfigs().get(marketTemp).get(marketType).getExpLossLimit();
    }

    private Double getCashOutProfitPercentage() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("LTD_CLOSE_OUT_PROFIT_PERCENTAGE"));
    }
}