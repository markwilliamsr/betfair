package com.betfair.aping.com.betfair.aping.events.betting;

/**
 * Created by markwilliams on 23/08/2014.
 */
public class PriceIncrement {

    public static double getIncrement(double price) {
        if (price >= 1.01 && price < 2) {
            return 0.01;
        } else if (price >= 2 && price < 3) {
            return 0.02;
        } else if (price >= 3 && price < 4) {
            return 0.05;
        } else if (price >= 4 && price < 6) {
            return 0.1;
        } else if (price >= 6 && price < 10) {
            return 0.2;
        } else if (price >= 10 && price < 20) {
            return 0.5;
        } else if (price >= 20 && price < 30) {
            return 1;
        } else if (price >= 30 && price < 50) {
            return 2;
        } else if (price >= 50 && price < 100) {
            return 5;
        } else if (price >= 100 && price < 1000) {
            return 10;
        }
        throw new IndexOutOfBoundsException("Must supply a price between 1.01 and 1000");
    }
}
