package com.songus.host;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.jwetherell.quick_response_code.data.Contents;
import com.jwetherell.quick_response_code.qrcode.QRCodeEncoder;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.songus.AddSongActivity;
import com.songus.JoinActivity;
import com.songus.SongQueueAdapter;
import com.songus.Songus;
import com.songus.model.Song;
import com.songus.model.SongQueue;
import com.songus.songus.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class QueueActivity extends ActionBarActivity{

    private Song currentSong = null;
    private boolean justSkipped = false;
    private RecyclerView mRecyclerView;
    private Songus songus;
    private String qr;
    private List<Song> songList;
    private SongQueue songQueue;
    private Bitmap qr_bm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);
        setTitle("Play Queue");
        if(getIntent().getExtras() != null){
            qr = getIntent().getExtras().getString("QR");
            setTitle(getString(R.string.queue_title_prefix) + qr);
        }

        songus = (Songus) getApplication();

        ParseQuery<SongQueue> query = ParseQuery.getQuery(SongQueue.class);
        songQueue = null;
        try {
            songQueue = query.get(qr);
            songQueue.fetchIfNeeded();
            songList = songQueue.getList();
            mRecyclerView = (RecyclerView) findViewById(R.id.queue_queue);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mRecyclerView.getContext());
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setAdapter(new SongQueueAdapter(songList, qr, songus, true));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Typeface roboto = songus.roboto;
        Typeface roboto_bold = songus.roboto_bold;
        ((Button)findViewById(R.id.queue_add)).setTypeface(roboto_bold);
        ((Button)findViewById(R.id.queue_end)).setTypeface(roboto);
        ((Button)findViewById(R.id.queue_qr)).setTypeface(roboto);

        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    refresh();
                    refreshLayout.setRefreshing(false);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public void addSong(View v){
        Intent i = new Intent(this, AddSongActivity.class);
        i.putExtra("QR", qr);
        i.putExtra("HOST", true);
        startActivity(i);
    }

    public void qr(View v){

        //Credit: http://stackoverflow.com/questions/7693633/android-image-dialog-popup
        final Dialog settingsDialog = new Dialog(this);
        settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        settingsDialog.setContentView(getLayoutInflater().inflate(R.layout.qr, null));
        //Credit: http://stackoverflow.com/questions/8800919/how-to-generate-a-qr-code-for-an-android-application

        // This uses jwetherell's zxing QRCodeEncoder
        try {
            // generate a 150x150 QR code
            QRCodeEncoder encoder = new QRCodeEncoder(qr,
                    null,
                    Contents.Type.TEXT,
                    BarcodeFormat.QR_CODE.toString(),
                    500);
            qr_bm = encoder.encodeAsBitmap();

            if(qr_bm != null) {
                ((ImageView)settingsDialog.findViewById(R.id.qr_img)).setImageBitmap(qr_bm);

            }else{/*Failed to generate QR code*/}
        } catch (WriterException e) {
            //eek
         }

        settingsDialog.findViewById(R.id.qr_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsDialog.dismiss();
            }
        });
        settingsDialog.show();
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
        if(v == null){
            if(justSkipped) {
                justSkipped = false;
                return;
            }
        }else{
            justSkipped = true;
        }
        if(currentSong!=null){
            try {
                ParseQuery<Song> songParseQuery = songQueue.getSongs().getQuery();
                songParseQuery.whereEqualTo(Song.TRACK_ID, currentSong.getTrack());
                Song justEnded = songParseQuery.getFirst();
                justEnded.delete();
                refresh();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        List<Song> songs = new ArrayList<>();
        songs = songList;
        if(songs.size()>0){
            final Song song = songs.get(0);
            // Get the track from the id of the track
            new AsyncTask<String, Void, Track> (){

                @Override
                protected Track doInBackground(String... params) {
                    return songus.getTrackById(params[0]);
                }

                @Override
                protected void onPostExecute(Track track) {
                    ((TextView)findViewById(R.id.playback_song_name)).setText(track.name);
                    ((TextView)findViewById(R.id.playback_artist_name)).setText(track.artists.get(0).name);
                    if(mBound) {
                        mService.play(track.uri);
                        currentSong = song;
                    }
                }
            }.execute(song.getTrack());


        }else{
            if(mBound) {
                mService.pause();
            }
        }
        mRecyclerView.setAdapter(new SongQueueAdapter(songs, qr, songus, true));
        mRecyclerView.getAdapter().notifyDataSetChanged();
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
        String pSeconds = progressSeconds%60+"", dSeconds = durationSeconds%60+"";
        if(progressSeconds%60 <10){
            pSeconds = "0"+pSeconds;
        }if(durationSeconds%60<10){
            dSeconds = "0"+dSeconds;
        }

        ((TextView)findViewById(R.id.playback_progress))
                .setText(progressSeconds / 60 + ":" + pSeconds + "/" +
                        durationSeconds / 60 + ":" + dSeconds);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(getApplicationContext(), PlayMusic.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void refresh() throws ParseException{
        songQueue.fetchIfNeeded();
        List<Song> tempSongList = songQueue.getList();
        songList.clear();
        songList.addAll(tempSongList);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }
    /**
     * Permanently ends the event. Removes it from Parse database.
     * @param v
     */
    public void endEvent(View v){

        new AlertDialog.Builder(this)
                .setTitle("End Event")
                .setMessage("Really end the event?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        songQueue.deleteEventually();
                        Intent i = new Intent(QueueActivity.this, JoinActivity.class);
                        startActivity(i);
                    }})
                .setNegativeButton("No", null).show();
    }
}

