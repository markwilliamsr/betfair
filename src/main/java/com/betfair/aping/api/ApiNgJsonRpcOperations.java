package com.betfair.aping.api;

import com.betfair.aping.ApiNGDemo;
import com.betfair.aping.containers.*;
import com.betfair.aping.entities.*;
import com.betfair.aping.enums.*;
import com.betfair.aping.exceptions.APINGException;
import com.betfair.aping.util.JsonConverter;
import com.betfair.aping.util.JsonrpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ApiNgJsonRpcOperations extends ApiNgOperations {

    private static ApiNgJsonRpcOperations instance = null;
    private Logger logger = LoggerFactory.getLogger(ApiNgJsonRpcOperations.class);
    String appKey;
    String sessionToken;

    private ApiNgJsonRpcOperations() {
    }

    public static ApiNgJsonRpcOperations getInstance() {
        if (instance == null) {
            instance = new ApiNgJsonRpcOperations();
        }
        return instance;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public List<EventTypeResult> listEventTypes(MarketFilter filter) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(FILTER, filter);
        params.put(LOCALE, locale);
        String result = getInstance().makeRequest(ApiNgOperation.LISTEVENTTYPES, params);
        if (ApiNGDemo.isDebug())
            logger.info("\nResponse: " + result);

        EventTypeResultContainer container = JsonConverter.convertFromJson(result, EventTypeResultContainer.class);
        if (container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult();

    }

    public List<CompetitionResult> listCompetitions(MarketFilter filter) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(FILTER, filter);
        params.put(LOCALE, locale);
        String result = getInstance().makeRequest(ApiNgOperation.LISTCOMPETITIONS, params);
        if (ApiNGDemo.isDebug())
            logger.info("\nResponse: " + result);

        CompetitionResultContainer container = JsonConverter.convertFromJson(result, CompetitionResultContainer.class);
        if (container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult();

    }

    public List<MarketBook> listMarketBook(List<String> marketIds, PriceProjection priceProjection, OrderProjection orderProjection,
                                           MatchProjection matchProjection, String currencyCode) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(LOCALE, locale);
        params.put(MARKET_IDS, marketIds);
        params.put(PRICE_PROJECTION, priceProjection);
        params.put(ORDER_PROJECTION, orderProjection);
        params.put(MATCH_PROJECTION, matchProjection);
        String result = getInstance().makeRequest(ApiNgOperation.LISTMARKETBOOK, params);
        if (ApiNGDemo.isDebug())
            logger.info("\nResponse: " + result);

        ListMarketBooksContainer container = JsonConverter.convertFromJson(result, ListMarketBooksContainer.class);

        if (container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult();


    }

    public List<MarketCatalogue> listMarketCatalogue(MarketFilter filter, Set<MarketProjection> marketProjection,
                                                     MarketSort sort, String maxResult) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(LOCALE, locale);
        params.put(FILTER, filter);
        params.put(SORT, sort);
        params.put(MAX_RESULT, maxResult);
        params.put(MARKET_PROJECTION, marketProjection);
        String result = getInstance().makeRequest(ApiNgOperation.LISTMARKETCATALOGUE, params);
        if (ApiNGDemo.isDebug())
            logger.info("\nResponse: " + result);

        ListMarketCatalogueContainer container = JsonConverter.convertFromJson(result, ListMarketCatalogueContainer.class);

        if (container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult();
    }

    @Override
    public List<EventResult> listEvents(MarketFilter filter) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(LOCALE, locale);
        params.put(FILTER, filter);

        String result = getInstance().makeRequest(ApiNgOperation.LISTEVENTS, params);
        if (ApiNGDemo.isDebug())
            logger.info("\nResponse: " + result);

        ListEventsContainer container = JsonConverter.convertFromJson(result, ListEventsContainer.class);

        if (container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult();

    }

    public PlaceExecutionReport placeOrders(String marketId, List<PlaceInstruction> instructions, String customerRef) throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(LOCALE, locale);
        params.put(MARKET_ID, marketId);
        params.put(INSTRUCTIONS, instructions);
        params.put(CUSTOMER_REF, customerRef);
        String result = getInstance().makeRequest(ApiNgOperation.PLACEORDERS, params);
        if (ApiNGDemo.isDebug())
            logger.info("\nResponse: " + result);

        PlaceOrdersContainer container = JsonConverter.convertFromJson(result, PlaceOrdersContainer.class);

        if (container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult();

    }

    protected String makeRequest(ApiNgOperation operation, Map<String, Object> params) {
        String requestString;
        //Handling the JSON-RPC request
        JsonrpcRequest request = new JsonrpcRequest();
        request.setId("1");
        request.setMethod(ApiNGDemo.getProp().getProperty(operation.getApiType()) + operation.getOperationName());
        request.setParams(params);

        requestString = JsonConverter.convertToJson(request);
        if (ApiNGDemo.isDebug())
            logger.info("\nRequest: " + requestString);

        //We need to pass the "sendPostRequest" method a string in util format:  requestString
        HttpUtil requester = new HttpUtil();
        return requester.sendPostRequestJsonRpc(requestString, operation, appKey, sessionToken);

    }

    @Override
    public AccountFundsResponse getAccountFunds() throws APINGException {
        Map<String, Object> params = new HashMap<String, Object>();
        String result = getInstance().makeRequest(ApiNgOperation.ACCOUNTFUNDS, params);
        if (ApiNGDemo.isDebug())
            logger.info("\nResponse: " + result);

        AccountFundsContainer container = JsonConverter.convertFromJson(result, AccountFundsContainer.class);

        if (container.getError() != null)
            throw container.getError().getData().getAPINGException();

        return container.getResult();
    }
}

