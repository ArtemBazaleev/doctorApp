package com.example.doctorapp.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.example.doctorapp.App;
import com.example.doctorapp.R;
import com.example.doctorapp.adapters.PatientAdapter;
import com.example.doctorapp.model.PatientModel;
import com.example.doctorapp.presentation.presenter.ChatMemberFragmentPresenter;
import com.example.doctorapp.presentation.view.ChatMembersFragmentView;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ChatMembersFragment extends MvpAppCompatFragment
        implements PatientAdapter.ItemInteraction, ChatMembersFragmentView {
    @BindView(R.id.recycler_patient) RecyclerView recyclerView;
    @BindView(R.id.image_del_btn_chat) ImageView del;
    @BindView(R.id.progressBar3) ProgressBar progressBar;
    @BindView(R.id.not_found_content_patients) ConstraintLayout patientsNotFoundContent;
    private PatientAdapter adapter;

    @InjectPresenter
    ChatMemberFragmentPresenter presenter;
    private Socket mSocket;
    private App app;

    @ProvidePresenter
    ChatMemberFragmentPresenter providePresenter(){
        App app = (App) Objects.requireNonNull(getActivity()).getApplication();
        return new ChatMemberFragmentPresenter(app.getmToken(), app.getmUserID());
    }

    public ChatMembersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chat_members, container, false);
        ButterKnife.bind(this,v);


        del.setOnClickListener(l->{
            adapter.desableDelMode();
            adapter.deleteSelected();
            del.setVisibility(View.GONE);
        });

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.onViewCreated();
    }

    @Override
    public void onItemClicked(PatientModel model) {
        presenter.onPatientClicked(model);
    }

    @Override
    public void onDelModeActive() {
        del.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDelModeDisabled() {
        del.setVisibility(View.GONE);
    }

    @Override
    public void showToastyMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void startChatActivity(String patientID) {
        Intent i = new Intent(getContext(), ChatActivity.class);
        i.putExtra(ChatActivity.CHAT_PATIENT_ID, patientID);
        Objects.requireNonNull(getActivity()).startActivity(i);
    }

    @Override
    public void setPatients(List<PatientModel> patients) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL,false));
        List<PatientModel> data = new ArrayList<>();
        PatientAdapter adapter = new PatientAdapter(getContext(), patients);
        adapter.setmListener(this);
        recyclerView.setAdapter(adapter);
        initSocket(); //получаем кол-во непрочитанных сообщений
    }

    private void initSocket() {
        app = (App) Objects.requireNonNull(getActivity()).getApplication();
        mSocket = app.getmSocket();
        if (mSocket.connected())
            Toast.makeText(getContext(), "Connected!!", Toast.LENGTH_SHORT).show();
        else mSocket.connect();

        JSONObject data = new JSONObject();
        try {
            data.put("userId", app.getmUserID());
            data.put("token", app.getmToken());
            //Log.d("", data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("auth", data);
        mSocket.on("authOk",authOk);
    }

    @Override
    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showPatientsNotFound() {
        patientsNotFoundContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void hidePatientsNotFound() {
        patientsNotFoundContent.setVisibility(View.GONE);
    }



    private Emitter.Listener authOk = args -> Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
        try {
            JSONObject data = (JSONObject) args[0];
            Log.d("authOk", data.toString());
            for (int i = 0; i <data.getJSONArray("dialogs").length() ; i++) {
                for (int j = 0; j <data.getJSONArray("dialogs").getJSONObject(i).getJSONArray("participants").length() ; j++) {
                    JSONObject participant = data.getJSONArray("dialogs").getJSONObject(i).getJSONArray("participants").getJSONObject(j);
                    String id  = participant.getString("id");
                    Log.d("AuthOK", "id = " + id + " unread = " + data.getJSONArray("dialogs").getJSONObject(i).getLong("unreadMessages"));
                    adapter.setCounterForPatient(id, data.getJSONArray("dialogs").getJSONObject(i).getLong("unreadMessages"));
                }

            }
            Log.d("", data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });
}
