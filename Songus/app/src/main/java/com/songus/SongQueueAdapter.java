package com.songus;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.songus.model.Song;
import com.songus.model.SongQueue;
import com.songus.songus.R;

import java.util.List;

/**
 * Created by amin on 4/6/15.
 */
public class SongQueueAdapter extends RecyclerView.Adapter<SongQueueAdapter.SongHolder> {

    private SongQueue queue;
    private List<Song> songList;
    private List<String> votedIds;
    public SongQueueAdapter(SongQueue queue, List<String> votedIds){
        super();
        this.queue = queue;
        this.songList = queue.getSongs();
        this.votedIds = votedIds;
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
        holder.position = position;
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
        public int position;

        private SongQueueAdapter mAdapter;

        public SongHolder(View itemView, SongQueueAdapter adapter) {
            super(itemView);
            itemView.findViewById(R.id.checkbox).setOnClickListener(this);
//            itemView.setOnClickListener(this);

            mAdapter = adapter;

            artist = (TextView) itemView.findViewById(R.id.artist);
            name = (TextView) itemView.findViewById(R.id.name);
            votes = (TextView) itemView.findViewById(R.id.votes);
            voteBox = (CheckBox) itemView.findViewById(R.id.checkbox);
            if(votedIds.contains(songList.get(position).getTrack().id)){
                voteBox.setChecked(true);
            }
        }

        @Override
        public void onClick(View v) {
//           int score = Integer.parseInt(votes.getText().toString())-1;
            queue.vote(songList.get(position).getTrack());
            votedIds.add(songList.get(position).getTrack().id);
            voteBox.setChecked(false);
            votes.setText(queue.getVote(songList.get(position).getTrack())+"");
//           if(voteBox.isChecked()){
//               score = Integer.parseInt(votes.getText().toString())+1;
//               queue.vote(songList.get(position).getTrack());
//               votedIds.add(songList.get(position).getTrack().id);
//           }else{
//               queue.withdrawVote(songList.get(position).getTrack());
//               votedIds.remove(songList.get(position).getTrack().id);
//           }
           votes.setText(queue.getVote(songList.get(position).getTrack())+"");
        }

    }
}
