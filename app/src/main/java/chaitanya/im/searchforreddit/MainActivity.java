package chaitanya.im.searchforreddit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import java.util.regex.Matcher;

import chaitanya.im.searchforreddit.Network.UrlSearch;

public class MainActivity extends AppCompatActivity {

    TextView displayText;
    TextView label;
    final String baseURL = "https://www.reddit.com";
    UrlSearch urlSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Nautilus");
        displayText = (TextView) findViewById(R.id.shared_content);
        label = (TextView) findViewById(R.id.label);
        urlSearch = new UrlSearch(baseURL, this);

        Log.d("MainActivity.java", "onCreate");
        assert(displayText != null);
        assert(label != null);
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
        int flag = 0;
        Log.d("MainActivity.java", "receiveIntent() toString - " + intent.toString());
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type!=null) {
            Log.d("MainActivity.java", "receiveIntent() - " + "Intent verified");
            if ("text/plain".equals(type)) {
                label.setVisibility(View.VISIBLE);
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                Matcher m = Patterns.WEB_URL.matcher(sharedText);
                while(m.find()) {
                    Log.d("receiveIntent - url", m.group());
                }
                String query = sharedText;
                if (Patterns.WEB_URL.matcher(sharedText).matches()) {
                    query = "url:" + query;
                    flag = 1;
                }
                Log.d("MainActivity.java", "Shared Text:" + sharedText);
                if (!sharedText.equals("")) {
                    urlSearch.executeSearch(query);
                    if (flag==1)
                        displayText.setText(sharedText);
                    else
                        displayText.setText("Not a URL:" + sharedText);
                }
                else {
                    displayText.setText("Empty search");
                }

            }
        }
    }

}

