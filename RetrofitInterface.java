package com.hub.dairy;


import androidx.room.Update;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitInterface {
    @FormUrlEncoded
    @POST("updateLocation")
    Call<Update> update(@Query("token") String token, @Query("lat") String latitude, @Field("long") String longitude);
}
