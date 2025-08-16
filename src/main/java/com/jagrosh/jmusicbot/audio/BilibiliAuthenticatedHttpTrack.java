package com.jagrosh.jmusicbot.audio;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authenticated HTTP track for Bilibili streams that uses proper authentication headers
 */
public class BilibiliAuthenticatedHttpTrack extends DelegatedAudioTrack {
    private static final Logger log = LoggerFactory.getLogger(BilibiliAuthenticatedHttpTrack.class);
    
    private final BilibiliAudioSourceManager sourceManager;
    private final String streamUrl;

    public BilibiliAuthenticatedHttpTrack(AudioTrackInfo trackInfo, BilibiliAudioSourceManager sourceManager, String streamUrl) {
        super(trackInfo);
        this.sourceManager = sourceManager;
        this.streamUrl = streamUrl;
    }

    @Override
    public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
        log.info("Starting authenticated processing of Bilibili stream: {}", streamUrl);
        
        // Validate authentication first
        validateAuthentication();
        
        log.info("Creating HttpAudioTrack for authenticated Bilibili stream playback");
        
        try {
            // Create a new HTTP audio source manager that will use our authentication
            HttpAudioSourceManager httpSourceManager = new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY) {
                @Override
                public HttpInterface getHttpInterface() {
                    return sourceManager.getHttpManager().getHttpInterfaceManager().getInterface();
                }
            };
            
            log.info("Loading track through HTTP source manager to detect container format");
            
            // Use the HTTP source manager to load the track properly
            // This will handle container detection and create the proper track
            AudioReference reference = new AudioReference(streamUrl, getInfo().title);
            AudioItem audioItem = httpSourceManager.loadItem(null, reference);
            
            AudioTrack httpTrack = null;
            if (audioItem instanceof AudioTrack) {
                httpTrack = (AudioTrack) audioItem;
            }
            
            if (httpTrack == null) {
                log.error("HTTP source manager failed to load track: {}", streamUrl);
                throw new FriendlyException("HTTP source manager could not load Bilibili stream", FriendlyException.Severity.COMMON, null);
            }
            
            log.info("Successfully loaded HTTP track, starting playback for: {}", getInfo().title);
            
            processDelegate((InternalAudioTrack) httpTrack, localExecutor);
            
            log.info("Bilibili audio playback completed successfully");
            
        } catch (Exception e) {
            log.error("Error during Bilibili stream playback: {}", e.getMessage(), e);
            throw new FriendlyException("Failed to play Bilibili stream", FriendlyException.Severity.COMMON, e);
        }
    }
    
    private void validateAuthentication() throws Exception {
        log.debug("Validating authentication for Bilibili stream");
        
        try (HttpInterface httpInterface = sourceManager.getHttpManager().getHttpInterfaceManager().getInterface()) {
            HttpGet request = new HttpGet(streamUrl);
            
            try (org.apache.http.client.methods.CloseableHttpResponse response = httpInterface.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                log.info("Authentication validation response status: {}", statusCode);
                
                if (statusCode == 200) {
                    log.info("✅ Bilibili stream authentication SUCCESSFUL!");
                    
                    org.apache.http.HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        long contentLength = entity.getContentLength();
                        String contentType = entity.getContentType() != null ? entity.getContentType().getValue() : "unknown";
                        
                        log.info("Stream details - Content-Type: {}, Content-Length: {} bytes", 
                            contentType, contentLength > 0 ? contentLength : "unknown");
                        
                        // Check if this is actual audio content
                        if (contentType.contains("audio") || contentType.contains("video") || contentType.contains("mp4")) {
                            log.info("✅ Stream contains valid media content: {}", contentType);
                        } else {
                            log.info("📄 Stream content type: {} (may be valid media)", contentType);
                        }
                        
                        log.info("🎉 MAJOR SUCCESS: Bilibili authentication breakthrough achieved!");
                        log.info("🎵 Audio stream is now accessible with SESSDATA authentication");
                        log.info("🔧 Integration ready for full playback implementation");
                    }
                } else if (statusCode == 403) {
                    log.error("❌ Authentication still failing with status 403");
                    log.error("This indicates SESSDATA or headers may need adjustment");
                    throw new IllegalStateException("Authentication failed, status code: " + statusCode);
                } else {
                    log.error("❌ Unexpected response status: {}", statusCode);
                    throw new IllegalStateException("Unexpected response, status code: " + statusCode);
                }
            }
        }
    }
    
    @Override
    protected AudioTrack makeShallowClone() {
        return new BilibiliAuthenticatedHttpTrack(getInfo(), sourceManager, streamUrl);
    }
    
    @Override
    public com.sedmelluq.discord.lavaplayer.source.AudioSourceManager getSourceManager() {
        return sourceManager;
    }
}