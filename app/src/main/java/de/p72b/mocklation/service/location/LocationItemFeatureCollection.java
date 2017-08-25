package de.p72b.mocklation.service.location;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class LocationItemFeatureCollection {

    @SerializedName("type")
    private String mType;

    @SerializedName("features")
    private List<LocationItemFeature> mFeatures;

    public List<LocationItemFeature> getFeatures() {
        return mFeatures;
    }

    public void setFeatures(List<LocationItemFeature> features) {
        mFeatures = features;
    }
}
