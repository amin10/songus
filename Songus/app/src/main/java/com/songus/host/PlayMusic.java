package com.songus.host;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.songus.Songus;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.PlayerStateCallback;
import com.spotify.sdk.android.player.Spotify;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class PlayMusic extends Service implements PlayerNotificationCallback,
        ConnectionStateCallback {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    private Player mPlayer;
    private final Semaphore playerReady = new Semaphore(0);
    Timer refreshView;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private QueueActivity view;

    public void setView(QueueActivity view) {
        this.view = view;
    }

    public QueueActivity getView() {
        return view;
    }

    public void pause() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    playerReady.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                playerReady.release();
                mPlayer.pause();
            }
        }).start();
    }


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        PlayMusic getService() {
            // Return this instance of LocalService so clients can call public methods
            return PlayMusic.this;
        }
    }




    @Override
    public void onCreate() {
        super.onCreate();
        refreshView = new Timer();
        Songus application = (Songus)getApplication();
        Config playerConfig = new Config(this, application.getResponse().getAccessToken(),
                application.CLIENT_ID, Config.DeviceType.COMPUTER);
        mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
            @Override
            public void onInitialized(Player player) {
                mPlayer.addConnectionStateCallback(PlayMusic.this);
                mPlayer.addPlayerNotificationCallback(PlayMusic.this);
                playerReady.release();
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    };


    public void play(){

        final QueueActivity view = this.view;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    playerReady.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                playerReady.release();
                mPlayer.getPlayerState(new PlayerStateCallback() {
                    @Override
                    public void onPlayerState(PlayerState playerState) {
                        if(playerState.playing){
                            mPlayer.pause();
                        }else{
                            mPlayer.resume();
                            refreshView = new Timer();
                            refreshView.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    mPlayer.getPlayerState(new PlayerStateCallback() {
                                        @Override
                                        public void onPlayerState(final PlayerState playerState) {
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    view.update(playerState.positionInMs,
                                                            playerState.durationInMs);
                                                }
                                            });
                                        }
                                    });
                                }
                            }, 1000, 1000);
                        }
                    }
                });
            }
        }).start();
    }
    public void play(final String URI){

        final QueueActivity view = this.view;

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    playerReady.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                playerReady.release();
                //String URI = intent.getStringExtra("uri");
                mPlayer.play(URI);
                refreshView = new Timer();
                refreshView.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mPlayer.getPlayerState(new PlayerStateCallback() {
                            @Override
                            public void onPlayerState(final PlayerState playerState) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        view.update(playerState.positionInMs, playerState.durationInMs);
                                    }
                                });
                            }
                        });
                    }
                }, 1000, 1000);
            }
        }).start();
    }

    @Override
    public void onLoggedIn() {

    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Throwable throwable) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    @Override
    public void onPlaybackEvent(EventType eventType, final PlayerState playerState) {
        if(eventType == EventType.PAUSE)
            refreshView.cancel();
        if(eventType == EventType.TRACK_END)
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    view.next(null);
                }
            });
        new Handler(Looper.getMainLooper()).post(new Runnable() {
             @Override
             public void run() {
                 view.update(playerState.positionInMs, playerState.durationInMs);
             }
         });
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {

    }
}
