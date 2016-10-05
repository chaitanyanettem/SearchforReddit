package chaitanya.im.searchforreddit;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.IBinder;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
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

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chaitanya.im.searchforreddit.DataModel.Child;
import chaitanya.im.searchforreddit.DataModel.RecyclerViewItem;
import chaitanya.im.searchforreddit.DataModel.Result;
import chaitanya.im.searchforreddit.Network.UrlSearch;

public class LauncherActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String BASE_URL = "https://www.reddit.com";
    private static final String TAG = "LauncherActivity.java";
    private static final String SORT_BUTTON_LABEL = "sortButtonLabel";
    private static final String TIME_BUTTON_LABEL = "timeButtonLabel";
    private static final String TIME_VALUE = "timeValue";
    private static final String SORT_VALUE = "sortValue";
    private static final int SOURCE = 1;
    private static final String SEARCH_OPTIONS_FLAG = "searchOptionsFlag";

    private static final String [] timeValues = {"day", "week", "month", "year", ""};

    private static final String [] sortValues = {"top", "new", "comments", ""};
    private boolean isChecked = false;

    private boolean searchOptionsFlag = false;
    private String sortButtonLabel ="Relevance";
    private String timeButtonLabel ="All Time";
    private SharedPreferences sharedPref;
    private GenericAlertDialog dialog;

    private LinearLayout searchOptions;
    private EditText searchEditText;
    //private Button filterButton;
    private Button sortButton;
    private Button timeButton;
    private SwipeRefreshLayout launcherRefresh;
    private static RecyclerView rvResults;
    private static Snackbar snackbar;
    private CoordinatorLayout coordinatorLayout;

    private IInAppBillingService mService;
    private ServiceConnection mServiceConn;

    private String skuToPurchase;
    private ArrayList<String> skuList;
    private final List<String> allPrices = new ArrayList<>();
    private static Bundle querySkus;
    private static final List<RecyclerViewItem> resultList = new ArrayList<>();
    private String timeValue = timeValues[4];
    private String sortValue = sortValues[3];
    private int theme;
    private int donate = 0;
    private UrlSearch urlSearch;
    private Map<String,String> finalQuery;
    private Typeface fontAwesome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get and set theme from shared preferences
        sharedPref = getSharedPreferences(getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        theme = sharedPref.getInt(getString(R.string.style_pref_key), 0);
        donate = sharedPref.getInt(getString(R.string.donate_check), 0);
        UtilMethods.onActivityCreateSetTheme(this, theme, SOURCE);

        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name,
                                           IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
                getPrices();
            }
        };

        //ComponentName myService = startService(new Intent(this, LauncherActivity.class));
        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        // IAP stuff
        skuList = new ArrayList<> ();
        skuList.add("donate");
        skuList.add("donate2");
        skuList.add("donate3");
        querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

        setContentView(R.layout.activity_launcher);

        fontAwesome = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        dialog = new GenericAlertDialog();
        dialog.setFontAwesome(fontAwesome);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        LinearLayout searchBox = (LinearLayout) findViewById(R.id.search_box);
        searchOptions = (LinearLayout) findViewById(R.id.search_options);
        launcherRefresh = (SwipeRefreshLayout) findViewById(R.id.launcher_refresh);
        launcherRefresh.setOnRefreshListener(refreshListener);
        searchEditText = (EditText) findViewById(R.id.search_edit_text);
        //filterButton = (Button) findViewById(R.id.filter_button);
        sortButton = (Button) findViewById(R.id.sort_button);
        timeButton = (Button) findViewById(R.id.time_button);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.launcher_coordinatorlayout);
        finalQuery = new HashMap<>();
        setSupportActionBar(toolbar);

        searchEditText.setOnClickListener(searchEditTextClickListener);
        searchEditText.setOnFocusChangeListener(searchFocusChangeListener);
        searchEditText.setOnKeyListener(onKeyListener);
        searchEditText.setOnTouchListener(searchEditTextTouchListener);

        sortButton.setOnLongClickListener(buttonLongClick);
        timeButton.setOnLongClickListener(buttonLongClick);
        //filterButton.setOnLongClickListener(buttonLongClick);

        rvResults = (RecyclerView) findViewById(R.id.result_view_launcher);
        ResultsAdapter adapter = new ResultsAdapter(resultList, this);
        rvResults.setAdapter(adapter);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.addItemDecoration(new SimpleDividerItemDecoration(this, theme));

        //rvResults.setRecyclerListener(recyclerListener);
        urlSearch = new UrlSearch(BASE_URL, this, 1, adapter);

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
        Log.d(TAG, "OnCreate");
        Log.d(TAG, "searchEditText - " + searchEditText.getText().toString());
        // TODO - fix this:
        if (!searchEditText.getText().toString().equals("")) {
            Log.d(TAG, "OnCreate, visibility");
            searchOptions.setVisibility(View.VISIBLE);
            searchOptionsFlag = true;
        }

        launcherRefresh.setColorSchemeResources(R.color.blue_tint,
                R.color.reddit_orange,
                R.color.material_light_black);

        Intent intent = getIntent();
        receiveIntent(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    private void getPrices() {
        Thread thread = new Thread(getPricesRunnable);
        thread.start();
    }

    private void getPurchase(String skuToPurchase) {
        this.skuToPurchase = skuToPurchase;
        Thread thread = new Thread(purchaseRunnable);
        thread.start();
    }

    private final Runnable getPricesRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                String sku;
                allPrices.clear();
                Bundle skuDetails = mService.getSkuDetails(3,
                        getPackageName(), "inapp", querySkus);
                int response = skuDetails.getInt("RESPONSE_CODE");
                Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);

                if (ownedItems.getInt("RESPONSE_CODE") == 0) {
                    ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");

                    if (ownedSkus != null) {
                        if (ownedSkus.size() > 0) {
                            Log.d(TAG, "ownedSkus = " + ownedSkus.toString());
                            if (donate!=1) {
                                SharedPreferences.Editor editor = sharedPref.edit();
                                donate = 1;
                                editor.putInt(getString(R.string.donate_check), donate);
                                editor.commit();
                                invalidateOptionsMenu();
                            }
                        }
                        else {
                            donate = 0;
                            invalidateOptionsMenu();
                            Log.d(TAG, "ownedSkus is empty");
                        }
                    }
                    else
                        Log.d(TAG, "ownedSkus is null");

/*
                    // For consuming purchase
                    ArrayList<String>  purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                    if (purchaseDataList != null) {
                        Log.d (TAG, "PurchaseDataList - " + Integer.toString(purchaseDataList.size()));
                        Log.d (TAG, purchaseDataList.toString());
                        JSONObject object = new JSONObject(purchaseDataList.get(0));
                        response = mService.consumePurchase(3, getPackageName(), object.getString("purchaseToken"));
                        Log.d(TAG, "response = " + response);
                    }
*/

                }
                else {
                    Log.d(TAG, "Response code not 0");
                }

                if (response == 0) {
                    ArrayList<String> responseList
                            = skuDetails.getStringArrayList("DETAILS_LIST");

                    assert responseList != null;
                    for (String thisResponse : responseList) {
                        JSONObject object = new JSONObject(thisResponse);
                        sku = object.getString("productId");
                        String price = object.getString("price");
                        allPrices.add(price);
                        Log.d(TAG, sku + ": " + price);

                    }
                    Log.d(TAG, "allPrices = " + allPrices.toString());
                    dialog.setAllPrices(allPrices);
                }
                else {
                    Log.d(TAG, "response = " + response);
                }

            }
            catch (android.os.RemoteException e) {
                Log.d(TAG, "Remote Exception, " + e.toString());
            }
            catch (org.json.JSONException e) {
                Log.d(TAG, "JSON exception, " + e.toString());
            }
            catch (NullPointerException e) {
                Log.d(TAG, "Null Pointer Exception, " + e.toString());
            }
