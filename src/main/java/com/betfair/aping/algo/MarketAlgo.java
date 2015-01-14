package com.betfair.aping.algo;

import com.betfair.aping.ApiNGDemo;
import com.betfair.aping.com.betfair.aping.events.betting.*;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.MarketClassification;
import com.betfair.aping.enums.OddsClassification;
import com.betfair.aping.enums.Side;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by markwilliams on 1/14/15.
 */
public abstract class MarketAlgo {
    protected Logger logger = LoggerFactory.getLogger(MarketAlgo.class);
    protected Gson gson = new Gson();
    protected int MAX_PREV_SCORES = getScoreStabalizationIteration();

    protected double getSize() {
        try {
            return new Double((String) ApiNGDemo.getProp().get(getAlgoType() + "_BET_SIZE"));
        } catch (NumberFormatException e) {
            //returning the default value
            return new Double(0.01);
        }
    }

    protected void classifyMarket(Event event) throws Exception {

        if (event.getMarketClassification() != null && getTimeSinceMarketStart(event) > 1) {
            return;
        }
        try {
            MatchOddsMarket mom = new MatchOddsMarket(event.getMarket().get(MarketType.MATCH_ODDS));
            Runner home = mom.getHomeRunner();
            Runner away = mom.getAwayRunner();

            Double bestHomeBack = mom.getBack(home, 0).getPrice();
            Double bestAwayBack = mom.getBack(away, 0).getPrice();

            OddsClassification homeClassification = classifyOdds(bestHomeBack);
            OddsClassification awayClassification = classifyOdds(bestAwayBack);

            if (event.getMarketClassification() == null) {
                event.setMarketClassification(MarketClassification.WARM);
            }

            if (homeClassification.equals(OddsClassification.HIGH) && awayClassification.equals(OddsClassification.HIGH)) {
                event.setMarketClassification(MarketClassification.COLD);
            } else if (homeClassification.equals(OddsClassification.HIGH) && awayClassification.equals(OddsClassification.MED)) {
                event.setMarketClassification(MarketClassification.WARM);
            } else if (homeClassification.equals(OddsClassification.HIGH) && awayClassification.equals(OddsClassification.LOW)) {
                event.setMarketClassification(MarketClassification.HOT);
            } else if (homeClassification.equals(OddsClassification.MED) && awayClassification.equals(OddsClassification.HIGH)) {
                event.setMarketClassification(MarketClassification.WARM);
            } else if (homeClassification.equals(OddsClassification.MED) && awayClassification.equals(OddsClassification.MED)) {
                event.setMarketClassification(MarketClassification.COLD);
            } else if (homeClassification.equals(OddsClassification.MED) && awayClassification.equals(OddsClassification.LOW)) {
                event.setMarketClassification(MarketClassification.COLD);
            } else if (homeClassification.equals(OddsClassification.LOW) && awayClassification.equals(OddsClassification.HIGH)) {
                event.setMarketClassification(MarketClassification.HOT);
            } else if (homeClassification.equals(OddsClassification.LOW) && awayClassification.equals(OddsClassification.MED)) {
                event.setMarketClassification(MarketClassification.WARM);
            } else if (homeClassification.equals(OddsClassification.LOW) && awayClassification.equals(OddsClassification.LOW)) {
                event.setMarketClassification(MarketClassification.WARM);
            }
        }
        catch (RuntimeException e) {
            logger.error("Exception Classifying Market: ", e);
            event.setMarketClassification(MarketClassification.COLD);
        }
    }

    private OddsClassification classifyOdds(Double odds) {
        Map<OddsClassification, Double> oddsConfigurations = getOddsConfigurations();
        OddsClassification classification = OddsClassification.MED;

        if (odds > oddsConfigurations.get(OddsClassification.HIGH)) {
            classification = OddsClassification.HIGH;
        } else if (odds > oddsConfigurations.get(OddsClassification.MED)) {
            classification = OddsClassification.MED;
        } else if (odds > oddsConfigurations.get(OddsClassification.LOW)) {
            classification = OddsClassification.LOW;
        }

        return classification;
    }

    protected void logEventName(Event event) {
        SimpleDateFormat df = new SimpleDateFormat("MMM dd HH:mm");

        if (isMarketStartingSoon(event)) {
            logger.info("{}; {}: Starts At: [{}], Elapsed [{}], Current Score: {}, Previous Score: {}",
                    String.format("%1$-35s", event.getName()), String.format("%1$4s", event.getMarketClassification()), df.format(event.getOpenDate()), getTimeSinceMarketStart(event), event.getScore(), event.getPreviousScores().toString());
        } else {
            logger.debug("{}; {}: Starts At: [{}], Elapsed [{}], Current Score: {}, Previous Score: {}",
                    String.format("%1$-35s", event.getName()), String.format("%1$4s", event.getMarketClassification()), df.format(event.getOpenDate()), getTimeSinceMarketStart(event), event.getScore(), event.getPreviousScores().toString());
        }
    }

