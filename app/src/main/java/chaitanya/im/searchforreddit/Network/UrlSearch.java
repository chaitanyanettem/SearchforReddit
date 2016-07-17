package chaitanya.im.searchforreddit.Network;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import chaitanya.im.searchforreddit.DataModel.Result;
import chaitanya.im.searchforreddit.LauncherActivity;
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
    Result result;

    public UrlSearch(String base_url, AppCompatActivity _activity) {
        activity = _activity;

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

    public void executeSearch(String query, final int source){
        Call<Result> call = endpoint.getSearchResults(query);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                int statusCode = response.code();
                result = response.body();
                if (result != null) {
                    if(source==0)
                        ShareActivity.updateDialog(result);
                    else
                        LauncherActivity.updateDialog(result);
                }
                Log.d("UrlSearch.java", "executeSearch() - Status code - " +
                        Integer.toString(statusCode));
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                String logMsg = "executeSearch failed. Throwable is - " +
                        t.toString() + "; URL was - " + call.request().url().toString();
                FirebaseCrash.report(new Exception(logMsg));
                Log.e("UrlSearch.java", logMsg);
            }
        });
    }

}
