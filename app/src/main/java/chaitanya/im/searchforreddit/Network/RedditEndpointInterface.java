package chaitanya.im.searchforreddit.Network;

import chaitanya.im.searchforreddit.DataModel.Result;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RedditEndpointInterface {

    // https://www.reddit.com/search.json?q=url:https://www.youtube.com/watch?v=CVEuPmVAb8o?t=1s&sort=top&t=all
    @GET("search.json")
    Call<Result> getSearchResults(@Query("q") String query);

}
