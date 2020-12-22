package de.p72b.mocklation.service.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {LocationItem.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public static final String DB_NAME_LOCATIONS = "locations";
    public abstract LocationItemDao locationItemDao();
}
