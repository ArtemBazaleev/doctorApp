package com.example.doctorapp.presentation.presenter;

import android.util.Log;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.example.doctorapp.model.PatientModel;
import com.example.doctorapp.networking.helpers.DataHelper;
import com.example.doctorapp.networking.responses.patients.Patient;
import com.example.doctorapp.presentation.view.ChatMembersFragmentView;

import org.json.JSONException;
import org.json.JSONObject;

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
    private List<Patient>  patientList;
    private JSONObject authOK;
    private boolean adapterInitialized = false;
    private boolean unreadShown = false;

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
                            patientList = Objects.requireNonNull(responseBodyResponse.body()).getData().getPatients();
                            if (patientList!=null)
                                for (Patient i: patientList) {
                                    PatientModel patientModel = new PatientModel();
                                    patientModel.setName(i.getName());
                                    patientModel.setSecondName(i.getSurname());
                                    patientModel.setPatientID(i.getId());
                                    patientModel.setDescription(i.getConclusion());
                                    patientModel.setDialogID(i.getDialogId());
                                    patients.add(patientModel);
                                }
                            if (patients.size() == 0)
                                getViewState().showPatientsNotFound();
                            else {
                                getViewState().setPatients(patients);
                                adapterInitialized = true;
                                if (unreadShown)
                                    setUnreadMessages();
                            }
                        }else{
                            getViewState().hideProgress();
                            getViewState().startLoginActivityAndClearStack();
                            Log.d("onViewCreated: ", Objects.requireNonNull(responseBodyResponse.errorBody()).string());
                        }
                },throwable -> {
                    throwable.printStackTrace();
                    getViewState().showToastyMessage("Error, try later");
                })
        );

    }

    public void onPatientClicked(PatientModel model) {
        getViewState().setUnreadMessages(model.getDialogID(), 0);
        //getViewState().startChatActivity(model.getPatientID(), model.getName() + " " + model.getSecondName());
        getViewState().startChatActivity(model);
    }

    public void onMessageReceived(JSONObject data) {
        Log.d(TAG, "newMessage: " + data.toString());
        try {
            getViewState().increaseCounterForPatient(data.getString("chatId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void authOK(JSONObject data) {
        authOK = data;
        if (adapterInitialized)
            setUnreadMessages();
    }

    private void setUnreadMessages(){
        if (authOK==null)
            return;

        try {
            for (int i = 0; i <authOK.getJSONArray("dialogs").length() ; i++) {
                if (authOK.getJSONArray("dialogs").getJSONObject(i).has("unreadMessages")){
                    getViewState().setUnreadMessages(
                            authOK.getJSONArray("dialogs").getJSONObject(i).getString("id"),
                            authOK.getJSONArray("dialogs").getJSONObject(i).getLong("unreadMessages")
                    );
                }
            }
            unreadShown = true;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updateUnreadMessages(String dialogID, int i) {
        getViewState().setUnreadMessages(dialogID, i);
    }
}
