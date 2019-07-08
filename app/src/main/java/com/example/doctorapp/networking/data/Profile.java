package com.example.doctorapp.networking.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Profile {
    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("surname")
    @Expose
    private String surname;

    @SerializedName("therapistPosition")
    @Expose
    private String therapistPosition;

    public Profile(String name, String surname, String therapistPosition) {
        this.name = name;
        this.surname = surname;
        this.therapistPosition = therapistPosition;
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

    public String getTherapistPosition() {
        return therapistPosition;
    }

    public void setTherapistPosition(String therapistPosition) {
        this.therapistPosition = therapistPosition;
    }
}
