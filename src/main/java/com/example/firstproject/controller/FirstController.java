package com.example.firstproject.controller;

import com.example.firstproject.CreateToken;
import com.example.firstproject.service.MusicSearch;
import com.example.firstproject.service.SpotifyService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class FirstController {

    private String cachedToken;
    private long tokenExpirationTime;

    // Access token을 가져오는 메서드
    private String getAccessToken() {
        if (cachedToken == null || System.currentTimeMillis() > tokenExpirationTime) {
            cachedToken = CreateToken.accessToken();
            tokenExpirationTime = System.currentTimeMillis() + 3600 * 1000; // 1시간 후 만료
        }
        return cachedToken;
    }

    // API 요청을 처리하는 메서드
    private ResponseEntity<List> makeApiRequest(String apiUrl, HttpHeaders headers) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(apiUrl, HttpMethod.GET, entity, List.class);
        } catch (ResourceAccessException e) {
            log.error("API 접근 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Collections.emptyList());
        }
    }

    @Autowired
    private SpotifyService spotifyService;

    @GetMapping("/hi")
    public String niceToMeetYou(Model model) {
        model.addAttribute("username", "cjy");
        log.info("Access token: " + getAccessToken());
        return "greetings";
    }

    @GetMapping("/hello")
    public String seeYouNext(Model model) {
        model.addAttribute("nickname", "홍길동");
        return "hello";
    }

    @GetMapping("/search")
    public String searchView(Model model, @RequestParam String keyword) throws Exception {
        MusicSearch search = new MusicSearch();
        String accessToken = getAccessToken();
        List<Map<String, String>> searchResults = search.search(accessToken, keyword);
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("totalTracks", searchResults.size());
        log.info("Keyword: " + keyword);
        log.info("Search Results: {} results found", searchResults.size());
        return "musicResult";
    }

    @GetMapping("/spotify")
    public String getSpotifyTop50(Model model) throws Exception {
        String playlistId = "4cRo44TavIHN54w46OqRVc";  // Spotify의 Top 50 Playlist ID
        String accessToken = getAccessToken();
        List<Map<String, Object>> tracks = spotifyService.getPlaylistTracks(playlistId, accessToken);

        int rank = 1;
        for (Map<String, Object> track : tracks) {
            Map<String, Object> trackData = (Map<String, Object>) track.get("track");
            Map<String, Object> albumData = (Map<String, Object>) trackData.get("album");

            String trackId = (String) trackData.get("id");
            String spotifyUrl = "https://open.spotify.com/track/" + trackId;
            String artistName = ((List<Map<String, String>>) trackData.get("artists")).get(0).get("name");
            String trackName = (String) trackData.get("name");
            String albumName = (String) albumData.get("name");
            String albumImageUrl = ((List<Map<String, String>>) albumData.get("images")).get(0).get("url");
            int durationMs = (int) trackData.get("duration_ms");
            String playtime = convertMsToTime(durationMs);

            // rank 추가
            track.put("rank", rank++);
            track.put("spotifyUrl", spotifyUrl);
            track.put("artistName", artistName);
            track.put("trackName", trackName);
            track.put("albumName", albumName);
            track.put("albumImageUrl", albumImageUrl);
            track.put("playtime", playtime);
        }

        model.addAttribute("totalTracks", tracks.size());
        model.addAttribute("searchResults", tracks);
        return "spotify";  // spotify.mustache 템플릿을 사용합니다
    }

    @GetMapping("/billboard")
    public String getBillboardTop100(Model model) throws Exception {
        // JSON 파일 경로
        String jsonFilePath = "C:\\start_with_none 2학기\\firstproject\\scripts\\billboard_Hot100.json";

        // JSON 파일에서 Billboard 데이터를 불러오기
        List<Map<String, String>> billboardSongs = readBillboardDataFromFile(jsonFilePath);

        // 앨범 이미지 URL을 포함하도록 맵핑
        for (Map<String, String> song : billboardSongs) {
            String albumImageUrl = song.get("albumImage"); // 'albumImage' 키를 사용하여 URL 가져오기
            song.put("albumImageUrl", albumImageUrl != null ? albumImageUrl : "기본 이미지 URL 또는 없음"); // 기본 이미지 설정
        }

        model.addAttribute("billboardSongs", billboardSongs);
        return "billboard"; // Mustache 템플릿 이름
    }

    // JSON 파일에서 데이터를 읽어오는 메서드
    private List<Map<String, String>> readBillboardDataFromFile(String filePath) {
        List<Map<String, String>> data = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // JSON 파일에서 데이터 읽기
            data = objectMapper.readValue(new File(filePath), new TypeReference<List<Map<String, String>>>() {});
        } catch (IOException e) {
            log.error("JSON 파일을 읽는 중 오류 발생: " + e.getMessage());
        }

        return data;
    }

    private String getSpotifyTrackId(String trackName, String artistName, String accessToken) {
        String apiUrl = "https://api.spotify.com/v1/search?q=track:" + trackName + "+artist:" + artistName + "&type=track";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> tracks = (List<Map<String, Object>>) ((Map<String, Object>) body.get("tracks")).get("items");
            if (tracks != null && !tracks.isEmpty()) {
                return (String) tracks.get(0).get("id");
            }
        } catch (Exception e) {
            log.error("Error searching Spotify track: " + e.getMessage());
        }
        return null;
    }

    private String convertMsToTime(int durationMs) {
        int minutes = (durationMs / 1000) / 60;
        int seconds = (durationMs / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
