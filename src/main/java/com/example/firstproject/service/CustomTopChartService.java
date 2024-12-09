package com.example.firstproject.service;

import com.example.firstproject.model.Song;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomTopChartService {

    public List<Song> createCustomChart(List<Song> billboardSongs, List<Song> spotifySongs) {
        List<Song> typeA = new ArrayList<>();
        List<Song> typeB = new ArrayList<>();
        List<Song> typeC = new ArrayList<>();

        Map<String, Song> billboardMap = billboardSongs.stream()
                .collect(Collectors.toMap(Song::getTrackName, song -> song));
        Map<String, Song> spotifyMap = spotifySongs.stream()
                .collect(Collectors.toMap(Song::getTrackName, song -> song));

        // Type A: 두 차트에 모두 있는 곡
        for (Song song : billboardSongs) {
            if (spotifyMap.containsKey(song.getTrackName())) {
                Song spotifySong = spotifyMap.get(song.getTrackName());
                // 새로운 Song 객체 생성 후 추가
                Song combinedSong = new Song(song.getRank(), song.getTrackName(), song.getArtistName(), song.getRank() + spotifySong.getRank());
                typeA.add(combinedSong);
            }
        }

        // Type B: 각각의 차트에만 있는 곡
        for (Song song : billboardSongs) {
            if (!spotifyMap.containsKey(song.getTrackName())) {
                typeB.add(new Song(song.getRank(), song.getTrackName(), song.getArtistName(), 0));  // combinedRank를 0으로 설정
            }
        }

        for (Song song : spotifySongs) {
            if (!billboardMap.containsKey(song.getTrackName())) {
                typeB.add(new Song(song.getRank(), song.getTrackName(), song.getArtistName(), 0));  // combinedRank를 0으로 설정
            }
        }

        // Type C: 두 차트에서 같은 순위지만 다른 곡
        Set<String> addedToTypeC = new HashSet<>(); // 중복을 방지하기 위한 Set

        for (Song song : billboardSongs) {
            for (Song spotifySong : spotifySongs) {
                if (song.getRank() == spotifySong.getRank() && !song.getTrackName().equals(spotifySong.getTrackName())) {
                    if (!addedToTypeC.contains(song.getTrackName())) {
                        typeC.add(song);
                        addedToTypeC.add(song.getTrackName());
                    }
                    if (!addedToTypeC.contains(spotifySong.getTrackName())) {
                        typeC.add(spotifySong);
                        addedToTypeC.add(spotifySong.getTrackName());
                    }
                }
            }
        }

        List<Song> finalChart = new ArrayList<>();

        // Type A 먼저 추가 (combinedRank 기준으로 정렬)
        finalChart.addAll(typeA.stream()
                .sorted(Comparator.comparingInt(Song::getCombinedRank))
                .collect(Collectors.toList()));

        // Type B 추가 (조건에 맞게 정렬)
        List<Song> sortedTypeB = typeB.stream()
                .sorted((s1, s2) -> {
                    if (billboardMap.containsKey(s1.getTrackName()) && !billboardMap.containsKey(s2.getTrackName())) {
                        return -1; // Billboard 곡이 우선
                    } else if (!billboardMap.containsKey(s1.getTrackName()) && billboardMap.containsKey(s2.getTrackName())) {
                        return 1; // Spotify 곡은 나중
                    } else {
                        return Integer.compare(s1.getRank(), s2.getRank());
                    }
                }).collect(Collectors.toList());

        // Type B 곡 추가
        finalChart.addAll(sortedTypeB);

        // Type C 추가 (Type B와 함께 추가)
        finalChart.addAll(typeC);

        return finalChart;
    }
}
