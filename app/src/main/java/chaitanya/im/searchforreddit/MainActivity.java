package chaitanya.im.searchforreddit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import chaitanya.im.searchforreddit.DataModel.Result;
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
        Log.d("MainActivity.java", "receiveIntent() toString - " + intent.toString());
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type!=null) {
            Log.d("MainActivity.java", "receiveIntent() - " + "Intent verified");
            if ("text/plain".equals(type)) {
                label.setVisibility(View.VISIBLE);
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                String query = "url:" + sharedText + "&sort=top" + "&t=all";
                urlSearch.getResults(query);

                Log.d("MainActivity.java", "Shared Text:" + sharedText);
                if (sharedText != null) {
                    displayText.setText(sharedText);
                }

            }
        }
    }

}

