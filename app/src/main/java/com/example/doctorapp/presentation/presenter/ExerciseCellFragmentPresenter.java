package com.example.doctorapp.presentation.presenter;

import android.util.Log;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.example.doctorapp.model.ExerciseModel;
import com.example.doctorapp.networking.helpers.DataHelper;
import com.example.doctorapp.networking.responses.exercise.Exercise;
import com.example.doctorapp.networking.responses.exercise.ResponseExercise;
import com.example.doctorapp.presentation.view.IExerciseCellFragment;
import com.example.doctorapp.ui.ExerciseCellFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

@InjectViewState
public class ExerciseCellFragmentPresenter extends MvpPresenter<IExerciseCellFragment> {

    private String mToken = "";
    private String mID = "";
    private int mode;
    private CompositeDisposable d = new CompositeDisposable();
    private DataHelper apiHelper = new DataHelper();
    private ExerciseModel model;

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    private String patientID;

    public ExerciseCellFragmentPresenter(String token, String id, String patientID){
        mID = id;
        mToken = token;
        this.patientID = patientID;
    }

    public void onViewCreated(){
        provideData();
    }

    private void provideData(){
        if (mode == ExerciseCellFragment.MODE_ALL) {
            getViewState().showProgress();
            d.add(apiHelper.getExercises(mToken, mID)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onSuccess, throwable -> {
                        throwable.printStackTrace();
                        getViewState().hideProgress();
                        getViewState().showToastyMessage("Error, try later");
                    }));
        }
        else {
            getViewState().showProgress();
            d.add(apiHelper.getSuggestetExercisesForPatient(mToken, mID, patientID)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(responseBodyResponse -> {
                        getViewState().hideProgress();
                        HashSet<String> category = new HashSet<>();
                        if (responseBodyResponse.isSuccessful()){
                            Log.d("Exercice", "mode: Suggested");
                            List<ExerciseModel> models = new ArrayList<>();
                            for (Exercise i: Objects.requireNonNull(responseBodyResponse.body()).getData().getExercises()) {
                                models.add(new ExerciseModel(i));
                                category.add(i.getCategory());
                            }
                            if (models.size() == 0)
                                getViewState().showContentNotFound();
                            else getViewState().hideContentNotFound();

                            List<ExerciseModel> result = new LinkedList<>();
                            for (String i: category) {
                                ExerciseModel header = new ExerciseModel();
                                header.setType(ExerciseModel.TYPE_HEADER);
                                header.setCategory(i);
                                result.add(header);
                                for (ExerciseModel j: models) {
                                    if (j.getCategory().equals(i))
                                        result.add(j);
                                }
                            }
                            getViewState().loadExerciseModels(result);
                        }
                        else Log.d("Exercise", "onError:" + responseBodyResponse.errorBody().string());
                    }, throwable -> {
                        throwable.printStackTrace();
                        getViewState().hideProgress();
                        getViewState().showToastyMessage("Error, try later");
                    }));
        }
    }

    private void onSuccess(Response<ResponseExercise> responseBodyResponse) {
        getViewState().hideProgress();
        if (responseBodyResponse.isSuccessful()) {
            Log.d("Exercise", "mode: All");
            HashSet<String> category = new HashSet<>();
            List<ExerciseModel> models = new ArrayList<>();
            for (Exercise i: Objects.requireNonNull(responseBodyResponse.body()).getData().getExercises()) {
                category.add(i.getCategory());
                models.add(new ExerciseModel(i));
            }
            if (models.size() == 0)
                getViewState().showContentNotFound();
            else getViewState().hideContentNotFound();

            //todo categoryFormatting
            List<ExerciseModel> result = new LinkedList<>();
            for (String i: category) {
                ExerciseModel header = new ExerciseModel();
                header.setType(ExerciseModel.TYPE_HEADER);
                header.setCategory(i);
                result.add(header);
                for (ExerciseModel j: models) {
                    if (j.getCategory().equals(i))
                        result.add(j);
                }
            }

            getViewState().loadExerciseModels(result);
        }
        else {
            try {
                Log.d("Exercise", "Token id: " + mToken + " " + mID);
                Log.d("Exercise", "onError: " + Objects.requireNonNull(responseBodyResponse.errorBody()).string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadMoreEllements(){

    }

    public void onItemClicked(ExerciseModel exerciseModel){
        getViewState().startVideoViewActivity(exerciseModel);
    }


    public void setMode(int mMode) {
        this.mode = mMode;
    }

    public void onDelClicked(ExerciseModel model) {
        this.model = model;
        getViewState().showConfirmDialog("Удаление", "Вы уверены, что хотите удалить данное упражнение у пациента?");
    }

    public void onAddClicked(ExerciseModel model) {
        this.model = model;
        getViewState().showConfirmDialog("Добавление", "Вы уверены, что хотите добавить данное упражнение пациенту?");
    }

    public void onConfirmedAction() {
        if (mode == ExerciseCellFragment.MODE_SUGGESTED) {
            getViewState().showToastyMessage("Упражнение удалено");
            d.add(
                    apiHelper.removeExerciceFromPatient(mToken, mID, model.getId(), patientID)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(responseBodyResponse -> {
                                if (responseBodyResponse.isSuccessful()){
                                    Log.d("onConfirmedAction: ",responseBodyResponse.body().string());
                                }
                                else {
                                    Log.d("onConfirmedAction: ",responseBodyResponse.errorBody().string());
                                }
                            }, Throwable::printStackTrace)

            );
        }
        else {
            getViewState().showToastyMessage("Упражнение добавлено");
            d.add(
                    apiHelper.addExerciceToPatient(mToken, mID, model.getId(), patientID)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(responseBodyResponse -> {
                                if (responseBodyResponse.isSuccessful()){
                                    Log.d("onConfirmedAction: ",responseBodyResponse.body().string());
                                }
                                else {
                                    Log.d("onConfirmedAction: ",responseBodyResponse.errorBody().string());
                                }
                            }, Throwable::printStackTrace)
            );
        }
    }

    public void onRefresh() {
        provideData();
    }
}
