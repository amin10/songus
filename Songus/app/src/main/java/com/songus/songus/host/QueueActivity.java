package com.songus.songus.host;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.songus.model.Song;
import com.songus.model.SongQueue;
import com.songus.songus.AddSongActivity;
import com.songus.songus.JoinActivity;
import com.songus.songus.R;
import com.songus.songus.SongQueueAdapter;
import com.songus.songus.Songus;

import java.util.ArrayList;

public class QueueActivity extends ActionBarActivity{

    private Song currentSong = null;
    private boolean justSkipped = false;
    private ArrayList<String> votedIds;
    private RecyclerView mRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);
        setTitle("Play Queue - Event #45123");

        votedIds = new ArrayList<>();

        mRecyclerView = (RecyclerView) findViewById(R.id.queue_queue);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(new SongQueueAdapter(((Songus)getApplication()).getSongQueue(), votedIds));


        // TODO
        SongQueue songQueue = ((Songus)getApplication()).getSongQueue();
        /*ParseObject queueParse = new ParseObject("PlayQueue");
        queueParse.put("songList", ((Songus)getApplication()).getSongQueue());*/
        songQueue.getParseObject().saveInBackground();

        Typeface roboto = ((Songus)getApplication()).roboto;
        Typeface roboto_bold = ((Songus)getApplication()).roboto_bold;
        ((Button)findViewById(R.id.queue_add)).setTypeface(roboto_bold);
        ((Button)findViewById(R.id.queue_end)).setTypeface(roboto);
        ((Button)findViewById(R.id.queue_qr)).setTypeface(roboto);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_queue, menu);
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

    public void addSong(View v){
        Intent i = new Intent(this, AddSongActivity.class);
        startActivity(i);
    }

    public void qr(View v){
        //Credit: http://stackoverflow.com/questions/7693633/android-image-dialog-popup
        final Dialog settingsDialog = new Dialog(this);
        settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        settingsDialog.setContentView(getLayoutInflater().inflate(R.layout.qr, null));
        settingsDialog.findViewById(R.id.qr_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsDialog.dismiss();
            }
        });
        settingsDialog.show();
    }

    public void endEvent(View v){
        Toast.makeText(this, "Event Ended", Toast.LENGTH_LONG);
        //TODO end the event
        Intent i = new Intent(this, JoinActivity.class);
        startActivity(i);
    }

    public void play(View v){
        if(currentSong == null) {
            next(v);
            ((ViewFlipper)findViewById(R.id.playback_play_pause)).showNext();
        }else
        if(mBound) {
            mService.play();
            ((ViewFlipper)findViewById(R.id.playback_play_pause)).showNext();

        }
    }

    public void next(View v){
        SongQueue songQueue = ((Songus) getApplication()).getSongQueue();

        if(v == null){
            if(justSkipped) {
                justSkipped = false;
                return;
            }
        }else{
            justSkipped = true;
        }
        if(currentSong!=null){
            songQueue.removeSong(currentSong.getTrack());
            RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.queue_queue);
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
        if(songQueue.getSongs().size()>0){
            Song song = songQueue.getSong(0);
            ((TextView)findViewById(R.id.playback_song_name)).setText(song.getTrack().name);
            ((TextView)findViewById(R.id.playback_artist_name)).setText(song.getTrack().artists.get(0).name);
            if(mBound) {
                mService.play(song.getTrack().uri);
                currentSong = song;
            }
        }else{
            if(mBound) {
                mService.pause();
            }
        }
        mRecyclerView.setAdapter(new SongQueueAdapter(((Songus)getApplication()).getSongQueue(), votedIds));
        mRecyclerView.getAdapter().notifyDataSetChanged();

        mRecyclerView.invalidate();
    }


    private boolean mBound = false;
    private PlayMusic mService;
    final private QueueActivity queueActivity = this;
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayMusic.LocalBinder binder = (PlayMusic.LocalBinder) service;
            mService = binder.getService();
            mService.setView(queueActivity);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void update(int positionInMs, int durationInMs) {
        int progressSeconds = positionInMs/1000,
                durationSeconds = durationInMs/1000;
        ((TextView)findViewById(R.id.playback_progress))
                .setText(progressSeconds / 60 + ":" + progressSeconds % 60 + "/" +
                        durationSeconds / 60 + ":" + durationSeconds % 60);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, PlayMusic.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

}
