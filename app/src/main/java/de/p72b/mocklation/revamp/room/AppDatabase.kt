package de.p72b.mocklation.revamp.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.maps.android.data.geojson.GeoJsonParser
import de.p72b.mocklation.App
import de.p72b.mocklation.util.AppUtil
import org.json.JSONException
import org.json.JSONObject

@Database(entities = [LocationItem::class], version = 2, exportSchema = false)
@TypeConverters(GeometryTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationItemDao(): LocationItemDao

    companion object {
        private const val DB_NAME_LOCATIONS = "locations"
        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                CREATE TABLE new_locations (
                    code TEXT PRIMARY KEY NOT NULL,
                    geom TEXT,
                    color INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    accuracy INTEGER NOT NULL,
                    favorite INTEGER NOT NULL,
                    speed INTEGER NOT NULL,
                    selected INTEGER NOT NULL
                )
                """.trimIndent())

                val cursor = database.query("SELECT * FROM locations")
                while (cursor.moveToNext()) {
                    val itemCode = cursor.getString(cursor.getColumnIndex("code"))
                    val itemGeoJson = cursor.getString(cursor.getColumnIndex("geo_json"))
                    val geometry = migrateFeatureToGeometry(itemGeoJson)
                    val color = cursor.getInt(cursor.getColumnIndex("color"))
                    val title = cursor.getString(cursor.getColumnIndex("displayed_name")) ?: "not set"
                    val accuracy = cursor.getInt(cursor.getColumnIndex("accuracy"))
                    val favorite = cursor.getInt(cursor.getColumnIndex("favorite"))
                    val speed = cursor.getInt(cursor.getColumnIndex("speed"))
                    val selected = 0

                    geometry?.let {
                        database.execSQL("INSERT INTO new_locations (code, geom, color, title, accuracy, favorite, speed, selected) VALUES (\""
                                + itemCode + "\", \""
                                + it + "\", \""
                                + color + "\", \""
                                + title + "\", \""
                                + accuracy + "\", \""
                                + favorite + "\", \""
                                + speed + "\", \""
                                + selected + "\")"
                        )
                    }
                }

                database.execSQL("DROP TABLE locations")
                database.execSQL("ALTER TABLE new_locations RENAME TO locations")
            }
        }

        private fun migrateFeatureToGeometry(itemGeoJson: String): String? {
            try {
                val geoJsonFeature = GeoJsonParser.parseFeature(JSONObject(itemGeoJson))
                return AppUtil.geometryToString(geoJsonFeature.geometry)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return null
        }

        @JvmStatic
        val locationsDb: Builder<AppDatabase>
            get() = Room.databaseBuilder(App.sInstance, AppDatabase::class.java,
                    DB_NAME_LOCATIONS)
                    .addMigrations(MIGRATION_1_2)
    }
}