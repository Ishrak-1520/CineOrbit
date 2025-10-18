package com.example.demo;

public class Movie {
    private int id;
    private String title;
    private String genre;
    private String duration;
    private String date;
    private String posterPath;
    private String status;

    public Movie(int id, String title, String genre, String duration, String date, String posterPath, String status) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.date = date;
        this.posterPath = posterPath;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public String getDuration() { return duration; }
    public String getDate() { return date; }
    public String getPosterPath() { return posterPath; }
    public String getStatus() { return status; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setDate(String date) { this.date = date; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return title;
    }
}
