package com.betfair.aping.api;

import com.betfair.aping.entities.*;
import com.betfair.aping.enums.*;
import com.betfair.aping.exceptions.APINGException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public abstract class ApiNgOperations {
	protected final String FILTER = "filter";
    protected final String LOCALE = "locale";
    protected final String SORT = "sort";
    protected final String MAX_RESULT = "maxResults";
    protected final String MARKET_IDS = "marketIds";
    protected final String MARKET_ID = "marketId";
    protected final String INSTRUCTIONS = "instructions";
    protected final String CUSTOMER_REF = "customerRef";
    protected final String MARKET_PROJECTION = "marketProjection";
    protected final String PRICE_PROJECTION = "priceProjection";
    protected final String MATCH_PROJECTION = "matchProjection";
    protected final String ORDER_PROJECTION = "orderProjection";
    protected final String locale = Locale.getDefault().toString();

	public abstract List<EventTypeResult> listEventTypes(MarketFilter filter) throws APINGException;

    public abstract List<CompetitionResult> listCompetitions(MarketFilter filter) throws APINGException;

	public abstract List<MarketBook> listMarketBook(List<String> marketIds, PriceProjection priceProjection, OrderProjection orderProjection,
						MatchProjection matchProjection, String currencyCode) throws APINGException;

    public abstract List<MarketCatalogue> listMarketCatalogue(MarketFilter filter, Set<MarketProjection> marketProjection,
        MarketSort sort, String maxResult) throws APINGException;

    public abstract List<EventResult> listEvents(MarketFilter filter) throws APINGException;

	public abstract PlaceExecutionReport placeOrders(String marketId, List<PlaceInstruction> instructions, String customerRef) throws APINGException;

    public abstract CancelExecutionReport cancelOrders(String marketId, List<CancelInstruction> instructions, String customerRef) throws APINGException;

    protected abstract String makeRequest(ApiNgOperation operation, Map<String, Object> params) throws  APINGException;

    public abstract AccountFundsResponse getAccountFunds() throws APINGException;

}

