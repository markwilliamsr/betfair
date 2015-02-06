package com.betfair.aping.events.betting;

import com.betfair.aping.ApiNGDemo;
import com.betfair.aping.IBetPlacer;
import com.betfair.aping.api.ApiNgJsonRpcOperations;
import com.betfair.aping.api.ApiNgOperations;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.Side;
import com.betfair.aping.exceptions.APINGException;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TestBetPlacer implements IBetPlacer {
    Gson gson = new Gson();
    SimpleDateFormat dtf = new SimpleDateFormat("yyyyMMdd.HHmmss.SSS");
    private Logger logger = LoggerFactory.getLogger(TestBetPlacer.class);
    private ApiNgOperations jsonOperations = ApiNgJsonRpcOperations.getInstance();
    private Event event;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {

        this.event = event;
    }

    public void placeBets(List<Bet> bets) throws APINGException {
        Calendar cal = Calendar.getInstance();
        List<PlaceInstruction> instructions = new ArrayList<PlaceInstruction>();
        String marketId = "";

        for (Bet bet : bets) {
            if (marketId.equals("")) {
                marketId = bet.getMarketId();
            } else if (!marketId.equals(bet.getMarketId())) {
                throw new IllegalArgumentException("Cannot mix markets in Bet submission list: MarketId1: " + marketId + ", MarketId2:" + bet.getMarketId());
            }

            if (isSafetyOff()) {
                List<Order> orders = getOrder(bet.getPriceSize().getSize(), bet.getPriceSize().getPrice(), bet.getSide());
                MarketBook marketBook = event.getMarket().get(MarketType.MATCH_ODDS).getMarketBook();
                Runner runner = marketBook.getRunners().get(bet.getSelectionId().intValue());
                if (runner.getOrders() == null) {
                    runner.setOrders(orders);
                } else {
                    runner.getOrders().addAll(orders);
                }
                logger.info("Your bet has been placed.");
            } else {
                logger.info("Safety is on, did NOT place any bets.");
            }
        }
    }

    private List<Order> getOrder(double sizeMatched, double price, Side side) {
        Order order = new Order();
        order.setSizeMatched(sizeMatched);
        order.setSizeRemaining(0d);
        order.setSide(side);
        order.setPrice(price);
        List<Order> orders = new ArrayList<Order>();
        orders.add(order);
        return orders;
    }

    public boolean isSafetyOff() {
        try {
            return Boolean.valueOf(ApiNGDemo.getProp().getProperty("SAFETY_OFF"));
        } catch (Exception e) {
            //returning the default value
            return true;
        }
    }
}