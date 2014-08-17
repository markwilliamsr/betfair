package com.betfair.aping;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;


public class HttpClientSSO {


    private static int port = 443;


    public static void main(String[] args) throws Exception {

        DefaultHttpClient httpClient = new DefaultHttpClient();

        try {
            SSLContext ctx = SSLContext.getInstance("TLS");

            KeyManager[] keyManagers = getKeyManagers("pkcs12", new FileInputStream(new File("client-2048.p12")), "password");
            ctx.init(keyManagers, null, new SecureRandom());
            SSLSocketFactory factory = new SSLSocketFactory(ctx, new StrictHostnameVerifier());

            ClientConnectionManager manager = httpClient.getConnectionManager();
            manager.getSchemeRegistry().register(new Scheme("https", port, factory));
            HttpPost httpPost = new HttpPost("https://identitysso.betfair.com/api/certlogin");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("username", "uid"));
            nvps.add(new BasicNameValuePair("password", "pw"));

            httpPost.setEntity(new UrlEncodedFormEntity(nvps));


            httpPost.setHeader("X-Application", "appkey");


            System.out.println("executing request" + httpPost.getRequestLine());

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();

            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            String responseString="";
            if (entity != null) {
                responseString = EntityUtils.toString(entity);
                //extract the session token from responsestring
                System.out.println("responseString" + responseString);
            }

            Gson gson = new Gson();
            gson = new GsonBuilder().disableHtmlEscaping().create();
            LoginResponse loginResponse = gson.fromJson(responseString, LoginResponse.class);

            System.out.println("sessionToken: " + loginResponse.getSessionToken());

            System.out.println("LoginResponse" + gson.toJson(loginResponse).toString());

        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }


    private static KeyManager[] getKeyManagers(String keyStoreType, InputStream keyStoreFile, String keyStorePassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(keyStoreFile, keyStorePassword.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyStorePassword.toCharArray());
        return kmf.getKeyManagers();
    }
}