package chaitanya.im.searchforreddit.Network;

import android.graphics.Typeface;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import java.util.Map;

import chaitanya.im.searchforreddit.DataModel.Result;
import chaitanya.im.searchforreddit.LauncherActivity;
import chaitanya.im.searchforreddit.R;
import chaitanya.im.searchforreddit.ShareActivity;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UrlSearch {
    RedditEndpointInterface endpoint;
    AppCompatActivity activity;
    SwipeRefreshLayout launcherRefresh;
    Result result;
    CoordinatorLayout coordinatorLayout;
    public Snackbar snackbar;
    TextView label;
    final String HTTP_ERROR = "There was an issue with Reddit's server. Please try again. HTTP Error code - ";
    final String PARSING_ERROR = "There was an issue with parsing the response from Reddit. The developer has been informed. Please try again.";
    final String UNKNOWN_ISSUE = "There was an unknown issue. The developer has been informed. Please try again.";

    public UrlSearch(String base_url, AppCompatActivity _activity, final int caller) {
        activity = _activity;
        coordinatorLayout = (CoordinatorLayout) activity.findViewById(R.id.launcher_coordinatorlayout);
        label = (TextView) activity.findViewById(R.id.label);
        launcherRefresh = (SwipeRefreshLayout) activity.findViewById(R.id.launcher_refresh);

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
                Log.d("UrlSearch.java", "executeSearch() - Status code - " +
                        Integer.toString(statusCode));
                if (response.isSuccessful()) {
                    Log.d("UrlSearch.java", "Response successful");
                    result = response.body();
                    if (result != null) {
                        Log.d("UrlSearch.java", "result!=null");
                        if (source == 0) {
                            Log.d("UrlSearch.java", "source = 0.");
                            ShareActivity.updateDialog(result);
                        }
                        else
                            LauncherActivity.updateDialog(result);
                    }
                    else {
                        Log.d("UrlSearch.java", "result=null");
                        if(source == 0) {
                            label.setText(PARSING_ERROR);
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
                    Log.d("UrlSearch.java", "Response unsuccessful");
                    if(source == 0) {
                        label.setText(HTTP_ERROR + statusCode);
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
                    label.setText(UNKNOWN_ISSUE);
                }
                else {
                    snackbar = Snackbar.make(coordinatorLayout, UNKNOWN_ISSUE, Snackbar.LENGTH_INDEFINITE);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(ContextCompat.getColor(activity, R.color.blue_tint));
                    snackbar.show();
                    launcherRefresh.setRefreshing(false);
                }
                Log.e("UrlSearch.java", logMsg);
            }
        });
    }

}
