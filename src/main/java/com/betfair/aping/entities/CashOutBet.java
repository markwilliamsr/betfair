package com.betfair.aping.entities;

import com.betfair.aping.enums.Side;

/**
 * Created by markwilliams on 26/08/2014.
 */
public class CashOutBet {
    PriceSize priceSize;
    Side side;
    String marketId;
    Long selectionId;

    public String getMarketId() {
        return marketId;
    }

    public void setMarketId(String marketId) {
        this.marketId = marketId;
    }

    public Long getSelectionId() {
        return selectionId;
    }

    public void setSelectionId(Long selectionId) {
        this.selectionId = selectionId;
    }

    public PriceSize getPriceSize() {
        return priceSize;
    }

    public void setPriceSize(PriceSize priceSize) {
        this.priceSize = priceSize;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

}
