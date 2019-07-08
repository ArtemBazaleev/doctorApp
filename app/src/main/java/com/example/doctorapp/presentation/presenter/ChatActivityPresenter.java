package com.example.doctorapp.presentation.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.example.doctorapp.presentation.view.IChatActivityView;

@InjectViewState
public class ChatActivityPresenter extends MvpPresenter<IChatActivityView> {
    private String token = "";
    private String userID = "";
    private String chatID = "";
    private String patientID = "";

    public ChatActivityPresenter(String token, String userID) {
        this.token = token;
        this.userID = userID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public void onExerciseClicked(){
        getViewState().startExerciseActivity(patientID);
    }

    public void onResultsClicked(){
        getViewState().startResultsActivity(patientID);
    }
}
