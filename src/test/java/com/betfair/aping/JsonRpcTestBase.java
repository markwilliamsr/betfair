package com.betfair.aping;

import com.betfair.aping.api.ApiNgJsonRpcOperations;
import com.betfair.aping.api.ApiNgOperations;
import com.betfair.aping.login.HttpClientSSO;
import com.betfair.aping.login.LoginResponse;
import com.google.gson.Gson;
import org.junit.Before;

import static com.betfair.aping.login.LoginConstants.LOGIN_PROPERTIES_FILE;

/**
 * Created by markwilliams on 25/08/2014.
 */
public class JsonRpcTestBase {
    protected Gson gson = new Gson();
    protected ApiNgOperations jsonOperations = ApiNgJsonRpcOperations.getInstance();
    @Before
    public void setupSession() throws Exception {
        ApiNGDemo.setPropertiesPath("/Users/markwilliams/GitHub/betfair/src/test/resources/test.properties");
        ApiNGDemo.loadProperties();
        HttpClientSSO sso = new HttpClientSSO();
        sso.setLoginPropertiesPath(ApiNGDemo.getProp().getProperty(LOGIN_PROPERTIES_FILE));
        LoginResponse loginResponse;

        loginResponse = sso.login();
        ApiNgJsonRpcOperations.getInstance().setAppKey(loginResponse.getApplicationKey());
        ApiNgJsonRpcOperations.getInstance().setSessionToken(loginResponse.getSessionToken());
    }
}
