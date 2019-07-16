package com.example.doctorapp;

import android.app.Application;
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
    private Socket mSocket;

    @Override
    public void onCreate() {
        super.onCreate();
        initSocket();
    }

    public void initSocket(){
        try {
            IO.Options mOptions = new IO.Options();
            mOptions.path = "/socstream/";
            mOptions.secure = false;
            Log.d("test", "initSocket: " + mOptions.toString());
            mSocket = IO.socket(Constants.BASE_SOCKET_URL, mOptions);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect(){
        mSocket.disconnect();
    }

    public Socket getmSocket(){
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
}
