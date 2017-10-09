package de.p72b.mocklation.map;


import android.view.View;

import com.google.android.gms.maps.model.LatLng;

public interface IMapsView {

    void selectLocation(LatLng latLng, String id, float zoom);

    void showMyLocation();

    void showSnackbar(int message, int action, View.OnClickListener listener, int duration);
}
