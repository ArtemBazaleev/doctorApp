package com.example.doctorapp.presentation.view;

import com.arellomobile.mvp.MvpView;

public interface IMainActivityView extends MvpView {
    void showToastyMessage(String message);
    void setEntryToTheDoctorFragment();
    void setExerciseFragment();
    void setResultsFragment();
    void setProfileFragment();
    void startChatActivity();
    void initFragments();
    void setEntryToTheDoctorSelected();
}
