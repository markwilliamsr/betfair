package com.betfair.aping.algo;

import com.betfair.aping.BetPlacer;
import com.betfair.aping.com.betfair.aping.events.betting.BaseMarket;
import com.betfair.aping.com.betfair.aping.events.betting.Exposure;
import com.betfair.aping.com.betfair.aping.events.betting.MatchOddsMarket;
import com.betfair.aping.com.betfair.aping.events.betting.PriceIncrement;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.MarketStatus;
import com.betfair.aping.enums.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by markwilliams on 12/14/14.
 */
public class BackLikelyOutcomeAlgo extends MarketAlgo implements IMarketAlgo {

    private static final String ALGO_TYPE = "BLO";
    private static final String OU25 = "OU25";
    protected Logger logger = LoggerFactory.getLogger(BackLikelyOutcomeAlgo.class);

    @Override
    protected void classifyMarket(Event event) throws Exception {
        super.classifyMarket(event, true);
        event.getMarketClassification().setMarketTemp(event.getScore().goalDifferenceAsTemp());
    }

    @Override
    public void process(Event event) throws Exception {
        BetPlacer betPlacer = new BetPlacer();

        updateEventScore(event);
        classifyMarket(event);
        logEventName(event);

        try {
            for (MarketCatalogue marketCatalogue : event.getMarket().values()) {
                if (!(new BaseMarket(marketCatalogue).getMarketType().equals(MarketType.MATCH_ODDS))) {
                    continue;
                }
                if (marketCatalogue != null && isCandidateBackMarket(event, marketCatalogue)) {
                    MatchOddsMarket matchOddsMarket = new MatchOddsMarket(marketCatalogue);
                    Runner runner = getOpeningBackRunner(event, matchOddsMarket);

                    Bet initialBet = getBetForMarket(marketCatalogue, runner, Side.BACK);
                    initialBet.getPriceSize().setSize(getSize());
                    List<Bet> bets = new ArrayList<Bet>();
                    bets.add(initialBet);
                    Bet cashOutBet = calcCashOutBetSize(event, initialBet);
                    bets.add(cashOutBet);
                    if (isSafetyOff()) {
                        logger.info("{}, {}, {}, OPEN: Candidate Mkt Found. Placing Bet: {}", event.getName(), event.getMarketClassification().getMarketTemp(), marketCatalogue.getMarketName(), initialBet.toString());
                        betPlacer.placeBets(bets);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            logger.info("Could not determine the MarketType. Exception: " + e.toString());
        }
    }

    private Bet calcCashOutBetSize(Event event, Bet initialBet) throws Exception {
        Bet cashOutBet = new Bet();
        PriceSize priceSize = new PriceSize();

        MarketConfig marketConfig = getMarketConfigs().get(event.getScore().goalDifferenceAsTemp()).get(MarketType.MATCH_ODDS);

        Double totalReturn = initialBet.getPriceSize().getSize() * initialBet.getPriceSize().getPrice();
        totalReturn = roundUpToNearestFraction(totalReturn, 0.01);

        priceSize.setSize(totalReturn);
        priceSize.setPrice(marketConfig.getBackLowerLimit());

        cashOutBet.setSide(Side.LAY);
        cashOutBet.setPriceSize(priceSize);

        cashOutBet.setMarketId(initialBet.getMarketId());
        cashOutBet.setSelectionId(initialBet.getSelectionId());

        return cashOutBet;
    }

    protected Integer getMinutesAfterMarketStartTimeToBet(Event event, MarketType marketType) {
        MarketConfig marketConfig = getMarketConfigs().get(event.getScore().goalDifferenceAsTemp()).get(marketType);
        if (marketConfig == null) {
            return 0;
        }
        return marketConfig.getBackTimeLimit();
    }

    private boolean isCandidateBackMarket(Event event, MarketCatalogue marketCatalogue) throws Exception {

        if (marketCatalogue == null) {
            return false;
        }

        MatchOddsMarket matchOddsMarket = new MatchOddsMarket(marketCatalogue);

        if (!marketCatalogue.getMarketBook().getStatus().equals(MarketStatus.OPEN)) {
            logger.info("{}; {}; Market is not OPEN", event.getName(), marketCatalogue.getMarketName());
            return false;
        }

        if (!isMarketStartingSoon(event)) {
            logger.debug("{}; {}; Market is not starting soon enough", event.getName(), marketCatalogue.getMarketName());
            return false;
        }

        if (!isEnoughTimeElapsed(event, matchOddsMarket)) {
            return false;
        }

        if (isBetAlreadyOpen(marketCatalogue, event)) {
            return false;
        }

//        if (isScoreChanging(event)) {
//            logger.info("{}; Score is currently changing. Previous scores: {}", event.getName(), event.getPreviousScores());
//            return false;
//        }

        try {
            if (!isBestOpeningBackPriceWithinBounds(event, matchOddsMarket)) {
                return false;
            }
        } catch (RuntimeException ex) {
            logger.error("{}: Error finding opening Back within bounds (Not enough Depth)", event.getName());
            return false;
        }

        try {
            if (!isBackLaySpreadWithinBounds(event, matchOddsMarket)) {
                return false;
            }
        } catch (RuntimeException ex) {
            logger.error("{}: Error checking the back / lay spread (Not enough Depth)", event.getName());
            return false;
        }

        return true;
    }

    protected boolean isEnoughTimeElapsed(Event event, BaseMarket oum) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1 * getMinutesAfterMarketStartTimeToBet(event, oum.getMarketType()));
        if (!isMarketStartTimeLimitOn()) {
            return true;
        }
        if (getMinutesAfterMarketStartTimeToBet(event, oum.getMarketType()) > 0
                && event.getOpenDate().before(calendar.getTime())) {
            return true;
        }
        return false;
    }

    protected boolean isBackLaySpreadWithinBounds(Event event, MatchOddsMarket matchOddsMarket) throws Exception {
        Runner runner = getOpeningBackRunner(event, matchOddsMarket);
        Double back = matchOddsMarket.getBack(runner, 0).getPrice();
        Double lay = matchOddsMarket.getLay(runner, 0).getPrice();
        Long spread = 0l;

        if (back != null && lay != null) {
            Double increment = PriceIncrement.getIncrement(back);
            spread = Math.round((lay - back) / increment);
            if (spread <= getMaxBackLaySpread()) {
                return true;
            }
        }
        logger.info("{}, {}; Back Lay Spread not within bounds. Lay: {}, Back: {}, Spread: {}", event.getName(), matchOddsMarket.getMarketType().getMarketName(), lay, back, spread);
        return false;
    }

    private Runner getOpeningBackRunner(Event event, MatchOddsMarket matchOddsMarket) throws Exception {
        MarketConfig marketConfig = getMarketConfig(event.getScore().goalDifferenceAsTemp(), MarketType.MATCH_ODDS);
        if (marketConfig == null) {
            return null;
        }

        Runner home = matchOddsMarket.getHomeRunner();
        Runner away = matchOddsMarket.getAwayRunner();
        Runner draw = matchOddsMarket.getDrawRunner();

        Double backUpperLimit = marketConfig.getBackUpperLimit();
        Double backLowerLimit = marketConfig.getBackLowerLimit();

        try {
            if (matchOddsMarket.getBack(home, 0).getPrice() <= backUpperLimit
                    && matchOddsMarket.getBack(home, 0).getPrice() > backLowerLimit) {
                logger.info("{}, {}; Back Price within bounds. Best Price: {}; Limit: {}", event.getName(), matchOddsMarket.getHomeRunnerName(), matchOddsMarket.getBack(home, 0).toString(), backUpperLimit);
                return home;
            }
        } catch (RuntimeException ex) {
        }
        try {
            if (matchOddsMarket.getBack(away, 0).getPrice() <= backUpperLimit
                    && matchOddsMarket.getBack(away, 0).getPrice() > backLowerLimit) {
                logger.info("{}, {}; Back Price within bounds. Best Price: {}; Limit: {}", event.getName(), matchOddsMarket.getAwayRunnerName(), matchOddsMarket.getBack(away, 0).toString(), backUpperLimit);
                return away;
            }
        } catch (RuntimeException ex) {
        }
        try {
            if (matchOddsMarket.getBack(draw, 0).getPrice() <= backUpperLimit
                    && matchOddsMarket.getBack(draw, 0).getPrice() > backLowerLimit) {
                logger.info("{}, {}; Back Price within bounds. Best Price: {}; Limit: {}", event.getName(), matchOddsMarket.getDrawRunnerName(), matchOddsMarket.getBack(draw, 0).toString(), backUpperLimit);
                return draw;
            }
        } catch (RuntimeException ex) {
        }
        logger.info("{}; Back Price not within bounds. Lower Limit: {}; Upper Limit: {}; Time Limit: {}", event.getName(), backLowerLimit, backUpperLimit, getMinutesAfterMarketStartTimeToBet(event, matchOddsMarket.getMarketType()));
        return null;
    }

    private boolean isBestOpeningBackPriceWithinBounds(Event event, MatchOddsMarket matchOddsMarket) throws Exception {
        if (getOpeningBackRunner(event, matchOddsMarket) != null) {
            return true;
        }
        return false;
    }

    private boolean isBetAlreadyOpen(MarketCatalogue marketCatalogue, Event event) throws Exception {
        Exposure exposure = new Exposure(event, marketCatalogue);
        if (exposure.calcBestCaseMatchOddsLiability(false) > 0.02) {
            logger.debug("{}; {}; Bet already open in the Market. Exposure: {}", event.getName(), marketCatalogue.getMarketName(), exposure.calcBestCaseMatchOddsLiability(false));
            return true;
        }
        return false;
    }

    @Override
    protected String getAlgoType() {
        return ALGO_TYPE;
    }
}