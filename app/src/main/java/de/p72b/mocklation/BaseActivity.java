package de.p72b.mocklation;

import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.p72b.mocklation.service.AppServices;
import de.p72b.mocklation.service.permission.IPermissionService;
import de.p72b.mocklation.service.setting.ISetting;
import de.p72b.mocklation.util.Logger;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();

    protected ISetting mSetting;
    protected IPermissionService mPermissionService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate");

        mSetting = (ISetting) AppServices.getService(AppServices.SETTINGS);
        mPermissionService = (IPermissionService) AppServices.getService(AppServices.PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        Logger.d(TAG, "onRequestPermissionsResult requestCode:" + requestCode);

        if (grantResults.length <= 0) {
            return;
        }
        // If request is cancelled, the result arrays are empty.

        mPermissionService.onPermissionsChanged(requestCode, permissions, grantResults);

        for (int i = 0; i < permissions.length; i++) {
            Logger.d(TAG, " permissions[" + i + "]:" + permissions[i] + " grantResults[" + i + "]:" + grantResults[i]);

            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                mPermissionService.permissionGranted(permissions[i], requestCode);
            } else {
                mPermissionService.permissionDenied(permissions[i], this, requestCode);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

