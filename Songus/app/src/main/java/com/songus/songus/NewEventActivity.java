package com.songus.songus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;


import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.User;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class NewEventActivity extends ActionBarActivity implements ConnectionStateCallback {

    // TODO make sure the user logs in

    private static final String CLIENT_ID = "d840bdd10b4947d49b4186ddac34b0de";
    private static final String REDIRECT_URI = "http://www.malekbr.com/";



    private static final int REQUEST_CODE = 1337;

    private String authCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "playlist-read",
                "playlist-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void choosePlaylist(View v){
        SpotifyApi api = new SpotifyApi();

        api.setAccessToken(authCode);

        final SpotifyService spotify = api.getService();


        final Context context = this;

        spotify.getMe(new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                spotify.getPlaylists(user.id, new Callback<Pager<Playlist>>() {
                    @Override
                    public void success(Pager<Playlist> playlistPager, Response response) {
                        List<Playlist> playlistList = playlistPager.items;
                        List<CharSequence> playlistNames = Lists.transform(playlistList, new Function<Playlist, CharSequence>() {
                            @Override
                            public CharSequence apply(Playlist input) {
                                return input.name;
                            }
                        });

                        final CharSequence items[] = new CharSequence[playlistNames.size()];
                        playlistNames.toArray(items);

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder adb = new AlertDialog.Builder(context);
                                adb.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface d, int n) {
                                        Toast.makeText(getApplicationContext(), n + "", Toast.LENGTH_SHORT).show();
                                    }

                                });
                                adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent i = new Intent(context, QueueActivity.class);
                                        startActivity(i);
                                    }
                                });
                                adb.setTitle("Choose Spotify Playlist");
                                adb.show();
                            }
                        });
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
                Log.d("SongUs Debug", retrofitError.getResponse().getReason());
            }
        });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            Toast.makeText(this, "Logged in", Toast.LENGTH_LONG).show();
            authCode = response.getAccessToken();

        }
    }

    @Override
    public void onLoggedIn() {
        Toast.makeText(this, "Logged in", Toast.LENGTH_LONG).show();
        Log.d("SongUs Debug", "Logged in");
    }

    @Override
    public void onLoggedOut() {
        Toast.makeText(this, "Logged out", Toast.LENGTH_LONG).show();
        Log.d("SongUs Debug", "Logged out");
    }

    @Override
    public void onLoginFailed(Throwable throwable) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {
        Log.d("SongUs Debug", s);
    }
}

