package com.example.firstproject.service;

import org.springframework.stereotype.Service; // 추가
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

@Service // 이 줄을 추가
public class SpotifyService {

    public List<Map<String, Object>> getPlaylistTracks(String playlistId, String accessToken) {
        String apiUrl = "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks";
        RestTemplate restTemplate = new RestTemplate();

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);

        List<Map<String, Object>> tracks = (List<Map<String, Object>>) response.getBody().get("items");
        return tracks;
    }
}
