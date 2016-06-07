package chaitanya.im.searchforreddit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity.java", "onCreate");

        ((TextView) findViewById(R.id.temp_textview)).setText("Share text/links from other apps");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String action = intent.getAction();
        intent.getFlags();
        Log.d("onResume - intent: ",intent.toString());
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type!=null) {
            Log.d("MainActivity.java", "Intent verified");
            TextView displayText = (TextView) findViewById(R.id.temp_textview);
            if ("text/plain".equals(type)) {
                handleSendText(intent, displayText);
            }
        }
    }

    void handleSendText(Intent intent, TextView displayText) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        Log.d("MainActivity.java", sharedText);
        if (sharedText != null) {
            displayText.setText(sharedText);
        }
    }
}

