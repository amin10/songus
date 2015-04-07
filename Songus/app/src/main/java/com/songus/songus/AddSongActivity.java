package com.songus.songus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;


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
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        final List<Track> tracks = new ArrayList<Track>(){};
        Track dummy = new Track();
        dummy.name = "Dummy Track";
        for (int i = 0; i< 20; i++) {
            tracks.add(dummy);
        }

        SELECTED_TRACK = tracks.get(0);

        CharSequence items[] = new CharSequence[tracks.size()];
        for (int i = 0; i < tracks.size(); i++){
            items[i] = tracks.get(i).name;
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
                Toast.makeText(getApplicationContext(), SELECTED_TRACK.name, Toast.LENGTH_LONG).show();
                Intent i = new Intent(getApplicationContext(), QueueActivity.class);
                i.putExtra("track", SELECTED_TRACK.id);
                startActivity(i);
            }
        });
        
        adb.setTitle("Results");
        adb.show();
    }
}
