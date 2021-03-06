package de.p72b.mocklation.service.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import de.p72b.mocklation.service.setting.ISetting;
import de.p72b.mocklation.util.Logger;

public class PermissionService implements IPermissionService {
    private static final String TAG = PermissionService.class.getSimpleName();

    private CopyOnWriteArrayList<OnPermissionChanged> mSubscribers = new CopyOnWriteArrayList<>();
    private Map<String, Boolean> mPermissions = new HashMap<>();

    private ISetting mSettings;

    public PermissionService(ISetting settings) {
        mSettings = settings;
    }

    @Override
    public void requestPermission(FragmentActivity activity, int requestCode) {
        requestPermissions(activity, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
    }

    @Override
    public void requestPermissions(FragmentActivity activity, String[] permissions, int requestCode) {
        Logger.d(TAG, "requestPermission permissions");
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    @Override
    public void permissionGranted(String permission, int requestCode) {
        mPermissions.put(permission, true);
        dispatch(permission, true, requestCode);
    }

    @Override
    public void permissionDenied(String permission, FragmentActivity activity, int requestCode) {
        checkIfExplanationShouldBeShown(permission, activity);

        mPermissions.put(permission, false);
        dispatch(permission, false, requestCode);
    }

    @Override
    public void onPermissionsChanged(int requestCode, String[] permissions, int[] grantResults) {
        for (OnPermissionChanged subscriber : mSubscribers) {
            subscriber.onPermissionsChanged(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull Activity activity,
                                                        @NonNull String permission) {
        return !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
                && !mSettings.getPermissionDecision(permission);
    }

    private void checkIfExplanationShouldBeShown(String permission, FragmentActivity activity) {
        if (shouldShowRequestPermissionRationale(activity, permission)) {

            // OK, system permission dialog was not shown and permission is denied. We have still
            // some cases where we need to show the user an explanation why we need this permission
            switch (permission) {
                case Manifest.permission.ACCESS_FINE_LOCATION:
                    // Location permission is really hard needed, e.g. the user ticked "don't show
                    // again" to system permission dialog and then he retried to press locate me
                    // button on map. Yes user is stupid, how can we know the location if he doesn't
                    // allow us. So in that reason we show again our custom dialog to please enable
                    // location permission.
                    // TODO show dialog here
                    break;
                default:
                    // do nothing
                    break;
            }
        }
    }

    @Override
    public boolean hasPermission(FragmentActivity activity) {
        return hasAllNeededPermissions(activity.getApplicationContext());
    }

    @Override
    public boolean hasPermission(Context context) {
        return hasAllNeededPermissions(context);
    }

    private boolean hasAllNeededPermissions(Context context) {
        final boolean hasFinePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        final boolean hasCoarsePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean isGranted = hasFinePermission && hasCoarsePermission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            final boolean hasBackgroundPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
            isGranted = hasBackgroundPermission && isGranted;
        }
        return isGranted;
    }

    @Override
    public void subscribeToPermissionChanges(OnPermissionChanged listener) {
        mSubscribers.add(listener);
    }

    @Override
    public void unSubscribeToPermissionChanges(OnPermissionChanged listener) {
        mSubscribers.remove(listener);
    }

    private void dispatch(String permission, boolean isGranted, int requestCode) {
        Logger.d(TAG, "dispatch permission:" + permission);

        // Save the user decision if the requested permission is given or denied.
        mSettings.setPermissionDecision(permission, isGranted);

        for (OnPermissionChanged subscriber : mSubscribers) {
            subscriber.onPermissionChanged(permission, mPermissions.get(permission), requestCode);
        }
    }
}

