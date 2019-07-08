package com.example.doctorapp.presentation.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.example.doctorapp.networking.ResponseSignIn;
import com.example.doctorapp.networking.SignInHelper;
import com.example.doctorapp.presentation.view.ILoginView;
import com.example.doctorapp.utils.SecuredSharedPreferences;

import org.json.JSONObject;

import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

@InjectViewState
public class LoginPresenter extends MvpPresenter<ILoginView> {

    private String login = "therapist@gmail.com";
    private String password = "12345";
    private SignInHelper signInHelper = new SignInHelper();
    private CompositeDisposable disposables = new CompositeDisposable();
    private SecuredSharedPreferences preferences;

    public LoginPresenter(SecuredSharedPreferences preferences){
        this.preferences = preferences;
    }

    public void onPasswordChanged(String password){
        this.password = password;
    }

    public void onLoginChanged(String login){
        this.login = login;
    }

    public void onBtnLoginClicked(){
        if (login.equals("") || password.equals("")) {
            getViewState().showToastyMessage("Заполните все поля");
        }
        getViewState().setEnabledSubmitBtn(false);
        getViewState().showLoadingIndicator();
        Disposable d = signInHelper.signIn(login,password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSuccess, throwable -> {
                    getViewState().setEnabledSubmitBtn(true);
                    getViewState().hideLoadingIndicator();
                    throwable.printStackTrace();
                    getViewState().showToastyMessage("Error, try later");
                });
        disposables.add(d);
    }

    private void onSuccess(Response<ResponseSignIn> response) {
        getViewState().setEnabledSubmitBtn(true);
        getViewState().hideLoadingIndicator();
        if (response.isSuccessful()) {
            if (!Objects.requireNonNull(response.body()).getData().getRole().equals("therapist")){
                getViewState().showToastyMessage("Доступно только для врача");
                return;
            }
            preferences.setToken(Objects.requireNonNull(response.body()).getData().getToken());
            preferences.setUserID(Objects.requireNonNull(response.body().getData().getId()));
            getViewState().startMainActivity(
                    Objects.requireNonNull(response.body()).getData().getToken(),
                    Objects.requireNonNull(response.body()).getData().getId()
            );
        }
        else {
            try {
                JSONObject jObjError = new JSONObject(Objects.requireNonNull(response.errorBody()).string());
                getViewState().showToastyMessage(jObjError.getJSONObject("data").getString("error"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
