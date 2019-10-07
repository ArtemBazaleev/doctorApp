package com.example.doctorapp.presentation.presenter;

import android.util.Log;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.example.doctorapp.model.ProfileModel;
import com.example.doctorapp.networking.helpers.DataHelper;
import com.example.doctorapp.presentation.view.ProfileFragmentView;

import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@InjectViewState
public class ProfileFragmentPresenter extends MvpPresenter<ProfileFragmentView> {
    private String token;
    private String id;
    private String name = "";
    private String surname = "";
    private String position = "";
    private DataHelper dataHelper;

    public ProfileFragmentPresenter(String token, String id) {
        this.token = token;
        this.id = id;
        dataHelper = new DataHelper();
    }

    public void onNameChanged(String name){
        this.name = name;
    }

    public void onSurnameChanged(String surname){
        this.surname = surname;
    }

    public void onPositionChanged(String position){
        this.position = position;
    }

    public void onSaveBtnClicked(){
//        if (name.equals("") || surname.equals("") || position.equals("")){
//            getViewState().showToastyMessage("Заполните все поля");
//            return;
//        }
        setProfile();
    }

    public void onViewCreated(){
        getProfile();
    }

    private void setProfile() {
        getViewState().showLoadingIndicator();
        getViewState().setEnabledSubmitBtn(false);
        Disposable d = dataHelper.setDoctorProfile(token, id, new ProfileModel(name,surname,position))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseBodyResponse -> {
                    getViewState().setEnabledSubmitBtn(true);
                    getViewState().hideLoadingIndicator();
                    if (responseBodyResponse.isSuccessful()){
                        getViewState().showToastyMessage("Информация сохранена");
                    }
                    else{
                        getViewState().showToastyMessage(responseBodyResponse.errorBody().string());
                    }
                },throwable -> {
                    getViewState().setEnabledSubmitBtn(true);
                    getViewState().hideLoadingIndicator();
                    throwable.printStackTrace();
                    getViewState().showToastyMessage("Error, try later");
                });
    }

    private void getProfile(){
        getViewState().showLoadingIndicator();
        getViewState().setEnabledSubmitBtn(false);
        Disposable d =  dataHelper.getDoctorProfile(token, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseBodyResponse -> {
                    getViewState().setEnabledSubmitBtn(true);
                    getViewState().hideLoadingIndicator();
                    if (responseBodyResponse.isSuccessful()){
                        getViewState().updateFields(new ProfileModel(
                                Objects.requireNonNull(responseBodyResponse.body()).getData().getTherapist().getName(),
                                responseBodyResponse.body().getData().getTherapist().getSurname(),
                                responseBodyResponse.body().getData().getTherapist().getTherapistPosition()
                        ));
                    }
                    else{
                        Log.d("getProfile: ", responseBodyResponse.errorBody().string());
                    }
                },throwable -> {
                    getViewState().setEnabledSubmitBtn(true);
                    getViewState().hideLoadingIndicator();
                    throwable.printStackTrace();
                    getViewState().showToastyMessage("Error, try later");
                });
    }
}
