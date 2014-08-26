package com.betfair.aping.com.betfair.aping.events.betting;

import com.betfair.aping.entities.*;
import com.betfair.aping.enums.Side;

import java.util.List;

/**
 * Created by markwilliams on 26/08/2014.
 */
public class Exposure {
    MarketCatalogue marketCatalogue;

    public Exposure(MarketCatalogue marketCatalogue) {
        this.marketCatalogue = marketCatalogue;
    }

    public Double calcExposureForSide(Runner runner, Side side) throws Exception {
        Double exposure = 0.0;
        List<Order> orders = runner.getOrders();

        if (orders == null) {
            return exposure;
        }

        for (Order order : orders) {
            if (order.getSide().equals(side)) {
                exposure += (order.getPrice() * order.getSizeMatched());
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

    public Double calcPnL(Runner runner) throws Exception {
        return (calcGrossExposure() / 2) - calcTotalStake(runner);
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

    public Double calcNetExposure() throws Exception {
        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
        Runner r = oum.getRunnerByName(OverUnderMarket.UNDER_2_5);

        Double backExposure = calcExposureForSide(r, Side.BACK);
        Double layExposure = calcExposureForSide(r, Side.LAY);
        Double totalExposure = backExposure - layExposure;

        System.out.println("Best Back: " + oum.getBack(r, 0).getPrice() + " Best Lay: " + oum.getLay(r, 0).getPrice());
        System.out.println("Back Exposure: " + backExposure + " Lay Exposure: " + layExposure + " Total Exposure: " + totalExposure);
        return totalExposure;
    }

    public Double calcGrossExposure() throws Exception {
        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
        Runner r = oum.getRunnerByName(OverUnderMarket.UNDER_2_5);

        Double backExposure = calcExposureForSide(r, Side.BACK);
        Double layExposure = calcExposureForSide(r, Side.LAY);
        Double totalExposure = backExposure + layExposure;

        System.out.println("Best Back: " + oum.getBack(r, 0).getPrice() + " Best Lay: " + oum.getLay(r, 0).getPrice());
        System.out.println("Back Exposure: " + backExposure + " Lay Exposure: " + layExposure + " Total Exposure: " + totalExposure);
        return totalExposure;
    }

    public CashOutBet calcCashOutBet() throws Exception {
        PriceSize priceSize = new PriceSize();
        CashOutBet cashOutBet = new CashOutBet();
        Double price = 0.0;
        Double cashOutSize = 0.0;
        Side side = null;

        Double totalExposure = calcNetExposure();

        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
        Runner r = oum.getRunnerByName(OverUnderMarket.UNDER_2_5);

        List<PriceSize> availableToBack = r.getEx().getAvailableToBack();
        List<PriceSize> availableToLay = r.getEx().getAvailableToLay();

        if (Math.abs(totalExposure) > 0) {
            if (totalExposure < 0) {
                //too much on the lay side
                price = availableToBack.get(0).getPrice();
                side = Side.BACK;
            } else {
                //too much on the back side
                price = availableToLay.get(0).getPrice();
                side = Side.LAY;
            }
            cashOutSize = Math.abs(totalExposure) / price;
        }

        priceSize.setPrice(price);
        priceSize.setSize(cashOutSize);

        cashOutBet.setPriceSize(priceSize);
        cashOutBet.setSide(side);
        cashOutBet.setSelectionId(r.getSelectionId());
        cashOutBet.setMarketId(marketCatalogue.getMarketId());

        System.out.println("Total Exposure: " + totalExposure + ", Side: " + side + ", cashOutBet: " + priceSize + ", Price: " + price);
        return cashOutBet;
    }
}
