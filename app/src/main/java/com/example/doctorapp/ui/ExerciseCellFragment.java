package com.example.doctorapp.ui;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.example.doctorapp.App;
import com.example.doctorapp.IOnLoadMore;
import com.example.doctorapp.PlayerActivity;
import com.example.doctorapp.R;
import com.example.doctorapp.adapters.ExerciseAdapter;
import com.example.doctorapp.model.ExerciseModel;
import com.example.doctorapp.presentation.presenter.ExerciseCellFragmentPresenter;
import com.example.doctorapp.presentation.view.IExerciseCellFragment;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseCellFragment extends MvpAppCompatFragment
        implements IOnLoadMore, IExerciseCellFragment, ExerciseAdapter.AdapterAction{


    private static final String ARG_MODE = "param1";
    public static final int MODE_ALL  = 0;
    public static  final int MODE_SUGGESTED = 1;

    private int mMode;

    private ExerciseAdapter adapter;
    private RecyclerView recyclerView;
    boolean isLoading = false;

    @BindView(R.id.not_found_content_exercise) ConstraintLayout constraintLayout;
    @BindView(R.id.progressBar4) ProgressBar progressBar;
    @BindView(R.id.swipeRefreshExercise) SwipeRefreshLayout swipeRefreshLayout;

    @InjectPresenter
    ExerciseCellFragmentPresenter presenter;
    @ProvidePresenter
    ExerciseCellFragmentPresenter providePresenter(){
        App app = (App) Objects.requireNonNull(getActivity()).getApplication();
        return new ExerciseCellFragmentPresenter(app.getmToken(), app.getmUserID(), app.getPatienID());
    }

    private ExerciseAdapter.AdapterInteraction mLister;

    public ExerciseCellFragment() {
    }
    public void setmLister(ExerciseAdapter.AdapterInteraction l){
        this.mLister = l;
    }

    public static ExerciseCellFragment newInstance(int param) {
        ExerciseCellFragment fragment = new ExerciseCellFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MODE, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            mMode = getArguments().getInt(ARG_MODE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_exercise_cell, container, false);
        ButterKnife.bind(this, v);
        recyclerView = v.findViewById(R.id.recycler_exercise_cell);
        presenter.setMode(mMode);
        swipeRefreshLayout.setOnRefreshListener(presenter::onRefresh);
        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.onViewCreated();
    }

    @Override
    public void loadMore(int currentCount) {
        presenter.onItemClicked(new ExerciseModel());
    }
    //MVP
    @Override
    public void showToastyMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void loadExerciseModels(List<ExerciseModel> data) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL,false));
        adapter = new ExerciseAdapter(getContext(), data,this);
        //adapter.setListner(this);
        if (mMode == MODE_ALL)
            adapter.setAdapterMode(ExerciseAdapter.MODE_ADD);
        else adapter.setAdapterMode(ExerciseAdapter.MODE_DEL);
        adapter.setListner(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void addExerciseModels(List<ExerciseModel> data) {
        adapter.addData(data);
    }

    @Override
    public void startVideoViewActivity(ExerciseModel exerciseModel) {
//        Intent i = new Intent(getContext(),VideoViewActivity.class);
//        i.putExtra(VideoViewActivity.VIDEO, exerciseModel.getUrlVideo());
//        Objects.requireNonNull(getActivity()).startActivity(i);
        startActivity(PlayerActivity.getVideoPlayerIntent(Objects.requireNonNull(getContext()),
                exerciseModel.getUrlVideo(),
                exerciseModel.getName(), R.drawable.ic_play_arrow_black_24dp));    }

    @Override
    public void showContentNotFound() {
        constraintLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideContentNotFound() {
        constraintLayout.setVisibility(View.GONE);
    }

    @Override
    public void showProgress() {
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideProgress() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showConfirmDialog(String header, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle(header)
                .setMessage(body)
                .setIcon(R.mipmap.ic_launcher)
                .setCancelable(false)
                .setPositiveButton("да",
                        (dialog, id)-> presenter.onConfirmedAction())
                .setNegativeButton("нет",
                        (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }

    //MVP
    @Override
    public void onDelClicked(ExerciseModel model) {
        presenter.onDelClicked(model);
    }

    @Override
    public void onAddClicked(ExerciseModel model) {
        presenter.onAddClicked(model);
    }

    @Override
    public void onClicked(ExerciseModel model) {
        presenter.onItemClicked(model);
    }
}
