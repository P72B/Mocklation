package de.p72b.mocklation.service.permission;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;


import de.p72b.mocklation.service.setting.ISetting;

public interface IPermissionService {

    void requestPermission(FragmentActivity activity, int requestCode);

    void requestPermissions(FragmentActivity activity, String[] permission, int requestCode);

    void permissionGranted(String permission, int requestCode);

    void permissionDenied(String permission, FragmentActivity activity, int requestCode);

    void onPermissionsChanged(int requestCode, String[] permissions, int[] grantResults);

    boolean hasPermission(FragmentActivity activity);

    boolean hasPermission(Context activity);

    void subscribeToPermissionChanges(OnPermissionChanged listener);

    void unSubscribeToPermissionChanges(OnPermissionChanged listener);

    boolean shouldShowRequestPermissionRationale(@NonNull Activity activity,
                                                 @NonNull String permission);

    interface OnPermissionChanged {

        void onPermissionChanged(String permission, boolean granted, int requestCode);

        void onPermissionsChanged(int requestCode, String[] permissions, int[] grantResults);
    }
}

