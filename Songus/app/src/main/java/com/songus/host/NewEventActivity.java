package com.songus.host;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.songus.JoinActivity;
import com.songus.Songus;
import com.songus.model.Song;
import com.songus.model.SongQueue;
import com.songus.songus.R;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.ConnectionStateCallback;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.User;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class NewEventActivity extends ActionBarActivity implements ConnectionStateCallback {

    private Songus songus;
    private AuthenticationResponse.Type t;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        //Get Spotify
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(Songus.CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                Songus.REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "playlist-read",
                "playlist-read-private", "streaming"});
        AuthenticationRequest request = builder.build();
        songus = (Songus)getApplication();
        AuthenticationClient.openLoginActivity(this, Songus.REQUEST_CODE, request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public void emptyPlaylist(View v){
        final SongQueue empty = new SongQueue();
        empty.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                final SongQueue q = new SongQueue();
                final ParseObject hostKey = new ParseObject("HostKey");
                hostKey.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        q.put("key",hostKey.getObjectId());
                        q.put("name", getString(R.string.unnamed_event));
                        q.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                Intent i = new Intent(getApplicationContext(), QueueActivity.class);
                                i.putExtra("QR", q.getObjectId());
                                i.putExtra("KEY", hostKey.getObjectId());
                                startActivity(i);
                            }
                        });
                    }
                });
            }
        });
    }
    public void choosePlaylist(View v){

        final SpotifyService spotify = songus.getSpotifyService();

        final Context context = this;

            spotify.getMe(new Callback<User>() {
                @Override
                public void success(final User user, Response response) {
                    spotify.getPlaylists(user.id, new Callback<Pager<Playlist>>() {
                        @Override
                        public void success(final Pager<Playlist> playlistPager, Response response) {
                            if (playlistPager.total == 0) {
                                findViewById(R.id.new_event_from_existing).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "You have no Playlists on Spotify.", Toast.LENGTH_LONG).show();
                                    }
                                });
                                return;
                            }
                            final List<Playlist> playlistList = playlistPager.items;
                            List<CharSequence> playlistNames = Lists.transform(playlistList, new Function<Playlist, CharSequence>() {
                                @Override
                                public CharSequence apply(Playlist input) {
                                    return input.name;
                                }
                            });

                            final CharSequence items[] = new CharSequence[playlistNames.size()];
                            playlistNames.toArray(items);

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder adb = new AlertDialog.Builder(context);
                                    final AtomicInteger choice = new AtomicInteger(0);
                                    adb.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface d, int n) {
                                            choice.set(n);
                                        }

                                    });
                                    adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            spotify.getPlaylistTracks(user.id, playlistList.get(choice.get()).id,
                                                    new Callback<Pager<PlaylistTrack>>() {
                                                        @Override
                                                        public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                                                            final SongQueue q = new SongQueue();
                                                            final ParseObject hostKey = new ParseObject("HostKey");
                                                            for (PlaylistTrack p : playlistTrackPager.items) {
                                                                Song s = new Song(p.track);
                                                                s.vote();
                                                                s.put("voters", Arrays.asList(ParseUser.getCurrentUser().getObjectId()));
                                                                try {
                                                                    s.save();
                                                                    q.addSong(s);
                                                                } catch (ParseException e) {
                                                                }
                                                            }
                                                            hostKey.saveInBackground(new SaveCallback() {
                                                                @Override
                                                                public void done(ParseException e) {
                                                                    q.put("key", hostKey.getObjectId());
                                                                    q.put("name", getString(R.string.unnamed_event));
                                                                    q.saveInBackground(new SaveCallback() {
                                                                        @Override
                                                                        public void done(ParseException e) {
                                                                            Intent i = new Intent(getApplicationContext(), QueueActivity.class);
                                                                            i.putExtra("QR", q.getObjectId());
                                                                            i.putExtra("KEY", hostKey.getObjectId());
                                                                            startActivity(i);
                                                                        }
                                                                    });
                                                                }
                                                            });

                                                        }

                                                        @Override
                                                        public void failure(RetrofitError retrofitError) {
                                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    });
                                        }
                                    });
                                    adb.setTitle("Choose Spotify Playlist");
                                    adb.show();
                                }
                            });
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    Log.d("SongUs Debug", retrofitError.getResponse().getReason());
                }
            });

    }
    public void existingEvent(View v){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Host Access Code");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                if(!value.isEmpty()){
                    ParseQuery<SongQueue> query = ParseQuery.getQuery(SongQueue.class);
                    query.whereEqualTo("key",value);
                    try {
                        SongQueue q = query.getFirst();
                        Intent i = new Intent(getApplicationContext(), QueueActivity.class);
                        i.putExtra("QR", q.getObjectId());
                        i.putExtra("KEY", value);
                        i.putExtra("REJOINING", true);
                        startActivity(i);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == Songus.REQUEST_CODE) {
            Intent back;
            //Get Spotify
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            final Context currentContext = this;
            AlertDialog.Builder errorMessage;
            switch(response.getType()){
                // Response was successful and contains auth token
                case TOKEN:

                    // Handle successful response
                    songus.setResponse(response);
                    songus.setAuthCode(response.getAccessToken());
                    SpotifyApi api = new SpotifyApi();
                    api.setAccessToken(songus.getAuthCode());
                    songus.setSpotifyApi(api);
                    final SpotifyService spotify = api.getService();
                    songus.setSpotifyService(spotify);
                    final ProgressDialog checkingPremium = new ProgressDialog(this);
                    checkingPremium.setTitle("Checking premium");
                    checkingPremium.setMessage("Please wait while we check if your account is premium.");
                    checkingPremium.show();
                    spotify.getMe(new Callback<User>() {
                        @Override
                        public void success(User user, Response response) {
                            checkingPremium.dismiss();
                            if(!user.product.equals("premium")){
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog.Builder errorMessage
                                                = new AlertDialog.Builder(currentContext);
                                        errorMessage.setTitle("Account not premium.");
                                        errorMessage
                                                .setMessage("Cannot host or stream if account not premium.")
                                                .setCancelable(false)
                                                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        AuthenticationClient.logout(currentContext);
                                                        Intent back = new Intent(currentContext, JoinActivity.class);
                                                        back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        startActivity(back);
                                                        finish();
                                                    }
                                                })
                                                .create().show();


                                    }
                                });
                            }
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            checkingPremium.dismiss();
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder errorMessage
                                            = new AlertDialog.Builder(currentContext);
                                    errorMessage.setTitle("Unable to check");
                                    errorMessage
                                            .setMessage("Unable to check if account is premium.")
                                            .setCancelable(false)
                                            .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    AuthenticationClient.logout(currentContext);
                                                    Intent back = new Intent(currentContext, JoinActivity.class);
                                                    back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    startActivity(back);
                                                    finish();
                                                }
                                            })
                                            .create().show();


                                }
                            });
                        }
                    });

                    break;

                case ERROR:
                    errorMessage = new AlertDialog.Builder(this);
                    errorMessage.setTitle("Error trying to sign in");
                    errorMessage
                            .setMessage("Error connecting to Spotify. Did you authorize the app?")
                            .setCancelable(false)
                            .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AuthenticationClient.logout(currentContext);
                                    Intent back = new Intent(currentContext, JoinActivity.class);
                                    back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(back);
                                    finish();
                                }
                            })
                            .create().show();
                    break;

                case UNKNOWN:
                    errorMessage = new AlertDialog.Builder(this);
                    errorMessage.setTitle("Unable to connect to Spotify");
                    errorMessage
                            .setMessage("Unable to connect to Spotify for unknown reasons.")
                            .setCancelable(false)
                            .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AuthenticationClient.logout(currentContext);
                                    Intent back = new Intent(currentContext, JoinActivity.class);
                                    back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(back);
                                    finish();
                                }
                            })
                            .create().show();
                    break;

                case EMPTY:
                    back = new Intent(currentContext, JoinActivity.class);
                    back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(back);
                    finish();
                    break;

                default:
                    Log.d("SpotifyAuthTest", "Lolz can't be here");
                    // TODO tell the user that they need spotify premium to host an event.
            }

        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("SongUs Debug", "Logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("SongUs Debug", "Logged out");
    }

    @Override
    public void onLoginFailed(Throwable throwable) {
        String s= throwable.getMessage();
        s +="";
    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {
        Log.d("SongUs Debug", s);
    }




}

