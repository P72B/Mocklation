package de.p72b.mocklation.service.room;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

@Dao
public interface LocationItemDao {
    @Query("SELECT * FROM locations")
    Maybe<List<LocationItem>> getAll();

    @Query("SELECT * FROM locations WHERE code IN (:locationItemIds)")
    Flowable<List<LocationItem>> loadAllByIds(int[] locationItemIds);

    @Query("SELECT * FROM locations where code = :code")
    Single<LocationItem> findByCode(String code);

    @Query("SELECT * FROM locations where displayed_name = :displayedName")
    Maybe<List<LocationItem>> findByDisplayedName(String displayedName);


    @Insert
    void insertAll(LocationItem... locationItems);

    @Delete
    void delete(LocationItem locationItem);

    @Update
    void updateLocationItems(LocationItem... locationItems);
}
