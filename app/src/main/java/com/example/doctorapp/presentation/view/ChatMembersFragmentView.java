package com.example.doctorapp.presentation.view;

import com.arellomobile.mvp.MvpView;
import com.example.doctorapp.model.PatientModel;

import java.util.List;

public interface ChatMembersFragmentView extends MvpView {
    void showToastyMessage(String message);
    void startChatActivity(String patientID);
    void setPatients(List<PatientModel> patients);
    void showProgress();
    void hideProgress();

    void showPatientsNotFound();
    void hidePatientsNotFound();
}
