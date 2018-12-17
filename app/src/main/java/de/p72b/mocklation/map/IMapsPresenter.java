package de.p72b.mocklation.map;

import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public interface IMapsPresenter {

    void onDestroy();

    void onMapLongClicked(LatLng latLng);

    void onMarkerClicked(Marker marker);

    void onClick(View view);

    void onMapReady();

    void removeMarker();
}
