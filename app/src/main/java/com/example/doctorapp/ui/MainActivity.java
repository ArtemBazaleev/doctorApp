package com.example.doctorapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.example.doctorapp.R;
import com.example.doctorapp.presentation.presenter.MainActivityPresenter;
import com.example.doctorapp.presentation.view.IMainActivityView;


// TODO: 5/30/2019 add fragments
public class MainActivity extends MvpAppCompatActivity implements IMainActivityView {
    public static final int RESULT_CHAT = 6458;
    private Fragment profileFragment;
    private Fragment chatMemberFragment;
    private Fragment activeFragment;
    private FragmentManager fm;
    private BottomNavigationView navigation;

    @InjectPresenter
    MainActivityPresenter presenter;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_chat:
                    presenter.onEntryToTheDoctorClicked();
                    return true;
                case R.id.navigation_profile:
                    presenter.onProfileClicked();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigation =  findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        presenter.onCreate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==RESULT_CHAT) {
            presenter.onActivityResult();
        }
    }

    @Override
    public void showToastyMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setEntryToTheDoctorFragment() {
        fm.beginTransaction()
                .hide(activeFragment)
                .show(chatMemberFragment)
                .commit();
        activeFragment  = chatMemberFragment;
    }

    @Override
    public void setExerciseFragment() {

    }

    @Override
    public void setResultsFragment() {

    }

    @Override
    public void setProfileFragment() {
        fm.beginTransaction()
                .hide(activeFragment)
                .show(profileFragment)
                .commit();
        activeFragment  = profileFragment;
    }

    @Override
    public void startChatActivity() {
        startActivityForResult(new Intent(MainActivity.this, ChatActivity.class),RESULT_CHAT);
    }

    @Override
    public void initFragments() {
        fm = getSupportFragmentManager();
        chatMemberFragment = new ChatMembersFragment();
        profileFragment = new ProfileFragment();
        fm.beginTransaction()
                .add(R.id.fragment_container, chatMemberFragment,"1")
                .commit();
        fm.beginTransaction()
                .add(R.id.fragment_container, profileFragment, "2")
                .hide(profileFragment)
                .commit();
        activeFragment = chatMemberFragment;
    }

    @Override
    public void setEntryToTheDoctorSelected() {
    }
}
