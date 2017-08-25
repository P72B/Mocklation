package de.p72b.mocklation.map;


import com.google.android.gms.maps.model.LatLng;

public interface IMapsView {
    void showMessage(String message);

    void selectLocation(LatLng latLng, String id, float zoom);
}
