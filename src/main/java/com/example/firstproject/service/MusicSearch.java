package com.example.firstproject.service;

import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MusicSearch {
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 2000;

    public List<Map<String, String>> search(String accessToken, String q, int i, int i1) throws Exception {
        List<Map<String, String>> results = new ArrayList<>();
        int limit = 50; // 한 번에 가져올 최대 항목 수
        int offset = 0; // 시작점

        // 첫 번째 요청
        results.addAll(getTracks(accessToken, q, limit, offset));

        // 두 번째 요청을 위한 offset 업데이트
        offset += limit;

        // 두 번째 요청
        results.addAll(getTracks(accessToken, q, limit, offset));

        return results;
    }

    public Object getAlbumImage(String spotifyAccessToken, String albumId) throws Exception {
        return makeRequestWithRetry(() -> {
            RestTemplate rest = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + spotifyAccessToken);
            headers.add("Content-type", "application/json");
            String body = "";

            String url = "https://api.spotify.com/v1/albums/" + albumId;

            HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.GET, requestEntity, String.class);
            String response = responseEntity.getBody();

            // JSON 응답 파싱
            JSONObject json = new JSONObject(response);
            JSONArray images = json.getJSONArray("images");

            return images.getJSONObject(0).getString("url");
        });
    }

    private List<Map<String, String>> getTracks(String accessToken, String q, int limit, int offset) throws Exception {
        return makeRequestWithRetry(() -> {
            RestTemplate rest = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);
            headers.add("Content-type", "application/json");
            String body = "";

            String url = "https://api.spotify.com/v1/search?type=track&q=" + q + "&limit=" + limit + "&offset=" + offset;

            HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.GET, requestEntity, String.class);
            HttpStatusCode httpStatus = responseEntity.getStatusCode();
            int status = httpStatus.value();
            String response = responseEntity.getBody();
            System.out.println("Response status: " + status);

            if (status == 429) {
                throw new RuntimeException("Rate limit exceeded");
            }

            // JSON 응답 파싱
            JSONObject json = new JSONObject(response);
            JSONArray tracks = json.getJSONObject("tracks").getJSONArray("items");
            List<Map<String, String>> results = new ArrayList<>();

            for (int i = 0; i < tracks.length(); i++) {
                JSONObject track = tracks.getJSONObject(i);
                Map<String, String> trackInfo = new HashMap<>();
                trackInfo.put("trackName", track.getString("name")); // 곡 제목
                JSONArray artists = track.getJSONArray("artists");
                trackInfo.put("artistName", artists.getJSONObject(0).getString("name")); // 첫 번째 가수 이름
                JSONObject album = track.getJSONObject("album");
                trackInfo.put("albumName", album.getString("name")); // 앨범명
                JSONArray images = album.getJSONArray("images");
                trackInfo.put("albumImageUrl", images.getJSONObject(0).getString("url")); // 앨범 이미지 URL
                int durationMs = track.getInt("duration_ms");
                int minutes = (durationMs / 1000) / 60;
                int seconds = (durationMs / 1000) % 60;
                trackInfo.put("playtime", String.format("%d:%02d", minutes, seconds)); // 예: "3:45"
                trackInfo.put("spotifyUrl", track.getJSONObject("external_urls").getString("spotify"));

                // 결과 리스트에 추가
                results.add(trackInfo);
            }

            return results;
        });
    }

    private <T> T makeRequestWithRetry(RequestSupplier<T> requestSupplier) throws Exception {
        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                return requestSupplier.get();
            } catch (Exception e) {
                if (e.getMessage().contains("429")) {
                    retries++;
                    System.out.println("Rate limit exceeded. Retrying... (" + retries + ")");
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    throw e;
                }
            }
        }
        throw new RuntimeException("Max retries exceeded");
    }


    @FunctionalInterface
    private interface RequestSupplier<T> {
        T get() throws Exception;
    }
}
