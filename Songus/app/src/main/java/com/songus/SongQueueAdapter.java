package com.songus;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Atomics;
import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.songus.Songus;
import com.songus.model.Song;
import com.songus.model.SongQueue;
import com.songus.songus.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by amin on 4/6/15.
 */
public class SongQueueAdapter extends RecyclerView.Adapter<SongQueueAdapter.SongHolder> {

    private String qr;
    private List<Song> songList;
    private List<String> votedIds;
    private Songus songus;

    public SongQueueAdapter(List<Song> songList, String qr, Songus songus){
        this.songus = songus;
        this.songList = songList;
        this.votedIds = new ArrayList<>();
    }

    @Override
    public SongHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_queue_row, parent, false);
        SongHolder vh = new SongHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final SongHolder holder,final int position) {
        final AtomicReference<Track> t = new AtomicReference<Track>();//TODO
        t.set(null);
        SpotifyService service = songus.getSpotifyService();
        songus.getSpotifyService().getTrack(this.songList.get(position).getTrack(), new Callback<Track>() {
            @Override
            public void success(Track track, Response response) {
                t.set(track);
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
            }
        });
        while(t.get() == null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        holder.position = position;
        holder.name.setText(t.get().name);
        holder.artist.setText(t.get().artists.get(0).name);
        int votes = songList.get(position).getVote();
        holder.votes.setText("" + votes);

    }

    @Override
    public int getItemCount() {
         return songList.size();
    }

    public class SongHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView artist, name, votes;
        public CheckBox voteBox;
        public int position;


        public SongHolder(View itemView) {
            super(itemView);
            itemView.findViewById(R.id.checkbox).setOnClickListener(this);
            artist = (TextView) itemView.findViewById(R.id.artist);
            name = (TextView) itemView.findViewById(R.id.name);
            votes = (TextView) itemView.findViewById(R.id.votes);
            voteBox = (CheckBox) itemView.findViewById(R.id.checkbox);
//            if(votedIds.contains(songList.get(position).getTrack())){
//                voteBox.setChecked(true);
//            }
        }

        @Override
        public void onClick(View v) {
//            int score = Integer.parseInt(votes.getText().toString())-1;
//            queue.vote(songList.get(position).getTrack());
//            votedIds.add(songList.get(position).getTrack());
//            voteBox.setChecked(false);
//            votes.setText(queue.getVote(songList.get(position).getTrack())+"");
//            votes.setText(queue.getVote(songList.get(position).getTrack())+"");
        }

    }
}
