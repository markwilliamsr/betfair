package com.betfair.aping.login;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

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
    }
}
