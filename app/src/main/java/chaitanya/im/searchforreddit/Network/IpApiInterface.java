package chaitanya.im.searchforreddit.Network;

import chaitanya.im.searchforreddit.DataModel.RoughDeviceLocation;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Chaitanya Nettem on 11/10/16.
 */

public interface IpApiInterface {

    @GET("json")
    Call<RoughDeviceLocation> sendLocationRequest();
}
