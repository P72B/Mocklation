package de.p72b.mocklation.service.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Color;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonPolygonStyle;

import org.json.JSONException;
import org.json.JSONObject;

import de.p72b.mocklation.service.location.LocationItemFeature;

@Entity(tableName = "locations")
public class LocationItem {
    @NonNull
    @PrimaryKey()
    @ColumnInfo(name = "code")
    private String mCode;

    @ColumnInfo(name = "displayed_name")
    private String mDisplayedName;

    @ColumnInfo(name = "geo_json")
    private String mGeoJson;

    @ColumnInfo(name = "accuracy")
    private int mAccuracy;

    @ColumnInfo(name = "speed")
    private int mSpeed;

    public LocationItem(@NonNull String code, String displayedName, String geoJson, int accuracy, int speed) {
        mCode = code;
        mDisplayedName = displayedName;
        mGeoJson = geoJson;
        mAccuracy = accuracy;
        mSpeed = speed;
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String code) {
        mCode = code;
    }

    public String getDisplayedName() {
        return mDisplayedName;
    }

    public void setDisplayedName(String firstName) {
        mDisplayedName = firstName;
    }

    public String getGeoJson() {
        return mGeoJson;
    }

    public void setGeoJson(String geoJson) {
        mGeoJson = geoJson;
    }

    public int getAccuracy() {
        return mAccuracy;
    }

    public void setAccuracy(int accuracy) {
        mAccuracy = accuracy;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public void setSpeed(int speed) {
        mSpeed = speed;
    }

    public LocationItemFeature deserialize() throws JsonParseException {
        final LocationItemFeature locationItemFeature = new Gson().fromJson(mGeoJson, LocationItemFeature.class);
        try {
            // The geoJSON parser is hidden inside GeoJsonLayer ^^
            GeoJsonLayer layer = new GeoJsonLayer(null, new JSONObject(mGeoJson));
            for (GeoJsonFeature feature : layer.getFeatures()) {
                locationItemFeature.setGeoJsonFeature(feature);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return locationItemFeature;
    }

    private GeoJsonPolygonStyle createStyle(String fillColor) {
        int fullColor = Color.parseColor(fillColor);
        GeoJsonPolygonStyle style = new GeoJsonPolygonStyle();
        style.setFillColor(getTransparentColor(fullColor));
        style.setStrokeWidth(1.0f);
        return style;
    }

    private int getTransparentColor(int color) {
        return Color.argb(100, Color.red(color), Color.green(color), Color.blue(color));
    }
}
