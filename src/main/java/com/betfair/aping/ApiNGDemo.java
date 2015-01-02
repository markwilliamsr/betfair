package com.betfair.aping;

import com.betfair.aping.api.ApiNgJsonRpcOperations;
import com.betfair.aping.login.HttpClientSSO;
import com.betfair.aping.login.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This is a demonstration class to show a quick demo of the new Betfair API-NG.
 * When you execute the class will: <li>find a market (next horse race in the
 * UK)</li> <li>get prices and runners on this market</li> <li>place a bet on 1
 * runner</li> <li>handle the error</li>
 */
public class ApiNGDemo {

    static Logger logger = LoggerFactory.getLogger(ApiNGDemo.class);

    private static Properties prop = new Properties();
    private static boolean debug;

    public static void main(String[] args) {

        loadProperties();

        logger.info("Welcome to the Betfair API NG!");

        HttpClientSSO sso = new HttpClientSSO();
        LoginResponse loginResponse = null;
        try {
            loginResponse = sso.login();
        } catch (Exception e) {
            logger.info(e.toString());
            System.exit(-1);
        }

        ApiNgJsonRpcOperations.getInstance().setAppKey(loginResponse.getApplicationKey());
        ApiNgJsonRpcOperations.getInstance().setSessionToken(loginResponse.getSessionToken());

        ApiNGJsonRpcDemo jsonRpcDemo = new ApiNGJsonRpcDemo();
        int i = 0;
        while (true) {
            try {
                jsonRpcDemo.start();
            } catch (Exception e) {
                logger.error("****************** Fatal error, Restarting *******************", i);
                logger.error("Error:", e);
                logger.error("****************** Restart Count {} **************************", i);
                i++;
            }
        }
    }

    public static void loadProperties() {
        try {
            //InputStream in = ApiNGDemo.class.getResourceAsStream("/apingdemo.properties");
            InputStream in = new FileInputStream("/Users/markwilliams/GitHub/betfair/src/main/resources/apingdemo.properties");
            prop.load(in);
            in.close();

            debug = new Boolean(prop.getProperty("DEBUG"));

        } catch (IOException e) {
            logger.info("Error loading the properties file: " + e.toString());
        }
    }

    public static Properties getProp() {
        return prop;
    }

    public static boolean isDebug() {
        return debug;
    }
}
