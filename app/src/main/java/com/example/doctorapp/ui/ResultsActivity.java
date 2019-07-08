package com.example.doctorapp.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.doctorapp.R;

public class ResultsActivity extends AppCompatActivity {
    public static final String USER_ID = "userID";
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        if (getIntent().getExtras() != null){
            userID = getIntent().getExtras().getString(USER_ID,"");
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_results, ResultsFragment.newInstance(userID))
                .commit();
    }
}
