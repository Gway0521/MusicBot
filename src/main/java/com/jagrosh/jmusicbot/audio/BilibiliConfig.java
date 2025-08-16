package com.jagrosh.jmusicbot.audio;

/**
 * Immutable configuration object for Bilibili audio source
 */
public class BilibiliConfig {
    private final boolean enabled;
    private final String userAgent;
    private final String referer;
    private final String origin;
    private final String sessdata;
    private final int maxBitrateKbps;
    
    public BilibiliConfig(boolean enabled, String userAgent, String referer, String origin, 
                         String sessdata, int maxBitrateKbps) {
        this.enabled = enabled;
        this.userAgent = userAgent;
        this.referer = referer;
        this.origin = origin;
        this.sessdata = sessdata != null ? sessdata : "";
        this.maxBitrateKbps = maxBitrateKbps;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public String getReferer() {
        return referer;
    }
    
    public String getOrigin() {
        return origin;
    }
    
    public String getSessdata() {
        return sessdata;
    }
    
    public boolean hasSessdata() {
        return sessdata != null && !sessdata.isEmpty();
    }
    
    public int getMaxBitrateKbps() {
        return maxBitrateKbps;
    }
    
    @Override
    public String toString() {
        return "BilibiliConfig{" +
                "enabled=" + enabled +
                ", userAgent='" + userAgent + '\'' +
                ", referer='" + referer + '\'' +
                ", origin='" + origin + '\'' +
                ", sessdata='" + BilibiliUtil.maskSensitiveData(sessdata) + '\'' +
                ", maxBitrateKbps=" + maxBitrateKbps +
                '}';
    }
}