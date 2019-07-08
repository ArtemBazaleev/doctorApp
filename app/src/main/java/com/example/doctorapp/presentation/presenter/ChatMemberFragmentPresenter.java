package com.example.doctorapp.presentation.presenter;

import android.util.Log;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.example.doctorapp.model.PatientModel;
import com.example.doctorapp.networking.helpers.DataHelper;
import com.example.doctorapp.networking.responses.patients.Patient;
import com.example.doctorapp.presentation.view.ChatMembersFragmentView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

@InjectViewState
public class ChatMemberFragmentPresenter extends MvpPresenter<ChatMembersFragmentView> {
    private String TAG = "ChatMemberFragmentPresenter";
    private String token = "";
    private String userID = "";
    private DataHelper dataHelper;
    private CompositeDisposable disposables;

    public ChatMemberFragmentPresenter(String token, String userID) {
        this.token = token;
        this.userID = userID;
        dataHelper = new DataHelper();
        disposables = new CompositeDisposable();
    }

    public void onViewCreated(){
        getViewState().showProgress();
        disposables.add(
                dataHelper.getPatients(token, userID, 0, 100)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseBodyResponse -> {
                        getViewState().hideProgress();
                        if (responseBodyResponse.isSuccessful()){
                            List<PatientModel> patients = new ArrayList<>();
                            for (Patient i:responseBodyResponse.body().getData().getPatients()) {
                                PatientModel patientModel = new PatientModel();
                                patientModel.setName(i.getName());
                                patientModel.setSecondName(i.getSurname());
                                patientModel.setPatientID(i.getId());
                                patientModel.setDescription(i.getConclusion());
                                patients.add(patientModel);
                            }
                            if (patients.size() == 0)
                                getViewState().showPatientsNotFound();
                            else getViewState().setPatients(patients);
                        }else{
                            getViewState().hideProgress();
                            Log.d("onViewCreated: ", responseBodyResponse.errorBody().string());
                        }
                },throwable -> {
                    throwable.printStackTrace();
                    getViewState().showToastyMessage("Error, try later");
                })
        );

    }

    public void onPatientClicked(PatientModel model) {
        getViewState().startChatActivity(model.getPatientID());
    }
}
