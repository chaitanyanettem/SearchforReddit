package chaitanya.im.searchforreddit;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Patterns;
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

    TextView sharedText;
    static TextView label;
    static TextView query;
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

        //FirebaseCrash.report(new Exception("My first Android non-fatal error"));

        rvResults = (RecyclerView) findViewById(R.id.result_view);
        adapter = new ResultsAdapter(resultList, this);
        rvResults.setAdapter(adapter);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.addItemDecoration(new SimpleDividerItemDecoration(this));

        sharedText = (TextView) findViewById(R.id.shared_content);
        label = (TextView) findViewById(R.id.label);
        query = (TextView) findViewById(R.id.query);
        ruler = findViewById(R.id.ruler);
        urlSearch = new UrlSearch(baseURL, this);

        Log.d("MainActivity.java", "onCreate");
        assert(sharedText != null);
        assert(label != null);
        assert(ruler != null);
        assert(query != null);
        sharedText.setText("Share text/links from other apps");

        Intent intent = getIntent();
        receiveIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ruler.setVisibility(View.INVISIBLE);
        Log.d("MainActivity.java", "onResume");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("MainActivity.java", "onNewIntent");
        receiveIntent(intent);
    }

    void receiveIntent(Intent intent) {
        if(resultList.size()>0) {
            resultList.clear();
            adapter.notifyDataSetChanged();
        }
        String action = intent.getAction();
        intent.getFlags();
        Log.d("MainActivity.java", "receiveIntent() toString - " + intent.toString());
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type!=null) {
            if ("text/plain".equals(type)) {
                label.setVisibility(View.VISIBLE);
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                Log.d("MainActivity.java", "Shared Text:" + sharedText);
                if(!sharedText.equals("")) {
                    String[] links = extractLinks(sharedText);
                    this.sharedText.setText("Shared Text - " + sharedText);
                    if (links.length > 0) {
                        Log.d("MainActivity.java", "receiveIntent() - link = " + links[0]);
                        query.setVisibility(View.VISIBLE);
                        query.setText(links[0]);
                        urlSearch.executeSearch("url:" + links[0]);
                    }
                    else{
                        query.setVisibility(View.GONE);
                        urlSearch.executeSearch(sharedText);
                    }
                }
                else {
                        this.sharedText.setText("Empty search");
                }
            }

        }
    }


    public static String[] extractLinks(String text) {
        List<String> links = new ArrayList<>();
        Matcher m = Patterns.WEB_URL.matcher(text);
        while (m.find()) {
            String url = m.group();
            Log.d("MainActivity.java", "URL extracted: " + url);
            links.add(url);
        }

        return links.toArray(new String[links.size()]);
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
            temp.setTimeString(getTimeString(d.getCreatedUtc()));
            resultList.add(temp);
        }

        if (result.getData().getChildren().size()!=0) {
            label.setText("Number of results:" + result.getData().getChildren().size());
            ruler.setVisibility(View.VISIBLE);
        }
        else
            label.setText("0 results found");
        adapter.notifyDataSetChanged();
    }

    public static String getTimeString(long utcTime) {
        // Inspired from https://gist.github.com/dmsherazi/5985a093076a8c4e7c38
        long difference;
        long unixTime = System.currentTimeMillis() / 1000L;  //get current time in seconds.
        int j;
        String[] periods = {"s", "m", "h", "day", "month", "year"};
        double[] lengths = {60, 60, 24, 30, 12};
        difference = unixTime - utcTime;
        String tense = " ago";
        for (j = 0; difference >= lengths[j] && j < lengths.length - 1; j++) {
            difference /= lengths[j];
        }

        if (difference > 1)
            if (j>2)
                return difference + periods[j] + "s" + tense;

        return difference + periods[j] + tense;
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