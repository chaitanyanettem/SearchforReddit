package chaitanya.im.searchforreddit;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import chaitanya.im.searchforreddit.DataModel.Child;
import chaitanya.im.searchforreddit.DataModel.Data_;
import chaitanya.im.searchforreddit.DataModel.RecyclerViewItem;
import chaitanya.im.searchforreddit.DataModel.Result;
import chaitanya.im.searchforreddit.Network.UrlSearch;

public class LauncherActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_EMAIL = 1;
    private EditText searchEditText;
    final String baseURL = "https://www.reddit.com";
    UrlSearch urlSearch;
    static List<RecyclerViewItem> resultList = new ArrayList<>();
    static RecyclerView rvResults;
    static ResultsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        searchEditText = (EditText) findViewById(R.id.search_edit_text);
        setSupportActionBar(toolbar);

        // To allow for multiline EditText with imeOptions set to actionSearch
//        searchEditText.setHorizontallyScrolling(false);
//        searchEditText.setMaxLines(Integer.MAX_VALUE);
        searchEditText.setOnKeyListener(onKeyListener);

        rvResults = (RecyclerView) findViewById(R.id.result_view_launcher);
        adapter = new ResultsAdapter(resultList, this);
        rvResults.setAdapter(adapter);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.addItemDecoration(new SimpleDividerItemDecoration(this));
        //rvResults.setRecyclerListener(recyclerListener);
        urlSearch = new UrlSearch(baseURL, this);

        // dp -> px : http://stackoverflow.com/a/9563438/1055475
        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = 2 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(px);
        }
        else {
            findViewById(R.id.shadow).setVisibility(View.VISIBLE);
        }

    }

    RecyclerView.RecyclerListener recyclerListener = new RecyclerView.RecyclerListener() {
        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            Log.d("LauncherActivity.java", Long.toString(holder.getItemId()));
        }
    };

    View.OnKeyListener onKeyListener = new View.OnKeyListener() {
        public boolean onKey(View v, int keycode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keycode == KeyEvent.KEYCODE_ENTER)) {
                initializeSearch();
                return true;
            }
            return false;
        }
    };

    public void initializeSearch(View view) {
        initializeSearch();
    }

    public void initializeSearch() {
        UtilMethods.hideKeyboard(this);
        String query = searchEditText.getText().toString();
        Toast.makeText(LauncherActivity.this, query, Toast.LENGTH_SHORT).show();
        if(!query.equals("")) {

            if(resultList.size()>0) {
                resultList.clear();
                adapter.notifyDataSetChanged();
            }

            String[] links = UtilMethods.extractLinks(query);
            if (links.length > 0) {
                Log.d("ShareActivity.java", "receiveIntent() - link = " + links[0]);
                urlSearch.executeSearch("url:" + links[0], 1);
            }
            else{
                urlSearch.executeSearch(query, 1);
            }
        }
        else {
            // Do something
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EMAIL && resultCode == RESULT_OK) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
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

        adapter.notifyDataSetChanged();
    }
}
