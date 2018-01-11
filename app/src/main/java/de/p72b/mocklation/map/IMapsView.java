package de.p72b.mocklation.map;


import android.view.View;

import java.util.List;

import de.p72b.mocklation.service.room.LocationItem;

public interface IMapsView {

    void showBottomSheet(LocationItem item);

    void showMyLocation();

    void showSnackbar(int message, int action, View.OnClickListener listener, int duration);

    void setAddress(String formattedAddress, String title);

    void setAddressProgressbarVisibility(int visibility);

    void addMarkers(List<LocationItem> items);

    void addNewMarker(LocationItem item);

}
