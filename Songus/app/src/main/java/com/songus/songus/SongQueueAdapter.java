package com.songus.songus;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.songus.model.SongQueue;

/**
 * Created by amin on 4/6/15.
 */
public class SongQueueAdapter extends RecyclerView.Adapter<SongQueueAdapter.SongHolder> {

    private SongQueue queue;
    public SongQueueAdapter(SongQueue queue){
        super();
        this.queue = queue;
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

    }

    @Override
    public int getItemCount() {
        return queue.getSongs().size(); //TODO this can be more efficient
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
        public void onClick(View v) {
            CheckBox voteBox = (CheckBox) v.findViewById(R.id.checkbox);
            voteBox.toggle();
        }
    }
}
