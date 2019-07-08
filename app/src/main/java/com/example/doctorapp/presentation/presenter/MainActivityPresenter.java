package com.example.doctorapp.presentation.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.example.doctorapp.presentation.view.IMainActivityView;

@InjectViewState
public class MainActivityPresenter extends MvpPresenter<IMainActivityView> {

    public void onEntryToTheDoctorClicked(){
        getViewState().setEntryToTheDoctorFragment();
    }

    public void onChatClicked(){
        getViewState().startChatActivity();
    }

    public void onExerciseClicked(){
        getViewState().setExerciseFragment();
    }

    public void onResultsClicked(){
        getViewState().setResultsFragment();
    }

    public void onProfileClicked(){
        getViewState().setProfileFragment();
    }

    public void onActivityResult(){
        getViewState().setEntryToTheDoctorSelected();
    }

    public void onCreate(){
        getViewState().initFragments();
    }

}
