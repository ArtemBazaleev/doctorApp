package com.example.doctorapp.presentation.view;

import com.arellomobile.mvp.MvpView;
import com.example.doctorapp.model.ProfileModel;

public interface ProfileFragmentView extends MvpView {
    void showToastyMessage(String message);

    void updateFields(ProfileModel model);

    void showLoadingIndicator();

    void hideLoadingIndicator();

    void setEnabledSubmitBtn(Boolean enabledSubmitBtn);
}
