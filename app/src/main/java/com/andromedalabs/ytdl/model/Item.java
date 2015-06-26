package com.andromedalabs.ytdl.model;

/**
 * Created by adnan on 2/9/15.
 */
public class Item {

    public String url;
    public String itag;
    public String title;

    public Item(String title,String url, String itag){
        this.title = title;
        this.url = url;
        this.itag = itag;
    }

    public String getQuality(String itag){
        switch (itag){
            case "17":
                return "3GP 144P";
            case "18":
                return "MP4 360P";
            case "22":
                return "MP4 720P";
            case "36":
                return "3GP 240P";
            case "37":
                return "MP4 1080P";
            case "140":
                return "MP3 Audio";
        }
        return null;
    }

    public String getExtension(String itag){
        switch (itag){
            case "17":
                return ".3gp";
            case "18":
                return ".mp4";
            case "22":
                return ".mp4";
            case "36":
                return ".3gp";
            case "37":
                return ".mp4";
            case "140":
                return ".mp3";
        }
        return null;
    }
}
