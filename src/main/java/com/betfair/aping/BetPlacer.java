package com.betfair.aping;

import com.betfair.aping.api.ApiNgJsonRpcOperations;
import com.betfair.aping.api.ApiNgOperations;
import com.betfair.aping.entities.Bet;
import com.betfair.aping.entities.LimitOrder;
import com.betfair.aping.entities.PlaceExecutionReport;
import com.betfair.aping.entities.PlaceInstruction;
import com.betfair.aping.enums.ExecutionReportStatus;
import com.betfair.aping.enums.OrderType;
import com.betfair.aping.enums.PersistenceType;
import com.betfair.aping.exceptions.APINGException;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BetPlacer {
    private Logger logger = LoggerFactory.getLogger(BetPlacer.class);
            Gson gson = new Gson();
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat dtf = new SimpleDateFormat("yyyyMMdd.HHmmss");
    private ApiNgOperations jsonOperations = ApiNgJsonRpcOperations.getInstance();

    public void placeBets(List<Bet> bets) throws APINGException {
        List<PlaceInstruction> instructions = new ArrayList<PlaceInstruction>();
        String marketId = "";

        for (Bet bet : bets) {
            if (marketId.equals("")) {
                marketId = bet.getMarketId();
            } else if (!marketId.equals(bet.getMarketId())) {
                throw new IllegalArgumentException("Cannot mix markets in Bet submission list: MarketId1: " + marketId + ", MarketId2:" + bet.getMarketId());
            }

            LimitOrder limitOrder = new LimitOrder();
            limitOrder.setPersistenceType(PersistenceType.PERSIST);
            limitOrder.setPrice(bet.getPriceSize().getPrice());
            limitOrder.setSize(bet.getPriceSize().getSize());

            PlaceInstruction instruction = new PlaceInstruction();
            instruction.setHandicap(0);
            instruction.setOrderType(OrderType.LIMIT);
            instruction.setSide(bet.getSide());
            instruction.setLimitOrder(limitOrder);
            instruction.setSelectionId(bet.getSelectionId());
            instructions.add(instruction);
        }
        String customerRef = "OU25:" + dtf.format(cal.getTime());

        if (isSafetyOff()) {
            PlaceExecutionReport placeBetResult = jsonOperations.placeOrders(marketId, instructions, customerRef);
            // Handling the operation result
            if (placeBetResult.getStatus() == ExecutionReportStatus.SUCCESS) {
                logger.info("Your bet has been placed!!");
                logger.info(gson.toJson(placeBetResult.getInstructionReports()));
            } else if (placeBetResult.getStatus() == ExecutionReportStatus.FAILURE) {
                logger.info("Your bet has NOT been placed :*( ");
                logger.info("The error is: " + placeBetResult.getErrorCode() + ": " + placeBetResult.getErrorCode().getMessage());
            }
        } else {
            logger.info("Safety is on, did NOT place any bets.");
        }
    }

    private boolean isSafetyOff() {
        try {
            return Boolean.valueOf(ApiNGDemo.getProp().getProperty("SAFETY_OFF"));
        } catch (Exception e) {
            //returning the default value
            return true;
        }
    }
}