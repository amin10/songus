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
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.base.Joiner;
import com.songus.model.Song;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class AddSongActivity extends ActionBarActivity {

    private static Track SELECTED_TRACK;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_song, menu);
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

    public void search(View v){
        final Context context = this;
        SpotifyService spotify = ((Songus)getApplication()).getSpotifyService();

        EditText artistField= (EditText)findViewById(R.id.add_song_artist);
        String artist = artistField.getText().toString();
        EditText albumField= (EditText)findViewById(R.id.add_song_album);
        String album = albumField.getText().toString();
        EditText songField= (EditText)findViewById(R.id.add_song_song);
        String song = songField.getText().toString();
        EditText keywordsField= (EditText)findViewById(R.id.add_song_keyword);
        String keywords = keywordsField.getText().toString();
        keywords = Joiner.on('+').join(keywords.split("[^A-Za-z0-9_]+"));
        song = Joiner.on('+').join(song.split("[^A-Za-z0-9_]+"));
        album = Joiner.on('+').join(album.split("[^A-Za-z0-9_]+"));
        artist = Joiner.on('+').join(artist.split("[^A-Za-z0-9_]+"));
        ArrayList<String> queryBuilder = new ArrayList<>();
        if(!keywords.isEmpty())
            queryBuilder.add(keywords);
        if(!album.isEmpty())
            queryBuilder.add("album:"+album);
        if(!artist.isEmpty())
            queryBuilder.add("artist:"+artist);
        if(!song.isEmpty())
            queryBuilder.add("track:" + song);
        String query = Joiner.on(' ').join(queryBuilder);

        spotify.searchTracks(query, new Callback<TracksPager>() {
            @Override
            public void success(final TracksPager tracksPager, Response response) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                     @Override
                     public void run() {
                         if(tracksPager.tracks.total == 0){
                             Toast.makeText(getApplicationContext(), "No results", Toast.LENGTH_SHORT).show();
                             return;
                         }
                         Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
                         AlertDialog.Builder adb = new AlertDialog.Builder(context);
                         final List<Track> tracks = new ArrayList<Track>(tracksPager.tracks.items);

                         SELECTED_TRACK = tracks.get(0);

                         CharSequence items[] = new CharSequence[tracks.size()];
                         for (int i = 0; i < tracks.size(); i++){
                             items[i] = tracks.get(i).artists.get(0).name +" - " + tracks.get(i).name;
                         }

                         adb.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface d, int n) {
                                 SELECTED_TRACK = tracks.get(n);
                             }
                         });
                         adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
//                               Toast.makeText(getApplicationContext(), SELECTED_TRACK.name, Toast.LENGTH_LONG).show();
                                 ((Songus) getApplication()).getSongQueue().addSong(new Song(SELECTED_TRACK));
                                 Intent i = new Intent(getApplicationContext(), QueueActivity.class);
                                 startActivity(i);
                             }
                         });

                         adb.setTitle("Results");
                         adb.show();
                     }
                 });

            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d("SongUs Debug", retrofitError.getResponse().getReason());
            }
        });

    }
}
