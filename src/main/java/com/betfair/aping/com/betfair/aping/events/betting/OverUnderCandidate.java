package com.betfair.aping.com.betfair.aping.events.betting;

import com.betfair.aping.entities.*;
import com.google.gson.Gson;
import org.junit.Test;

import java.util.List;

/**
 * Created by markwilliams on 23/08/2014.
 */
public class OverUnderCandidate {
    public final String under25Goals = "Under 2.5 Goals";
    public final String over25Goals = "Over 2.5 Goals";

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
}
