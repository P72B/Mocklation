package de.p72b.mocklation.util;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class AppUtil {
    private static final String COORDINATE_DECIMAL_FORMAT = "%.6f";


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

    public static String getFormattedTimeStamp(Calendar calendar) {

        int minute = calendar.get(Calendar.MINUTE);
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) +
                " " + calendar.get(Calendar.DAY_OF_MONTH) +
                " " + new SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.getTime()) +
                " " + calendar.get(Calendar.YEAR) +
                "  " + calendar.get(Calendar.HOUR_OF_DAY) +
                ":" + (minute < 10 ? "0" + minute : minute) +
                " GMT" + timeZone();
    }

    public static String getFormattedCoordinates(LatLng latLng) {
        LatLng roundedLatLng = AppUtil.roundLatLng(latLng);
        return roundedLatLng.latitude + " / " + roundedLatLng.longitude;
    }

    public static String timeZone() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault());
        String   timeZone = new SimpleDateFormat("Z").format(calendar.getTime());
        return timeZone.substring(0, 3) + ":"+ timeZone.substring(3, 5);
    }
}
