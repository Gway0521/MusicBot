package com.jagrosh.jmusicbot.audio;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clean, refactored Bilibili audio track implementation
 */
public class BilibiliAudioTrack extends DelegatedAudioTrack {
    private static final Logger log = LoggerFactory.getLogger(BilibiliAudioTrack.class);
    
    private final BilibiliAudioSourceManager sourceManager;
    private final long cid;
    
    public BilibiliAudioTrack(AudioTrackInfo trackInfo, BilibiliAudioSourceManager sourceManager) {
        this(trackInfo, sourceManager, 0);
    }
    
    public BilibiliAudioTrack(AudioTrackInfo trackInfo, BilibiliAudioSourceManager sourceManager, long cid) {
        super(trackInfo);
        this.sourceManager = sourceManager;
        this.cid = cid;
    }
    
    @Override
    public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
        log.info("Starting to process Bilibili track: {}", getInfo().title);
        
        try {
            String streamUrl = getStreamUrl();
            if (streamUrl == null) {
                throw new IllegalStateException("Could not get stream URL for Bilibili track");
            }
            
            log.info("Successfully got stream URL for Bilibili track, creating authenticated HTTP track");
            
            // Create authenticated HTTP track that uses our source manager's authentication
            AudioTrackInfo httpTrackInfo = new AudioTrackInfo(
                getInfo().title,
                getInfo().author,
                getInfo().length,
                streamUrl,
                getInfo().isStream,
                streamUrl
            );
            
            // Create authenticated HTTP track that uses our source manager's HTTP interface
            BilibiliAuthenticatedHttpTrack httpTrack = new BilibiliAuthenticatedHttpTrack(
                httpTrackInfo, sourceManager, streamUrl);
            
            log.info("Successfully created authenticated HTTP track, starting playback");
            processDelegate((com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack) httpTrack, localExecutor);
            
        } catch (Exception e) {
            log.error("Error processing Bilibili audio track: {}", getInfo().identifier, e);
            throw e;
        }
    }
    
    private String getStreamUrl() throws Exception {
        if (cid <= 0) {
            log.error("No CID available for stream URL extraction");
            return null;
        }
        
        String videoId = BilibiliUtil.extractVideoId(getInfo().identifier);
        if (videoId == null) {
            log.error("Could not extract video ID from: {}", getInfo().identifier);
            return null;
        }
        
        try {
            BilibiliStreamInfo streamInfo = sourceManager.getApiClient().getStreamInfo(videoId, cid);
            String streamUrl = streamInfo.getStreamUrl();
            
            log.info("Successfully extracted stream URL (format: {})", streamInfo.getFormat());
            return streamUrl;
            
        } catch (Exception e) {
            log.error("Error getting stream URL for video ID: {} with CID: {}", videoId, cid, e);
            throw e;
        }
    }
    
    @Override
    protected AudioTrack makeShallowClone() {
        return new BilibiliAudioTrack(getInfo(), sourceManager, cid);
    }
    
    @Override
    public AudioSourceManager getSourceManager() {
        return sourceManager;
    }
    
    public long getCid() {
        return cid;
    }
}