package com.betfair.aping.algo;

import com.betfair.aping.ApiNGDemo;
import com.betfair.aping.IBetPlacer;
import com.betfair.aping.com.betfair.aping.events.betting.*;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.MarketTemp;
import com.betfair.aping.enums.OddsClassification;
import com.betfair.aping.enums.Side;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by markwilliams on 1/14/15.
 */
public abstract class MarketAlgo {
    public static final String WIN = "WIN";
    protected Logger logger = LoggerFactory.getLogger(MarketAlgo.class);
    protected Gson gson = new Gson();
    protected int MAX_PREV_SCORES = getScoreStabalizationIteration();
    IBetPlacer betPlacer;

    public IBetPlacer getBetPlacer() {
        return betPlacer;
    }

    public void setBetPlacer(IBetPlacer betPlacer) {
        this.betPlacer = betPlacer;
    }

    protected double getSize() {
        try {
            return new Double((String) ApiNGDemo.getProp().get(getAlgoType() + "_BET_SIZE"));
        } catch (NumberFormatException e) {
            //returning the default value
            return new Double(0.01);
        }
    }

    protected void classifyMarket(Event event) throws Exception {
        MarketClassification marketClassification = new MarketClassification();

        if (event.getMarketClassification() != null
                && event.getMarketClassification().getMarketTemp() != null
                && getTimeSinceMarketStart(event) > 1) {
            return;
        }
        try {
            MarketCatalogue marketCatalogue = event.getMarket().get(MarketType.MATCH_ODDS);
            if (marketCatalogue != null) {
                MatchOddsMarket mom = new MatchOddsMarket(marketCatalogue);
                Runner home = mom.getHomeRunner();
                Runner away = mom.getAwayRunner();
                Runner draw = mom.getDrawRunner();

                Double bestHomeBack = mom.getBack(home, 0).getPrice();
                Double bestAwayBack = mom.getBack(away, 0).getPrice();
                Double bestDrawBack = mom.getBack(draw, 0).getPrice();

                OddsClassification homeClassification = classifyOdds(bestHomeBack, WIN);
                OddsClassification awayClassification = classifyOdds(bestAwayBack, WIN);

                if (marketClassification.getMarketTemp() == null) {
                    marketClassification.setMarketTemp(MarketTemp.WARM);
                }

                if (homeClassification.equals(OddsClassification.HIGH) && awayClassification.equals(OddsClassification.HIGH)) {
                    marketClassification.setMarketTemp(MarketTemp.COLD);
                } else if (homeClassification.equals(OddsClassification.HIGH) && awayClassification.equals(OddsClassification.MED)) {
                    marketClassification.setMarketTemp(MarketTemp.WARM);
                } else if (homeClassification.equals(OddsClassification.HIGH) && awayClassification.equals(OddsClassification.LOW)) {
                    marketClassification.setMarketTemp(MarketTemp.HOT);
                } else if (homeClassification.equals(OddsClassification.MED) && awayClassification.equals(OddsClassification.HIGH)) {
                    marketClassification.setMarketTemp(MarketTemp.WARM);
                } else if (homeClassification.equals(OddsClassification.MED) && awayClassification.equals(OddsClassification.MED)) {
                    marketClassification.setMarketTemp(MarketTemp.COLD);
                } else if (homeClassification.equals(OddsClassification.MED) && awayClassification.equals(OddsClassification.LOW)) {
                    marketClassification.setMarketTemp(MarketTemp.COLD);
                } else if (homeClassification.equals(OddsClassification.LOW) && awayClassification.equals(OddsClassification.HIGH)) {
                    marketClassification.setMarketTemp(MarketTemp.HOT);
                } else if (homeClassification.equals(OddsClassification.LOW) && awayClassification.equals(OddsClassification.MED)) {
                    marketClassification.setMarketTemp(MarketTemp.WARM);
                } else if (homeClassification.equals(OddsClassification.LOW) && awayClassification.equals(OddsClassification.LOW)) {
                    marketClassification.setMarketTemp(MarketTemp.WARM);
                } else if (homeClassification.equals(OddsClassification.V_HIGH) || awayClassification.equals(OddsClassification.V_HIGH)) {
                    marketClassification.setMarketTemp(MarketTemp.XHOT);
                } else {
                    marketClassification.setMarketTemp(MarketTemp.COLD);
                }
                marketClassification.setAwayOdds(bestAwayBack);
                marketClassification.setHomeOdds(bestHomeBack);
                marketClassification.setDrawOdds(bestDrawBack);
                marketClassification.setHomeOddsClassification(homeClassification);
                marketClassification.setAwayOddsClassification(awayClassification);
            }
        } catch (RuntimeException e) {
            logger.error("Exception Classifying Market: ", e);
            marketClassification.setMarketTemp(MarketTemp.COLD);
        }
        event.setMarketClassification(marketClassification);
    }

    protected OddsClassification classifyOdds(Double odds, String type) {
        Map<OddsClassification, Double> oddsConfigurations = getOddsConfigurations(type);
        OddsClassification classification = OddsClassification.MED;

        if (odds >= oddsConfigurations.get(OddsClassification.V_HIGH)) {
            classification = OddsClassification.V_HIGH;
        } else if (odds >= oddsConfigurations.get(OddsClassification.HIGH)) {
            classification = OddsClassification.HIGH;
        } else if (odds >= oddsConfigurations.get(OddsClassification.MED)) {
            classification = OddsClassification.MED;
        } else if (odds >= oddsConfigurations.get(OddsClassification.LOW)) {
            classification = OddsClassification.LOW;
        }

        return classification;
    }

