package com.example.doctorapp.networking.data;

public class ChangeConclusionBody {
    private String conclusion;

    public ChangeConclusionBody(String conclusion) {
        this.conclusion = conclusion;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }
}
