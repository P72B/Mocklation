package de.p72b.mocklation.service.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

@Dao
public interface LocationItemDao {
    @Query("SELECT * FROM locations")
    Maybe<List<LocationItem>> getAll();

    @Query("SELECT * FROM locations WHERE code IN (:locationItemIds)")
    Flowable<List<LocationItem>> loadAllByIds(int[] locationItemIds);

    @Query("SELECT * FROM locations where code = :code")
    Maybe<LocationItem> findByCode(String code);

    @Query("SELECT * FROM locations where displayed_name = :displayedName")
    Maybe<List<LocationItem>> findByDisplayedName(String displayedName);


    @Insert
    void insertAll(LocationItem... locationItems);

    @Delete
    void delete(LocationItem locationItem);

    @Update
    public void updateLocationItems(LocationItem... locationItems);
}
