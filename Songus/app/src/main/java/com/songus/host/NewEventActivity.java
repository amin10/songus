package com.songus.host;

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
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import com.songus.model.Song;
import com.songus.model.SongQueue;
import com.songus.songus.R;
import com.songus.Songus;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;


import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.User;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class NewEventActivity extends ActionBarActivity implements ConnectionStateCallback {

    private Songus songus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        //Get Spotify
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(Songus.CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                Songus.REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "playlist-read",
                "playlist-read-private", "streaming"});
        AuthenticationRequest request = builder.build();
        songus = (Songus)getApplication();
        AuthenticationClient.openLoginActivity(this, Songus.REQUEST_CODE, request);
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


    public void emptyPlaylist(View v){
        final SongQueue empty = new SongQueue();
        empty.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                final SongQueue q = new SongQueue();
                q.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Intent i = new Intent(getApplicationContext(), QueueActivity.class);
                        i.putExtra("QR", q.getObjectId());
                        startActivity(i);
                    }
                });
            }
        });
    }
    public void choosePlaylist(View v){

        final SpotifyService spotify = songus.getSpotifyService();

        final Context context = this;

        spotify.getMe(new Callback<User>() {
            @Override
            public void success(final User user, Response response) {
                spotify.getPlaylists(user.id, new Callback<Pager<Playlist>>() {
                    @Override
                    public void success(final Pager<Playlist> playlistPager, Response response) {
                        if(playlistPager.total == 0) {
                            Toast.makeText(context, "No playlists on Spotify", Toast.LENGTH_SHORT);
                            return;
                        }
                        final List<Playlist> playlistList = playlistPager.items;
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
                            final AtomicInteger choice = new AtomicInteger(0);
                            adb.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface d, int n) {
                                    choice.set(n);
                                }

                            });
                            adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                spotify.getPlaylistTracks(user.id, playlistList.get(choice.get()).id,
                                    new Callback<Pager<PlaylistTrack>>() {
                                        @Override
                                        public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                                            final SongQueue q = new SongQueue();
                                            for(PlaylistTrack p : playlistTrackPager.items){
                                                Song s = new Song(p.track);
                                                try{
                                                    s.save();
                                                    q.addSong(s);
                                                }catch(ParseException e){}
                                            }
                                            q.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                Intent i = new Intent(getApplicationContext(), QueueActivity.class);
                                                i.putExtra("QR", q.getObjectId());
                                                startActivity(i);
                                                }
                                            });
                                        }

                                        @Override
                                        public void failure(RetrofitError retrofitError) {
                                            Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
                                        }
                                    });
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
                Log.d("SongUs Debug", retrofitError.getResponse().getReason());
            }
        });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == Songus.REQUEST_CODE) {
            //Get Spotify
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            songus.setResponse(response);
            songus.setAuthCode(response.getAccessToken());
            SpotifyApi api = new SpotifyApi();
            api.setAccessToken(songus.getAuthCode());
            songus.setSpotifyApi(api);
            SpotifyService spotify = api.getService();
            songus.setSpotifyService(spotify);
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("SongUs Debug", "Logged in");
    }

    @Override
    public void onLoggedOut() {
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

