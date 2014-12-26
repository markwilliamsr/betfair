package com.betfair.aping.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RescriptResponseHandler implements ResponseHandler<String> {
	private static final String ENCODING_UTF_8 = "UTF-8";
    private Logger logger = LoggerFactory.getLogger(RescriptResponseHandler.class);

    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        StatusLine statusLine = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        if (statusLine.getStatusCode() != 200) {

            String s = entity == null ? null : EntityUtils.toString(entity, ENCODING_UTF_8);
            logger.info("Call to api-ng failed\n");
            logger.info(s);
            System.exit(0);

        }

        return entity == null ? null : EntityUtils.toString(entity,ENCODING_UTF_8);
    }
}
