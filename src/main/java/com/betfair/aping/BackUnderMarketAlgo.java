package com.betfair.aping;

import com.betfair.aping.com.betfair.aping.events.betting.Exposure;
import com.betfair.aping.com.betfair.aping.events.betting.OverUnderMarket;
import com.betfair.aping.com.betfair.aping.events.betting.Score;
import com.betfair.aping.com.betfair.aping.events.betting.ScoreEnum;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.Side;
import com.betfair.aping.exceptions.APINGException;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class BackUnderMarketAlgo {
    Gson gson = new Gson();

    private static double getSize() {
        try {
            return new Double((String) ApiNGDemo.getProp().get("BET_SIZE"));
        } catch (NumberFormatException e) {
            //returning the default value
            return new Double(0.01);
        }
    }

    public void process(Event event) throws Exception, APINGException {
        BetPlacer betPlacer = new BetPlacer();
        Score score = new Score(event);
        ScoreEnum previousScore = event.getScore();
        event.setScore(score.findScoreFromMarketOdds());
        System.out.println(event.getName() + ": Current Score: " + event.getScore() + ", Previous Score: " + previousScore);
        MarketCatalogue mc = event.getMarket().get(MarketType.OVER_UNDER_25);
//
//        if (i > 0 && !event.getScore().equals(previousScore)) {
//            System.out.println(event.getName() + ": GOOOOOOOAOAOAOAAAAAAALLLLL!!!!! Get the bets on!");
//        }

        if (mc != null) {
            if (isCandidateMarket(event)) {
                System.out.println("OPEN: Candidate Mkt Found:" + gson.toJson(event));
                Exposure exposure = new Exposure(mc);
                OverUnderMarket oum = new OverUnderMarket(mc);
                Runner runner = oum.getRunnerByName(OverUnderMarket.UNDER_2_5);

                Bet initialBet = getBet(mc, runner, Side.BACK);
                Bet cashOutBet = exposure.calcCashOutBet(initialBet, getCashOutProfitPercentage());
                List<Bet> bets = new ArrayList<Bet>();
                bets.add(initialBet);
                bets.add(cashOutBet);
                betPlacer.placeBets(bets);
            }
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
        bet.setSide(Side.BACK);
        bet.setSelectionId(runner.getSelectionId());

        return bet;
    }

    private boolean isCandidateMarket(Event event) throws Exception {
        MarketType marketType = MarketType.OVER_UNDER_25;
        MarketCatalogue marketCatalogue = event.getMarket().get(marketType);
        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
        Exposure exposure = new Exposure(marketCatalogue);
        Runner runner = oum.getRunnerByName(OverUnderMarket.UNDER_2_5);

        if (exposure.calcNetExposure() > 0.1) {
            //already bet on this market
            return false;
        }

        try {
            if (oum.getBack(runner, 0).getPrice() >= getOverUnderBackLimit()) {
                Score score = new Score(event);
                ScoreEnum correctScore = score.findScoreFromMarketOdds();
                if (correctScore.getTotalGoals() <= (marketType.getTotalGoals() - getSafetyGoalMargin())) {
                    System.out.println("Best Back Price: " + oum.getBack(runner, 0).toString());
                    return true;
                }
            }
        } catch (RuntimeException ex) {
            System.out.println(ex);
            return false;
        }
        return false;
    }

    private Double getOverUnderBackLimit() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("OVER_UNDER_BACK_LIMIT"));
    }

    private Double getCashOutProfitPercentage() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("CLOSE_OUT_PROFIT_PERCENTAGE"));
    }

    private Integer getSafetyGoalMargin() {
        return Integer.valueOf(ApiNGDemo.getProp().getProperty("SAFETY_GOAL_MARGIN", "2"));
    }
}