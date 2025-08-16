package com.jagrosh.jmusicbot.audio;

import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages HTTP client configuration for Bilibili API requests
 */
public class BilibiliHttpManager implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(BilibiliHttpManager.class);
    
    private HttpInterfaceManager httpInterfaceManager;
    private final BilibiliConfig config;
    
    public BilibiliHttpManager(BilibiliConfig config) {
        this.config = config;
        this.httpInterfaceManager = createHttpInterfaceManager();
    }
    
    private HttpInterfaceManager createHttpInterfaceManager() {
        List<Header> headers = buildHeaders();
        
        HttpClientBuilder clientBuilder = HttpClientBuilder.create()
            .setDefaultHeaders(headers)
            .setUserAgent(config.getUserAgent());
            
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(BilibiliConstants.DEFAULT_TIMEOUT_MS)
            .setSocketTimeout(BilibiliConstants.DEFAULT_TIMEOUT_MS)
            .setConnectionRequestTimeout(BilibiliConstants.DEFAULT_TIMEOUT_MS)
            .build();
            
        return new com.sedmelluq.discord.lavaplayer.tools.io.ThreadLocalHttpInterfaceManager(
            clientBuilder, requestConfig
        );
    }
    
    private List<Header> buildHeaders() {
        List<Header> headers = new ArrayList<>();
        
        headers.add(new BasicHeader(BilibiliConstants.HEADER_USER_AGENT, config.getUserAgent()));
        headers.add(new BasicHeader(BilibiliConstants.HEADER_REFERER, config.getReferer()));
        headers.add(new BasicHeader(BilibiliConstants.HEADER_ORIGIN, config.getOrigin()));
        headers.add(new BasicHeader(BilibiliConstants.HEADER_ACCEPT, BilibiliConstants.DEFAULT_ACCEPT));
        headers.add(new BasicHeader(BilibiliConstants.HEADER_ACCEPT_LANGUAGE, BilibiliConstants.DEFAULT_ACCEPT_LANGUAGE));
        headers.add(new BasicHeader(BilibiliConstants.HEADER_ACCEPT_ENCODING, BilibiliConstants.DEFAULT_ACCEPT_ENCODING));
        headers.add(new BasicHeader(BilibiliConstants.HEADER_CONNECTION, BilibiliConstants.DEFAULT_CONNECTION));
        
        // Add SESSDATA cookie if available
        if (config.hasSessdata()) {
            String cookieValue = "SESSDATA=" + config.getSessdata();
            headers.add(new BasicHeader(BilibiliConstants.HEADER_COOKIE, cookieValue));
            log.debug("Added SESSDATA cookie to HTTP headers");
        } else {
            log.debug("No SESSDATA configured - using unauthenticated requests");
        }
        
        return headers;
    }
    
    public HttpInterfaceManager getHttpInterfaceManager() {
        return httpInterfaceManager;
    }
    
    public void updateConfiguration(BilibiliConfig newConfig) {
        try {
            if (this.httpInterfaceManager != null) {
                this.httpInterfaceManager.close();
            }
        } catch (IOException e) {
            log.warn("Error closing old HTTP interface manager", e);
        }
        
        this.httpInterfaceManager = createHttpInterfaceManager();
        
        // Log configuration status (without revealing full SESSDATA)
        boolean hasAuth = newConfig.hasSessdata();
        log.info("BilibiliHttpManager configuration updated (authentication: {})", hasAuth ? "enabled" : "disabled");
        if (hasAuth) {
            log.debug("SESSDATA configured: {}...", BilibiliUtil.maskSensitiveData(newConfig.getSessdata()));
        }
    }
    
    @Override
    public void close() throws IOException {
        if (httpInterfaceManager != null) {
            httpInterfaceManager.close();
        }
    }
}