package com.example.doctorapp.networking.responses.results;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Info {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("conclusion")
    @Expose
    private String conclusion;
    @SerializedName("backbone")
    @Expose
    private List<String> backbone = null;
    @SerializedName("other")
    @Expose
    private List<String> other = null;
    @SerializedName("created")
    @Expose
    private Long created;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public List<String> getBackbone() {
        return backbone;
    }

    public void setBackbone(List<String> backbone) {
        this.backbone = backbone;
    }

    public List<String> getOther() {
        return other;
    }

    public void setOther(List<String> other) {
        this.other = other;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }
}
