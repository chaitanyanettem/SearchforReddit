package chaitanya.im.searchforreddit;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class LauncherActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private static final String LICENSE_FLAG = "licenseFlag";
    private static final String SORT_BUTTON_LABEL = "sortButtonLabel";
    private static final String TIME_BUTTON_LABEL = "timeButtonLabel";
    private static final String TIME_VALUE = "timeValue";
    private static final String SORT_VALUE = "sortValue";
    private static final String SEARCH_OPTIONS_FLAG = "searchOptionsFlag";
    public static final String [] timeValues = {"day", "week", "month", "year", ""};
    public static final String [] sortValues = {"top", "new", "comments", ""};

    private static final int REQUEST_CODE_EMAIL = 1;
    private final String baseURL = "https://www.reddit.com";
    private boolean isChecked = false;

    private String timeValue = timeValues[0];
    private String sortValue = sortValues[0];
    private int theme;
    private int licenseFlag = 0;
    private boolean searchOptionsFlag = false;
    Map<String,String> finalQuery;
    private String sortButtonLabel ="Relevance";
    private String timeButtonLabel ="All Time";

    private SharedPreferences sharedPref;
    private LinearLayout searchOptions;
    private EditText searchEditText;
    //private Button filterButton;
    private Button sortButton;
    private Button timeButton;
    private static SwipeRefreshLayout launcherRefresh;
    private AlertDialog alertDialog;
    static RecyclerView rvResults;
    private Snackbar snackbar;
    private CoordinatorLayout coordinatorLayout;

    static List<RecyclerViewItem> resultList = new ArrayList<>();
    static ResultsAdapter adapter;
    private UrlSearch urlSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get and set theme from shared preferences
        sharedPref = getSharedPreferences(getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        theme = sharedPref.getInt(getString(R.string.style_pref_key), 0);
        UtilMethods.onActivityCreateSetTheme(this, theme);

        setContentView(R.layout.activity_launcher);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        LinearLayout searchBox = (LinearLayout) findViewById(R.id.search_box);
        searchOptions = (LinearLayout) findViewById(R.id.search_options);
        launcherRefresh = (SwipeRefreshLayout) findViewById(R.id.launcher_refresh);
        launcherRefresh.setOnRefreshListener(this);
        searchEditText = (EditText) findViewById(R.id.search_edit_text);
        //filterButton = (Button) findViewById(R.id.filter_button);
        sortButton = (Button) findViewById(R.id.sort_button);
        timeButton = (Button) findViewById(R.id.time_button);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.launcher_coordinatorlayout);
        finalQuery = new HashMap<>();
        setSupportActionBar(toolbar);


        // To allow for multiline EditText with imeOptions set to actionSearch
