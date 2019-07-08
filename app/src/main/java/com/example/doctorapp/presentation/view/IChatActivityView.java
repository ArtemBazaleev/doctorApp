package com.example.doctorapp.presentation.view;

import com.arellomobile.mvp.MvpView;
import com.example.doctorapp.model.BaseMessage;

import java.util.List;

public interface IChatActivityView extends MvpView {
    void hideAll();
    void addMessage(BaseMessage message);
    void initRecycler(List<BaseMessage> messages);

    void startExerciseActivity(String patientID);

    void startResultsActivity(String patientID);
}
