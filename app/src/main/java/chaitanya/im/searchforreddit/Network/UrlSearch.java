package chaitanya.im.searchforreddit.Network;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import chaitanya.im.searchforreddit.DataModel.Result;
import chaitanya.im.searchforreddit.R;
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
    TextView label;

    public UrlSearch(String base_url, AppCompatActivity _activity) {
        activity = _activity;
        label = (TextView) activity.findViewById(R.id.label);

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

    public void executeSearch(String query){
        Call<Result> call = endpoint.getSearchResults(query);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                int statusCode = response.code();
                result = response.body();
                if (result != null) {
                    Log.d("UrlSearch.java", result.getKind());
                    updateDialog();
                }
                Log.d("UrlSearch.java", Integer.toString(statusCode));
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Log.e("UrlSearch.java", "Request failed.");
            }
        });
    }

    void updateDialog() {
        if (result.getData().getChildren()!=null)
            label.setText("children size:" + result.getData().getChildren().size());
        else
            label.setText("No Results found");
    }
}