    protected void logEventName(Event event) {
        SimpleDateFormat dtf = new SimpleDateFormat("MMM dd HH:mm");
        DecimalFormat def = new DecimalFormat("0.00");

        if (isMarketStartingSoon(event)) {
            logger.info("{}; {} [H:{} A:{} D:{}]: Start: [{}], Elapsed [{}], C. Score: {}, P. Score: {}",
                    String.format("%1$-35s", event.getName()),
                    String.format("%1$4s", event.getMarketClassification().getMarketTemp()),
                    def.format(event.getMarketClassification().getHomeOdds()),
                    def.format(event.getMarketClassification().getAwayOdds()),
                    def.format(event.getMarketClassification().getDrawOdds()),
                    dtf.format(event.getOpenDate()), getTimeSinceMarketStart(event),
                    event.getScore(), event.getPreviousScores().toString());
        } else {
            logger.debug("{}; {} [H:{} A:{} D:{}]: Start: [{}], Elapsed [{}], C. Score: {}, P. Score: {}",
                    String.format("%1$-35s", event.getName()),
                    String.format("%1$4s", event.getMarketClassification().getMarketTemp()),
                    def.format(event.getMarketClassification().getHomeOdds()),
                    def.format(event.getMarketClassification().getAwayOdds()),
                    def.format(event.getMarketClassification().getDrawOdds()),
                    dtf.format(event.getOpenDate()), getTimeSinceMarketStart(event),
                    event.getScore(), event.getPreviousScores().toString());
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

    protected boolean isMarketStartedTooLongAgo(Event event, BaseMarket oum) {
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

    protected boolean isBackLaySpreadWithinBounds(Event event, BaseMarket oum, Runner runner) {
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

    protected MarketConfig getMarketConfig(MarketTemp marketTemp, MarketType marketType) {
        return getMarketConfigs().get(marketTemp).get(marketType);
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
        MarketConfig marketConfig = getMarketConfigs().get(event.getMarketClassification().getMarketTemp()).get(marketType);
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
        return Boolean.valueOf(ApiNGDemo.getProp().getProperty(getAlgoType() + "_MARKET_START_TIME_LIMIT_ON", "true"));
    }

    protected Boolean isSafetyOff() {
        return Boolean.valueOf(ApiNGDemo.getProp().getProperty(getAlgoType() + "_SAFETY_OFF", "false"));
    }

    protected Double getMinimumBetSize() {
        return Double.valueOf(ApiNGDemo.getProp().getProperty("MINIMUM_BET_SIZE"));
    }

    protected Double roundDownToNearestFraction(Double number, Double fractionAsDecimal) {
        Double factor = 1 / fractionAsDecimal;
        return Math.round((number - (fractionAsDecimal / 2)) * factor) / factor;
    }

    protected Double roundUpToNearestFraction(Double number, Double fractionAsDecimal) {
        Double factor = 1 / fractionAsDecimal;
        return Math.round((number + (fractionAsDecimal / 2)) * factor) / factor;
    }

    public Map<MarketTemp, Map<MarketType, MarketConfig>> getMarketConfigs() {
        Map<String, Map<String, Map<String, Double>>> rawMarketConfigurations = new HashMap<String, Map<String, Map<String, Double>>>();
        Map<MarketTemp, Map<MarketType, MarketConfig>> marketConfigurations = new HashMap<MarketTemp, Map<MarketType, MarketConfig>>();

        String prop = ApiNGDemo.getProp().getProperty(getAlgoType() + "_OVER_UNDER_LAY_LIMIT");

        rawMarketConfigurations = gson.fromJson(prop, rawMarketConfigurations.getClass());

        for (Map.Entry<String, Map<String, Map<String, Double>>> classification : rawMarketConfigurations.entrySet()) {
            Map<MarketType, MarketConfig> marketConfigs = new HashMap<MarketType, MarketConfig>();
            for (Map.Entry<String, Map<String, Double>> type : classification.getValue().entrySet()) {
                MarketConfig marketConfig = new MarketConfig();
                marketConfig.setLayLimit(type.getValue().get("LAY_LIMIT"));
                marketConfig.setLayTimeLimit(type.getValue().get("LAY_TIME_LIMIT").intValue());
                marketConfig.setExpLossLimit(type.getValue().get("EXP_LOSS_LIMIT"));
                marketConfig.setStakeLossLimit(type.getValue().get("STAKE_LOSS_LIMIT"));
                marketConfig.setCashOutProfitPercentage(type.getValue().get("CASH_OUT_PROFIT_PERCENTAGE"));
                marketConfig.setSingleGoalTimeLimit(type.getValue().get("SINGLE_GOAL_TIME_LIMIT"));
                marketConfigs.put(MarketType.valueOf(type.getKey()), marketConfig);
            }
            marketConfigurations.put(MarketTemp.valueOf(classification.getKey()), marketConfigs);
        }

        return marketConfigurations;
    }

    public Map<OddsClassification, Double> getOddsConfigurations(String type) {
        Map<String, Double> rawOddsConfigurations = new HashMap<String, Double>();
        Map<OddsClassification, Double> oddsConfigurations = new HashMap<OddsClassification, Double>();

        String prop = ApiNGDemo.getProp().getProperty(getAlgoType() + "_" + type + "_ODDS_CLASSIFICATION");

        rawOddsConfigurations = gson.fromJson(prop, rawOddsConfigurations.getClass());

        for (Map.Entry<String, Double> entry : rawOddsConfigurations.entrySet()) {
            oddsConfigurations.put(OddsClassification.valueOf(entry.getKey()), entry.getValue());
        }

        return oddsConfigurations;
    }
}
