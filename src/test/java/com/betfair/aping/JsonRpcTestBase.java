package com.betfair.aping;

import com.betfair.aping.api.ApiNgJsonRpcOperations;
import com.betfair.aping.api.ApiNgOperations;
import com.betfair.aping.login.HttpClientSSO;
import com.betfair.aping.login.LoginResponse;
import com.google.gson.Gson;
import org.junit.Before;

/**
 * Created by markwilliams on 25/08/2014.
 */
public class JsonRpcTestBase {
    protected Gson gson = new Gson();
    protected ApiNGDemo api = new ApiNGDemo();
    protected ApiNgOperations jsonOperations = ApiNgJsonRpcOperations.getInstance();
    @Before
    public void setupSession() throws Exception {
        HttpClientSSO sso = new HttpClientSSO();
        LoginResponse loginResponse;

        loginResponse = sso.login();
        ApiNgJsonRpcOperations.getInstance().setAppKey(loginResponse.getApplicationKey());
        ApiNgJsonRpcOperations.getInstance().setSessionToken(loginResponse.getSessionToken());
    }
}
