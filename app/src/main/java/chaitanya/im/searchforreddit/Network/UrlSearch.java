package chaitanya.im.searchforreddit.Network;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import java.net.UnknownHostException;
import java.util.Map;

import chaitanya.im.searchforreddit.DataModel.Result;
import chaitanya.im.searchforreddit.LauncherActivity;
import chaitanya.im.searchforreddit.R;
import chaitanya.im.searchforreddit.ResultsAdapter;
import chaitanya.im.searchforreddit.RedditShare;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UrlSearch {
    private final RedditEndpointInterface endpoint;
    private final AppCompatActivity activity;
    private SwipeRefreshLayout launcherRefresh;
    private Result result;
    private CoordinatorLayout coordinatorLayout;
    public Snackbar snackbar;
    private TextView label;
    private final ResultsAdapter adapter;

    private final static String TAG = "UrlSearch.java";
    private final static String HTTP_ERROR = "There was an issue with Reddit's server. Please try again. HTTP Error code - ";
    private final static String PARSING_ERROR = "There was an issue with parsing the response from Reddit. The developer has been informed. Please try again.";
    private final static String UNKNOWN_ISSUE = "There was an unknown issue. The developer has been informed. Please try again.";
    private final static String INTERNET_ISSUE = "There is an issue with your internet. Reddit could not be reached.";

    public UrlSearch(String base_url, AppCompatActivity _activity, final int caller, ResultsAdapter _adapter) {
        adapter = _adapter;
        activity = _activity;
        if (caller == 1) {
            coordinatorLayout = (CoordinatorLayout) activity.findViewById(R.id.launcher_coordinatorlayout);
            launcherRefresh = (SwipeRefreshLayout) activity.findViewById(R.id.launcher_refresh);
        }
        if (caller == 0) {
            label = (TextView) activity.findViewById(R.id.label);
        }


        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(base_url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        endpoint = retrofit.create(RedditEndpointInterface.class);
    }

    public void executeSearch(Map<String, String> query, final int source){
        Call<Result> call = endpoint.getSearchResults(query);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                int statusCode = response.code();
                Log.d(TAG, "executeSearch() - Status code - " +
                        Integer.toString(statusCode));
                if (response.isSuccessful()) {
                    Log.d(TAG, "Response successful");
                    result = response.body();
                    if (result != null) {
                        Log.d(TAG, "result!=null");
                        if (source == 0) {
                            Log.d(TAG, "source = 0.");
                            RedditShare.updateDialog(activity, result, null, adapter);
                        }
                        else
                            LauncherActivity.updateDialog(activity, result, adapter);
                    }
                    else {
                        Log.d(TAG, "result=null");
                        if(source == 0) {
                            label.setText(PARSING_ERROR);
                            label.setVisibility(View.VISIBLE);
                        }
                        else {
                            launcherRefresh.setRefreshing(false);
                            snackbar = Snackbar.make(coordinatorLayout, PARSING_ERROR, Snackbar.LENGTH_INDEFINITE);
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(ContextCompat.getColor(activity, R.color.blue_tint));
                            snackbar.show();
                        }
                    }
                }
                else {
                    Log.d(TAG, "Response unsuccessful");
                    if(source == 0) {
                        RedditShare.updateDialog(activity, null, HTTP_ERROR + statusCode, adapter);
                    }
                    else {
                        snackbar = Snackbar.make(coordinatorLayout, HTTP_ERROR + statusCode, Snackbar.LENGTH_INDEFINITE);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(ContextCompat.getColor(activity, R.color.blue_tint));
                        snackbar.show();
                        launcherRefresh.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                String logMsg = "executeSearch failed. Throwable is - " + t.toString() +
                        "; URL was - " + call.request().url().toString();


                FirebaseCrash.report(new Exception(logMsg));
                if(source == 0) {
                    if (t instanceof UnknownHostException) {
                        RedditShare.updateDialog(activity, null, INTERNET_ISSUE, adapter);
                    }
                    else {
                        RedditShare.updateDialog(activity, null, UNKNOWN_ISSUE, adapter);
                    }
                }
                else {
                    if (t instanceof UnknownHostException)
                        snackbar = Snackbar.make(coordinatorLayout, INTERNET_ISSUE, Snackbar.LENGTH_INDEFINITE);
                    else
                        snackbar = Snackbar.make(coordinatorLayout, UNKNOWN_ISSUE, Snackbar.LENGTH_INDEFINITE);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(activity, R.color.blue_tint));
                    snackbar.show();
                    launcherRefresh.setRefreshing(false);
                }
                Log.e(TAG, logMsg);
            }
        });
    }

}
