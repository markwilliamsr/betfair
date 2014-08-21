package com.betfair.aping.login;

import com.google.gson.Gson;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by markwilliams on 17/08/2014.
 */
public class LoginTest {

    @Test
    public void loginTest() throws Exception {
        HttpClientSSO httpClientSSO = new HttpClientSSO();
        LoginResponse r = httpClientSSO.login();
        assertEquals("SUCCESS", r.getLoginStatus());
        assertEquals(false, r.isProd());
    }

    @Test
    public void gsonTest() {
        Set<String> countries = new HashSet<String>();
        String json = "[\"Premier League\",\"Champions League\"]";
        countries.add("GB");
        countries.add("JP");

        System.out.println(json);
        Gson gson = new Gson();
        System.out.println(gson.toJson(countries));

        countries = gson.fromJson(json, countries.getClass());
        for (String s : countries) {
            System.out.println("Country:" + s);
        }
        if (countries.contains("GB")) {
            System.out.println("Country:" + "GB");
        }
    }
}
