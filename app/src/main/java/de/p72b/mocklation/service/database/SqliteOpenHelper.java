package de.p72b.mocklation.service.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "app.db";

    private static final String DATABASE_LOCATIONS_CREATE = "create table "
            + LocationItem.TABLE + "("
            + LocationItem.COLUMN_CODE + " text primary key,"
            + LocationItem.COLUMN_GEOJSON + " text not null,"
            + LocationItem.COLUMN_SPEED + " integer,"
            + LocationItem.COLUMN_ACCURACY + " integer"
            + ");";

    public SqliteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.rawQuery("PRAGMA journal_mode=WAL;", null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_LOCATIONS_CREATE);

        // TODO: remove test item here
        db.insert(LocationItem.TABLE, null, new LocationItem.Builder()
                .code("berlin")
                .geojson("{'type':'Feature','properties':{},'geometry':{'type':'Point','coordinates':[13.388825,52.517043]}}")
                .accuracy(6)
                .speed(0)
                .build());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < DATABASE_VERSION) {
            db.execSQL(DATABASE_LOCATIONS_CREATE);
        }
    }
}
