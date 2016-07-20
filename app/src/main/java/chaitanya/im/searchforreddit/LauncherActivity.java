package chaitanya.im.searchforreddit;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import chaitanya.im.searchforreddit.DataModel.Child;
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
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        LinearLayout searchBox = (LinearLayout) findViewById(R.id.search_box);
        searchEditText = (EditText) findViewById(R.id.search_edit_text);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.launcher_coordinatorlayout);
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
        urlSearch = new UrlSearch(baseURL, this, 1);

        // dp -> px : http://stackoverflow.com/a/9563438/1055475
        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = (float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(px*4);
            searchBox.setElevation(px*3);
        }
        else {
            findViewById(R.id.shadow).setVisibility(View.VISIBLE);
            findViewById(R.id.shadow2).setVisibility(View.VISIBLE);
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
        Log.d("LauncherActivity.java", "query - " + query);
        //Toast.makeText(LauncherActivity.this, query, Toast.LENGTH_SHORT).show();
        if (UtilMethods.isNetworkAvailable(this)) {
            if (!query.equals("")) {
                if (resultList.size() > 0) {
                    resultList.clear();
                    adapter.notifyDataSetChanged();
                }

                String[] links = UtilMethods.extractLinks(query);
                if (links.length > 0) {
                    Log.d("ShareActivity.java", "receiveIntent() - link = " + links[0]);
                    urlSearch.executeSearch("url:" + links[0], 1);
                } else {
                    urlSearch.executeSearch(query, 1);
                }
            }
            else {
                Snackbar.make(coordinatorLayout, "Please enter something in the search box :)",
                        Snackbar.LENGTH_INDEFINITE).show();
            }
        }
        else {
            Snackbar.make(coordinatorLayout, "Oh noes! The internet cannot be reached :(",
                    Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EMAIL && resultCode == RESULT_OK) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("LauncherActivity.java", "onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public static void updateDialog(Result result) {
        RecyclerViewItem temp;
        resultList.clear();
        for (Child c:
                result.getData().getChildren()) {
            temp = UtilMethods.buildRecyclerViewItemt(c);
            resultList.add(temp);
        }
        adapter.notifyDataSetChanged();
    }
}
