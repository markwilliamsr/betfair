package com.betfair.aping.events.betting;

import com.betfair.aping.com.betfair.aping.events.betting.PriceIncrement;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by markwilliams on 23/08/2014.
 */
public class PriceIncrementTest {
    @Test
    public void incrementTest1() {
        assertEquals(0.01, PriceIncrement.getIncrement(1.06), 0);
    }

    @Test
    public void incrementTest2() {
        assertEquals(0.02, PriceIncrement.getIncrement(2.06), 0);
    }

    @Test
    public void incrementTest3() {
        assertEquals(0.05, PriceIncrement.getIncrement(3.1), 0);
    }

    @Test
    public void incrementTest4() {
        assertEquals(0.1, PriceIncrement.getIncrement(5.4), 0);
    }

    @Test
    public void incrementTest5() {
        assertEquals(0.2, PriceIncrement.getIncrement(8), 0);
    }

    @Test
    public void incrementTest6() {
        assertEquals(0.5, PriceIncrement.getIncrement(15), 0);
    }

    @Test
    public void incrementTest7() {
        assertEquals(1, PriceIncrement.getIncrement(26), 0);
    }

    @Test
    public void incrementTest8() {
        assertEquals(2, PriceIncrement.getIncrement(40), 0);
    }

    @Test
    public void incrementTest9() {
        assertEquals(5, PriceIncrement.getIncrement(60), 0);
    }

    @Test
    public void incrementTest10() {
        assertEquals(10, PriceIncrement.getIncrement(160), 0);
    }

    @Test
    public void incrementTest11() {
        Double tickSize = PriceIncrement.getIncrement(3);
        Double betPrice = 3d;
        betPrice = betPrice - tickSize;
        System.out.println(betPrice);
        Double newTick = PriceIncrement.getIncrement(betPrice);
        betPrice = roundDownToNearestFraction(betPrice, newTick);
        System.out.println(betPrice);
        assertEquals(2.94, betPrice, 0);
    }

    private Double roundDownToNearestFraction(Double number, Double fractionAsDecimal) {
        Double factor = 1 / fractionAsDecimal;
        return Math.round((number - (fractionAsDecimal / 2)) * factor) / factor;
    }
}

