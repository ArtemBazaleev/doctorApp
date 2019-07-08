package com.example.doctorapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.example.doctorapp.App;
import com.example.doctorapp.R;
import com.example.doctorapp.presentation.presenter.LoginPresenter;
import com.example.doctorapp.presentation.view.ILoginView;
import com.example.doctorapp.utils.SecuredSharedPreferences;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends MvpAppCompatActivity implements ILoginView {

    @BindView(R.id.login_act_enter)
    ImageButton enter;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.login)
    EditText login;
    @BindView(R.id.editText3)
    EditText password;

    @InjectPresenter
    LoginPresenter presenter;

    @ProvidePresenter
    LoginPresenter providePresenter(){
        return new LoginPresenter(new SecuredSharedPreferences(getApplicationContext()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        init();

    }

    private void init(){
        enter.setOnClickListener(l->presenter.onBtnLoginClicked());
        login.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                presenter.onLoginChanged(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                presenter.onPasswordChanged(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void startMainActivity(String token, String id) {
        App app = (App) getApplicationContext();
        app.setmToken(token);
        app.setmUserID(id);
        Log.d("startMainActivity: ", "token="+token+"id="+id);
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void showToastyMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setEnabledSubmitBtn(Boolean enabled) {
        enter.setEnabled(enabled);
    }

    @Override
    public void showLoadingIndicator() {
        progressBar.setVisibility(View.VISIBLE);
        enter.setImageResource(0);
    }

    @Override
    public void hideLoadingIndicator() {
        progressBar.setVisibility(View.GONE);
        enter.setImageResource(R.drawable.ic_arrow_forward_black_24dp);
    }

}
