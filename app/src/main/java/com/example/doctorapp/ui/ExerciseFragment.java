package com.example.doctorapp.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.example.doctorapp.IOnLoadMore;
import com.example.doctorapp.R;
import com.example.doctorapp.adapters.ExerciseAdapter;
import com.example.doctorapp.adapters.ExercisePagerAdapter;
import com.example.doctorapp.presentation.presenter.ExercisePresenter;
import com.example.doctorapp.presentation.view.IExerciseView;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ExerciseFragment extends MvpAppCompatFragment
        implements IOnLoadMore, IExerciseView, ExerciseAdapter.AdapterInteraction {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ExercisePagerAdapter adapter;
    private TabItem tabAll;
    private TabItem tabSuggeted;
    @BindView(R.id.constraintLayout4)
    ConstraintLayout toolbar;
    @BindView(R.id.exercise_del_btn)
    ImageView delBtn;
    @BindView(R.id.text_view_exercise)
    TextView header;

    @InjectPresenter
    ExercisePresenter presenter;


    public ExerciseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_exercise, container, false);
        ButterKnife.bind(this, v);
        tabLayout = v.findViewById(R.id.tab_layout);
        viewPager = v.findViewById(R.id.viewPager);
        tabAll = v.findViewById(R.id.all_exercises);
        tabSuggeted = v.findViewById(R.id.suggested_exercises);

        init();
        return v;
    }

    private void init(){
        adapter = new ExercisePagerAdapter(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), tabLayout.getTabCount());
        adapter.setmListener(this);
        viewPager.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    @Override
    public void loadMore(int currentCount) {

    }

    @Override
    public void showToastyMessage(String message) {
        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDelModeActive() {
        toolbar.setBackground(getResources().getDrawable(R.drawable.toolbar_doctor_gradient));
        delBtn.setVisibility(View.VISIBLE);
        header.setTextColor(getResources().getColor(R.color.color_white));

    }

    @Override
    public void onDelModeDisabled() {
        toolbar.setBackgroundColor(getResources().getColor(R.color.color_white));
        delBtn.setVisibility(View.GONE);
        header.setTextColor(getResources().getColor(R.color.color_dark_blue));
    }

    @Override
    public void onAddModeActive() {
    }

    @Override
    public void onAddModeDisabled() {
    }
}
