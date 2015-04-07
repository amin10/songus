package com.songus.songus;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Typeface roboto = ((Songus)getApplication()).roboto;
        Typeface roboto_bold = ((Songus)getApplication()).roboto_bold;
        setContentView(R.layout.login);
        TextView title = (TextView) findViewById(R.id.songus);
        title.setTypeface(roboto);
        ((TextView) findViewById(R.id.login_attendee)).setTypeface(roboto_bold);
        ((TextView) findViewById(R.id.login_host)).setTypeface(roboto);
    }

    public void loginHost(View v){
        Intent i = new Intent(this, NewEventActivity.class);
        startActivity(i);
    }

    public void loginAttendee(View v){

    }
}



