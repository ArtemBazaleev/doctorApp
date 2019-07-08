package com.example.doctorapp.presentation.view;

import com.arellomobile.mvp.MvpView;
import com.example.doctorapp.model.ExerciseModel;

import java.util.List;

public interface IExerciseCellFragment extends MvpView {
    void showToastyMessage(String message);

    void loadExerciseModels(List<ExerciseModel> data);

    void addExerciseModels(List<ExerciseModel> data);

    void startVideoViewActivity(ExerciseModel exerciseModel);

    void showContentNotFound();

    void hideContentNotFound();

    void showProgress();

    void hideProgress();

    void showConfirmDialog(String header, String body);
}
