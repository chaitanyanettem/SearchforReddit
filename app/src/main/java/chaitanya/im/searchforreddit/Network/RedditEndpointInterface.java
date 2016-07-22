package chaitanya.im.searchforreddit.Network;

import java.util.Map;

import chaitanya.im.searchforreddit.DataModel.Result;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface RedditEndpointInterface {

    // https://www.reddit.com/search.json?q=url:https://www.youtube.com/watch?v=CVEuPmVAb8o?t=1s&sort=top&t=all
    @GET("search.json")
    Call<Result> getSearchResults(
        @QueryMap Map<String, String> options
    );

}
