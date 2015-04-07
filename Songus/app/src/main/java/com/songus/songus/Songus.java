package com.songus.songus;

import android.app.Application;

import com.spotify.sdk.android.authentication.AuthenticationResponse;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 * Created by amin on 4/6/15.
 */
public class Songus extends Application {

    private String authCode;
    private AuthenticationResponse response;
    private SpotifyService spotifyService;
    private SpotifyApi spotifyApi;

    public String getAuthCode() {
        return authCode;
    }

    public AuthenticationResponse getResponse() {
        return response;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /*
         Initialize here
         */

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

    /*
     * Global Methods here.
     */
}
