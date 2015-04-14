package com.songus;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.songus.songus.R;
import com.songus.host.NewEventActivity;

public class JoinActivity extends Activity {

    private Songus songus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songus = ((Songus)getApplication());
        Typeface roboto = songus.roboto;
        Typeface roboto_bold = songus.roboto_bold;
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
        // Credit : http://stackoverflow.com/questions/8831050/android-how-to-read-qr-code-in-my-application
        try {

            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);

        } catch (Exception e) {

            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
            startActivity(marketIntent);

        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {

            if (resultCode == RESULT_OK) {
                String eventCode = data.getStringExtra("SCAN_RESULT");
                if(songus.isValidEventCode(eventCode)) {
                    Intent i = new Intent(this, com.songus.host.QueueActivity.class); //TODO attendee
                    i.putExtra("QR", eventCode);
                    startActivity(i);
                }else{
                    Toast.makeText(this, "Invalid Event Code", Toast.LENGTH_LONG).show();
                }
            }
            if(resultCode == RESULT_CANCELED){
                //handle cancel
            }
        }
    }
}



