package com.betfair.aping.enums;

public enum ApiNgOperation {
    LISTEVENTTYPES("listEventTypes", "SPORTS_APING_V1_0", "betting"),
    LISTCOMPETITIONS("listCompetitions", "SPORTS_APING_V1_0", "betting"),
    LISTTIMERANGES("listTimeRanges", "SPORTS_APING_V1_0", "betting"),
    LISTEVENTS("listEvents", "SPORTS_APING_V1_0", "betting"),
    LISTMARKETTYPES("listMarketTypes", "SPORTS_APING_V1_0", "betting"),
    LISTCOUNTRIES("listCountries", "SPORTS_APING_V1_0", "betting"),
    LISTVENUES("listVenues", "SPORTS_APING_V1_0", "betting"),
    LISTMARKETCATALOGUE("listMarketCatalogue", "SPORTS_APING_V1_0", "betting"),
    LISTMARKETBOOK("listMarketBook", "SPORTS_APING_V1_0", "betting"),
    PLACEORDERS("placeOrders", "SPORTS_APING_V1_0", "betting"),
    CANCELORDERS("cancelOrders", "SPORTS_APING_V1_0", "betting"),
    ACCOUNTFUNDS("getAccountFunds", "ACCOUNTS_APING_V1_0", "account"),;

    private String operationName;
    private String apiType;
    private String urlExtension;

    private ApiNgOperation(String operationName, String apiType, String urlExtension) {
        this.operationName = operationName;
        this.apiType = apiType;
        this.urlExtension = urlExtension;
    }

    public String getUrlExtension() {
        return urlExtension;
    }

    public String getOperationName() {
        return operationName;
    }

    public String getApiType() {
        return apiType;
    }

}
