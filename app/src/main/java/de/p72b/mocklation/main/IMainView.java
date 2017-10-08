package de.p72b.mocklation.main;


import android.view.View;

import java.util.List;

import de.p72b.mocklation.service.database.LocationItem;

public interface IMainView {

    void showSavedLocations(List<LocationItem> locationItems);

    void selectLocation(LocationItem item);

    void showEmptyPlaceholder();

    void setPlayStopStatus(int state);

    void showSnackbar(int message, int action, View.OnClickListener listener, int duration);
}
