package de.p72b.mocklation.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        FeatureEntity::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class FeatureDatabase : RoomDatabase() {
    abstract fun featureDao(): FeatureDao

    companion object {
        lateinit var database: FeatureDatabase
            private set

        fun provide(context: Context) {
            database = Room.databaseBuilder(context, FeatureDatabase::class.java, "mocklation.db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}