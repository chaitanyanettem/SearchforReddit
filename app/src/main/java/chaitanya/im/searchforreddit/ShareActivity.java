package chaitanya.im.searchforreddit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

    final String BASE_URL = "https://www.reddit.com";
    final String RULER_FLAG = "rulerFlag";
    final int SOURCE = 0;
    public final static String EXTRA_SHARED_TEXT = "com.mycompany.myfirstapp.SHARED_TEXT";

    private static boolean rulerFlag = false;
    private SharedPreferences sharedPref;
    private int theme;

    TextView sharedTextView;
    static TextView label;
    static RecyclerView rvResults;
    static View ruler;
    static ProgressBar markerProgress;
    ImageButton openInLauncherButton;

    UrlSearch urlSearch;
    String sharedText;
    static List<RecyclerViewItem> resultList = new ArrayList<>();
    static ResultsAdapter adapter;
    Map<String, String> finalQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = getSharedPreferences(getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        theme = sharedPref.getInt(getString(R.string.style_pref_key), SOURCE);
        UtilMethods.onActivityCreateSetTheme(this, theme, 0);
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_share);

        //FirebaseCrash.report(new Exception("My first Android non-fatal error"));

        urlSearch = new UrlSearch(BASE_URL, this, 0);
        rvResults = (RecyclerView) findViewById(R.id.result_view);
        adapter = new ResultsAdapter(resultList, this);
        rvResults.setAdapter(adapter);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.addItemDecoration(new SimpleDividerItemDecoration(this, theme));
        finalQuery = new HashMap<>();

        sharedTextView = (TextView) findViewById(R.id.shared_content);
        label = (TextView) findViewById(R.id.label);
        markerProgress = (ProgressBar) findViewById(R.id.marker_progress);
        ruler = findViewById(R.id.ruler);
        openInLauncherButton = (ImageButton) findViewById(R.id.open_in_main_button);

        openInLauncherButton.setOnLongClickListener(longClickListener);

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
            sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
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
            label.setText("Number of results: " + resultList.size());
            ruler.setVisibility(View.VISIBLE);
            rulerFlag = true;
        }
        else
            label.setText("0 results found");

        markerProgress.setVisibility(View.GONE);
        label.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
    }

    public void openLauncherActivity(View view) {
        Intent intent = new Intent(this, LauncherActivity.class);
        intent.putExtra(EXTRA_SHARED_TEXT, sharedText);
        startActivity(intent);
    }


    ImageButton.OnLongClickListener longClickListener = new ImageButton.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Toast buttonDesc = Toast.makeText(ShareActivity.this, v.getContentDescription(), Toast.LENGTH_SHORT);
            buttonDesc.setGravity(Gravity.CENTER_VERTICAL,0,0);
            buttonDesc.show();
            return false;
        }
    };
}