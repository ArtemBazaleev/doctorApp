package com.example.doctorapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.example.doctorapp.App;
import com.example.doctorapp.R;
import com.example.doctorapp.adapters.ExerciseAdapter;
import com.example.doctorapp.model.ResultModel;
import com.example.doctorapp.adapters.ResultsAdapter;
import com.example.doctorapp.presentation.presenter.ResultsFragmentPresenter;
import com.example.doctorapp.presentation.view.IResultsFragmentView;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ResultsFragment extends MvpAppCompatFragment
        implements ResultsAdapter.IOnResultClicked,
        IResultsFragmentView{

    public static final String USER_ID = "UserID";

    private String userID;

    @BindView(R.id.recycler_results) RecyclerView recyclerView;
    @BindView(R.id.not_found_content_results) ConstraintLayout emptyContent;
    @BindView(R.id.progressBar2) ProgressBar progressBar;
    @BindView(R.id.swipeRefreshResults) SwipeRefreshLayout swipeRefreshLayout;
    private ExerciseAdapter.AdapterInteraction mLister;

    @InjectPresenter
    ResultsFragmentPresenter presenter;

    @ProvidePresenter
    ResultsFragmentPresenter providePresenter(){
        App app = (App) Objects.requireNonNull(getActivity()).getApplication();
        return new ResultsFragmentPresenter(app.getmUserID(),app.getmToken());
    }

    public ResultsFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_result, container, false);
        ButterKnife.bind(this,v);
        Log.d("ResultsFragment", userID);
        swipeRefreshLayout.setOnRefreshListener(presenter::onRefresh);
        presenter.setUserID(userID);
        presenter.onCreateView();
        return v;
    }

    @Override
    public void onResult(ResultModel model) {
        presenter.onResultClicked(model);
    }


    public static ResultsFragment newInstance(String userID){
        ResultsFragment fragment = new ResultsFragment();
        Bundle args = new Bundle();
        args.putString(USER_ID, userID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            userID = getArguments().getString(USER_ID);
    }

    //MVP
    @Override
    public void showTastyMessage(String message) {
        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void loadResults(List<ResultModel> data) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL,false));
        ResultsAdapter adapter = new ResultsAdapter(getContext(), data);
        adapter.setmListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void startActivityResultView(ResultModel resultModel) {
        if (resultModel.getType() == ResultModel.TYPE_BACKBONE)
        {
            Intent i = new Intent(getContext(), BackBoneViewActivity.class);
            i.putExtra(BackBoneViewActivity.BACKBONE_ARRAY, resultModel.getBackBoneImage());
            startActivity(i);
        }
        else  {
            Intent i = new Intent(getContext(), ResultViewActivity.class);
            i.putExtra(ResultViewActivity.IMAGE_PARAM, resultModel.getUrl());
            startActivity(i);
        }
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
    public void showErrorNoResults() {
        emptyContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideErrorNoResults() {
        emptyContent.setVisibility(View.GONE);
    }
    //MVP
}