/*
            catch (IntentSender.SendIntentException e) {
                Log.d(TAG, "SendIntentException, " + e.toString());
            }
*/
        }
    };

    private final Runnable purchaseRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d(TAG, "purchaseRunnable: skuToPurchase = " + skuToPurchase);
                Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                        skuToPurchase, "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
                int code = buyIntentBundle.getInt("RESPONSE_CODE");
                Log.d(TAG, "RESPONSE_CODE = "+code);
                if (code == 0) {
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    assert pendingIntent != null;
                    startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), 0, 0, 0);

                }
            }
            catch (android.os.RemoteException e) {
                Log.d(TAG, "Remote Exception, " + e.toString());
            }
            catch (IntentSender.SendIntentException e) {
                Log.d(TAG, "SendIntentException, " + e.toString());
            }
            catch (NullPointerException e) {
                Log.d(TAG, "Null Pointer Exception, " + e.toString());
            }
        }
    };

    @SuppressWarnings("UnusedParameters")
    public void initializeSearch(View view) { initializeSearch(); }

    private void initializeSearch() {
        UtilMethods.hideKeyboard(this);
        if (snackbar != null) {
            snackbar.dismiss();
        }
        if (urlSearch.snackbar != null) {
            urlSearch.snackbar.dismiss();
        }
        String query = searchEditText.getText().toString();
        Log.d(TAG, "query - " + query);
        if (UtilMethods.isNetworkAvailable(this)) {
            if (!query.equals("")) {
                launcherRefresh.setRefreshing(true);
                searchOptions.setVisibility(View.GONE);
                searchOptionsFlag = false;
                String[] links = UtilMethods.extractLinks(query);
                if (links.length > 0) {
                    Log.d(TAG, "receiveIntent() - link = " + links[0]);
                    String youtubeID = UtilMethods.extractYoutubeID(links[0]);
                    if (youtubeID != null)
                        updateFinalQuery("url:" + youtubeID);
                    else
                        updateFinalQuery("url:" + links[0]);
                } else {
                    updateFinalQuery(query);
                }
                urlSearch.executeSearch(finalQuery, 1);
            }
            else {
                showMessageInSnackbar(getResources().getString(R.string.empty_search_box));
            }
        }
        else {
            showMessageInSnackbar(getResources().getString(R.string.no_internet));
        }
    }

    private void showMessageInSnackbar(String message) {
        launcherRefresh.setRefreshing(false);
        snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTypeface(fontAwesome);
        snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_tint));
        snackbar.show();
    }

    private void receiveIntent(Intent intent) {
        String sharedText = intent.getStringExtra(ShareActivity.EXTRA_SHARED_TEXT);
        if (sharedText != null) {
            searchEditText.setText(sharedText);
            initializeSearch();
            searchOptions.setVisibility(View.VISIBLE);
            searchOptionsFlag = true;
        }

    }

    private void updateFinalQuery(String q) {
        finalQuery.clear();
        finalQuery.put("t",timeValue);
        finalQuery.put("sort", sortValue);
        finalQuery.put("q", q);
    }

    public static void updateDialog(AppCompatActivity activity, Result result, boolean append, ResultsAdapter adapter) {
        RecyclerViewItem temp;
        if (!append)
            resultList.clear();
        for (Child c:
                result.getData().getChildren()) {
            temp = UtilMethods.buildRecyclerViewItem(c);
            resultList.add(temp);
        }

        if (resultList.size() == 0) {
            // I don't want to have to manually create a snackbar here instead of calling showMessageInSnackbar(String)
            // but showMessageInSnackbar is a non-static method and this is a static method. So...

            snackbar = Snackbar.make(activity.findViewById(R.id.launcher_coordinatorlayout), "0 Search results", Snackbar.LENGTH_INDEFINITE);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(ContextCompat.getColor(activity, R.color.blue_tint));
            snackbar.show();
        }
        else
            adapter.notifyDataSetChanged();
        ((SwipeRefreshLayout) activity.findViewById(R.id.launcher_refresh)).setRefreshing(false);
        rvResults.post(scrollToTop);
    }

    private static final Runnable scrollToTop = new Runnable() {
        @Override
        public void run() {
            rvResults.smoothScrollToPosition(0);
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1001) {
            //int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            //String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    if (sku.equals("donate") || sku.equals("donate2") || sku.equals("donate3")) {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        donate = 1;
                        editor.putInt(getString(R.string.donate_check), donate);
                        editor.commit();
                        invalidateOptionsMenu();
                        showMessageInSnackbar("Thank you for the donation! It is much appreciated :)");
                    }
                }
                catch (JSONException e) {
                    Log.d(TAG, "Failed to parse purchase data.");
                    showMessageInSnackbar("Something went wrong with the purchase! Contact us at overloadapps@gmail.com");
                    e.printStackTrace();
                }
            }
            else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Cancelled");
                showMessageInSnackbar("Purchase cancelled. We hope you reconsider :)");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem actionDark = menu.findItem(R.id.action_dark);

        if (donate == 1) {
            menu.findItem(R.id.action_donate).setVisible(false);
        }

        if (theme != 0) {
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
            case R.id.action_donate:
                dialog.setWhichDialog(0);
                dialog.setPurchaseDialog(-1);
                dialog.show(getSupportFragmentManager(), "tag");
                return true;
            case R.id.action_dark:
                if (donate == 1) {
                    isChecked = !(item.isChecked());
                    item.setChecked(isChecked);
                    if (isChecked)
                        UtilMethods.changeToTheme(this, 1, sharedPref);
                    else
                        UtilMethods.changeToTheme(this, 0, sharedPref);
                    return true;
                }
                else {
                    dialog.setWhichDialog(0);
                    dialog.setPurchaseDialog(1);
                    dialog.show(getSupportFragmentManager(), "tag");
                    return true;
                }
            case R.id.action_licenses:
                dialog.setWhichDialog(1);
                dialog.show(getSupportFragmentManager(), "tag");
                return true;
            case R.id.action_about:
                dialog.setWhichDialog(2);
                dialog.show(getSupportFragmentManager(), "tag");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(SORT_BUTTON_LABEL, sortButtonLabel);
        savedInstanceState.putString(TIME_BUTTON_LABEL, timeButtonLabel);
        savedInstanceState.putString(TIME_VALUE, timeValue);
        savedInstanceState.putString(SORT_VALUE, sortValue);
        savedInstanceState.putBoolean(SEARCH_OPTIONS_FLAG, searchOptionsFlag);

        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState (Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
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
    }

    @Override
    public void onBackPressed() {
        if (searchOptionsFlag) {
            searchOptionsFlag = false;
            searchOptions.setVisibility(View.GONE);
        }
        else {
            super.onBackPressed();
        }
    }

    private final SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        public void onRefresh() {
            Log.d(TAG, "onRefresh called");
            initializeSearch();
        }
    };

    private final EditText.OnClickListener searchEditTextClickListener = new EditText.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "Clicked search edittext");
            searchOptions.setVisibility(View.VISIBLE);
            searchOptionsFlag = true;
        }
    };

    private final EditText.OnTouchListener searchEditTextTouchListener = new EditText.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            searchOptions.setVisibility(View.VISIBLE);
            searchOptionsFlag = true;
            return false;
        }
    };

    private final Button.OnLongClickListener buttonLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Toast buttonDesc = Toast.makeText(LauncherActivity.this, v.getContentDescription(), Toast.LENGTH_SHORT);
            buttonDesc.setGravity(Gravity.CENTER_VERTICAL,0,0);
            buttonDesc.show();
            return false;
        }
    };

    private final View.OnFocusChangeListener searchFocusChangeListener= new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (!hasFocus) {
                searchOptions.setVisibility(View.GONE);
                searchOptionsFlag = false;
            }
        }
    };

    public void doPositiveClick(int value) {
        // Do stuff here.
        Log.d(TAG, "doPositiveClick - value = " + value);
        if (value>-1) {
            getPurchase(skuList.get(value));
        }
    }

// --Commented out by Inspection START (11/9/16 5:40 PM):
//    RecyclerView.RecyclerListener recyclerListener = new RecyclerView.RecyclerListener() {
//        @Override
//        public void onViewRecycled(RecyclerView.ViewHolder holder) {
//            Log.d(TAG, Long.toString(holder.getItemId()));
//        }
//    };
// --Commented out by Inspection STOP (11/9/16 5:40 PM)

    private final View.OnKeyListener onKeyListener = new View.OnKeyListener() {
        public boolean onKey(View v, int keycode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keycode == KeyEvent.KEYCODE_ENTER)) {
                initializeSearch();
                return true;
            }
            return false;
        }
    };


    private final PopupMenu.OnMenuItemClickListener timePopupListener = new PopupMenu.OnMenuItemClickListener() {
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

    private final PopupMenu.OnMenuItemClickListener sortPopupListener = new PopupMenu.OnMenuItemClickListener() {
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
}
