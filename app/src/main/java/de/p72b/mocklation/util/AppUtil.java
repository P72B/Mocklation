package de.p72b.mocklation.util;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Locale;

import de.p72b.mocklation.service.database.LocationItem;

public class AppUtil {
    private static final String COORDINATE_DECIMAL_FORMAT = "%.6f";

    public static final String LOCATION_ITEM_QUERY = "SELECT * FROM "
            + LocationItem.TABLE
            + " WHERE "
            + LocationItem.COLUMN_CODE
            + " = ? ";

    public static final String createLocationItemCode(LatLng latLng) {
        return latLng.latitude + "_" + latLng.longitude + "_" + Calendar.getInstance().getTimeInMillis();
    }

    public static LatLng roundLatLng(LatLng latLng) {
        Double lat = Double.parseDouble(String.format(Locale.ENGLISH, COORDINATE_DECIMAL_FORMAT,
                latLng.latitude));
        Double lng = Double.parseDouble(String.format(Locale.ENGLISH, COORDINATE_DECIMAL_FORMAT,
                latLng.longitude));
        return new LatLng(lat, lng);
    }
}
