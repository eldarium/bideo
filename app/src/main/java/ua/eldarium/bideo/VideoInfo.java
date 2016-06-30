package ua.eldarium.bideo;

import android.graphics.Bitmap;

public class VideoInfo {
    public Bitmap thumbnail;
    public String name;
    public String path;

    public VideoInfo(String name, Bitmap thumbnail, String path){
        this.name=name;
        this.thumbnail=thumbnail;
        this.path=path;
    }
}
