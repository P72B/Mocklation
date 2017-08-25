package de.p72b.mocklation.service.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonPolygonStyle;

import org.json.JSONException;
import org.json.JSONObject;

import de.p72b.mocklation.service.location.LocationItemFeature;
import io.reactivex.functions.Function;

@AutoValue
public abstract class LocationItem implements Parcelable {
    public static final String TABLE = "locations";

    public static final String COLUMN_CODE = "code";
    public static final String COLUMN_GEOJSON = "geojson";
    public static final String COLUMN_ACCURACY = "accuracy";
    public static final String COLUMN_SPEED = "speed";

    public abstract String code();
    public abstract String geojosn();
    public abstract int accuracy();
    public abstract int speed();

    public static final Function<Cursor, LocationItem> MAPPER = new Function<Cursor, LocationItem>() {
        @Override public LocationItem apply(Cursor cursor) {
            String code = Db.getString(cursor, COLUMN_CODE);
            String geojson = Db.getString(cursor, COLUMN_GEOJSON);
            int accuracy = Db.getInt(cursor, COLUMN_ACCURACY);
            int speed = Db.getInt(cursor, COLUMN_SPEED);

            return new AutoValue_LocationItem(code, geojson, accuracy, speed);
        }
    };

    public static final class Builder {
        private final ContentValues values = new ContentValues();

        public Builder code(String code) {
            values.put(COLUMN_CODE, code);
            return this;
        }

        public Builder geojson(String geojson) {
            values.put(COLUMN_GEOJSON, geojson);
            return this;
        }

        public Builder accuracy(int accuracy) {
            values.put(COLUMN_ACCURACY, accuracy);
            return this;
        }

        public Builder speed(int speed) {
            values.put(COLUMN_SPEED, speed);
            return this;
        }

        public ContentValues build() {
            return values; // TODO defensive copy?
        }
    }

    public LocationItemFeature deserialize() throws JsonParseException {
        final LocationItemFeature locationItemFeature = new Gson().fromJson(geojosn(), LocationItemFeature.class);
        try {
            // The geoJSON parser is hidden inside GeoJsonLayer ^^
            GeoJsonLayer layer = new GeoJsonLayer(null, new JSONObject(geojosn()));
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

