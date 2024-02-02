package com.example.caller.helpers;

import com.example.caller.R;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

//    @Headers({
//            "User-Agent : Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36",
//    })
    @POST("/v1/login")
    Call<ResponseBody> loginAccount(@Body JsonObject ob, @HeaderMap Map<String,String> headers);

    @GET("/v1/uo/matches?page=1&tab=&sub_type_id=1,186,340&sport_id=14&tag_id=&sort_id=1&period_id=-1&esports=false")
    Call<ResponseBody> getGames(@Query("limit") String limit);

    @POST("/v2/bet")
    Call<ResponseBody> placeBet(@Body HashMap<String,Object> payload);
    @POST("/v1/mybets/cancel")
    Call<ResponseBody> cancelBet(@Body HashMap<String,Object> payload);
    @POST("/v1/withdraw")
    Call<ResponseBody> withdraw(@Body HashMap<String,String> payload);
}
