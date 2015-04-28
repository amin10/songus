package com.songus.attendee;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
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
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.jwetherell.quick_response_code.data.Contents;
import com.jwetherell.quick_response_code.qrcode.QRCodeEncoder;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.songus.AddSongActivity;
import com.songus.JoinActivity;
import com.songus.SongQueueAdapter;
import com.songus.Songus;
import com.songus.model.Song;
import com.songus.model.SongQueue;
import com.songus.songus.R;

import java.util.List;

public class QueueActivity extends ActionBarActivity{

    private RecyclerView mRecyclerView;
    private Songus songus;
    private String qr;
    private List<Song> songList;
    private SongQueue songQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_activity_queue);

        setTitle("Play Queue");
        if(getIntent().getExtras() != null){
            qr = getIntent().getExtras().getString("QR");
            setTitle(getResources().getString(R.string.queue_title_prefix) + qr);
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
            mRecyclerView.setAdapter(new SongQueueAdapter(songList, qr, songus, false));


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

    private void refresh() throws ParseException{
        songQueue.fetchIfNeeded();
        List<Song> tempSongList = songQueue.getList();
        songList.clear();
        songList.addAll(tempSongList);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public void addSong(View v){
        Intent i = new Intent(this, AddSongActivity.class);
        i.putExtra("QR", qr);
        i.putExtra("HOST", false);
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
            Bitmap bm = encoder.encodeAsBitmap();

            if(bm != null) {
                ((ImageView)settingsDialog.findViewById(R.id.qr_img)).setImageBitmap(bm);
            }else{
                Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show();
            }
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

    /**
     * Leaves the event. It can be rejoined again at any time by scanning the QR code.
     * @param v
     */
    public void endEvent(View v){
        Toast.makeText(this, "Party's Over", Toast.LENGTH_SHORT);
        Intent i = new Intent(this, JoinActivity.class);
        startActivity(i);
    }

}
