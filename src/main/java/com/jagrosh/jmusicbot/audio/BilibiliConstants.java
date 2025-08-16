package com.jagrosh.jmusicbot.audio;

import java.util.regex.Pattern;

public final class BilibiliConstants {
    
    // URL Patterns
    public static final Pattern BILIBILI_URL_PATTERN = Pattern.compile(
        "^https?://(?:www\\.|m\\.)?bilibili\\.com/video/(BV[A-Za-z0-9]+|av\\d+)"
    );
    
    // API URLs
    public static final String API_BASE_URL = "https://api.bilibili.com";
    public static final String VIDEO_INFO_URL = API_BASE_URL + "/x/web-interface/view";
    public static final String PLAYER_URL = API_BASE_URL + "/x/player/playurl";
    
    // HTTP Headers
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_REFERER = "Referer";
    public static final String HEADER_ORIGIN = "Origin";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String HEADER_CONNECTION = "Connection";
    public static final String HEADER_COOKIE = "Cookie";
    
    // Default Header Values
    public static final String DEFAULT_ACCEPT = "application/json, text/plain, */*";
    public static final String DEFAULT_ACCEPT_LANGUAGE = "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7";
    public static final String DEFAULT_ACCEPT_ENCODING = "gzip, deflate, br";
    public static final String DEFAULT_CONNECTION = "keep-alive";
    
    // API Parameters
    public static final String PARAM_BVID = "bvid";
    public static final String PARAM_AID = "aid";
    public static final String PARAM_CID = "cid";
    public static final String PARAM_QN = "qn";
    public static final String PARAM_FNVAL = "fnval";
    public static final String PARAM_FOURK = "fourk";
    
    // Default Values
    public static final int DEFAULT_QUALITY = 80; // 1080P
    public static final int DEFAULT_FNVAL = 4048; // DASH format with authentication
    public static final int DEFAULT_FOURK = 1; // Enable 4K support
    public static final int DEFAULT_TIMEOUT_MS = 10000;
    
    // Source name
    public static final String SOURCE_NAME = "bilibili";
    
    // Error codes
    public static final int API_SUCCESS_CODE = 0;
    public static final int API_AUTH_ERROR_CODE = -403;
    public static final int API_NOT_FOUND_CODE = -404;
    
    private BilibiliConstants() {
        // Utility class - prevent instantiation
    }
}