package com.betfair.aping.algo;

import com.betfair.aping.ApiNGDemo;
import com.betfair.aping.BetPlacer;
import com.betfair.aping.com.betfair.aping.events.betting.Exposure;
import com.betfair.aping.com.betfair.aping.events.betting.MatchOddsMarket;
import com.betfair.aping.com.betfair.aping.events.betting.ScoreEnum;
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
public class LayTheDrawAlgo extends MarketAlgo implements IMarketAlgo {
    private static final String ALGO_TYPE = "LTD";
    private Logger logger = LoggerFactory.getLogger(LayTheDrawAlgo.class);

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

                    if (cashOutBetSize >= getMinimumBetSize()) {
                        Bet cashOutBet = getBetForMarket(marketCatalogue, runner, side);

                        cashOutBet.getPriceSize().setSize(cashOutBetSize);

                        List<Bet> coverBet = new ArrayList<Bet>();
                        coverBet.add(cashOutBet);
                        if (isSafetyOff()) {
                            logger.info("{}, {}, WIN: Candidate Mkt Found. Placing Bet: {}", event.getName(), marketCatalogue.getMarketName(), cashOutBet.toString());
                            betPlacer.placeBets(coverBet);
                        }
                    } else {
                        logger.warn("{}, {}; HUM. Cash Out Bet Size Less than Minimum Bet.", event.getName(), marketCatalogue.getMarketName(), cashOutBetSize);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            logger.info("Could not determine the MarketType. Exception: " + e.toString());
        }
    }

    private double calcBackRunnerCashOutBetSize(MatchOddsMarket mom, Double netExposure) throws Exception {
        Runner runner = mom.getDrawRunner();
        return roundUpToNearestFraction(netExposure / mom.getPrice(runner, 0, Side.BACK).getPrice(), 0.01);
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

    private boolean isBestOpeningLayPriceWithinBounds(Event event, MatchOddsMarket mom, Runner runner) {
        if (mom.getLay(runner, 0).getPrice() <= getLayTheDrawLayLimit()) {
            logger.info("{}, {}; Lay Price within bounds. Best Price: {}; Lay Limit: {}", event.getName(), mom.getDrawRunnerName(), mom.getLay(runner, 0).toString(), getLayTheDrawLayLimit());
            return true;
        }
        logger.info("{}, {}; Lay Price not within bounds. Best Lay Price: {}; Lay Limit: {}", event.getName(), mom.getDrawRunnerName(), mom.getLay(runner, 0).toString(), getLayTheDrawLayLimit());
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

    private Double calcPercentageProfit(Event event, MarketCatalogue marketCatalogue, MatchOddsMarket mom) throws Exception {
        Exposure exposure = new Exposure(event, marketCatalogue);
        Double layExposure = exposure.calcNetLtdExposure(true);
        Double cashOutStake = calcBackRunnerCashOutBetSize(mom, layExposure);
        Double initialStake = getSize();

        Double profit = initialStake - cashOutStake;

        profit = roundUpToNearestFraction(profit, 0.01);

        return (profit / initialStake) * 100;
    }

    private boolean isBetAlreadyOpen(MarketCatalogue marketCatalogue, Event event) throws Exception {
        Exposure exposure = new Exposure(event, marketCatalogue);
        if (exposure.calcNetLtdExposure(true) > 0.1) {
            logger.debug("{}; {}; Bet already open in the Market. Exposure: {}", event.getName(), marketCatalogue.getMarketName(), exposure.calcNetLtdExposure(true));
            return true;
        }
        return false;
    }

    private Double getLayTheDrawLayLimit() {
        Double limits = Double.valueOf(ApiNGDemo.getProp().getProperty("LTD_OVER_UNDER_LAY_LIMIT"));

        return limits;
    }

    private Double getCashOutProfitPercentage() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("LTD_CLOSE_OUT_PROFIT_PERCENTAGE"));
    }
}