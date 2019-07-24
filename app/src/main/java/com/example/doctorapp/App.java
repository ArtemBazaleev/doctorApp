package com.example.doctorapp;

import android.app.Application;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;


public class App extends Application {
    public String getPatienID() {
        return patienID;
    }

    public void setPatienID(String getPatienID) {
        this.patienID = getPatienID;
    }

    public String patienID;
    private String mToken = "";
    private String mUserID = "";
    private String dialogID = "";
    private Socket mSocket;
    private boolean inited = false;
    private Vibrator vibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        initSocket();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

    }

    public void initSocket() {
        if (inited)
            return;
        Log.d("Application", "initSocket: called!");
        try {
            IO.Options mOptions = new IO.Options();
            mOptions.path = "/socstream/";
            mOptions.secure = false;
            Log.d("test", "initSocket: " + mOptions.toString());
            mSocket = IO.socket(Constants.BASE_SOCKET_URL, mOptions);
            inited = true;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void forceInit(){
        if (!mSocket.connected()){
            inited = false;
            initSocket();
        }
    }

    public void disconnect() {
        //mSocket.disconnect();
    }

    public Socket getmSocket() {
        return mSocket;
    }

    public String getmToken() {
        return mToken;
    }

    public void setmToken(String mToken) {
        this.mToken = mToken;
    }

    public String getmUserID() {
        return mUserID;
    }

    public void setmUserID(String mUserID) {
        this.mUserID = mUserID;
    }

    public void vibrate() {
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(200);

        }
    }

    public String getDialogID() {
        return dialogID;
    }

    public void setDialogID(String dialogID) {
        this.dialogID = dialogID;
    }
}
