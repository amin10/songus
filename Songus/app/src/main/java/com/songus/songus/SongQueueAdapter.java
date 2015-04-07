package com.songus.songus;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.songus.model.Song;
import com.songus.model.SongQueue;

import java.util.List;

/**
 * Created by amin on 4/6/15.
 */
public class SongQueueAdapter extends RecyclerView.Adapter<SongQueueAdapter.SongHolder> {

    private SongQueue queue;
    private List<Song> songList;
    public SongQueueAdapter(SongQueue queue){
        super();
        this.queue = queue;
        this.songList = queue.getSongs();

    }
    @Override
    public SongHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_queue_row, parent, false);
        SongHolder vh = new SongHolder(v, this);
        return vh;
    }

    @Override
    public void onBindViewHolder(SongHolder holder, int position) {
        String name = this.songList.get(position).getTrack().name;
//        if(name.length() > 20){
//            name = name.substring(0, 20);
//        }
        holder.name.setText(name);
        String artist = this.songList.get(position).getTrack().artists.get(0).name;
        holder.artist.setText(artist);
        int votes = this.songList.get(position).getVote();
        holder.votes.setText(""+votes);
    }

    @Override
    public int getItemCount() {
        return this.songList.size();
    }

    public class SongHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView artist, name, votes;
        public CheckBox voteBox;

        private SongQueueAdapter mAdapter;

        public SongHolder(View itemView, SongQueueAdapter adapter) {
            super(itemView);
            itemView.setOnClickListener(this);

            mAdapter = adapter;

            artist = (TextView) itemView.findViewById(R.id.artist);
            name = (TextView) itemView.findViewById(R.id.name);
            votes = (TextView) itemView.findViewById(R.id.votes);
            voteBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }

        @Override
        public void onClick(View v) {}

    }
}
