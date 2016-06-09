package chaitanya.im.searchforreddit.Network;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import chaitanya.im.searchforreddit.DataModel.Result;
import chaitanya.im.searchforreddit.MainActivity;
import chaitanya.im.searchforreddit.R;
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

    public UrlSearch(String base_url, String query, AppCompatActivity _activity) {
        activity = _activity;
        label = (TextView) activity.findViewById(R.id.label);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(base_url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        endpoint = retrofit.create(RedditEndpointInterface.class);
        Call<Result> call = endpoint.getResults(query);

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
        label.setText("Kind:" + result.getKind());
    }
}
