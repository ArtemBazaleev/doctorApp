package com.example.doctorapp.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.example.doctorapp.App;
import com.example.doctorapp.model.BaseMessage;
import com.example.doctorapp.R;
import com.example.doctorapp.adapters.MessageListAdapter;
import com.example.doctorapp.presentation.presenter.ChatActivityPresenter;
import com.example.doctorapp.presentation.view.IChatActivityView;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;


public class ChatActivity extends MvpAppCompatActivity
        implements IChatActivityView, MessageListAdapter.IOnReceivedImageClicked {

    public static final String CHAT_ID = "ChatID";
    public static final String CHAT_PATIENT_ID = "ChatPatientID";
    private static final int GALLERY_REQUEST_CODE = 65535;

    private String chatID;
    private String patientID;

    @BindView(R.id.recycler_chat) RecyclerView recyclerView;
    @BindView(R.id.imageButton2) ImageButton imageButton;
    @BindView(R.id.editText4) EditText text;
    @BindView(R.id.imageView_exercise) ImageView exercise;
    @BindView(R.id.imageView_results) ImageView results;
    @BindView(R.id.addBtnChat) ImageView addBtn;
    private LinkedList<BaseMessage> history = new LinkedList<>();
    private int historyShown = 0;


    private App app;
    private MessageListAdapter adapter;
    private Socket mSocket;

    @InjectPresenter
    ChatActivityPresenter presenter;

    @ProvidePresenter
    ChatActivityPresenter providePresenter(){
        App app = (App) getApplication();
        return new ChatActivityPresenter(app.getmToken(), app.getmUserID());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        exercise.setOnClickListener(l-> presenter.onExerciseClicked());
        results.setOnClickListener(l-> presenter.onResultsClicked());
        app = (App)getApplication();
        initSocket();


        if (getIntent().getExtras()!= null){
            chatID = getIntent().getExtras().getString(CHAT_ID,"");
            patientID = getIntent().getExtras().getString(CHAT_PATIENT_ID,"");
            Log.d("CHAT ACTIVITY", "PatientID: " + patientID);
            presenter.setPatientID(patientID);
        }


        hideAll();
        LinearLayoutManager manager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        List<BaseMessage> messages = new ArrayList<>();
        adapter = new MessageListAdapter(this, messages);
        adapter.addOnRecivedImageListener(this);
        BaseMessage baseMessage31 = new BaseMessage();
        baseMessage31.messageType = BaseMessage.MESSAGE_TYPE_RECEIVER_PHOTO;
        baseMessage31.setUri(Uri.parse("https://zdnet3.cbsistatic.com/hub/i/2019/03/16/e118b0c5-cf3c-4bdb-be71-103228677b25/966244d1205becf3dd9a1af76b8d869a/android-logo.png"));
        adapter.addMessage(baseMessage31);
        recyclerView.setAdapter(adapter);

        imageButton.setOnClickListener(l->{
//            hideAll();

            BaseMessage baseMessage = new BaseMessage(text.getText().toString());
            baseMessage.setMessage(text.getText().toString());
            baseMessage.messageType = BaseMessage.MESSAGE_TYPE_SENDER;
            JSONObject message = new JSONObject();
            try {
                message.put("message", text.getText().toString());
                Log.d("",message.toString());
                mSocket.emit("message", message);
                recyclerView.scrollToPosition(adapter.getItemCount()-1);
                text.setText("");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter.addMessage(baseMessage);
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
        });

        addBtn.setOnClickListener(l->{
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            String[] mimeTypes = {"image/jpeg", "image/png"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
            startActivityForResult(intent, GALLERY_REQUEST_CODE);
        });

        initTopScroll();
    }

    private void initTopScroll() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (Objects.requireNonNull(manager).findFirstCompletelyVisibleItemPosition() == 0){
                    Log.d("onScrollStateChanged: ","called!!!!");
                    if (history.size() == historyShown)
                        return;
                    else {
                        if (historyShown + 50 < history.size()) {//показываем по 50 сообщений
                            adapter.addMessages(history.subList(historyShown, historyShown + 50));
                            historyShown = historyShown + 50;
                        }
                        else{
                            adapter.addMessages(history.subList(historyShown, history.size()));
                            historyShown = history.size();
                        }
                    }

                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void initSocket(){

        mSocket = app.getmSocket();
        if (mSocket.connected())
            Toast.makeText(this, "Connected!!", Toast.LENGTH_SHORT).show();
        else mSocket.connect();

        JSONObject data = new JSONObject();
        try {
            data.put("userId", app.getmUserID());
            data.put("token", app.getmToken());
            //Log.d("", data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //mSocket.on(Socket.EVENT_CONNECT, onConnected);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectedError);
        mSocket.on(Socket.EVENT_CONNECT, onConnected);
        mSocket.emit("auth", data);
        mSocket.on("authOk", authOk);
        mSocket.on("enteredDialog",enteredDialog);
        mSocket.on("messageReceive",messageReceive);
        mSocket.on("leavedDialog",leavedDialog);
        mSocket.on("newMessage",newMessage);
        mSocket.on("messageListReceive", messageListReceive);
        mSocket.on("error-pipe", error_pipe);
        Log.d("", "initSocket: " + data.toString());
        mSocket.emit("auth", data);

    }

    public void onActivityResult(int requestCode,int resultCode,Intent data){
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case GALLERY_REQUEST_CODE:
                    Uri selectedImage = data.getData();
                    BaseMessage baseMessage = new BaseMessage();
                    baseMessage.messageType = BaseMessage.MESSAGE_TYPE_SENDER_IMAGE;
                    baseMessage.setUri(selectedImage);
                    adapter.addMessage(baseMessage);
                    recyclerView.scrollToPosition(adapter.getItemCount()-1);
            }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_OK);
        finish();
    }

    //MVP
    @Override
    public void hideAll() {

    }

    @Override
    public void addMessage(BaseMessage message) {
        adapter.addMessage(message);

    }

    @Override
    public void initRecycler(List<BaseMessage> messages) {

    }

    @Override
    public void startExerciseActivity(String patientID) {
        Intent i = new Intent(this, ExerciseActivity.class);
        i.putExtra(ExerciseActivity.PATIENT_ID, patientID);
        app.setPatienID(patientID);
        startActivity(i);
    }

    @Override
    public void startResultsActivity(String patientID) {
        Intent i = new Intent(this, ResultsActivity.class);
        i.putExtra(ResultsActivity.USER_ID, patientID);
        startActivity(i);
    }


    //MVP
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off("authOk", authOk);
        mSocket.off("enteredDialog",enteredDialog);
        mSocket.off("messageReceive",messageReceive);
        mSocket.off("leavedDialog",leavedDialog);
        mSocket.off("newMessage",newMessage);
        mSocket.off("messageListReceive", messageListReceive);
        mSocket.off("error-pipe", error_pipe);
    }

    private Emitter.Listener error_pipe  = args -> ChatActivity.this.runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        Log.d("error_pipe", data.toString());
    });

    private Emitter.Listener messageListReceive = args -> ChatActivity.this.runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        Log.d("messageListReceive", data.toString());
    });

    private Emitter.Listener newMessage = args -> ChatActivity.this.runOnUiThread(() -> {
//        try {
//            JSONObject data = (JSONObject) args[0];
//            Log.d("newMessage", data.toString());
//            BaseMessage baseMessage = new BaseMessage();
//            baseMessage.messageType = BaseMessage.MESSAGE_TYPE_RECIVER;
//            baseMessage.setMessage(data.getJSONObject("message").getString("message"));
//            baseMessage.setTime(data.getJSONObject("message").getLong("date"));
//            adapter.addMessage(baseMessage);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    });

    private Emitter.Listener leavedDialog = args -> ChatActivity.this.runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        Log.d("leavedDialog", data.toString());
    });

    private Emitter.Listener messageReceive  = args -> ChatActivity.this.runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        Log.d("messageReceive", data.toString());
        try {
            BaseMessage baseMessage = new BaseMessage();
            baseMessage.messageType = BaseMessage.MESSAGE_TYPE_RECIVER;
            baseMessage.setMessage(data.getJSONObject("message").getString("message"));
            baseMessage.setTime(data.getJSONObject("message").getLong("date"));
            if (!data.getJSONObject("message").getString("author").equals(app.getmUserID())) {
                adapter.addMessage(baseMessage);
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });


    private Emitter.Listener enteredDialog = args -> ChatActivity.this.runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        Log.d("enteredDialog", data.toString());
        try{
            for (int i = 0; i<data.getJSONArray("messages").length(); i++) {
                BaseMessage baseMessage = new BaseMessage();
                baseMessage.setMessage(data.getJSONArray("messages").getJSONObject(i).getString("message"));
                if (app.getmUserID().equals(data.getJSONArray("messages").getJSONObject(i).getString("author")))
                    baseMessage.messageType = BaseMessage.MESSAGE_TYPE_SENDER;
                else baseMessage.messageType = BaseMessage.MESSAGE_TYPE_RECIVER;
                history.add(baseMessage);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        Log.d("HistorySize", String.valueOf(history.size()));
        if (history.size() >= 50) {
            adapter.addMessages(history.subList(0, 50));
            historyShown = 50;
        }
        else{
            adapter.addMessages(history);
            historyShown = history.size();
        }
    });

    private Emitter.Listener authOk = args -> ChatActivity.this.runOnUiThread(() -> {
        try {
            JSONObject data = (JSONObject) args[0];
            Log.d("authOk", data.toString());
            for (int i = 0; i <data.getJSONArray("dialogs").length() ; i++) {
                for (int j = 0; j <data.getJSONArray("dialogs").getJSONObject(i).getJSONArray("participants").length() ; j++) {
                    JSONObject participant = data.getJSONArray("dialogs").getJSONObject(i).getJSONArray("participants").getJSONObject(j);
                    String id  = participant.getString("id");
                    //Log.d("", id);
                    if (id.equals(patientID)){
                        JSONObject emitObj = new JSONObject();
                        emitObj.put("dialogId",data.getJSONArray("dialogs").getJSONObject(i).getString("id"));
                        mSocket.emit("enterInDialog", emitObj);
                        Log.d("", emitObj.toString());
                        break;
                    }
                }

            }
            Log.d("", data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });

    private Emitter.Listener onConnected = args -> ChatActivity.this.runOnUiThread(() -> {
        //JSONObject data = (JSONObject) args[0];
        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();

    });

    private Emitter.Listener onConnectedError = args -> ChatActivity.this.runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();

    });
    @Override
    public void onReceivedImage(BaseMessage baseMessage) {

    }
}