    protected void updateEventScore(Event event) {
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

    protected boolean isScoreChanging(Event event) {
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

    protected long getTimeSinceMarketStart(Event event) {
        Date eventStart = event.getOpenDate();
        Date now = Calendar.getInstance().getTime();

        long diffMs = now.getTime() - eventStart.getTime();
        long diffSec = diffMs / 1000;
        long min = diffSec / 60;

        return min;
    }

    protected boolean isMarketStartingSoon(Event event) {
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

    protected boolean isMarketStartedTooLongAgo(Event event, OverUnderMarket oum) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1 * getMinutesAfterMarketStartTimeToBet(event, oum.getMarketType()));
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

    protected boolean isBackLaySpreadWithinBounds(Event event, OverUnderMarket oum, Runner runner) {
        Double back = oum.getBack(runner, 0).getPrice();
        Double lay = oum.getLay(runner, 0).getPrice();
        Long spread = 0l;

        if (back != null && lay != null) {
            Double increment = PriceIncrement.getIncrement(back);
            spread = Math.round((lay - back) / increment);
            if (spread <= getMaxBackLaySpread()) {
                return true;
            }
        }
        logger.info("{}, {}; Back Lay Spread not within bounds. Lay: {}, Back: {}, Spread: {}", event.getName(), oum.getMarketType().getMarketName(), lay, back, spread);
        return false;
    }

    protected MarketConfig getMarketConfig(MarketClassification marketClassification, MarketType marketType) {
        return getMarketConfigurations().get(marketClassification).get(marketType);
    }

    protected Bet getBetForMarket(MarketCatalogue marketCatalogue, Runner runner, Side side) {
        OverUnderMarket oum = new OverUnderMarket(marketCatalogue);
        Bet bet = new Bet();
        PriceSize priceSize = new PriceSize();

        priceSize.setPrice(oum.getPrice(runner, 0, side).getPrice());

        bet.setMarketId(marketCatalogue.getMarketId());
        bet.setPriceSize(priceSize);
        bet.setSide(side);
        bet.setSelectionId(runner.getSelectionId());

        return bet;
    }

    protected abstract String getAlgoType();

    private Integer getMinutesBeforeMarketStartTimeToBet() {
        return Integer.valueOf(ApiNGDemo.getProp().getProperty(getAlgoType() + "_MINUTES_BEFORE_MARKET_START"));
    }

    protected Integer getMinutesAfterMarketStartTimeToBet(Event event, MarketType marketType) {
        MarketConfig marketConfig = getMarketConfigurations().get(event.getMarketClassification()).get(marketType);
        return marketConfig.getLayTimeLimit();
    }

    protected Integer getTotalGoalLimit() {
        return Integer.valueOf(ApiNGDemo.getProp().getProperty(getAlgoType() + "_TOTAL_GOAL_LIMIT"));
    }

    protected Integer getMaxBackLaySpread() {
        return Integer.valueOf(ApiNGDemo.getProp().getProperty("MAX_BACK_LAY_SPREAD_INCREMENT"));
    }

    private Integer getScoreStabalizationIteration() {
        return Integer.valueOf(ApiNGDemo.getProp().getProperty("GOAL_STABALIZATION_ITERATION_COUNT"));
    }

    private Boolean isMarketStartTimeLimitOn() {
        return Boolean.valueOf(ApiNGDemo.getProp().getProperty("MARKET_START_TIME_LIMIT_ON", "true"));
    }

    protected Boolean isSafetyOff() {
        return Boolean.valueOf(ApiNGDemo.getProp().getProperty(getAlgoType() + "_SAFETY_OFF", "false"));
    }

    protected Double getMinimumBetSize() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("MINIMUM_BET_SIZE"));
    }

    private Double roundDownToNearestFraction(Double number, Double fractionAsDecimal) {
        Double factor = 1 / fractionAsDecimal;
        return Math.round((number - (fractionAsDecimal / 2)) * factor) / factor;
    }

    protected Double roundUpToNearestFraction(Double number, Double fractionAsDecimal) {
        Double factor = 1 / fractionAsDecimal;
        return Math.round((number + (fractionAsDecimal / 2)) * factor) / factor;
    }

    public Map<MarketClassification, Map<MarketType, MarketConfig>> getMarketConfigurations() {
        Map<String, Map<String, Map<String, Double>>> rawMarketConfigurations = new HashMap<String, Map<String, Map<String, Double>>>();
        Map<MarketClassification, Map<MarketType, MarketConfig>> marketConfigurations = new HashMap<MarketClassification, Map<MarketType, MarketConfig>>();

        String prop = ApiNGDemo.getProp().getProperty(getAlgoType() + "_OVER_UNDER_LAY_LIMIT");

        rawMarketConfigurations = gson.fromJson(prop, rawMarketConfigurations.getClass());

        for (Map.Entry<String, Map<String, Map<String, Double>>> classification : rawMarketConfigurations.entrySet()) {
            Map<MarketType, MarketConfig> marketConfigs = new HashMap<MarketType, MarketConfig>();
            for (Map.Entry<String, Map<String, Double>> type : classification.getValue().entrySet()) {
                MarketConfig marketConfig = new MarketConfig();
                marketConfig.setLayLimit(type.getValue().get("LAY_LIMIT"));
                marketConfig.setLayTimeLimit(type.getValue().get("LAY_TIME_LIMIT").intValue());
                marketConfigs.put(MarketType.valueOf(type.getKey()), marketConfig);
            }
            marketConfigurations.put(MarketClassification.valueOf(classification.getKey()), marketConfigs);
        }

        return marketConfigurations;
    }

    public Map<OddsClassification, Double> getOddsConfigurations() {
        Map<String, Double> rawOddsConfigurations = new HashMap<String, Double>();
        Map<OddsClassification, Double> oddsConfigurations = new HashMap<OddsClassification, Double>();

        String prop = ApiNGDemo.getProp().getProperty(getAlgoType() + "_ODDS_CLASSIFICATION");

        rawOddsConfigurations = gson.fromJson(prop, rawOddsConfigurations.getClass());

        for (Map.Entry<String, Double> entry : rawOddsConfigurations.entrySet()) {
            oddsConfigurations.put(OddsClassification.valueOf(entry.getKey()), entry.getValue());
        }

        return oddsConfigurations;
    }
}
