package de.p72b.mocklation.service.setting;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

public class Setting implements ISetting{

    private static final String TAG = Setting.class.getSimpleName();

    public static final String PERMISSION_LOCATION = "PERMISSION_LOCATION";

    private static final String SHARED_PREFS_FILE = "omagu.settings";
    private static final String LAST_POSITION_LAT = "LAST_POSITION_LAT";
    private static final String LAST_POSITION_LNG = "LAST_POSITION_LNG";
    private static final String ACTIVE_MOCK_LOCATION_CODE = "ACTIVE_MOCK_LOCATION_CODE";
    private static final String LAST_SELECTED_LOCATION_CODE = "LAST_SELECTED_LOCATION_CODE";

    private SharedPreferences mPreferences;

    public Setting(Context context) {
        mPreferences = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
    }

    @Override
    public void saveLocation(double latitude, double longitude) {
        if (mPreferences == null) {
            return;
        }
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putFloat(LAST_POSITION_LAT, (float) latitude);
        edit.putFloat(LAST_POSITION_LNG, (float) longitude);
        edit.commit();
    }

    @Override
    public void setPermissionDecision(String permissionKey, boolean decision) {
        if (mPreferences == null) {
            return;
        }
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putBoolean(permissionKey, decision);
        edit.commit();
    }

    @Override
    public boolean getPermissionDecision(String permission) {
        if (mPreferences == null) {
            return false;
        }
        return mPreferences.getBoolean(resolvePermissionPreferencesKey(permission), false);
    }

    @Nullable
    private String resolvePermissionPreferencesKey(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return Setting.PERMISSION_LOCATION;
            default:
                return null;
        }
    }

    @Override
    public void setMockLocationItemCode(String code) {
        Log.d(TAG, "setMockLocationItemCode: " + code);
        if (mPreferences == null) {
            return;
        }
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString(ACTIVE_MOCK_LOCATION_CODE, code);
        edit.commit();
    }

    @Override
    public void saveLastPressedLocation(String code) {
        Log.d(TAG, "saveLastPressedLocation: " + code);
        if (mPreferences == null) {
            return;
        }
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString(LAST_SELECTED_LOCATION_CODE, code);
        edit.commit();
    }

    @Override
    public String getLastPressedLocationCode() {
        Log.d(TAG, "getLastPressedLocationCode mPreferences is null: " + (mPreferences != null));
        if (mPreferences == null) {
            return null;
        }
        return mPreferences.getString(LAST_SELECTED_LOCATION_CODE, null);
    }

    @Override
    public String getMockLocationItemCode() {
        Log.d(TAG, "getMockLocationItemCode mPreferences is null: " + (mPreferences != null));
        if (mPreferences == null) {
            return null;
        }
        return mPreferences.getString(ACTIVE_MOCK_LOCATION_CODE, null);
    }
}
