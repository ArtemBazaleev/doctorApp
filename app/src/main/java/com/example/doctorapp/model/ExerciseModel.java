package com.example.doctorapp.model;

import android.util.Log;

import com.example.doctorapp.Constants;
import com.example.doctorapp.networking.responses.exercise.Exercise;

public class ExerciseModel {
    private String urlImage = "https://img.webmd.com/dtmcms/live/webmd/consumer_assets/site_images/articles/health_tools/7_most_effective_exercises_slideshow/webmd_photo_of_trainer_walking_on_treadmill.jpg";
    private boolean isLoadingIndicator = false;
    private boolean isSelected = false;
    private String urlVideo = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
    private String name = "No Name";
    private String category = "";
    private String id = "";
    public static final int TYPE_VIDEO = 87;
    public static final int TYPE_HEADER = 51;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private int type = TYPE_VIDEO;

    public ExerciseModel() {

    }

    public String getUrlVideo() {
        return urlVideo;
    }

    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ExerciseModel(Exercise exercise) {
        urlImage = Constants.BASE_URL_IMAGE + exercise.getVideos().get(0).getPreview();
        urlVideo = Constants.BASE_URL_IMAGE + exercise.getVideos().get(0).getVideo();
        name = exercise.getName();
        category = exercise.getCategory();
        id = exercise.getId();
        name = exercise.getName();
        Log.d("ExerciseModel: " ,
                "urlImage="+exercise.getVideos().get(0).getPreview()
                        + "urlVideo="+exercise.getVideos().get(0).getVideo()
                        + "category=" + exercise.getCategory()
        );
    }


    public boolean isLoadingIndicator() {
        return isLoadingIndicator;
    }

    public void setLoadingIndicator(boolean loadingIndicator) {
        isLoadingIndicator = loadingIndicator;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public String getUrlImage() {
        return urlImage;
    }

}
