package com.example.doctorapp.networking.helpers;

import com.example.doctorapp.Constants;
import com.example.doctorapp.model.ProfileModel;
import com.example.doctorapp.networking.ApiData;
import com.example.doctorapp.networking.data.ChangeConclusionBody;
import com.example.doctorapp.networking.data.Profile;
import com.example.doctorapp.networking.data.ProfileDoctorBody;
import com.example.doctorapp.networking.responses.exercise.ResponseExercise;
import com.example.doctorapp.networking.responses.patients.ResponsePatients;
import com.example.doctorapp.networking.responses.profile.ResponseDoctorProfile;
import com.example.doctorapp.networking.responses.results.ResponseResults;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;


import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DataHelper {
    private Retrofit provideRetrofit(){
        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public Observable<Response<ResponsePatients>> getPatients(String token, String userID, int skip, int limit){
        Retrofit retrofit = provideRetrofit();
        String idAndToken = "token=" + token + "; " + "id=" + userID;
        ApiData apiData = retrofit.create(ApiData.class);
        return apiData.getPatients(idAndToken, userID, skip,limit);
    }

    public Observable<Response<ResponseBody>> getDiagnosticsForPatient(String token, String userID, String patientID){
        Retrofit retrofit = provideRetrofit();
        String idAndToken = "token=" + token + "; " + "id=" + userID;
        ApiData apiData = retrofit.create(ApiData.class);
        return apiData.getDiagnostics(idAndToken, patientID);
    }

    public Observable<Response<ResponseExercise>> getExercises(String token, String userID){
        Retrofit retrofit = provideRetrofit();
        String idAndToken = "token=" + token + "; " + "id=" + userID;
        ApiData apiData = retrofit.create(ApiData.class);
        return apiData.getExercise(idAndToken, 0 ,300);
    }

    public Observable<Response<ResponseExercise>> getSuggestetExercisesForPatient(String token, String userID, String patientID){
        Retrofit retrofit = provideRetrofit();
        String idAndToken = "token=" + token + "; " + "id=" + userID;
        ApiData apiData = retrofit.create(ApiData.class);
        return apiData.getSuggestedExercises(idAndToken, patientID,0,300);
    }

    public Observable<Response<ResponseBody>> addExerciceToPatient(String token, String id, String exercise, String patientID){
        Retrofit retrofit = provideRetrofit();
        String idAndToken =  "token=" + token + "; " + "id=" + id;
        ApiData apiData = retrofit.create(ApiData.class);
        return apiData.addExerciseToPatient(idAndToken,patientID,exercise);
    }

    public Observable<Response<ResponseBody>> removeExerciceFromPatient(String token, String id, String exercise, String patientID){
        Retrofit retrofit = provideRetrofit();
        String idAndToken =  "token=" + token + "; " + "id=" + id;
        ApiData apiData = retrofit.create(ApiData.class);
        return apiData.removeExerciseFromPatient(idAndToken,patientID,exercise);
    }

    public Observable<Response<ResponseDoctorProfile>> getDoctorProfile(String token, String id){
        Retrofit retrofit = provideRetrofit();
        String idAndToken =  "token=" + token + "; " + "id=" + id;
        ApiData apiData = retrofit.create(ApiData.class);
        return apiData.getProfile(idAndToken, id);
    }

    public Observable<Response<ResponseBody>> setDoctorProfile(String token, String id, ProfileModel profileModel){
        Retrofit retrofit = provideRetrofit();
        String idAndToken =  "token=" + token + "; " + "id=" + id;
        ApiData apiData = retrofit.create(ApiData.class);
        ProfileDoctorBody body = new ProfileDoctorBody(new Profile(
                profileModel.getName(),
                profileModel.getSurname(),
                profileModel.getPosition()
        ));
        return apiData.setProfile(idAndToken, id, body);
    }

    public Observable<Response<ResponseBody>> changeConclusion(String token,
                                                               String id,
                                                               String patientID,
                                                               String infoID,
                                                               String newConclusion){
        Retrofit retrofit = provideRetrofit();
        String idAndToken =  "token=" + token + "; " + "id=" + id;
        ApiData apiData = retrofit.create(ApiData.class);
        return apiData.changeConclusion(idAndToken, patientID, infoID, new ChangeConclusionBody(newConclusion));
    }

}
