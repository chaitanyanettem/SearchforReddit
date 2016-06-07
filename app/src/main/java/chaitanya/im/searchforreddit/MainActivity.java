package chaitanya.im.searchforreddit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView displayText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayText = ((TextView) findViewById(R.id.temp_textview));
        Log.d("MainActivity.java", "onCreate");
        assert(displayText !=null);

        displayText.setText("Share text/links from other apps");
        Intent intent = getIntent();
        receiveIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity.java", "onResume");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("MainActivity.java", "onNewIntent");
        receiveIntent(intent);
    }

    void receiveIntent(Intent intent) {
        String action = intent.getAction();
        intent.getFlags();
        Log.d("MainActivity.java", "receiveIntent() toString - " + intent.toString());
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type!=null) {
            Log.d("MainActivity.java", "receiveIntent() - " + "Intent verified");
            if ("text/plain".equals(type)) {
                handleSendText(intent, displayText);
            }
        }
    }

    void handleSendText(Intent intent, TextView displayText) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        Log.d("MainActivity.java", "Shared Text:" + sharedText);
        if (sharedText != null) {
            displayText.setText(sharedText);
        }
    }
}

