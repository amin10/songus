package com.songus.songus;

import android.app.Application;
import android.graphics.Typeface;

/**
 * Created by amin on 4/6/15.
 */
public class Songus extends Application {

    /*
     Global Fields Here
     */
    public static Typeface roboto;
    public static Typeface roboto_bold;
    private static SongQueue songQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        /*
         Initialize here
         */
        roboto = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        roboto_bold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
        songQueue = new SongQueue();
    }

    /*
     * Global Methods here.
     */
    public SongQueue getSongQueue(){
        return songQueue;
    }
}
