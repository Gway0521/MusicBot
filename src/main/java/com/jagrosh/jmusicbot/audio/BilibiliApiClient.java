package com.jagrosh.jmusicbot.audio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Client for making API calls to Bilibili services
 */
public class BilibiliApiClient {
    private static final Logger log = LoggerFactory.getLogger(BilibiliApiClient.class);
    
    private final ObjectMapper objectMapper;
    private final BilibiliHttpManager httpManager;
    
    public BilibiliApiClient(BilibiliHttpManager httpManager) {
        this.httpManager = httpManager;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Fetches video information from Bilibili API
     */
    public BilibiliVideoInfo getVideoInfo(String videoId) throws IOException {
        String apiUrl = BilibiliUtil.buildVideoInfoUrl(videoId);
        if (apiUrl == null) {
            throw new IllegalArgumentException("Invalid video ID format: " + videoId);
        }
        
        log.info("Calling Bilibili API: {}", apiUrl);
        
        try (HttpInterface httpInterface = httpManager.getHttpInterfaceManager().getInterface()) {
            String response = makeHttpRequest(httpInterface, apiUrl);
            return parseVideoInfo(response);
        }
    }
    
    /**
     * Fetches stream URLs from Bilibili player API
     */
    public BilibiliStreamInfo getStreamInfo(String videoId, long cid) throws IOException {
        String apiUrl = BilibiliUtil.buildPlayerUrl(videoId, cid);
        if (apiUrl == null) {
            throw new IllegalArgumentException("Invalid parameters - videoId: " + videoId + ", cid: " + cid);
        }
        
        log.info("Calling Bilibili player API: {}", apiUrl);
        
        try (HttpInterface httpInterface = httpManager.getHttpInterfaceManager().getInterface()) {
            String response = makeHttpRequest(httpInterface, apiUrl);
            return parseStreamInfo(response);
        }
    }
    
    private String makeHttpRequest(HttpInterface httpInterface, String url) throws IOException {
        try (CloseableHttpResponse response = httpInterface.execute(new HttpGet(url))) {
            int statusCode = response.getStatusLine().getStatusCode();
            log.info("Bilibili API response status: {}", statusCode);
            
            if (statusCode != 200) {
                throw new IOException("Bilibili API returned non-200 status: " + statusCode + " for URL: " + url);
            }
            
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new IOException("Bilibili API response entity is null for URL: " + url);
            }
            
            String responseBody = EntityUtils.toString(entity, "UTF-8");
            log.info("Successfully received API response ({} characters)", responseBody.length());
            return responseBody;
        }
    }
    
    private BilibiliVideoInfo parseVideoInfo(String response) throws IOException {
        try {
            JsonNode root = objectMapper.readTree(response);
            
            int code = root.path("code").asInt(-1);
            if (code != BilibiliConstants.API_SUCCESS_CODE) {
                String message = root.path("message").asText("Unknown error");
                throw new IOException("Bilibili API returned error code: " + code + " - " + message);
            }
            
            JsonNode data = root.path("data");
            if (data.isMissingNode()) {
                throw new IOException("No data field in Bilibili API response");
            }
            
            // Extract basic video information
            String title = data.path("title").asText("Unknown Title");
            String author = data.path("owner").path("name").asText("Unknown Artist");
            long durationSeconds = data.path("duration").asLong(0);
            
            // Get CID for audio stream extraction
            long cid = data.path("cid").asLong(0);
            if (cid == 0) {
                // Try to get CID from pages array
                JsonNode pages = data.path("pages");
                if (pages.isArray() && pages.size() > 0) {
                    cid = pages.get(0).path("cid").asLong(0);
                }
            }
            
            if (cid == 0) {
                throw new IOException("Could not extract CID from video info");
            }
            
            log.info("Extracted video info: title='{}', author='{}', duration={}s, cid={}", 
                title, author, durationSeconds, cid);
            
            return new BilibiliVideoInfo(title, author, durationSeconds * 1000, cid);
            
        } catch (Exception e) {
            throw new IOException("Error parsing video info: " + e.getMessage(), e);
        }
    }
    
    private BilibiliStreamInfo parseStreamInfo(String response) throws IOException {
        try {
            JsonNode root = objectMapper.readTree(response);
            
            int code = root.path("code").asInt(-1);
            if (code != BilibiliConstants.API_SUCCESS_CODE) {
                String message = root.path("message").asText("Unknown error");
                
                // Check for specific authentication errors
                if (code == BilibiliConstants.API_AUTH_ERROR_CODE || 
                    code == BilibiliConstants.API_NOT_FOUND_CODE || 
                    message.contains("权限") || message.contains("登录")) {
                    throw new IOException("Authentication required: This video may need SESSDATA cookie or login");
                }
                
                throw new IOException("Bilibili player API returned error code: " + code + " - " + message);
            }
            
            JsonNode data = root.path("data");
            if (data.isMissingNode()) {
                throw new IOException("No data field in Bilibili player API response");
            }
            
            // Try to extract audio stream from DASH format first
            String streamUrl = extractDashAudioUrl(data);
            if (streamUrl != null) {
                return new BilibiliStreamInfo(streamUrl, "DASH");
            }
            
            // Fallback to durl format (legacy format)
            streamUrl = extractDurlUrl(data);
            if (streamUrl != null) {
                return new BilibiliStreamInfo(streamUrl, "FLV");
            }
            
            throw new IOException("No compatible audio streams found in Bilibili player API response");
            
        } catch (Exception e) {
            throw new IOException("Error parsing stream info: " + e.getMessage(), e);
        }
    }
    
    private String extractDashAudioUrl(JsonNode data) {
        JsonNode dash = data.path("dash");
        if (dash.isMissingNode()) {
            return null;
        }
        
        log.info("Found DASH format streams");
        JsonNode audioStreams = dash.path("audio");
        
        if (!audioStreams.isArray() || audioStreams.size() == 0) {
            return null;
        }
        
        // Select the best quality audio stream
        JsonNode bestAudio = selectBestAudioStream(audioStreams);
        if (bestAudio == null) {
            return null;
        }
        
        String audioUrl = bestAudio.path("baseUrl").asText();
        if (audioUrl.isEmpty()) {
            audioUrl = bestAudio.path("base_url").asText();
        }
        
        if (!audioUrl.isEmpty()) {
            log.info("Selected best audio stream: id={}, bandwidth={}", 
                bestAudio.path("id").asInt(), bestAudio.path("bandwidth").asInt());
            return audioUrl;
        }
        
        return null;
    }
    
    private String extractDurlUrl(JsonNode data) {
        JsonNode durl = data.path("durl");
        if (!durl.isArray() || durl.size() == 0) {
            return null;
        }
        
        log.info("Found durl format streams (fallback)");
        String videoUrl = durl.get(0).path("url").asText();
        if (!videoUrl.isEmpty()) {
            log.info("Selected video stream URL (contains audio)");
            return videoUrl;
        }
        
        return null;
    }
    
    private JsonNode selectBestAudioStream(JsonNode audioStreams) {
        JsonNode bestStream = null;
        int bestBandwidth = 0;
        
        for (JsonNode stream : audioStreams) {
            int bandwidth = stream.path("bandwidth").asInt(0);
            int id = stream.path("id").asInt(0);
            
            log.debug("Found audio stream: id={}, bandwidth={}", id, bandwidth);
            
            // Select stream with highest bandwidth (best quality)
            if (bandwidth > bestBandwidth) {
                bestBandwidth = bandwidth;
                bestStream = stream;
            }
        }
        
        return bestStream;
    }
}