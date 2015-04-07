package com.songus.songus;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class QueueActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.queue_queue);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(new SongQueueAdapter(((Songus)getApplication()).getSongQueue()));

        Typeface roboto = ((Songus)getApplication()).roboto;
        Typeface roboto_bold = ((Songus)getApplication()).roboto_bold;
        ((Button)findViewById(R.id.queue_add)).setTypeface(roboto_bold);
        ((Button)findViewById(R.id.queue_end)).setTypeface(roboto);
        ((Button)findViewById(R.id.queue_qr)).setTypeface(roboto);
        setTitle("Play Queue - Event #45123");
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

    }

    public void endEvent(View v){
        Toast.makeText(this, "Event Ended", Toast.LENGTH_LONG);
        //TODO end the event
        Intent i = new Intent(this, JoinActivity.class);
        startActivity(i);
    }
}