//        searchEditText.setHorizontallyScrolling(false);
//        searchEditText.setMaxLines(Integer.MAX_VALUE);
        searchEditText.setOnClickListener(searchEditTextClickListener);
        searchEditText.setOnFocusChangeListener(searchFocusChangeListener);
        searchEditText.setOnKeyListener(onKeyListener);
        searchEditText.setOnTouchListener(searchEditTextTouchListener);

        sortButton.setOnLongClickListener(buttonLongClick);
        timeButton.setOnLongClickListener(buttonLongClick);
        //filterButton.setOnLongClickListener(buttonLongClick);

        rvResults = (RecyclerView) findViewById(R.id.result_view_launcher);
        adapter = new ResultsAdapter(resultList, this);
        rvResults.setAdapter(adapter);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.addItemDecoration(new SimpleDividerItemDecoration(this, theme));

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
        Log.d("LauncherActivity.java", "OnCreate");
        Log.d("LauncherActivity.java", "searchEditText - " + searchEditText.getText().toString());
        // TODO - fix this:
        if(!searchEditText.getText().toString().equals("")) {
            Log.d("LauncherActivity.java", "OnCreate, visibility");
            searchOptions.setVisibility(View.VISIBLE);
            searchOptionsFlag = true;
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

    public void onRefresh() {
        Log.d("LauncherActivity.java", "onRefresh called");
        initializeSearch();
    }

    public void initializeSearch(View view) {
        initializeSearch();
    }

    public void initializeSearch() {
        UtilMethods.hideKeyboard(this);
        if (snackbar != null) {
            snackbar.dismiss();
        }
        if (urlSearch.snackbar != null) {
            urlSearch.snackbar.dismiss();
        }
        String query = searchEditText.getText().toString();
        Log.d("LauncherActivity.java", "query - " + query);
        if (UtilMethods.isNetworkAvailable(this)) {
            if (!query.equals("")) {
                launcherRefresh.setRefreshing(true);
                searchOptions.setVisibility(View.GONE);
                searchOptionsFlag = false;
                String[] links = UtilMethods.extractLinks(query);
                if (links.length > 0) {
                    Log.d("ShareActivity.java", "receiveIntent() - link = " + links[0]);
                    updateFinalQuery("url:" + links[0]);
                } else {
                    updateFinalQuery(query);
                }
                urlSearch.executeSearch(finalQuery, 1);
            }
            else {
                snackbar = Snackbar.make(coordinatorLayout, "Please enter something in the search box :)",
                        Snackbar.LENGTH_INDEFINITE);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_tint));
                snackbar.show();
            }
        }
        else {
            snackbar = Snackbar.make(coordinatorLayout, "Oh noes! The internet cannot be reached :(",
                    Snackbar.LENGTH_INDEFINITE);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_tint));
            snackbar.show();
        }
    }

    void updateFinalQuery(String q){
        finalQuery.clear();
        finalQuery.put("t",timeValue);
        finalQuery.put("sort", sortValue);
        finalQuery.put("q", q);
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
        MenuItem actionDark = menu.findItem(R.id.action_dark);
        if(theme != 0) {
            actionDark.setChecked(true);
            isChecked = true;
            searchEditText.getText().clear();
            menu.findItem(R.id.action_refresh).setIcon(R.drawable.ic_refresh_white);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                initializeSearch();
                return true;
            case R.id.action_licenses:
                showLicenses();
                return true;
            case R.id.action_dark:
                isChecked = !(item.isChecked());
                item.setChecked(isChecked);
                if (isChecked)
                    UtilMethods.changeToTheme(this, 1, sharedPref);
                else
                    UtilMethods.changeToTheme(this, 0, sharedPref);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(LICENSE_FLAG, licenseFlag);
        savedInstanceState.putString(SORT_BUTTON_LABEL, sortButtonLabel);
        savedInstanceState.putString(TIME_BUTTON_LABEL, timeButtonLabel);
        savedInstanceState.putString(TIME_VALUE, timeValue);
        savedInstanceState.putString(SORT_VALUE, sortValue);
        savedInstanceState.putBoolean(SEARCH_OPTIONS_FLAG, searchOptionsFlag);

        if (alertDialog!=null)
            alertDialog.dismiss();
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState (Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        licenseFlag = savedInstanceState.getInt(LICENSE_FLAG);
        sortButtonLabel = savedInstanceState.getString(SORT_BUTTON_LABEL);
        timeButtonLabel = savedInstanceState.getString(TIME_BUTTON_LABEL);
        timeValue = savedInstanceState.getString(TIME_VALUE);
        sortValue = savedInstanceState.getString(SORT_VALUE);
        searchOptionsFlag = savedInstanceState.getBoolean(SEARCH_OPTIONS_FLAG);

        if (searchOptionsFlag) {
            searchOptions.setVisibility(View.VISIBLE);
        }
        timeButton.setText(timeButtonLabel);
        sortButton.setText(sortButtonLabel);
        if (licenseFlag == 1)
            showLicenses();
    }

    @Override
    public void onBackPressed() {
        searchEditText.setCursorVisible(false);
        super.onBackPressed();
    }

    EditText.OnClickListener searchEditTextClickListener = new EditText.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("LauncherActivity.java", "Clicked search edittext");
            searchOptions.setVisibility(View.VISIBLE);
            searchOptionsFlag = true;
        }
    };
    EditText.OnTouchListener searchEditTextTouchListener = new EditText.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            searchOptions.setVisibility(View.VISIBLE);
            searchOptionsFlag = true;
            return false;
        }
    };

    Button.OnLongClickListener buttonLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Toast buttonDesc = Toast.makeText(LauncherActivity.this, v.getContentDescription(), Toast.LENGTH_SHORT);
            buttonDesc.setGravity(Gravity.CENTER_VERTICAL,0,0);
            buttonDesc.show();
        return false;
        }
    };

    View.OnFocusChangeListener searchFocusChangeListener= new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (!hasFocus) {
                searchOptions.setVisibility(View.GONE);
                searchOptionsFlag = false;
            }
        }
    };

    DialogInterface.OnClickListener dialogOkListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            licenseFlag = 0;
            alertDialog.dismiss();
        }
    };

    DialogInterface.OnCancelListener dialogCancelListener = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            licenseFlag = 0;
            alertDialog.dismiss();
        }
    };
