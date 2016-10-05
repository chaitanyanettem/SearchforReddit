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
import chaitanya.im.searchforreddit.DataModel.RecyclerViewItem;
import chaitanya.im.searchforreddit.DataModel.Result;
import chaitanya.im.searchforreddit.Network.UrlSearch;

public class ShareActivity extends AppCompatActivity {

    private final static String TAG = "ShareActivity.java";
    @SuppressWarnings("FieldCanBeLocal")
    private final String BASE_URL = "https://www.reddit.com";
    @SuppressWarnings("FieldCanBeLocal")
    private final int SOURCE = 0;
    public final static String EXTRA_SHARED_TEXT = "chaitanya.im.searchforreddit.SHARED_TEXT";

    private TextView sharedTextView;
    @SuppressWarnings("FieldCanBeLocal")
    private RecyclerView rvResults;

    private UrlSearch urlSearch;
    private String sharedText;
    private static final List<RecyclerViewItem> resultList = new ArrayList<>();
    private ResultsAdapter adapter;
    private Map<String, String> finalQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        int theme = sharedPref.getInt(getString(R.string.style_pref_key), SOURCE);
        UtilMethods.onActivityCreateSetTheme(this, theme, 0);
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_share);

        //FirebaseCrash.report(new Exception("My first Android non-fatal error"));

        rvResults = (RecyclerView) findViewById(R.id.result_view);
        adapter = new ResultsAdapter(resultList, this);
        rvResults.setAdapter(adapter);
        urlSearch = new UrlSearch(BASE_URL, this, 0, adapter);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.addItemDecoration(new SimpleDividerItemDecoration(this, theme));
        finalQuery = new HashMap<>();

        sharedTextView = (TextView) findViewById(R.id.shared_content);
        ProgressBar markerProgress = (ProgressBar) findViewById(R.id.marker_progress);
        View ruler = findViewById(R.id.ruler);
        Button openInLauncherButton = (Button) findViewById(R.id.open_in_main_button);

        openInLauncherButton.setOnLongClickListener(longClickListener);
        markerProgress.setVisibility(View.VISIBLE);
        ruler.setVisibility(View.VISIBLE);
        clearResultList();

        Log.d(TAG, "onCreate");
        if (BuildConfig.DEBUG && sharedTextView == null)
            throw new RuntimeException();
        if (BuildConfig.DEBUG && ruler == null)
            throw new RuntimeException();

        //assert(query != null);

        Intent intent = getIntent();
        receiveIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    private void clearResultList() {
        if (resultList.size() != 0) {
            resultList.clear();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");
        receiveIntent(intent);
    }

    private void receiveIntent(Intent intent) {
        clearResultList();
        String action = intent.getAction();
        intent.getFlags();
        Log.d(TAG, "receiveIntent() toString - " + intent.toString());
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
            sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            Log.d(TAG, "Shared Text:" + sharedText);
            String sharedTextLabel = getResources().getString(R.string.shared_text) + sharedText;
            if (!sharedText.equals("")) {
                sharedTextView.setText(sharedTextLabel);
                if (UtilMethods.isNetworkAvailable(this)) {
                    String[] links = UtilMethods.extractLinks(sharedText);
                    if (links.length > 0) {
                        Log.d(TAG, "receiveIntent() - link = " + links[0]);
                        String youtubeID = UtilMethods.extractYoutubeID(links[0]);
                        if (youtubeID != null)
                            updateFinalQuery("url:" + youtubeID);
                        else
                            updateFinalQuery("url:" + links[0]);

                    } else {
                        updateFinalQuery(sharedText);
                    }
                    urlSearch.executeSearch(finalQuery, 0);
                }
                else {
                    sharedTextView.setText(getResources().getString(R.string.internet_unreachable));
                }
            }
            else {
                this.sharedTextView.setText(getResources().getString(R.string.empty_search));
            }
        }

    }

    private void updateFinalQuery(String q){
        finalQuery.clear();
        finalQuery.put("t","");
        finalQuery.put("sort", "");
        finalQuery.put("q", q);
    }

    public static void updateDialog(AppCompatActivity _activity, Result result, String message, ResultsAdapter _adapter) {

        TextView label = (TextView) _activity.findViewById(R.id.label);
        ProgressBar markerProgress = (ProgressBar) _activity.findViewById(R.id.marker_progress);
        View ruler = _activity.findViewById(R.id.ruler);

        if (_adapter == null)
            Log.d(TAG, "UpdateDialog() - adapter is null");
        if (result != null) {
            if (_adapter != null) {
                RecyclerViewItem temp;
                resultList.clear();
                _adapter.notifyDataSetChanged();

                for (Child c :
                        result.getData().getChildren()) {
                    temp = UtilMethods.buildRecyclerViewItem(c);
                    resultList.add(temp);
                }

                String numberOfResults = _activity.getResources().getString(R.string.number_of_results_string) + resultList.size();
                label.setText(numberOfResults);

                if (resultList.size() != 0) {
                    ruler.setVisibility(View.VISIBLE);
                    _adapter.notifyDataSetChanged();
                }
            }
        }
        else {
            label.setText(message);
        }

        markerProgress.setVisibility(View.GONE);
        label.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("UnusedParameters")
    public void openLauncherActivity(View view) {
        Intent intent = new Intent(this, LauncherActivity.class);
        intent.putExtra(EXTRA_SHARED_TEXT, sharedText);
        startActivity(intent);
    }


    private final ImageButton.OnLongClickListener longClickListener = new ImageButton.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Toast buttonDesc = Toast.makeText(ShareActivity.this, v.getContentDescription(), Toast.LENGTH_SHORT);
            buttonDesc.setGravity(Gravity.CENTER_VERTICAL,0,0);
            buttonDesc.show();
            return false;
        }
    };
}