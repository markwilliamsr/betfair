package com.betfair.aping.login;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class HttpClientSSO {
    private static int port = 443;
    private static boolean debug;
    private Properties properties = new Properties();
    private Properties loginProperties = new Properties();
    private Logger logger = LoggerFactory.getLogger(HttpClientSSO.class);
    private String loginPropertiesPath;

    public HttpClientSSO() {
    }

    private static KeyManager[] getKeyManagers(String keyStoreType, InputStream keyStoreFile, String keyStorePassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(keyStoreFile, keyStorePassword.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyStorePassword.toCharArray());
        return kmf.getKeyManagers();
    }

    public String getLoginPropertiesPath() {
        return loginPropertiesPath;
    }

    public void setLoginPropertiesPath(String loginPropertiesPath) {
        this.loginPropertiesPath = loginPropertiesPath;
    }

    public void loadProperties() {
        try {
            debug = new Boolean(properties.getProperty("DEBUG"));

            InputStream in = new FileInputStream(getLoginPropertiesPath());
            loginProperties.load(in);
            in.close();

        } catch (IOException e) {
            logger.info("Error loading the properties file: " + e.toString());
            System.exit(-1);
        }
    }

    public LoginResponse login() throws Exception {
        loadProperties();
        String responseString = "";
        DefaultHttpClient httpClient = new DefaultHttpClient();

        try {
            KeyManager[] keyManagers = getKeyManagers();

            setupSSLContext(httpClient, keyManagers);
            HttpResponse response = httpClient.execute(getHttpPost());
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                responseString = EntityUtils.toString(entity);
            }

            Gson gson = new Gson();
            LoginResponse loginResponse = gson.fromJson(responseString, LoginResponse.class);

            setApplicationKey(loginResponse);

            if (debug) {
                logger.info(response.getStatusLine().toString());
                logger.info("loginResponse" + loginResponse.toString());
                //logger.info("sessionToken: " + loginResponse.getSessionToken());
            }

            return loginResponse;

        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }

    private void setApplicationKey(LoginResponse loginResponse) {
        if (loginProperties.getProperty(LoginConstants.APPLICATION_NAME).toUpperCase().contains("PROD")) {
            loginResponse.setApplicationKey(loginProperties.getProperty(LoginConstants.BETFAIR_PROD_APPLICATION_KEY));
            loginResponse.setProd(true);
        } else {
            loginResponse.setApplicationKey(loginProperties.getProperty(LoginConstants.BETFAIR_DELAY_APPLICATION_KEY));
            loginResponse.setProd(false);
        }
    }

    private HttpPost getHttpPost() throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(loginProperties.getProperty(LoginConstants.BETFAIR_SSO_URL));
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("username", loginProperties.getProperty(LoginConstants.BETFAIR_USERNAME)));
        nvps.add(new BasicNameValuePair("password", loginProperties.getProperty(LoginConstants.BETFAIR_PASSWORD)));

        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        httpPost.setHeader("X-Application", loginProperties.getProperty(LoginConstants.APPLICATION_NAME));
        return httpPost;
    }

    private void setupSSLContext(DefaultHttpClient httpClient, KeyManager[] keyManagers) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(keyManagers, null, new SecureRandom());
        SSLSocketFactory factory = new SSLSocketFactory(ctx, new StrictHostnameVerifier());

        ClientConnectionManager manager = httpClient.getConnectionManager();
        manager.getSchemeRegistry().register(new Scheme("https", port, factory));
    }

    private KeyManager[] getKeyManagers() throws Exception {
        String keyStoreType = loginProperties.getProperty(LoginConstants.KEY_STORE_TYPE);
        String keyStoreFilePath = loginProperties.getProperty(LoginConstants.KEY_FILE_PATH);
        String keyStoreFileName = loginProperties.getProperty(LoginConstants.KEY_FILE_NAME);
        String keyStorePassword = loginProperties.getProperty(LoginConstants.KEY_FILE_PASSWORD);

        return getKeyManagers(keyStoreType, new FileInputStream(new File(keyStoreFilePath + keyStoreFileName)), keyStorePassword);
    }
}