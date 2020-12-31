package de.p72b.mocklation.main;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import de.p72b.locator.location.LocationManager;
import de.p72b.mocklation.BuildConfig;
import de.p72b.mocklation.R;
import de.p72b.mocklation.service.location.MockLocationService;
import de.p72b.mocklation.service.setting.ISetting;
import de.p72b.mocklation.util.AppUtil;
import de.p72b.mocklation.util.Logger;

public class MockServiceInteractor implements IMockServiceInteractor {

    static final int PERMISSIONS_MOCKING = 115;
    static final int REQUEST_CODE_DEFAULT_MOCK_APP = 9902;
    static final int REQUEST_CODE_ENABLE_DEVELOPER_OPTIONS = 9877;
    static final int SERVICE_STATE_STOP = 0;
    static final int SERVICE_STATE_RUNNING = 1;
    static final int SERVICE_STATE_PAUSE = 2;

    private static final String TAG = MockServiceInteractor.class.getSimpleName();
    private final ISetting mSetting;
    private Activity mActivity;
    private LocationManager mLocationManager;
    private List<Class<?>> mRunningServices;
    private Context mContext;
    private String mLocationItemCode;
    private MockServiceListener mListener;
    private @ServiceStatus int mState;
    private final BroadcastReceiver mLocalAppBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Logger.d(TAG, "Received local broadcast: " + AppUtil.toString(intent));
            final String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (action) {
                case MockLocationService.EVENT_PAUSE:
                    Logger.d(TAG, "Pause service");
                    mState = SERVICE_STATE_PAUSE;
                    break;
                case MockLocationService.EVENT_PLAY:
                    Logger.d(TAG, "Play service");
                    mState = SERVICE_STATE_RUNNING;
                    break;
                case MockLocationService.EVENT_STOP:
                    Logger.d(TAG, "Stop service");
                    mState = SERVICE_STATE_STOP;
                    if (mRunningServices.contains(MockLocationService.class)) {
                        mRunningServices.remove(MockLocationService.class);
                    }
                    break;
            }
            if (mListener != null) {
                mListener.onUpdate();
            }
        }
    };

    MockServiceInteractor(Activity activity, ISetting setting, MockServiceListener listener, LocationManager locationManager) {
        mLocationManager = locationManager;
        mActivity = activity;
        mSetting = setting;
        mContext = activity.getApplicationContext();
        mRunningServices = new ArrayList<>();
        mListener = listener;
        mState = isServiceRunning() ? SERVICE_STATE_RUNNING : SERVICE_STATE_STOP;
        AppUtil.registerLocalBroadcastReceiver(
                mActivity,
                mLocalAppBroadcastReceiver,
                MockLocationService.EVENT_PAUSE,
                MockLocationService.EVENT_PLAY,
                MockLocationService.EVENT_STOP);
    }

    @Override
    public void onDefaultMockAppRequest(int results) {
        if (isDefaultAppForMockLocations()) {
            Logger.d(TAG, "MockLocations is now enabled APP for Mocklation");
            startMockLocationService(MockLocationService.class);
        } else {
            Toast.makeText(mContext, R.string.error_1019, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMockPermissionsResult(int[] grantResults) {
        if (hasRequiredPermissions()) {
            checkDefaultMockLocationApp();
            return;
        }
        if (grantResults.length < 3) {
            Toast.makeText(mContext, R.string.error_1022, Toast.LENGTH_LONG).show();
            return;
        }
        final boolean hasFineLocationAccess = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        final boolean hasBackgroundLocationAccess = grantResults[1] == PackageManager.PERMISSION_GRANTED;
        final boolean hasCoarseLocationAccess = grantResults[2] == PackageManager.PERMISSION_GRANTED;

        boolean isGranted = hasFineLocationAccess && hasCoarseLocationAccess;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isGranted = hasBackgroundLocationAccess && isGranted;
        }

        if (isGranted) {
            checkDefaultMockLocationApp();
        } else {
            Toast.makeText(mContext, R.string.error_1023, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void pauseMockLocationService() {
        if (isServiceRunning()) {
            AppUtil.sendLocalBroadcast(mContext, new Intent(
                    MockLocationService.EVENT_PAUSE));
            mState = SERVICE_STATE_PAUSE;
        }
    }

    @Override
    public void playMockLocationService() {
        if (isServiceRunning()) {
            AppUtil.sendLocalBroadcast(mContext, new Intent(
                    MockLocationService.EVENT_PLAY));
            mState = SERVICE_STATE_RUNNING;
        }
    }

    @Override
    public void stopMockLocationService() {
        if (isServiceRunning()) {
            stopMockLocationService(MockLocationService.class);
        }
    }

    @Override
    public boolean isServiceRunning() {
        return isServiceRunning(MockLocationService.class);
    }

    @Override
    public boolean hasRequiredPermissions() {
        boolean allPermissionsGranted = true;
        for (String permission : getRequiredPermissions()) {
            if (!hasPermission(permission)) {
                Logger.d(TAG, permission + " not granted.");
                allPermissionsGranted = false;
                break;
            }
        }
        return allPermissionsGranted;
    }

    @Override
    public void setLocationItem(String code) {
        mLocationItemCode = code;
    }

    @Override
    public void startMockLocation(@NonNull String locationItemCode) {
        setLocationItem(locationItemCode);
        Logger.d(TAG, "startMockLocation");

        if (hasRequiredPermissions()) {
            Logger.d(TAG, "All permissions are granted.");
            checkDefaultMockLocationApp();
        } else {
            Logger.d(TAG, "Some permissions aren't granted.");
            requestRequiredPermissions();
        }
    }

    @Override
    public void requestRequiredPermissions() {
        ActivityCompat.requestPermissions(
                mActivity,
                getRequiredPermissions(),
                PERMISSIONS_MOCKING);
    }

    @Override
    public @ServiceStatus int getState() {
        return mState;
    }

    @NonNull
    private String[] getRequiredPermissions() {
        String[] permissionsToBeRequired = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsToBeRequired = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }
        return permissionsToBeRequired;
    }

    private void startMockLocationService(Class<?> service) {
        if (mLocationItemCode == null) {
            return;
        }

        if (isServiceRunning(service)) {
            Logger.d(TAG, service.getSimpleName() + " is already running");
            return;
        }


        mRunningServices.add(service);
        Logger.d(TAG, "START: " + service.getSimpleName());
        Intent intent = new Intent(mContext, service);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mSetting.setMockLocationItemCode(mLocationItemCode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mActivity.startForegroundService(intent);
        } else {
            mActivity.startService(intent);
        }
        mState = SERVICE_STATE_RUNNING;
        if (mListener != null) {
            mListener.onStart();
        }
    }

    private void stopMockLocationService(Class<?> service) {
        if (!isServiceRunning(service)) {
            Logger.d(TAG, service.getSimpleName() + " not running");
            return;
        }

        Logger.d(TAG, "STOP: " + service.getSimpleName());
        mActivity.stopService(new Intent(mContext, service));

        if (mRunningServices.contains(service)) {
            mRunningServices.remove(service);
        }
        mState = SERVICE_STATE_STOP;
        if (mListener != null) {
            mListener.onStop();
        }

    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void checkDefaultMockLocationApp() {
        Logger.d(TAG, "checkDefaultMockLocationApp");
        if (isDefaultAppForMockLocations()) {
            Logger.d(TAG, "MockLocations is enabled APP for Mocklation");
            startMockLocationService(MockLocationService.class);
        } else {
            try {
                mActivity.startActivityForResult(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS), REQUEST_CODE_DEFAULT_MOCK_APP);
            } catch (ActivityNotFoundException activityNotFound) {
                Toast.makeText(mContext, R.string.error_1019, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean areDeveloperOptionsEnabled() {
       return Settings.Secure.getInt(mActivity.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED , 0) == 0;
    }

    @Override
    public void requestEnableDeveloperOptions() {
        mActivity.startActivityForResult(new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS), REQUEST_CODE_ENABLE_DEVELOPER_OPTIONS);
    }

    @Override
    public void requestSetMockLocationApp() {
        checkDefaultMockLocationApp();
    }

    @Override
    public boolean isDefaultAppForMockLocations() {
        boolean isMockLocation = false;
        try {
            //if marshmallow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AppOpsManager opsManager = (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
                if (opsManager != null) {
                    isMockLocation = (opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), BuildConfig.APPLICATION_ID) == AppOpsManager.MODE_ALLOWED);
                }
            } else {
                // in marshmallow this will always return true
                isMockLocation = !android.provider.Settings.Secure.getString(mContext.getContentResolver(), "mock_location").equals("0");
            }
        } catch (Exception e) {
            return false;
        }
        return isMockLocation;
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    public interface MockServiceListener {
        void onStart();
        void onStop();
        void onUpdate();
    }

    @IntDef({SERVICE_STATE_RUNNING, SERVICE_STATE_STOP, SERVICE_STATE_PAUSE})
    @Retention(RetentionPolicy.SOURCE)
    @interface ServiceStatus {
    }
}
