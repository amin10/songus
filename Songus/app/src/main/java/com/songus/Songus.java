package com.songus;

import android.app.Application;
import android.graphics.Typeface;
import android.os.StrictMode;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;
import com.songus.model.Song;
import com.songus.model.SongQueue;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by amin on 4/6/15.
 */
public class Songus extends Application {

    /*
     Global Fields Here
     */

    public static final String CLIENT_ID = "d840bdd10b4947d49b4186ddac34b0de";
    public static final String REDIRECT_URI = "http://www.malekbr.com/";



    public static final int REQUEST_CODE = 1337;
    private String authCode;
    private AuthenticationResponse response;
    private SpotifyService spotifyService;
    private SpotifyApi spotifyApi;

    public static Typeface roboto;
    public static Typeface roboto_bold;
//    private static SongQueue songQueue;

    public static final String IS_HOST = "isHost";

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ParseObject.registerSubclass(Song.class);
        ParseObject.registerSubclass(SongQueue.class);
        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicWriteAccess(true);
        defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

        ParseCrashReporting.enable(this);
        Parse.initialize(this, "U4wtb9EosO0IeqGXMKd68QCgDGi68d2AVyg5NsHT", "42SJicw8BkGDB5Bkn0Pof1vWqcyl9bQmT2kqjrJP");
        /*
         Initialize here
         */
        roboto = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        roboto_bold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public void setResponse(AuthenticationResponse response) {
        this.response = response;
    }


    public void setSpotifyService(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    public SpotifyService getSpotifyService() {
        return spotifyService;
    }

    public void setSpotifyApi(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    public SpotifyApi getSpotifyApi() {
        return spotifyApi;
    }

    public String getAuthCode() {
        return authCode;
    }

    public AuthenticationResponse getResponse() {
        return response;
    }
//
//    /*
//     * Global Methods here.
//     */
//    public SongQueue getSongQueue(){
//        return songQueue;
//    }
//
//    public void setSongQueue(SongQueue songQueue) {
//        Songus.songQueue = songQueue;
//    }

    public boolean isValidEventCode(String code){
        return true; //TODO
    }

    public Track getTrackById(String id){
        return spotifyService.getTrack(id);
    }
}
