package com.example.notepad;

public class firebasemodel {
    private String datetime;
    private String title;
    private String content;

    public firebasemodel(){

    }

    public firebasemodel(String datetime, String title, String content){
        this.datetime = datetime;
        this.title = title;
        this.content = content;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
