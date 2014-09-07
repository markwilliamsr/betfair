package com.betfair.aping;

import com.betfair.aping.com.betfair.aping.events.betting.Exposure;
import com.betfair.aping.com.betfair.aping.events.betting.OverUnderMarket;
import com.betfair.aping.com.betfair.aping.events.betting.Score;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.MarketStatus;
import com.betfair.aping.enums.Side;
import com.betfair.aping.exceptions.APINGException;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BackUnderMarketAlgo implements MarketAlgo {
    Gson gson = new Gson();

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
        updateEventScore(event);
        System.out.println(event.getName() + ": Starts At: [" + event.getOpenDate() + "], Current Score: " + event.getScore() + ", Previous Score: " + "TODO!");
        MarketCatalogue mc = getMarketCatalogueForTotalGoals(event);

        if (mc != null) {
            if (isCandidateMarket(event)) {
                System.out.println("OPEN: Candidate Mkt Found:" + gson.toJson(event));
                Exposure exposure = new Exposure(mc);
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

    private void updateEventScore(Event event) {
        Score score = new Score(event);
        event.setScore(score.findScoreFromMarketOdds());
    }

    private Bet getBet(MarketCatalogue marketCatalogue, Runner runner, Side side) {
        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
        Bet bet = new Bet();
        PriceSize priceSize = new PriceSize();

        priceSize.setSize(getSize());

        priceSize.setPrice(oum.getPrice(runner, 0, side).getPrice());

        bet.setMarketId(marketCatalogue.getMarketId());
        bet.setPriceSize(priceSize);
        bet.setSide(Side.BACK);
        bet.setSelectionId(runner.getSelectionId());

        return bet;
    }

    private boolean isCandidateMarket(Event event) throws Exception {
        MarketCatalogue marketCatalogue = getMarketCatalogueForTotalGoals(event);

        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
        Runner runner = oum.getUnderRunner();

        if (!marketCatalogue.getMarketBook().getStatus().equals(MarketStatus.OPEN)){
            return false;
        }

        if (!isMarketStartingSoon(event)) {
            return false;
        }

        if (isBetAlreadyOpen(marketCatalogue)) {
            return false;
        }

        if (event.getScore().getTotalGoals() >= getTotalGoalLimit()) {
            //don't bet on some goalfest
            return false;
        }

        try {
            if (oum.getBack(runner, 0).getPrice() >= getOverUnderBackLimit()) {
                System.out.println("Best Back Price: " + oum.getBack(runner, 0).toString());
                return true;
            }
        } catch (RuntimeException ex) {
            System.out.println(ex);
            return false;
        }
        return false;
    }

    private MarketCatalogue getMarketCatalogueForTotalGoals(Event event) {
        Integer totalGoalsForMarket = event.getScore().getTotalGoals() + getSafetyGoalMargin();
        MarketType marketType = MarketType.fromTotalGoals(totalGoalsForMarket);

        return event.getMarket().get(marketType);
    }

    private boolean isBetAlreadyOpen(MarketCatalogue marketCatalogue) throws Exception {
        Exposure exposure = new Exposure(marketCatalogue);
        if (exposure.calcNetExposure() > 0.1) {
            //already bet on this market
            return true;
        }
        return false;
    }

    private boolean isMarketStartingSoon(Event event) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        if (isMarketStartTimeLimitOn() && event.getOpenDate().before(calendar.getTime())) {
            //don't bet on something that won't start for a while
            return true;
        }
        return false;
    }

    private Double getOverUnderBackLimit() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("OVER_UNDER_BACK_LIMIT"));
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

    private Boolean isMarketStartTimeLimitOn() {
        return Boolean.valueOf(ApiNGDemo.getProp().getProperty("MARKET_START_TIME_LIMIT_ON", "2"));
    }
}