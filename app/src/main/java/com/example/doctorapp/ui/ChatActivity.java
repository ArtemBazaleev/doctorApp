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
import com.example.doctorapp.PlayerActivity;
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import com.example.doctorapp.utils.ImageFilePath;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import org.json.JSONException;
import org.json.JSONObject;


public class ChatActivity extends MvpAppCompatActivity
        implements IChatActivityView, MessageListAdapter.IOnReceivedImageClicked {

    public static final String CHAT_ID = "ChatID";
    public static final String CHAT_PATIENT_ID = "ChatPatientID";
    public static final String PATIENT_NAME = "patient_NAME";
    private static final int GALLERY_REQUEST_CODE = 65535;
    private static final String TAG = "ChatActivity";

    private String patientID;
    private String patientName;
    private  boolean isFirstInited = true;
    private LinkedList<JSONObject> imagesToSent;
    private String dialogID = "";

    @BindView(R.id.recycler_chat) RecyclerView recyclerView;
    @BindView(R.id.imageButton2) ImageButton imageButton;
    @BindView(R.id.editText4) EditText text;
    @BindView(R.id.imageView_exercise) ImageView exercise;
    @BindView(R.id.imageView_results) ImageView results;
    @BindView(R.id.addBtnChat) ImageView addBtn;
    @BindView(R.id.progressBar6) ProgressBar progressBar;
    private LinkedList<BaseMessage> messagesToAdd;
    private boolean authOKFlag = true;
    private LinkedList<BaseMessage> history = new LinkedList<>();
    private int historyShown = 0;
    private long totalMessages = 0;

    private AlertDialog progressDialog;


    private App app;
    private MessageListAdapter adapter;
    private Socket mSocket;

    @InjectPresenter
    ChatActivityPresenter presenter;
    private long lastVisibleItem;
    private boolean isInBackGround = false;
    private boolean isLoadingMessages = false;
    private int firstVisibleItem;
    private boolean inited = false;
    private Timer timer = new Timer();

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
        init();
    }


    private void init(){
        imagesToSent = new LinkedList<>();
        messagesToAdd = new LinkedList<>();
        exercise.setOnClickListener(l-> presenter.onExerciseClicked());
        results.setOnClickListener(l-> presenter.onResultsClicked());
        app = (App)getApplication();
        checkToken();

        if (getIntent().getExtras()!= null){
            patientID = getIntent().getExtras().getString(CHAT_PATIENT_ID,"");
            Log.d("CHAT ACTIVITY", "PatientID: " + patientID);
            patientName = getIntent().getExtras().getString(PATIENT_NAME, "");
            presenter.setPatientID(patientID);
        }

        hideAll();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        List<BaseMessage> messages = new ArrayList<>();
        adapter = new MessageListAdapter(this, messages);
        adapter.addOnRecivedImageListener(this);
        recyclerView.setAdapter(adapter);
        imageButton.setOnClickListener(l-> sendMessage());
        addBtn.setOnClickListener(l-> requestImage());

        initTopScroll();
    }

    private void checkToken() {
        if (app.getmToken().equals("") || app.getmUserID().equals(""))
            startLoginActivityAndClearStack();
    }

    private void startLoginActivityAndClearStack() {
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private void sendMessage() {
        if (text.getText().toString().equals(""))
            return;
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
                lastVisibleItem = Objects.requireNonNull(manager).findFirstCompletelyVisibleItemPosition();
                firstVisibleItem = Objects.requireNonNull(manager).findLastVisibleItemPosition();
                Log.d("ChatActivity", "lastVisible = : " + lastVisibleItem);
                if (lastVisibleItem == 0 && !isLoadingMessages && !isInBackGround){
                    isLoadingMessages = true;
                    Log.d("onScrollStateChanged: ","called!!!!");
                    getMessages(totalMessages, 30);
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
                    if (firstVisibleItem == adapter.getItemCount()-1)
                        recyclerView.scrollToPosition(firstVisibleItem);
                });
    }


    private void initSocket() {
        initializeSocket();
        Log.d(TAG, "initSocket: Called!");
        onSocket();
        setEnabledProgressBar(true);
        if (mSocket.connected()){
            Log.d(TAG, "initSocket: Connected");
            JSONObject data = new JSONObject();
            try {
                Log.d("", "enteringDialog: " + data.toString());
                data.put("dialogId", app.getDialogID());
                mSocket.emit("enterInDialog", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{
            Log.d(TAG, "initSocket: socketNotInited...");
            mSocket.connect();
        }

    }

    private void onSocket(){
        if (!mSocket.hasListeners("enteredDialog"))
            mSocket.on("enteredDialog",enteredDialogChat);
        if (!mSocket.hasListeners("messageReceive"))
            mSocket.on("messageReceive",messageReceiveChat);
        if (!mSocket.hasListeners("leavedDialog"))
            mSocket.on("leavedDialog",leavedDialogChat);
        if (!mSocket.hasListeners("messageListReceive"))
            mSocket.on("messageListReceive", messageListReceiveChat);
        if (!mSocket.hasListeners("pipe"))
            mSocket.on("error-pipe", error_pipeChat);
        if (!mSocket.hasListeners("authOk"))
            mSocket.on("authOk",authOk);
        if (!mSocket.hasListeners("getDialogs"))
            mSocket.on("dialogList", dialogList);
        //if (!mSocket.hasListeners(Socket.EVENT_CONNECT))
        mSocket.on(Socket.EVENT_CONNECT, onConnected);
    }

    private void offSocket() {
        mSocket.off("enteredDialog",enteredDialogChat);
        mSocket.off("messageReceive",messageReceiveChat);
        mSocket.off("leavedDialog",leavedDialogChat);
        mSocket.off("messageListReceive", messageListReceiveChat);
        mSocket.off("error-pipe", error_pipeChat);
        mSocket.off("authOk", authOk);
        mSocket.off(Socket.EVENT_CONNECT, onConnected);
    }

    public void onActivityResult(int requestCode,int resultCode,Intent data){
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == GALLERY_REQUEST_CODE) {
                String[] arrTemp = ImageFilePath.getPath(this, data.getData()).split("\\.");
                if (arrTemp[arrTemp.length - 1].equals("mp4")) {
                    compressVideo(data);
                    return;
                }
                Uri selectedImage = data.getData();
                BaseMessage baseMessage = new BaseMessage();
                baseMessage.messageType = BaseMessage.MESSAGE_TYPE_SENDER_IMAGE;
                baseMessage.setUri(selectedImage);
                String base64Photo = fileToBase64(data);
                String strMessage = "data:image/" + arrTemp[arrTemp.length - 1] + ";base64," + base64Photo;
                messagesToAdd.add(baseMessage);
                //adapter.addMessage(baseMessage);
                JSONObject message = new JSONObject();
                try {
                    message.put("message", strMessage);
                    message.put("type", "photo");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                imagesToSent.add(message);
//                mSocket.emit("message", message);
//                recyclerView.scrollToPosition(adapter.getItemCount()-1);
            }
    }

    private void compressVideo(Intent data) {
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
        if (!isInBackGround) {
            mSocket.emit("message", message);
            BaseMessage baseMessage = new BaseMessage();
            baseMessage.messageType = BaseMessage.MESSAGE_TYPE_SENDER_VIDEO;
            baseMessage.setUri(Uri.parse(path));
            baseMessage.setResourceLocalPreview(R.drawable.bg_black_rect);
            adapter.addMessage(baseMessage);
        }else{
            Toast.makeText(this, "Не закрывайте приложение, пока видео не отправится", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgressDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.progress_dialog,null);
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
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ___________________________________________");
        isInBackGround = false;
        initSocket();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Called-------------------------------------------------------");
        mSocket.emit("exitFromChat", new JSONObject());
        isInBackGround = true;
        inited = false;
        isFirstInited = true;
        offSocket();
        setEnabledProgressBar(false);
        mSocket.disconnect();
    }



    protected void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy: Called ______________________________________________________");
        if (progressDialog!=null)
            progressDialog.dismiss();
    }

    private Emitter.Listener error_pipeChat  = args -> ChatActivity.this.runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        Log.d("error_pipe", data.toString());
    });

    private Emitter.Listener messageListReceiveChat = args -> ChatActivity.this.runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        Log.d("", data.toString());
        setEnabledProgressBar(false);
        try {

            if (data.getLong("count") == 0)
                return;
            else totalMessages += data.getLong("count");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        LinkedList<BaseMessage> baseMessagesData = new LinkedList<>(getMessagesFromJSON(data));
        adapter.addMessages(baseMessagesData);
        if (isFirstInited){
            Log.d(TAG, "FirstInited______________________________________________________: ");
            if (authOKFlag) { //esli avtorizovan
            for (JSONObject i : imagesToSent) { //sending images after entering dialog
                mSocket.emit("message", i);
            }
            for (BaseMessage i : messagesToAdd) {
                adapter.addMessage(i);
            }
            imagesToSent.clear();
            messagesToAdd.clear();
        }
        lastVisibleItem = adapter.getItemCount()-1;
        firstVisibleItem = adapter.getItemCount()-1;
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
        }
        isFirstInited = false;
        isLoadingMessages = false;
    });



    private Emitter.Listener leavedDialogChat = args -> ChatActivity.this.runOnUiThread(() -> Log.d("leavedDialog", ""));

    private Emitter.Listener messageReceiveChat  = args -> ChatActivity.this.runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        Log.d("messageReceive", data.toString());
        try {
            BaseMessage baseMessage = new BaseMessage();
            baseMessage.setTime(data.getJSONObject("message").getLong("date"));
            switch (data.getJSONObject("message").getString("type")) {
                case "text":
                    baseMessage.messageType = BaseMessage.MESSAGE_TYPE_RECIVER;
                    baseMessage.setMessage(data.getJSONObject("message").getString("message"));
                    break;
                case "photo":
                    baseMessage.messageType = BaseMessage.MESSAGE_TYPE_RECEIVER_PHOTO;
                    baseMessage.setUri(Uri.parse(Constants.BASE_URL_IMAGE + data.getJSONObject("message").getString("message")));
                    break;
                case "video":
                    baseMessage.messageType = BaseMessage.MESSAGE_TYPE_RECEIVER_VIDEO;
                    baseMessage.setUri(Uri.parse(Constants.BASEURL_VIDEO_TEMP + data.getJSONObject("message").getString("message")));
                    break;
            }
            String author = data.getJSONObject("message").getString("author");
            Log.d( "MessageAuthor" , author);
            if (app.getmUserID().equals(author))
                return;
            adapter.addMessage(baseMessage);
            recyclerView.scrollToPosition(adapter.getItemCount()-1);
            lastVisibleItem = adapter.getItemCount()-1;
            firstVisibleItem = adapter.getItemCount()-1;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });

    private Emitter.Listener enteredDialogChat = args -> ChatActivity.this.runOnUiThread(() -> {
        JSONObject data = (JSONObject) args[0];
        setEnabledProgressBar(false);
        if (inited)
            return;
        Log.d(TAG,"enteredDialog "+ data.toString());
        inited = true;
        adapter.clearAll();
        history.clear();
        totalMessages = 0;
        getMessages(0,30);
//        setEnabledProgressBar(false);
//        history.addAll(getMessagesFromJSON(data));
//        totalMessages = history.size();
//        Log.d("HistorySize", String.valueOf(history.size()));
//        if (history.size() >= 50) {
//            adapter.addMessages(history.subList(0, 50));
//            historyShown = 50;
//        }
//        else{
//            adapter.addMessages(history);
//            historyShown = history.size();
//        }
//
//
//        recyclerView.scrollToPosition(adapter.getItemCount()-1);
//        if (authOKFlag) { //esli avtorizovan
//            for (JSONObject i : imagesToSent) { //sending images after entering dialog
//                mSocket.emit("message", i);
//            }
//            for (BaseMessage i : messagesToAdd) {
//                adapter.addMessage(i);
//                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
//            }
//            imagesToSent.clear();
//            messagesToAdd.clear();
//        }
//        lastVisibleItem = adapter.getItemCount()-1;
//        firstVisibleItem = adapter.getItemCount()-1;
    });


    private Emitter.Listener onConnected = args -> ChatActivity.this.runOnUiThread(() -> {
        Log.d(TAG, "onConnected: called");
        Log.d(TAG, "onConnected socket: "+ mSocket.connected());

        JSONObject data = new JSONObject();
        try {
            data.put("userId", app.getmUserID());
            data.put("token", app.getmToken());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("Emitting...", "initSocket: " + data.toString());
        mSocket.emit("auth", data);


        if (!inited)
            requestDialogs();
    });
    private Emitter.Listener authOk = args -> ChatActivity.this.runOnUiThread(() -> {
        Log.d(TAG, "AuthOK: called!!");
        authOKFlag = true;
        JSONObject data = new JSONObject();
        try {
            Log.d("", "enteringDialog: " + data.toString());
            data.put("dialogId", app.getDialogID());
            mSocket.emit("enterInDialog", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });

    @Override
    protected void onResume() {
        super.onResume();
        App app = (App)getApplication();
        if (app.getmToken().equals("") || app.getmUserID().equals("")){
            Log.d(TAG, "onResume: emptyData ------------------------------------------------");
            finish();
        }
    }

    @Override
    public void onReceivedImage(BaseMessage baseMessage) {
        Intent i = new Intent(this, ResultViewActivity.class);
        i.putExtra(ResultViewActivity.IMAGE_PARAM, baseMessage.getUri().toString());
        startActivity(i);
    }

    @Override
    public void onReceivedVideo(BaseMessage baseMessage) {
        Log.d("ChatActivity", "onReceivedVideo: Called");
        Intent i = PlayerActivity.getVideoPlayerIntent(this,
                baseMessage.getUri().toString(),"", R.drawable.ic_video_play);
        startActivity(i);
    }

    private boolean checkPermission(){
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void getMessages(long skip, long limit){
        if (mSocket!=null && mSocket.connected() && !isInBackGround) {
            JSONObject requestMessages = new JSONObject();
            try {
                requestMessages.put("skip", skip);
                requestMessages.put("limit", limit);
                mSocket.emit("getMessageList" , requestMessages);
                setEnabledProgressBar(true);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    private void setEnabledProgressBar(boolean enabled){
        if (enabled)
            progressBar.setVisibility(View.VISIBLE);
        else progressBar.setVisibility(View.GONE);
    }



    private Emitter.Listener dialogList = args -> ChatActivity.this.runOnUiThread(()->{
        JSONObject data = (JSONObject) args[0];
        Log.d(TAG, "DialogListReceived: " + data.toString());
        authOKFlag = true;
        JSONObject dataEmit = new JSONObject();
        try {
            dataEmit.put("dialogId", app.getDialogID());
            Log.d("", "enteringDialog: " + dataEmit.toString());
            mSocket.emit("enterInDialog", dataEmit);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    });

    private void  requestDialogs() {
        timer = new Timer();
        TimerTask hourlyTask = new TimerTask () {
            @Override
            public void run () {
                if (inited)
                    timer.cancel();
                Log.d(TAG, "requesting dialogs................................................: ");
                mSocket.emit("getDialogs");
            }
        };
        timer.schedule (hourlyTask, 0L, 1000);

    }

    private void initializeSocket(){
        try {
            IO.Options mOptions = new IO.Options();
            mOptions.path = "/socstream/";
            mOptions.secure = false;
            mOptions.forceNew = true; //added
            mOptions.reconnection = true;
            Log.d("test", "initSocket: " + mOptions.toString());
            mSocket = IO.socket(Constants.BASE_SOCKET_URL, mOptions);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private List<BaseMessage> getMessagesFromJSON(JSONObject data){
        List<BaseMessage> result = new LinkedList<>();

        try{
            for (int i = 0; i<data.getJSONArray("messages").length(); i++) {
                BaseMessage baseMessage = new BaseMessage();
                baseMessage.setTime(data.getJSONArray("messages").getJSONObject(i).getLong("date"));
                if (!data.getJSONArray("messages").getJSONObject(i).has("type")) { // if type !exist it might be old message
                    baseMessage.setMessage(data.getJSONArray("messages").getJSONObject(i).getString("message"));
                    if (app.getmUserID().equals(data.getJSONArray("messages").getJSONObject(i).getString("author")))
                        baseMessage.messageType = BaseMessage.MESSAGE_TYPE_SENDER;
                    else baseMessage.messageType = BaseMessage.MESSAGE_TYPE_RECIVER;
                    history.add(baseMessage);
                    continue;
                }

                //Log.d("ChatActivity", data.getJSONArray("messages").getJSONObject(i).getString("type"));
                switch (data.getJSONArray("messages").getJSONObject(i).getString("type")) {
                    case "text":  //если текст
                        baseMessage.setMessage(data.getJSONArray("messages").getJSONObject(i).getString("message"));
                        if (app.getmUserID().equals(data.getJSONArray("messages").getJSONObject(i).getString("author")))
                            baseMessage.messageType = BaseMessage.MESSAGE_TYPE_SENDER;
                        else baseMessage.messageType = BaseMessage.MESSAGE_TYPE_RECIVER;
                        break;
                    case "photo":  // если фото
                        baseMessage.setUri(Uri.parse(Constants.BASE_URL_IMAGE + data.getJSONArray("messages").getJSONObject(i).getString("message")));
                        if (app.getmUserID().equals(data.getJSONArray("messages").getJSONObject(i).getString("author")))
                            baseMessage.messageType = BaseMessage.MESSAGE_TYPE_SENDER_IMAGE;
                        else baseMessage.messageType = BaseMessage.MESSAGE_TYPE_RECEIVER_PHOTO;
                        break;
                    case "video":
                        baseMessage.setUri(Uri.parse(Constants.BASEURL_VIDEO_TEMP + data.getJSONArray("messages").getJSONObject(i).getString("message")));
                        baseMessage.setMessage(Constants.BASEURL_VIDEO_TEMP + data.getJSONArray("messages").getJSONObject(i).getString("message"));
                        if (app.getmUserID().equals(data.getJSONArray("messages").getJSONObject(i).getString("author"))) {
                            baseMessage.messageType = BaseMessage.MESSAGE_TYPE_SENDER_VIDEO;
                        } else {
                            baseMessage.messageType = BaseMessage.MESSAGE_TYPE_RECEIVER_VIDEO;
                        }
                        break;
                }

                result.add(baseMessage);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return result;
    }

}
