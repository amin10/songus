package com.songus;

/**
 * Created by Malek on 4/6/2015.
 */

import com.songus.model.SongQueue;

/**
 * To be implemented by classes that want to listen to changes done on song queues
 */
public interface SongQueueEventListener {
    /**
     * This method will be called when a queue that the object is listening to goes through changes.
     * @param queue the queue which went through the changes
     */
    void onSongQueueChange(SongQueue queue);
}
