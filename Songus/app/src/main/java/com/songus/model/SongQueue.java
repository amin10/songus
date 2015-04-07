package com.songus.model;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SendCallback;
import com.songus.songus.SongQueueEventListener;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Malek on 4/6/2015.
 * Mutable. Represents the song queue. Handles votes.
 */
public class SongQueue {
    private List<Song> songs;
    private Set<SongQueueEventListener> listeners;

    /**
     * Creates an empty song queue
     */
    public SongQueue() {
        songs = new ArrayList<>();
        listeners = new HashSet<>();
    }

    /**
     * Creates a queue from the default playlist
     * @param defaultPlaylist contains no duplicates in track ids
     */
    public SongQueue(List<Song> defaultPlaylist){
        this();
        songs = new ArrayList<>(Lists.transform(defaultPlaylist, new Function<Song, Song>() {
            @Override
            public Song apply(Song input) {
                return input.getCopy();
            }
        }));
        reorder();
    }

    /**
     * @return the list of all songs, ordered by votes
     */
    public List<Song> getSongs(){
        List<Song> songsCopy = new ArrayList<>();
        for(Song song:songs){
            songsCopy.add(song.getCopy());
        }
        return songsCopy;
    }

    /**
     * @param index in {0...queue size-1}
     * @return Song such that there is a one to one mapping of index to songs acoss {0...queue size -1}
     */
    public Song getSong(int index){
        return getSongs().get(index);
    }


    /**
     * Votes for the track with the same id in the list
     * @param track the track to be voted for
     * @return True if voting was successful (track found)
     */
    public boolean vote(Track track){
        for(Song song:songs){
            if(song.getTrack().id.equals(track.id)){
                song.vote();
                reorder();
                sendChangeNotification();
                return true;
            }
        }
        return false;
    }

    /**
     * Withdraws a vote for the track with the same id in the list
     * @param track the track in question
     * @return True if action was successful (track found)
     */
    public boolean withdrawVote(Track track){
        for(Song song:songs){
            if(song.getTrack().id.equals(track.id)){
                song.withdrawVote();
                reorder();
                sendChangeNotification();
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the vote count for the track with the same id in the list
     * @param track the track in question
     * @param votes the new vote count
     * @return true if action was successful (track found)
     */
    public boolean setVotes(Track track, int votes){
        for(Song song:songs){
            if(song.getTrack().id.equals(track.id)){
                song.setVote(votes);
                reorder();
                sendChangeNotification();
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a song to the queue if it doesn't exist and is not blocked
     * @param song song to be added
     * @return true if the song added (not blocked and doesn't exist)
     */
    public boolean addSong(Song song){
        for(Song otherSong:songs){
            if(otherSong.sameSong(song)){
                return false;
            }
        }
        songs.add(song);
        reorder();
        sendChangeNotification();
        return true;
    }

    /**
     * Removes a song whose track is equal to the id of the track from the queue
     * @param track track to be removed
     * @return true if the track is removed (it exists)
     */
    public boolean removeSong(Track track){
        Song song = null;
        for(Song potentialSong:songs){
            if(track.id.equals(potentialSong.getTrack().id)){
                song = potentialSong;
                break;
            }
        }
        if(song != null){
            songs.remove(song);
            reorder();
            sendChangeNotification();
            return true;
        }
        return false;
    }

    /**
     * Adds the listener to the modifications of the queue to the queue if it doesn't exist
     * @param listener the listener in question
     */
    public void addListener(SongQueueEventListener listener){
        listeners.add(listener);
    }

    /**
     * Remove the listener to the modifications of the queue from the queue if it exists
     * @param listener the listener in question
     */
    public void removeListener(SongQueueEventListener listener){
        listeners.remove(listener);
    }

    /**
     * Notifies listeners of changes
     */
    private void sendChangeNotification(){
        // TODO
        getParseObject().saveInBackground();
        ParsePush push = new ParsePush();
        push.setChannel("QueueChanges");
        push.setMessage("Database updated");
        push.sendInBackground(new SendCallback() {
            @Override
            public void done(ParseException e) {
                Log.d("SongUs","push sent done");
                Log.d("SongUs", (e == null) + "");
            }


        });
        for(SongQueueEventListener listener:listeners){
            listener.onSongQueueChange(this);
        }
    }

    /**
     * Reorders the list taking into account the votes
     */
    private void reorder() {
        Collections.sort(songs, Collections.reverseOrder());
    }

    public ParseObject getParseObject(){
        ParseObject parseObject = new ParseObject("SongQueue");
        String ids[] = new String[songs.size()];
        int votes[] = new int[ids.length];
        for(int i=0; i<songs.size(); i++){
            ids[i] = songs.get(i).getTrack().id;
            votes[i] = songs.get(i).getVote();
        }

        try {
            parseObject.put("ids", new JSONArray(ids));
        } catch (JSONException e) {
            return null;
        }

        try {
            parseObject.put("votes", new JSONArray(votes));
        } catch (JSONException e) {
            return null;
        }
        // TODO
        return parseObject;
    }
}
