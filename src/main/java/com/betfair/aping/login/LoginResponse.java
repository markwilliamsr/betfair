package com.betfair.aping.login;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by markwilliams on 8/17/14.
 */
public class LoginResponse {
    String sessionToken;
    String loginStatus;
    String applicationKey;
    boolean prod;

    public boolean isProd() {
        return prod;
    }

    public void setProd(boolean prod) {
        this.prod = prod;
    }

    public String getApplicationKey() {
        return applicationKey;
    }

    public void setApplicationKey(String applicationKey) {
        this.applicationKey = applicationKey;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus;
    }

    public String toString() {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this).toString();
    }
}
