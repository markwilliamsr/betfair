package com.betfair.aping.login;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by markwilliams on 17/08/2014.
 */
public class LoginTest {

    @Test
    public void loginTest() {
        HttpClientSSO httpClientSSO = new HttpClientSSO();
        try {
            httpClientSSO.loadProperties();
            httpClientSSO.login();
        } catch (Exception e) {
            assert false;
        }
    }
}
