package com.betfair.aping.login;

import com.betfair.aping.ApiNGDemo;
import com.betfair.aping.containers.ListEventsContainer;
import com.betfair.aping.entities.EventResult;
import com.betfair.aping.util.JsonConverter;
import com.google.gson.Gson;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static com.betfair.aping.login.LoginConstants.LOGIN_PROPERTIES_FILE;
import static org.junit.Assert.assertEquals;

/**
 * Created by markwilliams on 17/08/2014.
 */
public class LoginTest {
    private Logger logger = LoggerFactory.getLogger(LoginTest.class);

    @Test
    public void loginTest() throws Exception {
        ApiNGDemo.setPropertiesPath("/Users/markwilliams/GitHub/betfair/src/test/resources/test.properties");
        ApiNGDemo.loadProperties();
        HttpClientSSO httpClientSSO = new HttpClientSSO();
        httpClientSSO.setLoginPropertiesPath(ApiNGDemo.getProp().getProperty(LOGIN_PROPERTIES_FILE));

        LoginResponse r = httpClientSSO.login();
        assertEquals("SUCCESS", r.getLoginStatus());
    }

    @Test
    public void gsonTest() {
        Set<String> countries = new HashSet<String>();
        String json = "[\"Premier League\",\"Champions League\"]";
        countries.add("GB");
        countries.add("JP");

        logger.info(json);
        Gson gson = new Gson();
        logger.info(gson.toJson(countries));

        countries = gson.fromJson(json, countries.getClass());
        for (String s : countries) {
            logger.info("Country:" + s);
        }
        if (countries.contains("GB")) {
            logger.info("Country:" + "GB");
        }
    }

    @Test
    public void eventTest() {
        String result = "{\"jsonrpc\":\"2.0\",\"result\":[{\"event\":{\"id\":\"27249515\",\"name\":\"Tottenham v QPR\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-24T12:30:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27249641\",\"name\":\"Man City v Liverpool\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-25T19:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27251658\",\"name\":\"Leverkusen v FC Copenhagen\",\"countryCode\":\"DE\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-27T17:45:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27251659\",\"name\":\"Ath Bilbao v Napoli\",\"countryCode\":\"ES\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-27T18:45:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27251656\",\"name\":\"Ludogorets v Steaua Bucharest\",\"countryCode\":\"BG\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-27T17:45:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27251662\",\"name\":\"Malmo FF v Red Bull Salzburg\",\"countryCode\":\"SE\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-27T18:45:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27251663\",\"name\":\"Arsenal v Besiktas\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-27T18:45:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27249344\",\"name\":\"Hull v Stoke\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-24T12:30:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27251320\",\"name\":\"Celtic v NK Maribor\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-26T18:45:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27249348\",\"name\":\"Sunderland v Man Utd\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-24T15:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27251319\",\"name\":\"Zenit St Petersburg v Standard\",\"countryCode\":\"RU\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-26T15:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27248670\",\"name\":\"Millwall v Rotherham\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-23T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27251348\",\"name\":\"APOEL v AaB\",\"countryCode\":\"CY\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-26T17:45:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27251351\",\"name\":\"BATE Borisov v Slovan Bratislava\",\"countryCode\":\"BY\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-26T17:45:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27251352\",\"name\":\"Porto v Lille\",\"countryCode\":\"PT\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-26T17:45:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252729\",\"name\":\"Newcastle v C Palace\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252728\",\"name\":\"Man City v Stoke\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252731\",\"name\":\"QPR v Sunderland\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252730\",\"name\":\"West Ham v Southampton\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252733\",\"name\":\"Sheff Wed v Nottm Forest\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T11:15:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252732\",\"name\":\"Swansea v West Brom\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252735\",\"name\":\"Everton v Chelsea\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T16:30:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252734\",\"name\":\"Burnley v Man Utd\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T11:45:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252721\",\"name\":\"Middlesbrough v Reading\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252720\",\"name\":\"Millwall v Blackpool\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27248592\",\"name\":\"Nottm Forest v Reading\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-23T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252723\",\"name\":\"Norwich v Bournemouth\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27248593\",\"name\":\"Brentford v Birmingham\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-23T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252724\",\"name\":\"Rotherham v Brentford\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252712\",\"name\":\"Leeds v Bolton\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27248587\",\"name\":\"Wigan v Blackpool\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-23T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27248586\",\"name\":\"Ipswich v Norwich\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-23T11:15:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27248585\",\"name\":\"Huddersfield v Charlton\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-23T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27248680\",\"name\":\"Everton v Arsenal\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-23T16:30:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252714\",\"name\":\"Wigan v Birmingham\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27248681\",\"name\":\"Aston Villa v Newcastle\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-23T11:45:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27248584\",\"name\":\"Blackburn v Bournemouth\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-23T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252715\",\"name\":\"Wolves v Blackburn\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27248591\",\"name\":\"Derby v Fulham\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-23T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252716\",\"name\":\"Watford v Huddersfield\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27248590\",\"name\":\"Brighton v Bolton\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-23T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252717\",\"name\":\"Brighton v Charlton\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27248589\",\"name\":\"Wolves v Cardiff\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-23T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252718\",\"name\":\"Fulham v Cardiff\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27252719\",\"name\":\"Derby v Ipswich\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-30T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27248588\",\"name\":\"Watford v Leeds\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-23T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27248674\",\"name\":\"C Palace v West Ham\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-23T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27248675\",\"name\":\"Chelsea v Leicester\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-23T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27248673\",\"name\":\"Swansea v Burnley\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-23T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27248583\",\"name\":\"Middlesbrough v Sheff Wed\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-23T14:00:00.000Z\"},\"marketCount\":2},{\"event\":{\"id\":\"27248677\",\"name\":\"Southampton v West Brom\",\"countryCode\":\"GB\",\"timezone\":\"Europe/London\",\"openDate\":\"2014-08-23T14:00:00.000Z\"},\"marketCount\":2}],\"id\":\"1\"}";
        ListEventsContainer container = JsonConverter.convertFromJson(result, ListEventsContainer.class);
        logger.info("Events, dear boy...");

        for (EventResult e : container.getResult()){
            logger.info(e.getEvent().toString());
        }
    }
}
