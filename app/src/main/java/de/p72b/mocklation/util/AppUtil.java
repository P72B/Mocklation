package de.p72b.mocklation.util;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import de.p72b.mocklation.R;
import de.p72b.mocklation.service.room.LocationItem;

public class AppUtil {
    private static final String COORDINATE_DECIMAL_FORMAT = "%.6f";


    public static String createLocationItemCode(LatLng latLng) {
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

    private static String timeZone() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault());
        String   timeZone = new SimpleDateFormat("Z", Locale.getDefault()).format(calendar.getTime());
        return timeZone.substring(0, 3) + ":"+ timeZone.substring(3, 5);
    }

    public static void removeMarkerAnimated(final Marker marker, final long duration) {
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
        valueAnimator.setStartDelay((long) 0);
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

    /**
     * Creates a string from a {@link Bundle} for debugging purposes.
     *
     * @param bundle The bundle to create a String from.
     * @return String describing the given bundle.
     */
    private static String toString(Bundle bundle) {
        if (bundle == null || bundle.isEmpty()) {
            return null;
        }
        final StringBuilder strBuf = new StringBuilder();
        final Set<String> keys = bundle.keySet();
        Object value;
        for (String key : keys) {
            value = bundle.get(key);
            strBuf.append("\t");
            strBuf.append(key);
            if (value == null) {
                strBuf.append(":null");
                strBuf.append("\n");
            } else {
                strBuf.append("[");
                strBuf.append(value.getClass().toString());
                strBuf.append("]:");
                strBuf.append(value);
                strBuf.append("\n");
            }
        }
        return strBuf.toString();
    }

    /**
     * Creates a string from a {@link Intent} for debugging purposes.
     *
     * @param intent The intent to create a String from.
     * @return String describing the given intent.
     */
    public static String toString(Intent intent) {
        return "Intent:\n" +
                "-------\n" +
                "Action:\n" +
                intent.getAction() +
                "Data:\n" +
                intent.getDataString() +
                "Bundle:\n" +
                toString(intent.getExtras());
    }
    /**
     * Registers a given {@link BroadcastReceiver} to a list of actions.
     *
     * @param context           The {@link Context} of the {@link LocalBroadcastManager}.
     * @param broadcastReceiver The  {@link BroadcastReceiver} to register.
     * @param actions           The actions to register for.
     */
    public static void registerLocalBroadcastReceiver(
            @NonNull final Context context,
            @NonNull final BroadcastReceiver broadcastReceiver,
            @NonNull final String... actions) {
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        for (String action : actions) {
            localBroadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(action));
        }
    }

    /**
     * Unregisters a given {@link BroadcastReceiver}.
     *
     * @param context           The {@link Context} of the {@link LocalBroadcastManager}.
     * @param broadcastReceiver The  {@link BroadcastReceiver} to unregister.
     */
    public static void unregisterLocalBroadcastReceiver(
            @NonNull final Context context,
            @NonNull final BroadcastReceiver broadcastReceiver) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
    }

    /**
     * Sends a local broadcast.
     *
     * @param context The {@link Context} sending the local broadcast.
     * @param intent  The {@link Intent} to send.
     */
    public static void sendLocalBroadcast(@NonNull final Context context, @NonNull final Intent intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static TextView stripUnderlines(TextView textView) {
        Spannable s = new SpannableString(textView.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span: spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
        return textView;
    }

    public static @Nullable LatLngBounds getBounds(@Nullable List<LocationItem> locationItems) {
        if (locationItems == null || locationItems.size() <= 1) {
            return null;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LocationItem item: locationItems) {
            Object geometry = item.getGeometry();
            if (!(geometry instanceof LatLng)) {
                break;
            }
            LatLng point = (LatLng) geometry;
            builder.include(point);
        }
        return builder.build();
    }

    public static void openInCustomTab(@NonNull final Context context, @NonNull final String url, final boolean shareable) {
        final Resources resources = context.getResources();
        final Bitmap icon = BitmapFactory.decodeResource(resources, R.drawable.ic_arrow_back_black_24dp);

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(context, R.color.face))
                .enableUrlBarHiding()
                .setCloseButtonIcon(icon)
                .setShowTitle(true);

        if (shareable) {
            builder.addDefaultShareMenuItem();
        }

        builder.build().launchUrl(context, Uri.parse(url));
    }

    @NonNull
    public static SpannableString underline(@NonNull final Context context, final int master, final int snipped) {
        final String highlight = context.getString(snipped);
        final String messageContent = String.format(context.getString(master), highlight);
        final SpannableString content = new SpannableString(messageContent);
        final int start = messageContent.indexOf(highlight);
        final int end = start + highlight.length();
        content.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent)), start, end, 0);
        return content;
    }

    private static class URLSpanNoUnderline extends URLSpan {
        URLSpanNoUnderline(String url) {
            super(url);
        }
        @Override public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }
}
