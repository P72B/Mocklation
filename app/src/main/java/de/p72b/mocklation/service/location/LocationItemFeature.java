package de.p72b.mocklation.service.location;

import com.google.gson.annotations.SerializedName;
import com.google.maps.android.geojson.GeoJsonFeature;

public class LocationItemFeature {

    @SerializedName("type")
    private String mType;

    @SerializedName("properties")
    private LocationItemFeatureProperties mProperties;

    private GeoJsonFeature mGeoJsonFeature;

    public LocationItemFeatureProperties getProperties() {
        return mProperties;
    }

    public GeoJsonFeature getGeoJsonFeature() {
        return mGeoJsonFeature;
    }

    public void setGeoJsonFeature(GeoJsonFeature geoJsonFeature) {
        mGeoJsonFeature = geoJsonFeature;
    }

    public class LocationItemFeatureProperties {
        @SerializedName("strokeColor")
        private String mStrokeColor;

        public String getStrokeColor() {
            return mStrokeColor;
        }
    }
}
