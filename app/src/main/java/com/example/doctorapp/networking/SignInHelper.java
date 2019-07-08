package com.example.doctorapp.networking;

import com.example.doctorapp.Constants;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SignInHelper {
    public Observable<Response<ResponseSignIn>> signIn(String login, String password){
        Retrofit retrofit = provideRetrofit();

        ApiLogin api = retrofit.create(ApiLogin.class);
        return api.signIn(new SignInBody(login,password));
    }

    private Retrofit provideRetrofit(){
        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }
}
