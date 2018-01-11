package de.p72b.mocklation.service.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonPoint;
import com.google.maps.android.geojson.GeoJsonPolygonStyle;

import org.json.JSONException;
import org.json.JSONObject;

import de.p72b.mocklation.service.location.LocationItemFeature;

@Entity(tableName = "locations")
public class LocationItem implements Parcelable {
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

    @ColumnInfo(name = "favorite")
    private boolean mIsFavorite;

    @ColumnInfo(name = "color")
    private int mColor;

    public static final Parcelable.Creator<LocationItem> CREATOR = new Parcelable.Creator<LocationItem>() {
        public LocationItem createFromParcel(Parcel in) {
            return new LocationItem(in);
        }

        public LocationItem[] newArray(int size) {
            return new LocationItem[size];
        }
    };

    public LocationItem(@NonNull String code, @NonNull String displayedName,
                        @NonNull String geoJson, int accuracy, int speed) {
        mCode = code;
        mDisplayedName = displayedName;
        mGeoJson = geoJson;
        mAccuracy = accuracy;
        mSpeed = speed;
        mIsFavorite = false;
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

    int getAccuracy() {
        return mAccuracy;
    }

    public void setAccuracy(int accuracy) {
        mAccuracy = accuracy;
    }

    int getSpeed() {
        return mSpeed;
    }

    public void setSpeed(int speed) {
        mSpeed = speed;
    }

    public boolean isIsFavorite() {
        return mIsFavorite;
    }

    public void setIsFavorite(boolean isFavorite) {
        mIsFavorite = isFavorite;
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

    @Nullable
    public Object getGeometry() {
        LocationItemFeature feature = deserialize();
        switch (feature.getGeoJsonFeature().getGeometry().getType()) {
            case "Point":
                GeoJsonPoint point = (GeoJsonPoint) feature.getGeoJsonFeature().getGeometry();
                return new LatLng(point.getCoordinates().latitude,
                        point.getCoordinates().longitude);
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mCode);
        out.writeString(mDisplayedName);
        out.writeString(mGeoJson);
        out.writeInt(mAccuracy);
        out.writeInt(mSpeed);
    }

    public static String getNameToBeDisplayed(LocationItem item) {
        String name = item.getCode();
        if (!TextUtils.isEmpty(item.getDisplayedName())) {
            name = item.getDisplayedName();
        }
        return name;
    }

    private LocationItem(Parcel in) {
        mCode = in.readString();
        mDisplayedName = in.readString();
        mGeoJson = in.readString();
        mAccuracy = in.readInt();
        mSpeed = in.readInt();
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

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }
}
