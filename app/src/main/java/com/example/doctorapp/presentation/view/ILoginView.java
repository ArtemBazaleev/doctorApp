package com.example.doctorapp.presentation.view;

import com.arellomobile.mvp.MvpView;

public interface ILoginView extends MvpView {
    void startMainActivity(String token, String id);
    void showToastyMessage(String message);
    void setEnabledSubmitBtn(Boolean enabled);
    void showLoadingIndicator();
    void hideLoadingIndicator();
}
