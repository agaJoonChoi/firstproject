package com.example.firstproject.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MusicSearch {
    private static final Logger log = LoggerFactory.getLogger(MusicSearch.class);
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;
    private static final String SPOTIFY_API_BASE_URL = "https://api.spotify.com/v1";

    // Spotify 검색을 수행하는 메서드
    public List<Map<String, String>> search(String accessToken, String query) throws Exception {
        List<Map<String, String>> results = new ArrayList<>();
        int limit = 50; // 한 번에 가져올 최대 항목 수

        // 첫 번째 요청
        results.addAll(getTracks(accessToken, query, limit, 0));

        // 두 번째 요청
        results.addAll(getTracks(accessToken, query, limit, limit));

        return results;
    }

    // 앨범 이미지를 가져오는 메서드
    public Optional<String> getAlbumImage(String spotifyAccessToken, String albumId) throws Exception {
        return makeRequestWithRetry(() -> {
            String url = SPOTIFY_API_BASE_URL + "/albums/" + albumId;
            HttpHeaders headers = createHeaders(spotifyAccessToken);
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            RestTemplate rest = new RestTemplate();
            ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.GET, requestEntity, String.class);
            JSONObject json = new JSONObject(responseEntity.getBody());
            JSONArray images = json.getJSONArray("images");

            return images.length() > 0 ? Optional.of(images.getJSONObject(0).getString("url")) : Optional.empty(); // 이미지 URL 반환
        });
    }

    // 트랙을 가져오는 메서드
    private List<Map<String, String>> getTracks(String accessToken, String query, int limit, int offset) throws Exception {
        return makeRequestWithRetry(() -> {
            String url = String.format("%s/search?type=track&q=%s&limit=%d&offset=%d", SPOTIFY_API_BASE_URL, query, limit, offset);
            HttpHeaders headers = createHeaders(accessToken);
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            RestTemplate rest = new RestTemplate();
            ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.GET, requestEntity, String.class);
            JSONObject json = new JSONObject(responseEntity.getBody());

            return parseTracks(json); // JSON 응답 파싱
        });
    }

    // 요청 헤더를 생성하는 메서드
    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/json");
        return headers;
    }

    // 요청 메서드 재시도 로직
    private <T> T makeRequestWithRetry(RequestSupplier<T> requestSupplier) throws Exception {
        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                return requestSupplier.get();
            } catch (Exception e) {
                log.error("요청 중 오류 발생: " + e.getMessage(), e);
                if (e.getMessage().contains("429")) {
                    retries++;
                    log.warn("Rate limit exceeded. Retrying... ({}회)", retries);
                    Thread.sleep(RETRY_DELAY_MS);
                } else {
                    throw e;
                }
            }
        }
        throw new RuntimeException("최대 재시도 횟수 초과");
    }

    // 트랙 파싱 메서드
    private List<Map<String, String>> parseTracks(JSONObject json) {
        JSONArray tracks = json.getJSONObject("tracks").getJSONArray("items");
        List<Map<String, String>> results = new ArrayList<>();
        for (int i = 0; i < tracks.length(); i++) {
            JSONObject track = tracks.getJSONObject(i);
            Map<String, String> trackInfo = new HashMap<>();
            trackInfo.put("trackName", track.getString("name"));
            trackInfo.put("artistName", track.getJSONArray("artists").getJSONObject(0).getString("name"));
            trackInfo.put("albumName", track.getJSONObject("album").getString("name"));
            trackInfo.put("albumImageUrl", track.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url"));
            trackInfo.put("playtime", formatPlaytime(track.getInt("duration_ms")));
            trackInfo.put("spotifyUrl", track.getJSONObject("external_urls").getString("spotify"));
            results.add(trackInfo);
        }
        return results;
    }

    // 플레이타임을 포맷팅하는 메서드
    private String formatPlaytime(int durationMs) {
        int minutes = (durationMs / 1000) / 60;
        int seconds = (durationMs / 1000) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    @FunctionalInterface
    private interface RequestSupplier<T> {
        T get() throws Exception;
    }
}
