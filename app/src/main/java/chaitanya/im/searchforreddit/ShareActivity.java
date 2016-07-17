package chaitanya.im.searchforreddit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import chaitanya.im.searchforreddit.DataModel.Child;
import chaitanya.im.searchforreddit.DataModel.Data_;
import chaitanya.im.searchforreddit.DataModel.RecyclerViewItem;
import chaitanya.im.searchforreddit.DataModel.Result;
import chaitanya.im.searchforreddit.Network.UrlSearch;

public class ShareActivity extends AppCompatActivity {

    TextView sharedText;
    static TextView label;
    //static TextView query;
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
        setContentView(R.layout.activity_share);

        //FirebaseCrash.report(new Exception("My first Android non-fatal error"));

        rvResults = (RecyclerView) findViewById(R.id.result_view);
        adapter = new ResultsAdapter(resultList, this);
        rvResults.setAdapter(adapter);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.addItemDecoration(new SimpleDividerItemDecoration(this));

        sharedText = (TextView) findViewById(R.id.shared_content);
        label = (TextView) findViewById(R.id.label);
        //query = (TextView) findViewById(R.id.query);
        ruler = findViewById(R.id.ruler);
        urlSearch = new UrlSearch(baseURL, this);

        Log.d("ShareActivity.java", "onCreate");
        assert(sharedText != null);
        assert(label != null);
        assert(ruler != null);
        //assert(query != null);
        sharedText.setText("Share text/links from other apps");

        Intent intent = getIntent();
        receiveIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ruler.setVisibility(View.INVISIBLE);
        Log.d("ShareActivity.java", "onResume");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("ShareActivity.java", "onNewIntent");
        receiveIntent(intent);
    }

    void receiveIntent(Intent intent) {
        if(resultList.size()>0) {
            resultList.clear();
            adapter.notifyDataSetChanged();
        }
        String action = intent.getAction();
        intent.getFlags();
        Log.d("ShareActivity.java", "receiveIntent() toString - " + intent.toString());
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type!=null) {
            if ("text/plain".equals(type)) {
                label.setVisibility(View.VISIBLE);
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                Log.d("ShareActivity.java", "Shared Text:" + sharedText);
                if(!sharedText.equals("")) {
                    String[] links = UtilMethods.extractLinks(sharedText);
                    this.sharedText.setText("Shared Text - " + sharedText);
                    if (links.length > 0) {
                        Log.d("ShareActivity.java", "receiveIntent() - link = " + links[0]);
                        urlSearch.executeSearch("url:" + links[0], 0);
                    }
                    else{
                        urlSearch.executeSearch(sharedText, 0);
                    }
                }
                else {
                    this.sharedText.setText("Empty search");
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
            temp.setTimeString(UtilMethods.getTimeString(d.getCreatedUtc()));
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
}