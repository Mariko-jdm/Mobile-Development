package com.example.dayflow.models;

public class Note {
    private long id;
    private String title;
    private String text;
    private String date;
    private boolean isLiked;

    public Note(String title, String text, String date, long id) {
        this.title = title;
        this.text = text;
        this.date = date;
        this.id = id;
        this.isLiked = false;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { this.isLiked = liked; }
}