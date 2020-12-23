package com.androidsolution.searchpix.api_services;

import com.androidsolution.searchpix.models.PixabayImageList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PixabayApi {

    @GET("/api/")               //generating required url to be searched
    Call<PixabayImageList> getImageResults(@Query("key") String key, @Query("q") String query, @Query("page") int page, @Query("per_page") int perPage);
}
