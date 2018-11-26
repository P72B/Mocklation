package de.p72b.mocklation.service.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

import de.p72b.mocklation.dagger.MocklationApp;

@Database(entities = {LocationItem.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DB_NAME_LOCATIONS = "locations";

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE locations ADD COLUMN mode TEXT");
            database.execSQL("UPDATE locations SET mode='FIXED'");
        }
    };

    @NonNull
    public static RoomDatabase.Builder<AppDatabase> getLocationsDb() {
        return Room.databaseBuilder(MocklationApp.getInstance(), AppDatabase.class,
                AppDatabase.DB_NAME_LOCATIONS)
                .addMigrations(MIGRATION_1_2);
    }

    public abstract LocationItemDao locationItemDao();
}
