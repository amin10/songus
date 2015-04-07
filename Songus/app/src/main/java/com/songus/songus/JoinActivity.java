package com.songus.songus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class JoinActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
    }

    public void loginHost(View v){
        Intent i = new Intent(this, NewEventActivity.class);
        startActivity(i);
    }

    public void loginAttendee(View v){

    }
}



