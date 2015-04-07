package com.songus.songus;

import android.app.Application;
import android.graphics.Typeface;

import com.parse.Parse;
import com.songus.model.SongQueue;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

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
    private static SongQueue songQueue;

    public static final String IS_HOST = "isHost";

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, "U4wtb9EosO0IeqGXMKd68QCgDGi68d2AVyg5NsHT", "42SJicw8BkGDB5Bkn0Pof1vWqcyl9bQmT2kqjrJP");

        /*
         Initialize here
         */
        roboto = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        roboto_bold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
        //songQueue = new SongQueue();
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

    /*
     * Global Methods here.
     */
    public SongQueue getSongQueue(){
        return songQueue;
    }

    public void setSongQueue(SongQueue songQueue) {
        Songus.songQueue = songQueue;
    }
}
