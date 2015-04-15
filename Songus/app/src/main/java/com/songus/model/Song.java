package com.songus.model;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Malek on 4/6/2015.
 * Mutable class.
 * Represents a song with the vote for the song.
 */

@ParseClassName("Song")
public class Song extends ParseObject implements Comparable{
    public final static String VOTES = "votes", TRACK_ID = "track_id";


    public Song(){

    }

    public Song(String track){
        put(VOTES, 0);
        put(TRACK_ID, track);
    }
    /***
     * Makes a Song object with 0 votes
     * @param track the track representing this song
     */
    public Song(Track track){
        put(VOTES, 0);
        put(TRACK_ID, track.id);
    }


    /**
     * Increases the vote count by 1
     */
    public void vote(){
        increment(VOTES);
    }

    /**
     * Decreases the vote count by 1
     */
    public void withdrawVote(){
        increment(VOTES, -1);
    }

    /**
     * @return the vote count for this song
     */
    public int getVote(){
        return getInt(VOTES);
    }

    /**
     * Changes the vote count for this song
     * @param vote the new vote count
     */
    public void setVote(int vote){
        put(VOTES, vote);
    }

    /**
     *
     * @return the corresponding track
     */
    public String getTrack() {
        return getString(TRACK_ID);
    }

    @Override
    public int compareTo(Object another) {
        if(!(another instanceof Song))
            throw new IllegalArgumentException("Can only compare a Song object to a song object.");
        return this.getVote() - ((Song)another).getVote();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        if (getVote() != song.getVote()) return false;
        if (!getTrack().equals(song.getTrack())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getTrack().hashCode();
        result = 31 * result + (int) (getVote() ^ (getVote() >>> 32));
        return result;
    }

    /**
     *
     * @param another the other song to be compared to
     * @return true iff the two tracks have the same id
     */
    public boolean sameSong(Song another){
        return another.getTrack().equals(getTrack());
    }
}
