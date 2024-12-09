package com.example.firstproject.model;

public class Song {
    private int rank;
    private String trackName;
    private String artistName;
    private int combinedRank;  // combinedRank 필드 추가

    // 기본 생성자
    public Song() {
    }

    // rank, trackName, artistName, combinedRank을 받는 생성자
    public Song(int rank, String trackName, String artistName, int combinedRank) {
        this.rank = rank;
        this.trackName = trackName;
        this.artistName = artistName;
        this.combinedRank = combinedRank;
    }

    // Getter와 Setter 추가
    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    // combinedRank 필드에 대한 Getter와 Setter 추가
    public int getCombinedRank() {
        return combinedRank;
    }

    public void setCombinedRank(int combinedRank) {
        this.combinedRank = combinedRank;
    }
}
