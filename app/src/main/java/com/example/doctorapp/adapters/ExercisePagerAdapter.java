package com.example.doctorapp.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.doctorapp.ui.ExerciseCellFragment;

public class ExercisePagerAdapter extends FragmentStatePagerAdapter
        implements ExerciseAdapter.AdapterInteraction {

    private int tabsCount;
    private ExerciseAdapter.AdapterInteraction mListener;

    public void setmListener(ExerciseAdapter.AdapterInteraction mListener) {
        this.mListener = mListener;
    }

    public ExercisePagerAdapter(FragmentManager fm, int count) {
        super(fm);
        tabsCount = count;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                ExerciseCellFragment fragment = ExerciseCellFragment.newInstance(ExerciseCellFragment.MODE_SUGGESTED);
                fragment.setmLister(this);
                return fragment;
            case 1:
                ExerciseCellFragment fragment1 = ExerciseCellFragment.newInstance(ExerciseCellFragment.MODE_ALL);
                fragment1.setmLister(this);
                return fragment1;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabsCount;
    }

    @Override
    public void onDelModeActive() {
        mListener.onDelModeActive();
    }

    @Override
    public void onDelModeDisabled() {
        mListener.onDelModeDisabled();
    }

    @Override
    public void onAddModeActive() {
        mListener.onAddModeActive();
    }

    @Override
    public void onAddModeDisabled() {
        mListener.onAddModeDisabled();
    }
}
