package chaitanya.im.searchforreddit;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import chaitanya.im.searchforreddit.DataModel.Child;
import chaitanya.im.searchforreddit.DataModel.Data_;
import chaitanya.im.searchforreddit.DataModel.RecyclerViewItem;
import chaitanya.im.searchforreddit.DataModel.Result;
import chaitanya.im.searchforreddit.Network.UrlSearch;

public class MainActivity extends AppCompatActivity {

    TextView displayText;
    static TextView label;
    final String baseURL = "https://www.reddit.com";
    UrlSearch urlSearch;
    static List<RecyclerViewItem> resultList = new ArrayList<>();
    static RecyclerView rvResults;
    static ResultsAdapter adapter;
    static View ruler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        rvResults = (RecyclerView) findViewById(R.id.result_view);
        adapter = new ResultsAdapter(resultList, this);
        rvResults.setAdapter(adapter);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.addItemDecoration(new SimpleDividerItemDecoration(this));

        displayText = (TextView) findViewById(R.id.shared_content);
        label = (TextView) findViewById(R.id.label);
        ruler = findViewById(R.id.ruler);
        urlSearch = new UrlSearch(baseURL, this);

        Log.d("MainActivity.java", "onCreate");
        assert(displayText != null);
        assert(label != null);
        assert(ruler != null);
        displayText.setText("Share text/links from other apps");

        Intent intent = getIntent();
        receiveIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ruler.setVisibility(View.GONE);
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

    public static void updateDialog(Result result) {

        RecyclerViewItem temp;
        resultList.clear();
        Data_ d;
        for (Child c:
                result.getData().getChildren()) {

            d = c.getData();
            temp = new RecyclerViewItem();
            temp.setTitle(d.getTitle());
            temp.setSubreddit("r/" + d.getSubreddit());
            temp.setAuthor("u/" + d.getAuthor());
            temp.setNumComments(d.getNumComments());
            temp.setScore(d.getScore());
            temp.setPermalink("http://m.reddit.com" + d.getPermalink());
            resultList.add(temp);
        }
        ruler.setVisibility(View.VISIBLE);

        if (result.getData().getChildren()!=null)
            label.setText("Number of results:" + result.getData().getChildren().size());
        else
            label.setText("No Results found");
        adapter.notifyDataSetChanged();
    }

    public static void resultClicked(int position, AppCompatActivity context) {

        String url = resultList.get(position).getPermalink();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(url));
        Log.d("resultClicked()", url);

        if (browserIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(browserIntent);
        }

    }

}