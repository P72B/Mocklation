package de.p72b.mocklation.service.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.migration.Migration;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.maps.android.data.Geometry;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;

import org.json.JSONException;
import org.json.JSONObject;

import de.p72b.mocklation.dagger.MocklationApp;
import de.p72b.mocklation.util.AppUtil;

@Database(entities = {LocationItem.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DB_NAME_LOCATIONS = "locations";

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE locations ADD COLUMN mode TEXT");
            database.execSQL("UPDATE locations SET mode='FIXED'");

            final Cursor cursor = database.query("SELECT * FROM locations");
            while (cursor.moveToNext()) {
                final String itemCode = cursor.getString(cursor.getColumnIndex("code"));
                final String itemGeoJson = cursor.getString(cursor.getColumnIndex("geo_json"));
                final String geometry = migrateFeatureToGeometry(itemGeoJson);
                if (geometry != null) {
                    database.execSQL("UPDATE locations SET geo_json=\""
                            + geometry + "\" WHERE code=\"" + itemCode + "\"");
                }
            }
        }
    };

    @Nullable
    private static String migrateFeatureToGeometry(@NonNull final String itemGeoJson) {
        String result = null;
        try {
            // The geoJSON parser is hidden inside GeoJsonLayer ^^
            GeoJsonLayer layer = new GeoJsonLayer(null, new JSONObject(itemGeoJson));
            for (GeoJsonFeature feature : layer.getFeatures()) {
                result = AppUtil.geometryToString(feature.getGeometry());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @NonNull
    public static RoomDatabase.Builder<AppDatabase> getLocationsDb() {
        return Room.databaseBuilder(MocklationApp.getInstance(), AppDatabase.class,
                AppDatabase.DB_NAME_LOCATIONS)
                .addMigrations(MIGRATION_1_2);
    }

    public abstract LocationItemDao locationItemDao();
}
