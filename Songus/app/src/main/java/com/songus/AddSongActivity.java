package com.songus;

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
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.songus.model.Song;
import com.songus.model.SongQueue;
import com.songus.songus.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AddSongActivity extends ActionBarActivity {
    private String qr;
    private boolean host;
    private static Track SELECTED_TRACK;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song);
        qr = getIntent().getExtras().getString("QR");
        host = getIntent().getExtras().getBoolean("HOST");
    }

    private void startQueueActivity(){
        if(host) {
            Intent i = new Intent(getApplicationContext(), com.songus.host.QueueActivity.class);
            i.putExtra("QR", qr);
            startActivity(i);
        }else{
            Intent i = new Intent(getApplicationContext(), com.songus.attendee.QueueActivity.class);
            i.putExtra("QR", qr);
            startActivity(i);
        }
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
                                 final Song s = new Song(SELECTED_TRACK);

                                 ParseQuery<SongQueue> query = ParseQuery.getQuery(SongQueue.class);
                                 query.getInBackground(qr, new GetCallback<SongQueue>() {
                                     public void done(SongQueue songQueue, ParseException e) {
                                         if (e == null) {

                                                 ParseQuery<Song> currentSongs = songQueue.getSongs().getQuery();
                                                 currentSongs.whereEqualTo(Song.TRACK_ID, SELECTED_TRACK.id);
                                                 try{
                                                     Song existingSong = currentSongs.getFirst();
                                                     //If no exception thrown. Song already in queue:

                                                     List<Object> voters = existingSong.getList("voters");
                                                     if(voters.contains(ParseUser.getCurrentUser().getObjectId())){
                                                         Toast.makeText(getApplicationContext(),getResources().getString(R.string.song_exists_and_voted), Toast.LENGTH_LONG).show();
                                                         startQueueActivity();
                                                     }else {
                                                         Toast.makeText(getApplicationContext(), getResources().getString(R.string.song_exists), Toast.LENGTH_LONG).show();
                                                         existingSong.vote();
                                                         existingSong.add("voters", ParseUser.getCurrentUser().getObjectId());
                                                         existingSong.save();
                                                         startQueueActivity();
                                                     }
                                                 }catch(ParseException e1){//Else, song being saved is in fact unique.
                                                     try {
                                                         s.vote();
                                                         s.put("voters", Arrays.asList(ParseUser.getCurrentUser().getObjectId()));
                                                         s.save();
                                                         songQueue.getSongs().add(s);
                                                         songQueue.saveInBackground(new SaveCallback() {
                                                             @Override
                                                             public void done(ParseException e) {
                                                                 if(e != null){
                                                                     e.printStackTrace();
                                                                 }
                                                               startQueueActivity();
                                                             }
                                                         });
                                                     } catch (ParseException e2) {//saving fails.
                                                         e2.printStackTrace();
                                                     }
                                                 }

                                         }else{
                                             e.printStackTrace();
                                         }
                                     }
                                 });

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
