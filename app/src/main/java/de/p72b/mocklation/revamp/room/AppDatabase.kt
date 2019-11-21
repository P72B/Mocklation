package de.p72b.mocklation.revamp.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.maps.android.data.geojson.GeoJsonParser
import de.p72b.mocklation.App
import de.p72b.mocklation.util.AppUtil
import org.json.JSONException
import org.json.JSONObject

@Database(entities = [LocationItem::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationItemDao(): LocationItemDao

    companion object {
        private const val DB_NAME_LOCATIONS = "locations"
        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val cursor = database.query("SELECT * FROM locations")
                while (cursor.moveToNext()) {
                    val itemCode = cursor.getString(cursor.getColumnIndex("code"))
                    val itemGeoJson = cursor.getString(cursor.getColumnIndex("geo_json"))
                    val geometry = migrateFeatureToGeometry(itemGeoJson)
                    if (geometry != null) {
                        database.execSQL("UPDATE locations SET geo_json=\""
                                + geometry + "\" WHERE code=\"" + itemCode + "\"")
                    }
                }
            }
        }

        private fun migrateFeatureToGeometry(itemGeoJson: String): String? {
            var result: String? = null
            try {
                val geometry = GeoJsonParser.parseGeometry(JSONObject(itemGeoJson))
                result = AppUtil.geometryToString(geometry)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return result
        }

        @JvmStatic
        val locationsDb: Builder<AppDatabase>
            get() = Room.databaseBuilder(App.sInstance, AppDatabase::class.java,
                    DB_NAME_LOCATIONS)
                    .addMigrations(MIGRATION_1_2)
    }
}