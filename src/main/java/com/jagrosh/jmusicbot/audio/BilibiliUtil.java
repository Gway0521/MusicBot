package com.jagrosh.jmusicbot.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;

public final class BilibiliUtil {
    private static final Logger log = LoggerFactory.getLogger(BilibiliUtil.class);
    
    private BilibiliUtil() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Validates if a URL is a valid Bilibili URL
     */
    public static boolean isValidBilibiliUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        
        try {
            return BilibiliConstants.BILIBILI_URL_PATTERN.matcher(url).find();
        } catch (Exception e) {
            log.debug("Error checking Bilibili URL pattern: {}", url, e);
            return false;
        }
    }
    
    /**
     * Extracts video ID (BV or av format) from a Bilibili URL
     */
    public static String extractVideoId(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        Matcher matcher = BilibiliConstants.BILIBILI_URL_PATTERN.matcher(url);
        if (matcher.find()) {
            String videoId = matcher.group(1);
            // Clean up any trailing slashes or special characters
            if (videoId.endsWith("/")) {
                videoId = videoId.substring(0, videoId.length() - 1);
            }
            return videoId;
        }
        return null;
    }
    
    /**
     * Builds API URL for getting video information
     */
    public static String buildVideoInfoUrl(String videoId) {
        if (videoId == null || videoId.isEmpty()) {
            return null;
        }
        
        if (videoId.startsWith("BV")) {
            return BilibiliConstants.VIDEO_INFO_URL + "?" + BilibiliConstants.PARAM_BVID + "=" + videoId;
        } else if (videoId.startsWith("av")) {
            String aid = videoId.substring(2); // Remove "av" prefix
            return BilibiliConstants.VIDEO_INFO_URL + "?" + BilibiliConstants.PARAM_AID + "=" + aid;
        }
        return null;
    }
    
    /**
     * Builds API URL for getting player/stream information
     */
    public static String buildPlayerUrl(String videoId, long cid) {
        if (videoId == null || videoId.isEmpty() || cid <= 0) {
            return null;
        }
        
        StringBuilder url = new StringBuilder(BilibiliConstants.PLAYER_URL);
        url.append("?");
        
        if (videoId.startsWith("BV")) {
            url.append(BilibiliConstants.PARAM_BVID).append("=").append(videoId);
        } else if (videoId.startsWith("av")) {
            String aid = videoId.substring(2); // Remove "av" prefix
            url.append(BilibiliConstants.PARAM_AID).append("=").append(aid);
        } else {
            return null;
        }
        
        url.append("&").append(BilibiliConstants.PARAM_CID).append("=").append(cid);
        url.append("&").append(BilibiliConstants.PARAM_QN).append("=").append(BilibiliConstants.DEFAULT_QUALITY);
        url.append("&").append(BilibiliConstants.PARAM_FNVAL).append("=").append(BilibiliConstants.DEFAULT_FNVAL);
        url.append("&").append(BilibiliConstants.PARAM_FOURK).append("=").append(BilibiliConstants.DEFAULT_FOURK);
        
        return url.toString();
    }
    
    /**
     * Masks sensitive data for logging (keeps first 8 characters)
     */
    public static String maskSensitiveData(String data) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        
        if (data.length() <= 8) {
            return "***";
        }
        
        return data.substring(0, 8) + "***";
    }
}