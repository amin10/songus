package com.songus.songus;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.songus.songus.attendee.QueueActivity;
import com.songus.songus.host.NewEventActivity;

public class JoinActivity extends Activity {

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
        // Credit : http://stackoverflow.com/questions/8831050/android-how-to-read-qr-code-in-my-application
        try {

            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes

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
                String contents = data.getStringExtra("SCAN_RESULT");
                Intent i = new Intent(this, QueueActivity.class);
                i.putExtra("QR", contents);
                startActivity(i);
            }
            if(resultCode == RESULT_CANCELED){
                //handle cancel
            }
        }
    }
}



