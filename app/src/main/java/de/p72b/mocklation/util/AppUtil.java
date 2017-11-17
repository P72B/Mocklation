package de.p72b.mocklation.util;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

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

    /**
     * Opens the keyboard.
     *
     * @param context The {@link Context} showing the keyboard.
     */
    public static void showKeyboard(Context context) {
        InputMethodManager imm = ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE));
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    /**
     * Closes the keyboard.
     *
     * @param context The {@link Context} hiding the keyboard.
     */
    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE));
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
