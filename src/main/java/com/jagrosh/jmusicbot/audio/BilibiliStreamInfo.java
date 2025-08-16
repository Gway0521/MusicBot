package com.jagrosh.jmusicbot.audio;

/**
 * Represents stream information retrieved from Bilibili player API
 */
public class BilibiliStreamInfo {
    private final String streamUrl;
    private final String format;
    
    public BilibiliStreamInfo(String streamUrl, String format) {
        this.streamUrl = streamUrl;
        this.format = format;
    }
    
    public String getStreamUrl() {
        return streamUrl;
    }
    
    public String getFormat() {
        return format;
    }
    
    @Override
    public String toString() {
        return "BilibiliStreamInfo{" +
                "streamUrl='" + streamUrl + '\'' +
                ", format='" + format + '\'' +
                '}';
    }
}