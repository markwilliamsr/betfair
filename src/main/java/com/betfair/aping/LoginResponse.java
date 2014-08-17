package com.betfair.aping;

/**
 * Created by markwilliams on 8/17/14.
 */
public class LoginResponse {
    String sessionToken;
    String loginStatus;

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
}
