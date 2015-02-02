package com.betfair.aping.com.betfair.aping.events.betting;

import com.betfair.aping.entities.*;
import com.betfair.aping.enums.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by markwilliams on 26/08/2014.
 */
public class Exposure {
    private final Event event;
    private final MarketCatalogue marketCatalogue;
    private Logger logger = LoggerFactory.getLogger(Exposure.class);

    public Exposure(Event event, MarketCatalogue marketCatalogue) {
        this.event = event;
        this.marketCatalogue = marketCatalogue;
    }

    public Double calcPlacedBetsForSide(Runner runner, Side side, boolean includeUnMatched) throws Exception {
        Double bet = 0.0;
        List<Order> orders = runner.getOrders();

        if (orders == null) {
            return bet;
        }

        for (Order order : orders) {
            if (order.getSide().equals(side)) {
                bet += order.getSizeMatched();
                if (includeUnMatched) {
                    bet += order.getSizeRemaining();
                }
            }
        }
        return bet;
    }

    public Double calcExposureForSide(Runner runner, Side side, boolean includeUnMatched) throws Exception {
        Double exposure = 0.0;
        List<Order> orders = runner.getOrders();

        if (orders == null) {
            return exposure;
        }

        for (Order order : orders) {
            if (order.getSide().equals(side)) {
                exposure += (order.getPrice() * order.getSizeMatched());
                if (includeUnMatched) {
                    exposure += (order.getPrice() * order.getSizeRemaining());
                }
            }
        }
        return exposure;
    }

    public Double calcBlendedPriceForSide(Runner runner, Side side) throws Exception {
        Double value = 0.0;
        Double totalStake = 0.0;
        List<Order> orders = runner.getOrders();

        if (orders == null) {
            return 0.0;
        }

        for (Order order : orders) {
            if (order.getSide().equals(side)) {
                value += (order.getPrice() * order.getSizeMatched());
                totalStake += order.getSizeMatched();
            }
        }
        return (value / totalStake);
    }

    public Double calcUnrealisedPnL(Runner runner) throws Exception {
        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
        Double bestLayPrice = oum.getLay(runner, 0).getPrice();
        Double backedPrice = calcBlendedPriceForSide(runner, Side.BACK);
        Double totalStake = calcStakeForSide(runner, Side.BACK);

        return (backedPrice - bestLayPrice) * totalStake / 2;
    }

    public Double calcPercentagePnL(Runner runner) throws Exception {
        if (calcTotalStake(runner).equals(0d)) {
            return 0d;
        } else {
            return 100.0 * (calcUnrealisedPnL(runner) / calcTotalStake(runner));
        }
    }

    public Double calcTotalStake(Runner runner) throws Exception {
        return calcStakeForSide(runner, Side.BACK) + calcStakeForSide(runner, Side.LAY);
    }

    public Double calcStakeForSide(Runner runner, Side side) throws Exception {
        Double stake = 0.0;
        List<Order> orders = runner.getOrders();

        if (orders == null) {
            return stake;
        }

        for (Order order : orders) {
            if (order.getSide().equals(side)) {
                stake += order.getSizeMatched();
            }
        }
        return stake;
    }

    public Double calcNetExposure(boolean includeUnMatched) throws Exception {
        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
        Runner r = oum.getUnderRunner();

        Double backUnderExposure = calcExposureForSide(r, Side.BACK, includeUnMatched);
        Double layUnderExposure = calcExposureForSide(r, Side.LAY, includeUnMatched);
        Double totalUnderExposure = backUnderExposure - layUnderExposure;

        r = oum.getOverRunner();

        Double backOverExposure = calcExposureForSide(r, Side.BACK, includeUnMatched);
        Double layOverExposure = calcExposureForSide(r, Side.LAY, includeUnMatched);
        Double totalOverExposure = backOverExposure - layOverExposure;

        Double totalExposure = Math.abs(totalOverExposure - totalUnderExposure);
        //round to nearest penny
        totalExposure = totalExposure != 0 ? roundUpToNearestFraction(totalExposure, 0.01) : 0d;

        logger.debug("{}; {}; Total Exposure: {}", event.getName(), marketCatalogue.getMarketName(), totalExposure);

        return totalExposure;
    }

    public Double calcNetLtdExposure(boolean includeUnMatched) throws Exception {
        MatchOddsMarket mom = new MatchOddsMarket(marketCatalogue);

        Double drawExposure = calcExposureForRunner(includeUnMatched, mom.getDrawRunner());
        Double homeExposure = calcExposureForRunner(includeUnMatched, mom.getHomeRunner());
        Double awayExposure = calcExposureForRunner(includeUnMatched, mom.getAwayRunner());

        Double totalExposure = drawExposure + homeExposure + awayExposure;

        //round to nearest penny
        totalExposure = totalExposure != 0 ? roundDownToNearestFraction(totalExposure, 0.01) : 0d;

        logger.debug("{}; {}; Total Exposure: {}", event.getName(), marketCatalogue.getMarketName(), totalExposure);

        return totalExposure;
    }

    public Double calcWorstCaseMatchOddsExposure() throws Exception {
        MatchOddsMarket mom = new MatchOddsMarket(marketCatalogue);

        Double drawExposure = calcExposureForRunner(true, mom.getDrawRunner());
        Double homeExposure = calcExposureForRunner(true, mom.getHomeRunner());
        Double awayExposure = calcExposureForRunner(true, mom.getAwayRunner());

        Double ifDraw = drawExposure - homeExposure - awayExposure;
        Double ifHome = homeExposure - drawExposure - awayExposure;
        Double ifAway = awayExposure - drawExposure - homeExposure;

        Double worstCaseExposure = Math.min(Math.min(ifDraw, ifHome), ifAway);

        logger.debug("{}; {}; Total Exposure: {}", event.getName(), marketCatalogue.getMarketName(), worstCaseExposure);

        return worstCaseExposure;
    }

    public Double calcBestCaseMatchOddsExposure() throws Exception {
        MatchOddsMarket mom = new MatchOddsMarket(marketCatalogue);

        Double drawExposure = calcExposureForRunner(true, mom.getDrawRunner());
        Double homeExposure = calcExposureForRunner(true, mom.getHomeRunner());
        Double awayExposure = calcExposureForRunner(true, mom.getAwayRunner());

        Double ifDraw = drawExposure - homeExposure - awayExposure;
        Double ifHome = homeExposure - drawExposure - awayExposure;
        Double ifAway = awayExposure - drawExposure - homeExposure;

        Double bestCaseExposure = Math.max(Math.max(ifDraw, ifHome), ifAway);

        logger.debug("{}; {}; Total Exposure: {}", event.getName(), marketCatalogue.getMarketName(), bestCaseExposure);

        return bestCaseExposure;
    }


    private Double calcExposureForRunner(boolean includeUnMatched, Runner r) throws Exception {
        Double backUnderExposure = calcExposureForSide(r, Side.BACK, includeUnMatched);
        Double layUnderExposure = calcExposureForSide(r, Side.LAY, includeUnMatched);
        Double totalExposure = backUnderExposure - layUnderExposure;

        //totalExposure = Math.abs(totalExposure);
        return totalExposure;
    }

    public Double calcNetExposure() throws Exception {
        return calcNetExposure(false);
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
