package chaitanya.im.searchforreddit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chaitanya.im.searchforreddit.DataModel.Child;
import chaitanya.im.searchforreddit.DataModel.Data_;
import chaitanya.im.searchforreddit.DataModel.RecyclerViewItem;
import chaitanya.im.searchforreddit.DataModel.Result;
import chaitanya.im.searchforreddit.Network.UrlSearch;

public class ShareActivity extends AppCompatActivity {

    final String baseURL = "https://www.reddit.com";

    private SharedPreferences sharedPref;
    private int theme;

    TextView sharedTextView;
    static TextView label;
    static RecyclerView rvResults;
    static View ruler;

    UrlSearch urlSearch;
    static List<RecyclerViewItem> resultList = new ArrayList<>();
    static ResultsAdapter adapter;
    Map<String, String> finalQuery;

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
        rvResults.addItemDecoration(new SimpleDividerItemDecoration(this, theme));
        finalQuery = new HashMap<>();

        sharedTextView = (TextView) findViewById(R.id.shared_content);
        label = (TextView) findViewById(R.id.label);
        //query = (TextView) findViewById(R.id.query);
        ruler = findViewById(R.id.ruler);
        urlSearch = new UrlSearch(baseURL, this, 0);

        Log.d("ShareActivity.java", "onCreate");
        assert(sharedTextView != null);
        assert(label != null);
        assert(ruler != null);
        //assert(query != null);

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

        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            Log.d("ShareActivity.java", "Shared Text:" + sharedText);
            sharedTextView.setText("Shared Text - " + sharedText);
            if (!sharedText.equals("")) {
                sharedTextView.setText("Shared Text - " + sharedText);
                if (UtilMethods.isNetworkAvailable(this)) {
                    String[] links = UtilMethods.extractLinks(sharedText);
                    if (links.length > 0) {
                        Log.d("ShareActivity.java", "receiveIntent() - link = " + links[0]);
                        updateFinalQuery("url:" + links[0]);

                    } else {
                        updateFinalQuery(sharedText);
                    }
                    urlSearch.executeSearch(finalQuery, 0);
                }
                else {
                    sharedTextView.setText("Oh noes! The internet can't be reached :(");
                }
            }
            else {
                this.sharedTextView.setText("Empty search");
            }
        }

    }

    void updateFinalQuery(String q){
        finalQuery.clear();
        finalQuery.put("t","");
        finalQuery.put("sort", "");
        finalQuery.put("q", q);
    }

    public static void updateDialog(Result result) {

        RecyclerViewItem temp;
        resultList.clear();
        Data_ d;
        for (Child c:
                result.getData().getChildren()) {
            temp = UtilMethods.buildRecyclerViewItemt(c);
            resultList.add(temp);
        }

        if (resultList.size()!=0) {
            label.setText("Number of results:" + resultList.size());
            ruler.setVisibility(View.VISIBLE);
        }
        else
            label.setText("0 results found");
        label.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
    }
}