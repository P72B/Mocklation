package de.p72b.mocklation.map;

import android.location.Location;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public interface IMapsPresenter {

    void onStart();

    void onStop();

    void onDestroy();

    void onMapLongClicked(LatLng latLng);

    void onMarkerClicked(Marker marker);

    void setLastKnownLocation(Location location);

    void onClick(View view);

    void onMapReady();

    void removeMarker();
}
