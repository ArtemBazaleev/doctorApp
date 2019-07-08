package com.example.doctorapp.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.doctorapp.R;
import com.example.doctorapp.adapters.ExerciseAdapter;

import butterknife.BindView;

public class ExerciseActivity extends AppCompatActivity  {

    public static final String PATIENT_ID = "patientID";
    private String patientID = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras()!= null){
            patientID = getIntent().getExtras().getString(PATIENT_ID, "");
            Log.d("ExerciseActivity", "patientID: " + patientID);
        }

        setContentView(R.layout.activity_exercise);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_exercise, new ExerciseFragment())
                .commit();
    }
}
