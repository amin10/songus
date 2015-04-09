package com.songus.attendee;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.songus.model.Song;
import com.songus.model.SongQueue;
import com.songus.songus.R;
import com.songus.Songus;
import com.songus.AddSongActivity;
import com.songus.JoinActivity;

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
        findViewById(R.id.current_playback).setVisibility(View.GONE);
        ((Button)findViewById(R.id.queue_end)).setText("LEAVE");

        if(getIntent().getExtras() != null){
            String qr = getIntent().getExtras().getString("QR");
            setTitle("Play Queue - Event #" + qr);
        }

        votedIds = new ArrayList<>();

        mRecyclerView = (RecyclerView) findViewById(R.id.queue_queue);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
//        mRecyclerView.setAdapter(new SongQueueAdapter(((Songus)getApplication()).getSongQueue(), votedIds));
        ((Songus)getApplication()).setSongQueue(new SongQueue());
        SongQueue songQueue = ((Songus)getApplication()).getSongQueue();
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
        Toast.makeText(this, "Left Event", Toast.LENGTH_LONG);
        Intent i = new Intent(this, JoinActivity.class);
        startActivity(i);
    }

}
