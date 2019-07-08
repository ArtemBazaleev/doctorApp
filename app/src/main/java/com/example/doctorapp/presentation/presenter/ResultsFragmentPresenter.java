package com.example.doctorapp.presentation.presenter;

import android.util.Log;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.example.doctorapp.Constants;
import com.example.doctorapp.model.ExerciseModel;
import com.example.doctorapp.model.ResultModel;
import com.example.doctorapp.networking.ApiData;
import com.example.doctorapp.networking.helpers.DataHelper;
import com.example.doctorapp.networking.responses.exercise.Exercise;
import com.example.doctorapp.networking.responses.results.Info;
import com.example.doctorapp.presentation.view.IResultsFragmentView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;

@InjectViewState
public class ResultsFragmentPresenter extends MvpPresenter<IResultsFragmentView> {

    private String userID;
    private String token;
    private String patientID;
    private DataHelper dataHelper;
    //private List<ResultModel> data;
    private DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public ResultsFragmentPresenter(String userID, String token) {
        this.userID = userID;
        this.token = token;
        dataHelper= new DataHelper();
    }


    public void onCreateView(){
        provideData();
    }

    private void provideData() {
        getViewState().showProgress();
        Log.d("Presenter", "DoctorID " + userID);
        Disposable d = dataHelper.getDiagnosticsForPatient(token,userID,patientID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseResultsResponse -> {
                    getViewState().hideProgress();
                    if (responseResultsResponse.isSuccessful())
                    {
                        JSONObject jsonObject = new JSONObject(responseResultsResponse.body().string());
                        List<ResultModel> data = new ArrayList<>();
                        Log.d("Presenter", jsonObject.toString());
                        try {
                            JSONArray infos = jsonObject.getJSONObject("data").getJSONArray("info");
                            for (int i = 0; i< infos.length(); i++) {
                                Date date = new Date(infos.getJSONObject(i).getLong("created"));
                                String today = formatter.format(date);
                                data.add(new ResultModel("",
                                        infos.getJSONObject(i).getString("conclusion"),
                                        "Заключение",
                                        ResultModel.TYPE_CONCLUSION));
                                JSONArray arrJson = infos.getJSONObject(i).getJSONArray("backbone");
                                String[] arr = new String[arrJson.length()];
                                for(int j = 0; j < arrJson.length(); j++)
                                    arr[j] = arrJson.getString(j);
                                ResultModel resultModel = new ResultModel(
                                        "",
                                        "3D модель позвоночника",
                                        today,
                                        ResultModel.TYPE_BACKBONE
                                );
                                resultModel.setBackBoneImage(arr);
                                data.add(resultModel);
                                JSONArray otherArr = infos.getJSONObject(i).getJSONArray("other");
                                for (int k=0; k < otherArr.length(); k++){
                                    data.add(new ResultModel(
                                            Constants.BASE_URL_IMAGE + otherArr.getJSONObject(k).getString("image"),
                                            otherArr.getJSONObject(k).getString("name"),
                                            today,
                                            ResultModel.TYPE_OTHER));
                                }
                            }
                            if (data.size()== 0)
                                getViewState().showErrorNoResults();
                            else getViewState().hideErrorNoResults();
                            getViewState().loadResults(data);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    else{
                        Log.d("Presenter", "onCreateView: "  + responseResultsResponse.errorBody().string());
                    }
                }, throwable -> {
                    if (throwable instanceof SocketTimeoutException)
                        getViewState().showTastyMessage("Bad internet connection, try later");
                    else getViewState().showTastyMessage("Error, try later");
                    throwable.printStackTrace();
                    getViewState().showErrorNoResults();
                    getViewState().hideProgress();
                });
    }

    public void onResultClicked(ResultModel model){
        getViewState().startActivityResultView(model);
    }

    public void setUserID(String patientID) {
        this.patientID = patientID;
    }

    public void onRefresh() {
        provideData();
    }
}
