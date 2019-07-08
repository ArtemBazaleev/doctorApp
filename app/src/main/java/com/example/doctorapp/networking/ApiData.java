package com.example.doctorapp.networking;

import com.example.doctorapp.networking.data.ProfileDoctorBody;
import com.example.doctorapp.networking.responses.exercise.ResponseExercise;
import com.example.doctorapp.networking.responses.patients.ResponsePatients;
import com.example.doctorapp.networking.responses.profile.ResponseDoctorProfile;
import com.example.doctorapp.networking.responses.results.ResponseResults;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiData {

    @GET("therapist/{therapistID}/patient")
    Observable<Response<ResponsePatients>> getPatients(
            @Header("Cookie") String tokenAndId,
            @Path("therapistID") String therapistID,
            @Query("skip") int skip,
            @Query("limit") int limit
    );

    @GET("therapist/{therapistID}/profile")
    Observable<Response<ResponseDoctorProfile>> getProfile(
            @Header("Cookie") String tokenAndId,
            @Path("therapistID") String therapistID
    );

    @POST("therapist/{therapistID}/profile/update")
    Observable<Response<ResponseBody>> setProfile(
            @Header("Cookie") String tokenAndId,
            @Path("therapistID") String therapistID,
            @Body ProfileDoctorBody body
    );

    @POST("patient/{patientID}/exercise/{exerciseID}/append")
    Observable<Response<ResponseBody>> addExerciseToPatient(
            @Header("Cookie") String tokenAndId,
            @Path("patientID") String patientID,
            @Path("exerciseID") String exerciseID
    );

    @POST("patient/{patientID}/exercise/{exerciseID}/remove")
    Observable<Response<ResponseBody>> removeExerciseFromPatient(
            @Header("Cookie") String tokenAndId,
            @Path("patientID") String patientID,
            @Path("exerciseID") String exerciseID
    );

    @GET("patient/{patientID}/exercise")
    Observable<Response<ResponseExercise>> getSuggestedExercises(
            @Header("Cookie") String tokenAndId,
            @Path("patientID") String patientID
    );

    @GET("exercise")
    Observable<Response<ResponseExercise>> getExercise(@Header("Cookie") String tokenAndId);

    @GET("patient/{patientID}/diagnosticInfo")
    Observable<Response<ResponseBody>> getDiagnostics(
            @Header("Cookie") String tokenAndId,
            @Path("patientID") String userID
    );
}
