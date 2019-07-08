package com.example.doctorapp.presentation.view;

import com.arellomobile.mvp.MvpView;
import com.example.doctorapp.model.ResultModel;

import java.util.List;

public interface IResultsFragmentView extends MvpView {

    void showTastyMessage(String message);

    void loadResults(List<ResultModel> data);

    void startActivityResultView(ResultModel model);

    void showProgress();

    void hideProgress();

    void showErrorNoResults();

    void hideErrorNoResults();

}
