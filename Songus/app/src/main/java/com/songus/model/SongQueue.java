package com.songus.model;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Malek on 4/6/2015.
 * Mutable. Represents the song queue. Handles votes.
 */
@ParseClassName("SongQueue")
public class SongQueue extends ParseObject{
    public final static String SONGS = "songs";

    /**
     * Creates an empty song queue
     */
    public SongQueue() {
     }

    /**
     * Creates a queue from the default playlist
     * @param defaultPlaylist contains no duplicates in track ids
     */
    public SongQueue(List<Song> defaultPlaylist){
        super();
        for(Song s : defaultPlaylist){
            getSongs().add(s);
        }
    }

    /**
     * @return the list of all songs, ordered by votes
     */
    public ParseRelation<Song> getSongs(){
        return getRelation(SONGS);
    }

    public List<Song> getList() throws ParseException {
        ParseQuery<Song> query = getSongs().getQuery();
        query.addDescendingOrder(Song.VOTES);
        return query.find();
    }

    /**
     * Votes for the track with the same id in the list
     * @param track the track to be voted for
     * @return True if voting was successful (track found)
     */
    public boolean vote(String track){
        ParseQuery pq = getSongs().getQuery();
        pq.whereEqualTo(Song.TRACK_ID, track);
        try {
            Song s = (Song) pq.getFirst();
            s.vote();
            s.saveInBackground();
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Withdraws a vote for the track with the same id in the list
     * @param track the track in question
     * @return True if action was successful (track found)
     */
    public boolean withdrawVote(String track){
        ParseQuery pq = getSongs().getQuery();
        pq.whereEqualTo(Song.TRACK_ID, track);
        try {
            Song s = (Song) pq.getFirst();
            s.withdrawVote();
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sets the vote count for the track with the same id in the list
     * @param track the track in question
     * @param vote the new vote count
     * @return true if action was successful (track found)
     */
    public boolean setVotes(String track, int vote){
        ParseQuery pq = getSongs().getQuery();
        pq.whereEqualTo(Song.TRACK_ID, track);
        try {
            Song s = (Song) pq.getFirst();
            s.setVote(vote);
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getVote(String track){
        ParseQuery pq = getSongs().getQuery();
        pq.whereEqualTo(Song.TRACK_ID, track);
        try {
            Song s = (Song) pq.getFirst();
            return s.getVote();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Adds a song to the queue if it doesn't exist and is not blocked
     * @param song song to be added
     * @return true if the song added (not blocked and doesn't exist)
     */
    public boolean addSong(final Song song){
        getSongs().add(song);
        return true;
    }

    /**
     * Removes a song whose track is equal to the id of the track from the queue
     * @param track track to be removed
     */
    public void removeSong(String track){
        getSongs().getQuery().whereEqualTo(Song.TRACK_ID, track).getFirstInBackground(new GetCallback<Song>() {
            @Override
            public void done(Song song, ParseException e) {
                if(e != null){
                    getSongs().remove(song);
                }
            }
        });
    }

}
