package com.example.doctorapp.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.example.doctorapp.App;
import com.example.doctorapp.Constants;
import com.example.doctorapp.model.BaseMessage;
import com.example.doctorapp.R;
import com.example.doctorapp.adapters.MessageListAdapter;
import com.example.doctorapp.presentation.presenter.ChatActivityPresenter;
import com.example.doctorapp.presentation.view.IChatActivityView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import com.example.doctorapp.utils.ImageFilePath;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

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

    private ProgressBar progressBarDialog;
    private AlertDialog progressDialog;


    private App app;
    private MessageListAdapter adapter;
    private Socket mSocket;

    @InjectPresenter
    ChatActivityPresenter presenter;
    private long lastVisibleItem;

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
        recyclerView.setAdapter(adapter);
        imageButton.setOnClickListener(l-> sendMessage());
        addBtn.setOnClickListener(l-> requestImage());

        initTopScroll();
    }

    private void sendMessage() {
        BaseMessage baseMessage = new BaseMessage(text.getText().toString());
        baseMessage.setMessage(text.getText().toString());
        baseMessage.messageType = BaseMessage.MESSAGE_TYPE_SENDER;
        JSONObject message = new JSONObject();
        try {
            message.put("message", text.getText().toString());
            message.put("type","text");
            Log.d("",message.toString());
            mSocket.emit("message", message);
            recyclerView.scrollToPosition(adapter.getItemCount()-1);
            text.setText("");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.addMessage(baseMessage);
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    void requestImage(){
        if(!checkPermission()) {// if no permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/* video/*");
        String[] mimeTypes = {"image/jpeg", "image/png", "video/mp4"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    private void initTopScroll() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                lastVisibleItem = Objects.requireNonNull(manager).findLastVisibleItemPosition();
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



        KeyboardVisibilityEvent.setEventListener(
                this,
                isOpen -> {
                    if (lastVisibleItem == adapter.getItemCount()-1)
                        recyclerView.scrollToPosition(adapter.getItemCount()-1);
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //mSocket.on(Socket.EVENT_CONNECT, onConnected);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectedError);
        mSocket.on(Socket.EVENT_CONNECT, onConnected);
        //mSocket.emit("auth", data);
        mSocket.on("authOk", authOk);
        mSocket.on("enteredDialog",enteredDialog);
        mSocket.on("messageReceive",messageReceive);
        mSocket.on("leavedDialog",leavedDialog);
        mSocket.on("newMessage", newMessage);
        mSocket.on("messageListReceive", messageListReceive);
        mSocket.on("error-pipe", error_pipe);
        Log.d("", "initSocket: " + data.toString());
        mSocket.emit("auth", data);
    }

    public void onActivityResult(int requestCode,int resultCode,Intent data){
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case GALLERY_REQUEST_CODE:
                    String[]arrTemp = ImageFilePath.getPath(this, data.getData()).split("\\.");
                    if (arrTemp[arrTemp.length-1].equals("mp4")) {
                        compressVideo(data);
                        return;
                    }
                    Uri selectedImage = data.getData();
                    BaseMessage baseMessage = new BaseMessage();
                    baseMessage.messageType = BaseMessage.MESSAGE_TYPE_SENDER_IMAGE;
                    baseMessage.setUri(selectedImage);
                    String base64Photo = fileToBase64(data);
                    String strMessage = "data:image/" + arrTemp[arrTemp.length-1] + ";base64," + base64Photo;
                    adapter.addMessage(baseMessage);
                    JSONObject message = new JSONObject();
                    try{
                        message.put("message", strMessage);
                        message.put("type", "photo");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    Log.d("ChatActivity", "sending string..." + message.toString());
                    mSocket.emit("message", message);
                    recyclerView.scrollToPosition(adapter.getItemCount()-1);
                    lastVisibleItem = adapter.getItemCount()-1;
            }
    }

    private void compressVideo(Intent data) {
        String filePath = "";
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(this, data.getData());
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);
        Log.d("ChatActivity", "VideoDur: " + timeInMillisec/1000);
        if (timeInMillisec/1000 > 60){
            Toast.makeText(this, "Максимальная длина видео 1 минута", Toast.LENGTH_SHORT).show();
            return;
        }
        showProgressDialog();
        Disposable d = Observable.create((ObservableOnSubscribe<String>) emitter -> {
            emitter.onNext(new ImageFilePath().compressVideo(data,this));
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    progressDialog.dismiss();
                    Log.d("ChatActivity", "compressedVideo: " + s);
                    File file = new File(s);
                    long length = file.length() / 1048576;
                    if (length<10){
                        sendVideo(s);
                    }
                    Log.d("ChatActivity", "compressedVideoSize: " + length);
                });


    }

    private void sendVideo(String path) {
        String base64String = ImageFilePath.fileToBase64(path, this);
        String strMessage = "data:video/" + "mp4" + ";base64," + base64String;
        JSONObject message = new JSONObject();
        try{
            message.put("message", strMessage);
            message.put("type", "video");
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d("ChatActivity", "sending Video..." + message.toString());
        mSocket.emit("message", message);
        BaseMessage baseMessage = new BaseMessage();
        baseMessage.messageType = BaseMessage.MESSAGE_TYPE_SENDER_VIDEO;
        baseMessage.setUri(Uri.parse(path));
        adapter.addMessage(baseMessage);
    }

    private void showProgressDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.progress_dialog,null);
        progressBarDialog = v.findViewById(R.id.progressBar7);
        builder.setView(v);
        builder.setCancelable(false);
        progressDialog = builder.create();
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.show();
    }


    String fileToBase64(Intent data) {
        try {
            Log.d("ChatActivity", "fileToBase64: " + ImageFilePath.getPath(this, data.getData()));
            InputStream inputStream = new FileInputStream(ImageFilePath.getPath(this, data.getData()));//You can get an inputStream using any IO API
            byte[] bytes;
            byte[] buffer = new byte[8192];
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            bytes = output.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка кодировки файла", Toast.LENGTH_SHORT).show();
            return null;
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
        //mSocket.disconnect();
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
        try {
            Toast.makeText(this, data.getString("message"), Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });

    private Emitter.Listener messageListReceive = args -> ChatActivity.this.runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        Log.d("messageListReceive", data.toString());
    });

    private Emitter.Listener newMessage = args -> ChatActivity.this.runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        Log.d("ChatActivity", "newMessage: " + data.toString());
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
            if (data.getJSONObject("message").getString("type").equals("text")){
                baseMessage.messageType = BaseMessage.MESSAGE_TYPE_RECIVER;
                baseMessage.setMessage(data.getJSONObject("message").getString("message"));
            }
            else if (data.getJSONObject("message").getString("type").equals("photo")){
                baseMessage.messageType = BaseMessage.MESSAGE_TYPE_RECEIVER_PHOTO;
                baseMessage.setUri(Uri.parse(Constants.BASE_URL_IMAGE + data.getJSONObject("message").getString("message")));
            }
            //baseMessage.setTime(data.getJSONObject("message").getLong("date"));
            String author = data.getJSONObject("message").getString("author");
            Log.d( "MessageAuthor" , author);
            if (app.getmUserID().equals(author))
                return;
            adapter.addMessage(baseMessage);
            recyclerView.scrollToPosition(adapter.getItemCount()-1);
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
                if (!data.getJSONArray("messages").getJSONObject(i).has("type")) { // if type !exist it might be old message
                    baseMessage.setMessage(data.getJSONArray("messages").getJSONObject(i).getString("message"));
                    if (app.getmUserID().equals(data.getJSONArray("messages").getJSONObject(i).getString("author")))
                        baseMessage.messageType = BaseMessage.MESSAGE_TYPE_SENDER;
                    else baseMessage.messageType = BaseMessage.MESSAGE_TYPE_RECIVER;
                    history.add(baseMessage);
                    continue;
                }

                Log.d("ChatActivity", data.getJSONArray("messages").getJSONObject(i).getString("type"));

                if (data.getJSONArray("messages").getJSONObject(i).getString("type").equals("text")){ //если текст
                    baseMessage.setMessage(data.getJSONArray("messages").getJSONObject(i).getString("message"));
                    if (app.getmUserID().equals(data.getJSONArray("messages").getJSONObject(i).getString("author")))
                        baseMessage.messageType = BaseMessage.MESSAGE_TYPE_SENDER;
                    else baseMessage.messageType = BaseMessage.MESSAGE_TYPE_RECIVER;
                }
                else if(data.getJSONArray("messages").getJSONObject(i).getString("type").equals("photo")){ // если фото
                    baseMessage.setUri(Uri.parse(Constants.BASE_URL_IMAGE + data.getJSONArray("messages").getJSONObject(i).getString("message")));
                    if (app.getmUserID().equals(data.getJSONArray("messages").getJSONObject(i).getString("author")))
                        baseMessage.messageType = BaseMessage.MESSAGE_TYPE_SENDER_IMAGE;
                    else baseMessage.messageType = BaseMessage.MESSAGE_TYPE_RECEIVER_PHOTO;
                }
                else if (data.getJSONArray("messages").getJSONObject(i).getString("type").equals("video"))
                {
                    baseMessage.setUri(Uri.parse(Constants.BASEURL_VIDEO_TEMP + data.getJSONArray("messages").getJSONObject(i).getString("message")));
                    baseMessage.setMessage(Constants.BASEURL_VIDEO_TEMP + data.getJSONArray("messages").getJSONObject(i).getString("message"));
                    if (app.getmUserID().equals(data.getJSONArray("messages").getJSONObject(i).getString("author"))) {
                        baseMessage.messageType = BaseMessage.MESSAGE_TYPE_SENDER_VIDEO;
                    }
                    else {
                        baseMessage.messageType = BaseMessage.MESSAGE_TYPE_RECEIVER_VIDEO;
                    }
                }

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
        recyclerView.scrollToPosition(adapter.getItemCount()-1);
        lastVisibleItem = adapter.getItemCount()-1;
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

    public void requestMoreMessages(int count){

    }


    @Override
    public void onReceivedImage(BaseMessage baseMessage) {
        Intent i = new Intent(this, ResultViewActivity.class);
        i.putExtra(ResultViewActivity.IMAGE_PARAM, baseMessage.getUri().toString());
        startActivity(i);
    }

    private boolean checkPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            return false;
        else return true;
    }
}
