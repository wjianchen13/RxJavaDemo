package com.example.rxjavademo.thread;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface Api {

    @GET("banner/json")
    Observable<ResponseBody> login(@QueryMap Map<String, String> params);

    @GET("banner/json")
    Observable<ResponseBody> register(@QueryMap Map<String, String> params);
}

