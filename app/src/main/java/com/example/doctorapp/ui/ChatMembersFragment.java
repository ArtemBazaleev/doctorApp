package com.example.doctorapp.ui;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.constraint.Constraints.TAG;


public class ChatMembersFragment extends MvpAppCompatFragment
        implements PatientAdapter.ItemInteraction, ChatMembersFragmentView {
    private static final String CHANNEL_ID = "DoctorApp";
    @BindView(R.id.recycler_patient) RecyclerView recyclerView;
    @BindView(R.id.image_del_btn_chat) ImageView del;
    @BindView(R.id.progressBar3) ProgressBar progressBar;
    @BindView(R.id.not_found_content_patients) ConstraintLayout patientsNotFoundContent;
    private PatientAdapter adapter;

    @InjectPresenter
    ChatMemberFragmentPresenter presenter;
    private Socket mSocket;
    private App app;
    private int notificationId = 0;

    @ProvidePresenter
    ChatMemberFragmentPresenter providePresenter(){
        App app = (App) Objects.requireNonNull(getActivity()).getApplication();
        return new ChatMemberFragmentPresenter(app.getmToken(), app.getmUserID());
    }

    public ChatMembersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
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
        adapter = new PatientAdapter(getContext(), patients);
        adapter.setmListener(this);
        recyclerView.setAdapter(adapter);
        //initSocket(); //получаем кол-во непрочитанных сообщений
    }

    private void initSocket() {
        Log.d(TAG, "initSocket: called");
        App app = (App)getActivity().getApplication();
        app.initSocket();
        mSocket = app.getmSocket();
        if (!mSocket.connected())
            mSocket.connect();
        JSONObject data = new JSONObject();
        try {
            data.put("userId", app.getmUserID());
            data.put("token", app.getmToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("auth", data);
        mSocket.on("newMessage", newMessage);
        mSocket.on("error-pipe", error_pipe);
        mSocket.on("authOk", authOk);
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

    @Override
    public void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Kenilab";
            String description = "MedicApp";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = Objects.requireNonNull(getActivity()).getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void showNotif(String title, String contentTitle, String contentText){
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(contentTitle)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setSound(alarmSound)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, builder.build());
        notificationId++;
    }

    @Override
    public void setUnreadMessages(String string, long aLong) {
        adapter.setCounterForPatient(
                string,
                aLong
        );
    }


    private Emitter.Listener authOk = args -> Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
//        try {
            JSONObject data = (JSONObject) args[0];
            Log.d("authOk", data.toString());
            presenter.authOK(data);
//            for (int i = 0; i <data.getJSONArray("dialogs").length() ; i++) {
//                if (data.getJSONArray("dialogs").getJSONObject(i).has("unreadMessages")){
//                    adapter.setCounterForPatient(
//                            data.getJSONArray("dialogs").getJSONObject(i).getString("id"),
//                            data.getJSONArray("dialogs").getJSONObject(i).getLong("unreadMessages")
//                    );
//                }
//                for (int j = 0; j <data.getJSONArray("dialogs").getJSONObject(i).getJSONArray("participants").length() ; j++) {
//                    JSONObject participant = data.getJSONArray("dialogs").getJSONObject(i).getJSONArray("participants").getJSONObject(j);
//                    String id  = participant.getString("id");
//                    Log.d("AuthOK", "id = " + id + " unread = " + data.getJSONArray("dialogs").getJSONObject(i).getLong("unreadMessages"));
//                    if (data.getJSONArray("dialogs").getJSONObject(i).has("unreadMessages")){
//
//                    }
//                    //adapter.setCounterForPatient(id, data.getJSONArray("dialogs").getJSONObject(i).getLong("unreadMessages"));
//                }
//
//            }
//            Log.d("", data.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    });


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called");
        initSocket();
    }

    @Override
    public void onStop() {
        super.onStop();
        mSocket.off("newMessage", newMessage);
        mSocket.off("error-pipe", error_pipe);
        mSocket.off("authOk", authOk);
    }

    private Emitter.Listener newMessage = args -> Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        Log.d(TAG, "newMessage:" + data.toString());
        try {
            presenter.onMessageReceived(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    });

    private Emitter.Listener error_pipe  = args -> Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        Log.d("error_pipe", data.toString());
    });

}