//
//    <item android:id="@+id/time_hour"
//    android:orderInCategory="1"
//    android:title="@string/time_hour"/>
//
//    <item android:id="@+id/time_24"
//    android:orderInCategory="2"
//    android:title="@string/time_24"/>
//
//    <item android:id="@+id/time_week"
//    android:orderInCategory="3"
//    android:title="@string/time_week"/>
//
//    <item android:id="@+id/time_month"
//    android:orderInCategory="4"
//    android:title="@string/time_month"/>
//
//    <item android:id="@+id/time_year"
//    android:orderInCategory="5"
//    android:title="@string/time_year"/>
//
//    <item android:id="@+id/time_all"
//    android:orderInCategory="6"
//    android:title="@string/time_all"/>
    PopupMenu.OnMenuItemClickListener timePopupListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.time_24:
                    timeValue = timeValues[0];
                    timeButtonLabel = getResources().getString(R.string.time_24);
                    timeButton.setText(timeButtonLabel);
                    return true;
                case R.id.time_week:
                    timeValue = timeValues[1];
                    timeButtonLabel = getResources().getString(R.string.time_week);
                    timeButton.setText(timeButtonLabel);
                    return true;
                case R.id.time_month:
                    timeValue = timeValues[2];
                    timeButtonLabel = getResources().getString(R.string.time_month);
                    timeButton.setText(timeButtonLabel);
                    return true;
                case R.id.time_year:
                    timeValue = timeValues[3];
                    timeButtonLabel = getResources().getString(R.string.time_year);
                    timeButton.setText(timeButtonLabel);
                    return true;
                case R.id.time_all:
                    timeValue = timeValues[4];
                    timeButtonLabel = getResources().getString(R.string.time_all);
                    timeButton.setText(timeButtonLabel);
                    return true;
                default:
                    return false;
            }
        }
    };

    PopupMenu.OnMenuItemClickListener sortPopupListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.sort_top:
                    sortValue = sortValues[0];
                    sortButtonLabel = getResources().getString(R.string.sort_top);
                    sortButton.setText(sortButtonLabel);
                    return true;
                case R.id.sort_new:
                    sortValue = sortValues[1];
                    sortButtonLabel = getResources().getString(R.string.sort_new);
                    sortButton.setText(sortButtonLabel);
                    return true;
                case R.id.sort_comments:
                    sortValue = sortValues[2];
                    sortButtonLabel = getResources().getString(R.string.sort_comments);
                    sortButton.setText(sortButtonLabel);
                    return true;
                case R.id.sort_relevance:
                    sortValue = sortValues[3];
                    sortButtonLabel = getResources().getString(R.string.sort_relevance);
                    sortButton.setText(sortButtonLabel);
                    return true;
                default:
                    return false;
            }
        }
    };

    public void showPopup(View view) {
        UtilMethods.hideKeyboard(this);
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        if (view.getId() == R.id.time_button) {
            inflater.inflate(R.menu.time_filter, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(timePopupListener);
        }
        else if (view.getId() == R.id.sort_button) {
            inflater.inflate(R.menu.sort_filter, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(sortPopupListener);
        }
        popupMenu.show();
    }

    void showLicenses() {
        licenseFlag = 1;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.license_dialog, coordinatorLayout, false);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Licenses");
        dialogBuilder.setPositiveButton("OK",dialogOkListener);
        dialogBuilder.setOnCancelListener(dialogCancelListener);
        TextView apache = (TextView) dialogView.findViewById(R.id.apache);
        apache.setText(LicenseText.apacheLicense);
        alertDialog = dialogBuilder.create();
        alertDialog.show();
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
        stopRefreshing();
    }

    public static void stopRefreshing() {
        launcherRefresh.setRefreshing(false);
    }
}
