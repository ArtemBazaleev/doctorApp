package com.example.doctorapp.utils;

import android.content.Context;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.NoSuchPaddingException;

import devliving.online.securedpreferencestore.DefaultRecoveryHandler;
import devliving.online.securedpreferencestore.SecuredPreferenceStore;

public class SecuredSharedPreferences {
    private SecuredPreferenceStore securedSharedPreferences;

    private static final String TOKEN = "SECURED_TOKEN";
    private static final String ID = "SECURED_ID";
    public static final String LOGIN = "SECURED_LOG";
    public static final String PASS = "SECURED_PASS";
    public SecuredSharedPreferences(Context context) {
        //not mandatory, can be null too
        String storeFileName = "securedStore";
        //not mandatory, can be null too
        String keyPrefix = "vss";
        //it's better to provide one, and you need to provide the same key each time after the first time
        byte[] seedKey = "SecuredDataMedicApp".getBytes();
        try {
            SecuredPreferenceStore.init(context.getApplicationContext(), storeFileName, keyPrefix, seedKey, new DefaultRecoveryHandler());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (SecuredPreferenceStore.MigrationFailedException e) {
            e.printStackTrace();
        }
        securedSharedPreferences = SecuredPreferenceStore.getSharedInstance();
    }

    public void setToken(String token){
        securedSharedPreferences.edit()
                .putString(TOKEN, token)
                .apply();
    }

    public String getToken(){
        return securedSharedPreferences.getString(TOKEN, "");
    }

    public void setUserID(String id){
        securedSharedPreferences.edit()
                .putString(ID,id)
                .apply();
    }

    public String getUserID(){
        return securedSharedPreferences.getString(ID,"");
    }

    public void setLogin(String login){
        securedSharedPreferences.edit()
                .putString(LOGIN, login)
                .apply();
    }
    public String getLogin(){
        return securedSharedPreferences.getString(LOGIN, "");
    }
    public void setPassword(String password){
        securedSharedPreferences.edit()
                .putString(PASS, password)
                .apply();
    }
    public String getPassword(){
        return securedSharedPreferences.getString(PASS, "");
    }
}
