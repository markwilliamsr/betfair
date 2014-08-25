package com.betfair.aping.events.betting.overunder;

import com.betfair.aping.ApiNGDemo;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.ExecutionReportStatus;
import com.betfair.aping.enums.OrderType;
import com.betfair.aping.enums.PersistenceType;
import com.betfair.aping.enums.Side;
import com.betfair.aping.exceptions.APINGException;
import com.google.gson.Gson;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by markwilliams on 23/08/2014.
 */
public class OverUnderTest {

    public final String under25Goals = "Under 2.5 Goals";
    public final String over25Goals = "Over 2.5 Goals";

    @Test
    public void castTest() {
        Integer priceRange = ((int) Math.floor(2.02));
        log(priceRange.toString());
    }

    @Test
    public void overUnderBetTest() throws Exception {
        String jsonEvent = "{\"id\":\"27249641\",\"name\":\"Man City v Liverpool\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"Aug 25, 2014 8:00:00 PM\",\"market\":{\"OVER_UNDER_25\":{\"marketId\":\"1.115036531\",\"marketName\":\"Over/Under 2.5 Goals\",\"description\":{\"persistenceEnabled\":true,\"bspMarket\":false,\"marketTime\":\"Aug 25, 2014 8:00:00 PM\",\"suspendTime\":\"Aug 25, 2014 8:00:00 PM\",\"bettingType\":\"ODDS\",\"turnInPlayEnabled\":true,\"marketType\":\"OVER_UNDER_25\",\"marketBaseRate\":5.0,\"discountAllowed\":true,\"wallet\":\"UK wallet\",\"rules\":\"\",\"rulesHasDate\":true},\"runners\":[{\"selectionId\":47972,\"runnerName\":\"Under 2.5 Goals\",\"handicap\":0.0},{\"selectionId\":47973,\"runnerName\":\"Over 2.5 Goals\",\"handicap\":0.0}],\"competition\":{\"id\":\"31\",\"name\":\"Barclays Premier League\"},\"event\":{\"id\":\"27249641\",\"name\":\"Man City v Liverpool\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"Aug 25, 2014 8:00:00 PM\",\"market\":{}},\"marketBook\":{\"marketId\":\"1.115036531\",\"isMarketDataDelayed\":true,\"status\":\"OPEN\",\"betDelay\":0,\"bspReconciled\":false,\"complete\":true,\"inplay\":false,\"numberOfWinners\":1,\"numberOfRunners\":2,\"numberOfActiveRunners\":2,\"totalMatched\":5531.36,\"totalAvailable\":18293.52,\"crossMatching\":true,\"runnersVoidable\":false,\"version\":795300510,\"runners\":[{\"selectionId\":47972,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":2.48,\"size\":18.39},{\"price\":2.46,\"size\":23.84},{\"price\":2.4,\"size\":90.5}],\"availableToLay\":[{\"price\":2.5,\"size\":127.0},{\"price\":2.54,\"size\":380.25},{\"price\":2.56,\"size\":258.02}],\"tradedVolume\":[]}},{\"selectionId\":47973,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":1.67,\"size\":10.22},{\"price\":1.66,\"size\":385.84},{\"price\":1.65,\"size\":384.05}],\"availableToLay\":[{\"price\":1.68,\"size\":22.97},{\"price\":1.69,\"size\":184.02},{\"price\":1.7,\"size\":76.68}],\"tradedVolume\":[]}}]}},\"CORRECT_SCORE\":{\"marketId\":\"1.115036530\",\"marketName\":\"Correct Score\",\"description\":{\"persistenceEnabled\":true,\"bspMarket\":false,\"marketTime\":\"Aug 25, 2014 8:00:00 PM\",\"suspendTime\":\"Aug 25, 2014 8:00:00 PM\",\"bettingType\":\"ODDS\",\"turnInPlayEnabled\":true,\"marketType\":\"CORRECT_SCORE\",\"marketBaseRate\":5.0,\"discountAllowed\":true,\"wallet\":\"UK wallet\",\"rules\":\"\",\"rulesHasDate\":true},\"runners\":[{\"selectionId\":1,\"runnerName\":\"0 - 0\",\"handicap\":0.0},{\"selectionId\":4,\"runnerName\":\"0 - 1\",\"handicap\":0.0},{\"selectionId\":9,\"runnerName\":\"0 - 2\",\"handicap\":0.0},{\"selectionId\":16,\"runnerName\":\"0 - 3\",\"handicap\":0.0},{\"selectionId\":2,\"runnerName\":\"1 - 0\",\"handicap\":0.0},{\"selectionId\":3,\"runnerName\":\"1 - 1\",\"handicap\":0.0},{\"selectionId\":8,\"runnerName\":\"1 - 2\",\"handicap\":0.0},{\"selectionId\":15,\"runnerName\":\"1 - 3\",\"handicap\":0.0},{\"selectionId\":5,\"runnerName\":\"2 - 0\",\"handicap\":0.0},{\"selectionId\":6,\"runnerName\":\"2 - 1\",\"handicap\":0.0},{\"selectionId\":7,\"runnerName\":\"2 - 2\",\"handicap\":0.0},{\"selectionId\":14,\"runnerName\":\"2 - 3\",\"handicap\":0.0},{\"selectionId\":10,\"runnerName\":\"3 - 0\",\"handicap\":0.0},{\"selectionId\":11,\"runnerName\":\"3 - 1\",\"handicap\":0.0},{\"selectionId\":12,\"runnerName\":\"3 - 2\",\"handicap\":0.0},{\"selectionId\":13,\"runnerName\":\"3 - 3\",\"handicap\":0.0},{\"selectionId\":4506345,\"runnerName\":\"Any Unquoted \",\"handicap\":0.0}],\"competition\":{\"id\":\"31\",\"name\":\"Barclays Premier League\"},\"event\":{\"id\":\"27249641\",\"name\":\"Man City v Liverpool\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"Aug 25, 2014 8:00:00 PM\",\"market\":{}},\"marketBook\":{\"marketId\":\"1.115036530\",\"isMarketDataDelayed\":true,\"status\":\"OPEN\",\"betDelay\":0,\"bspReconciled\":false,\"complete\":true,\"inplay\":false,\"numberOfWinners\":1,\"numberOfRunners\":17,\"numberOfActiveRunners\":17,\"totalMatched\":4623.37,\"totalAvailable\":20971.3,\"crossMatching\":true,\"runnersVoidable\":false,\"version\":795300526,\"runners\":[{\"selectionId\":1,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":17.5,\"size\":168.32},{\"price\":17.0,\"size\":33.69},{\"price\":16.5,\"size\":45.3}],\"availableToLay\":[{\"price\":18.0,\"size\":58.57},{\"price\":18.5,\"size\":109.87},{\"price\":19.5,\"size\":76.68}],\"tradedVolume\":[]}},{\"selectionId\":4,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":18.5,\"size\":153.87},{\"price\":18.0,\"size\":27.77},{\"price\":17.5,\"size\":28.57}],\"availableToLay\":[{\"price\":20.0,\"size\":2.47},{\"price\":21.0,\"size\":133.08},{\"price\":22.0,\"size\":55.12}],\"tradedVolume\":[]}},{\"selectionId\":9,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":34.0,\"size\":89.12},{\"price\":32.0,\"size\":15.62},{\"price\":30.0,\"size\":16.66}],\"availableToLay\":[{\"price\":40.0,\"size\":51.77},{\"price\":50.0,\"size\":3.42},{\"price\":60.0,\"size\":3.42}],\"tradedVolume\":[]}},{\"selectionId\":16,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":70.0,\"size\":6.72},{\"price\":60.0,\"size\":5.93},{\"price\":50.0,\"size\":50.0}],\"availableToLay\":[{\"price\":90.0,\"size\":3.0},{\"price\":95.0,\"size\":2.0},{\"price\":100.0,\"size\":16.77}],\"tradedVolume\":[]}},{\"selectionId\":2,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":10.5,\"size\":1141.58},{\"price\":10.0,\"size\":175.0},{\"price\":9.8,\"size\":103.02}],\"availableToLay\":[{\"price\":11.0,\"size\":2.11},{\"price\":12.0,\"size\":128.28},{\"price\":12.5,\"size\":36.71}],\"tradedVolume\":[]}},{\"selectionId\":3,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":9.2,\"size\":482.87},{\"price\":9.0,\"size\":159.03},{\"price\":8.8,\"size\":56.81}],\"availableToLay\":[{\"price\":10.0,\"size\":190.29},{\"price\":14.5,\"size\":37.0}],\"tradedVolume\":[]}},{\"selectionId\":8,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":15.5,\"size\":79.2},{\"price\":15.0,\"size\":80.68},{\"price\":14.0,\"size\":26.92}],\"availableToLay\":[{\"price\":17.5,\"size\":80.68},{\"price\":18.0,\"size\":84.67},{\"price\":25.0,\"size\":3.42}],\"tradedVolume\":[]}},{\"selectionId\":15,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":40.0,\"size\":67.61},{\"price\":38.0,\"size\":22.36},{\"price\":29.0,\"size\":50.0}],\"availableToLay\":[{\"price\":42.0,\"size\":15.6},{\"price\":44.0,\"size\":3.21},{\"price\":46.0,\"size\":18.17}],\"tradedVolume\":[]}},{\"selectionId\":5,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":11.5,\"size\":379.68},{\"price\":11.0,\"size\":70.45},{\"price\":10.5,\"size\":47.61}],\"availableToLay\":[{\"price\":13.0,\"size\":3.71},{\"price\":13.5,\"size\":120.48},{\"price\":14.0,\"size\":122.87}],\"tradedVolume\":[]}},{\"selectionId\":6,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":9.4,\"size\":1105.25},{\"price\":9.2,\"size\":663.03},{\"price\":9.0,\"size\":65.01}],\"availableToLay\":[{\"price\":9.8,\"size\":1007.66},{\"price\":10.0,\"size\":2253.69},{\"price\":10.5,\"size\":3037.35}],\"tradedVolume\":[]}},{\"selectionId\":7,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":14.5,\"size\":101.9},{\"price\":14.0,\"size\":80.68},{\"price\":13.0,\"size\":62.5}],\"availableToLay\":[{\"price\":16.0,\"size\":77.22},{\"price\":16.5,\"size\":3.48},{\"price\":19.0,\"size\":10.0}],\"tradedVolume\":[]}},{\"selectionId\":14,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":40.0,\"size\":99.01},{\"price\":38.0,\"size\":13.15},{\"price\":34.0,\"size\":10.61}],\"availableToLay\":[{\"price\":46.0,\"size\":51.14},{\"price\":120.0,\"size\":2.39}],\"tradedVolume\":[]}},{\"selectionId\":10,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":18.0,\"size\":85.25},{\"price\":17.5,\"size\":2.0},{\"price\":17.0,\"size\":86.23}],\"availableToLay\":[{\"price\":22.0,\"size\":50.0},{\"price\":23.0,\"size\":50.0},{\"price\":24.0,\"size\":80.68}],\"tradedVolume\":[]}},{\"selectionId\":11,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":15.5,\"size\":93.33},{\"price\":14.5,\"size\":34.48},{\"price\":13.0,\"size\":62.5}],\"availableToLay\":[{\"price\":16.5,\"size\":2.69},{\"price\":17.0,\"size\":7.71},{\"price\":17.5,\"size\":124.08}],\"tradedVolume\":[]}},{\"selectionId\":12,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":24.0,\"size\":43.45},{\"price\":23.0,\"size\":42.96},{\"price\":22.0,\"size\":43.93}],\"availableToLay\":[{\"price\":27.0,\"size\":84.55},{\"price\":28.0,\"size\":35.0},{\"price\":120.0,\"size\":4.91}],\"tradedVolume\":[]}},{\"selectionId\":13,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":50.0,\"size\":58.0},{\"price\":48.0,\"size\":131.45},{\"price\":36.0,\"size\":50.0}],\"availableToLay\":[{\"price\":55.0,\"size\":140.94},{\"price\":60.0,\"size\":151.71},{\"price\":65.0,\"size\":198.34}],\"tradedVolume\":[]}},{\"selectionId\":4506345,\"handicap\":0.0,\"status\":\"ACTIVE\",\"totalMatched\":0.0,\"ex\":{\"availableToBack\":[{\"price\":7.2,\"size\":184.66},{\"price\":6.8,\"size\":73.52},{\"price\":6.0,\"size\":41.66}],\"availableToLay\":[{\"price\":7.6,\"size\":2.6},{\"price\":7.8,\"size\":84.67},{\"price\":8.6,\"size\":2.0}],\"tradedVolume\":[]}}]}}}}";
        Gson gson = new Gson();
        Event e = gson.fromJson(jsonEvent, Event.class);
        MarketCatalogue mk = e.getMarket().get(MarketType.OVER_UNDER_25);
        MarketBook mb = e.getMarket().get(MarketType.OVER_UNDER_25).getMarketBook();

        RunnerCatalog rc = getRunnerByName(mk.getRunners(), under25Goals);
        log(rc.toString());

        Runner r = getRunnerBySelectionId(mb.getRunners(), rc.getSelectionId());
        log(r.toString());

        PriceSize back = getBack(r, 0);
        log(getBack(r, 0).toString());
        log(getBack(r, 1).toString());
        log(getBack(r, 2).toString());

        PriceSize lay = getLay(r, 0);
        log(lay.toString());
    }

    public RunnerCatalog getRunnerByName(List<RunnerCatalog> runners, String runnerName) throws Exception {
        for (RunnerCatalog r : runners) {
            if (r.getRunnerName().equals(runnerName)) {
                return r;
            }
        }
        throw new Exception("Runner not found: " + runnerName);
    }

    public Runner getRunnerBySelectionId(List<Runner> runners, long selectionId) throws Exception {
        for (Runner r : runners) {
            if (r.getSelectionId() == selectionId) {
                return r;
            }
        }
        throw new Exception("Runner not found: " + selectionId);
    }

    public PriceSize getBack(Runner runner, int position) {
        return runner.getEx().getAvailableToBack().get(position);
    }

    public PriceSize getLay(Runner runner, int position) {
        return runner.getEx().getAvailableToLay().get(position);
    }

    public void log(String s) {
        System.out.println(s);
    }

    @Test
    public void calendarTest() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd.HHmmss");
        System.out.println(df.format(cal.getTime()));
    }

    @Test
    public void safetyTest() {
        boolean safety = Boolean.valueOf("trues");

        System.out.println(safety);
    }
}
