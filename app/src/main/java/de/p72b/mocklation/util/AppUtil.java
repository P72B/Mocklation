package de.p72b.mocklation.util;

import android.animation.Animator;
import android.animation.ValueAnimator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

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

    public static void removeMarkerAnimated(final Marker marker, final long duration, long delay) {
        // Animate marker
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = valueAnimator.getAnimatedFraction();
                marker.setAlpha(1f - value);
            }
        });

        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                marker.remove();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        valueAnimator.setFloatValues(0, 1); // Ignored.
        valueAnimator.setDuration(duration);
        valueAnimator.setStartDelay(delay);
        valueAnimator.start();
    }
}
