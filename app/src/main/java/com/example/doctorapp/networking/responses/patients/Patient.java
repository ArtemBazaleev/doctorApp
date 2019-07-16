
package com.example.doctorapp.networking.responses.patients;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Patient {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("surname")
    @Expose
    private String surname;
    @SerializedName("conclusion")
    @Expose
    private String conclusion;
    @SerializedName("dialogId")
    @Expose
    private String dialogId;

    public String getDialogId() {
        return dialogId;
    }

    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

}
