
package com.example.doctorapp.networking.responses.profile;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Therapist {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("role")
    @Expose
    private String role;
    @SerializedName("created")
    @Expose
    private long created;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("surname")
    @Expose
    private String surname;
    @SerializedName("therapistPosition")
    @Expose
    private String therapistPosition;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
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
