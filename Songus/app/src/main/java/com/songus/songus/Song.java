package com.songus.songus;

import android.util.Log;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Malek on 4/6/2015.
 * Mutable class.
 * Represents a song with the vote for the song.
 */

public class Song  implements Comparable{
    final private Track track;
    private int vote;

    /***
     * Makes a Song object with 0 votes
     * @param track the track representing this song
     */
    public Song(Track track){
        this.track = track;
        vote = 0;
    }

    /**
     * Increases the vote count by 1
     */
    public void vote(){
        vote++;
    }

    /**
     * Decreases the vote count by 1
     */
    public void withdrawVote(){
        vote--;
    }

    /**
     *
     * @return the vote count for this song
     */
    public int getVote(){
        return vote;
    }

    /**
     * Changes the vote count for this song
     * @param vote the new vote count
     */
    public void setVote(int vote){
        this.vote = vote;
    }

    /**
     *
     * @return the corresponding track
     */
    public Track getTrack() {
        return track;
    }

    /**
     *
     * @return An exact copy of this object (for defensive copying).==
     */
    public Song getCopy(){
        Song copy = new Song(track);
        copy.setVote(vote);
        return copy;
    }

    @Override
    public int compareTo(Object another) {
        if(!(another instanceof Song))
            throw new IllegalArgumentException("Can only compare a Song object to a song object.");
        return this.vote - ((Song)another).vote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        if (vote != song.vote) return false;
        if (!track.equals(song.track)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = track.hashCode();
        result = 31 * result + (int) (vote ^ (vote >>> 32));
        return result;
    }

    /**
     *
     * @param another the other song to be compared to
     * @return true iff the two tracks have the same id
     */
    public boolean sameSong(Song another){
        return another.track.id == track.id;
    }
}
