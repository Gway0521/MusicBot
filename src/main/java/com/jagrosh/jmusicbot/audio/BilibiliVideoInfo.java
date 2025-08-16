package com.jagrosh.jmusicbot.audio;

/**
 * Represents video information retrieved from Bilibili API
 */
public class BilibiliVideoInfo {
    private final String title;
    private final String author;
    private final long durationMs;
    private final long cid;
    
    public BilibiliVideoInfo(String title, String author, long durationMs, long cid) {
        this.title = title;
        this.author = author;
        this.durationMs = durationMs;
        this.cid = cid;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public long getDurationMs() {
        return durationMs;
    }
    
    public long getCid() {
        return cid;
    }
    
    @Override
    public String toString() {
        return "BilibiliVideoInfo{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", durationMs=" + durationMs +
                ", cid=" + cid +
                '}';
    }
}