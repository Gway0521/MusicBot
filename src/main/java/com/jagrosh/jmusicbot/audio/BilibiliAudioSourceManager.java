package com.jagrosh.jmusicbot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Clean, refactored Bilibili audio source manager
 */
public class BilibiliAudioSourceManager implements AudioSourceManager, HttpConfigurable {
    private static final Logger log = LoggerFactory.getLogger(BilibiliAudioSourceManager.class);
    
    private BilibiliConfig config;
    private BilibiliHttpManager httpManager;
    private BilibiliApiClient apiClient;
    
    public BilibiliAudioSourceManager() {
        // Initialize with default configuration
        this.config = createDefaultConfig();
        this.httpManager = new BilibiliHttpManager(config);
        this.apiClient = new BilibiliApiClient(httpManager);
        
        log.info("BilibiliAudioSourceManager initialized with default configuration");
    }
    
    private BilibiliConfig createDefaultConfig() {
        return new BilibiliConfig(
            true, // enabled by default
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "https://www.bilibili.com/",
            "https://www.bilibili.com",
            "",
            320
        );
    }
    
    /**
     * Updates the configuration and reinitializes HTTP components
     */
    public void updateConfiguration(BilibiliConfig newConfig) {
        this.config = newConfig;
        this.httpManager.updateConfiguration(newConfig);
        this.apiClient = new BilibiliApiClient(httpManager);
        
        log.info("BilibiliAudioSourceManager configuration updated: {}", newConfig);
    }
    
    @Override
    public String getSourceName() {
        return BilibiliConstants.SOURCE_NAME;
    }
    
    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        if (!config.isEnabled()) {
            log.debug("BilibiliAudioSourceManager is disabled, skipping URL: {}", reference.identifier);
            return null;
        }
        
        String url = reference.identifier;
        if (!BilibiliUtil.isValidBilibiliUrl(url)) {
            log.debug("URL is not a valid Bilibili URL: {}", url);
            return null;
        }
        
        log.info("Loading Bilibili URL: {}", url);
        
        try {
            return loadBilibiliTrack(url);
        } catch (Exception e) {
            log.error("Error loading Bilibili track from URL: {}", url, e);
            return null;
        }
    }
    
    private AudioItem loadBilibiliTrack(String url) throws Exception {
        String videoId = BilibiliUtil.extractVideoId(url);
        if (videoId == null) {
            throw new IllegalArgumentException("Could not extract video ID from URL: " + url);
        }
        
        log.info("Extracted video ID: {} from URL: {}", videoId, url);
        
        // Get video information
        BilibiliVideoInfo videoInfo = apiClient.getVideoInfo(videoId);
        
        // Create audio track info
        AudioTrackInfo trackInfo = new AudioTrackInfo(
            videoInfo.getTitle(),
            videoInfo.getAuthor(),
            videoInfo.getDurationMs(),
            url, // identifier
            false, // isStream
            url // uri
        );
        
        log.info("Created AudioTrackInfo for Bilibili video: title='{}', author='{}', length={}ms", 
            trackInfo.title, trackInfo.author, trackInfo.length);
        
        // Create and return the track
        BilibiliAudioTrack track = new BilibiliAudioTrack(trackInfo, this, videoInfo.getCid());
        log.info("Successfully created BilibiliAudioTrack for URL: {} with CID: {}", url, videoInfo.getCid());
        return track;
    }
    
    /**
     * Package-private method for tracks to access the API client
     */
    BilibiliApiClient getApiClient() {
        return apiClient;
    }
    
    /**
     * Package-private method for tracks to access the HTTP manager
     */
    BilibiliHttpManager getHttpManager() {
        return httpManager;
    }
    
    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return track instanceof BilibiliAudioTrack;
    }
    
    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        // Implementation for encoding track state for serialization
        // This is used when saving/loading playlists
    }
    
    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        // Implementation for decoding track state from serialization
        return new BilibiliAudioTrack(trackInfo, this);
    }
    
    @Override
    public void shutdown() {
        try {
            if (httpManager != null) {
                httpManager.close();
            }
        } catch (IOException e) {
            log.error("Error shutting down BilibiliAudioSourceManager", e);
        }
    }
    
    // HttpConfigurable implementation
    @Override
    public void configureRequests(Function<RequestConfig, RequestConfig> configurator) {
        if (httpManager != null && httpManager.getHttpInterfaceManager() != null) {
            httpManager.getHttpInterfaceManager().configureRequests(configurator);
        }
    }
    
    @Override
    public void configureBuilder(Consumer<HttpClientBuilder> configurator) {
        if (httpManager != null && httpManager.getHttpInterfaceManager() != null) {
            httpManager.getHttpInterfaceManager().configureBuilder(configurator);
        }
    }
}