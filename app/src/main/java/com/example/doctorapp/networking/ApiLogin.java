package com.example.doctorapp.networking;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiLogin {
    @Headers("Content-Type: application/json")
    @POST("auth/signin")
    Observable<Response<ResponseSignIn>> signIn(@Body SignInBody body);

}
