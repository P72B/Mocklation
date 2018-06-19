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
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import de.p72b.mocklation.BuildConfig;
import de.p72b.mocklation.R;
import de.p72b.mocklation.service.location.MockLocationService;
import de.p72b.mocklation.service.setting.ISetting;
import de.p72b.mocklation.util.AppUtil;

public class MockServiceInteractor implements IMockServiceInteractor {

    static final int PERMISSIONS_MOCKING = 115;
    static final int SERVICE_STATE_STOP = 0;
    static final int SERVICE_STATE_RUNNING = 1;
    static final int SERVICE_STATE_PAUSE = 2;

    private static final String TAG = MockServiceInteractor.class.getSimpleName();
    private final ISetting mSetting;
    private Activity mActivity;
    private List<Class<?>> mRunningServices;
    private Context mContext;
    private String mLocationItemCode;
    private MockServiceListener mListener;
    private @ServiceStatus int mState;
    private final BroadcastReceiver mLocalAppBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.d(TAG, "Received local broadcast: " + AppUtil.toString(intent));
            final String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (action) {
                case MockLocationService.EVENT_PAUSE:
                    Log.d(TAG, "Pause service");
                    mState = SERVICE_STATE_PAUSE;
                    break;
                case MockLocationService.EVENT_PLAY:
                    Log.d(TAG, "Play service");
                    mState = SERVICE_STATE_RUNNING;
                    break;
                case MockLocationService.EVENT_STOP:
                    Log.d(TAG, "Stop service");
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

    MockServiceInteractor(Activity activity, ISetting setting, MockServiceListener listener) {
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
    public void onMockPermissionsResult(int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkDefaultMockLocationApp();
        } else {
            // TODO: permission is needed.
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
    public void startMockLocation(@NonNull String locationItemCode) {
        mLocationItemCode = locationItemCode;
        Log.d(TAG, "startMockLocation");
        String[] permissionsToBeRequired = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        boolean shouldRequestPermission = false;

        for (String permission : permissionsToBeRequired) {
            if (!hasPermission(permission)) {
                Log.d(TAG, permission + " not granted.");
                shouldRequestPermission = true;
                break;
            }
        }

        if (shouldRequestPermission) {
            Log.d(TAG, "Some permissions aren't granted.");

            /*// Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsToBeRequired)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {

            }
            */
            ActivityCompat.requestPermissions(
                    mActivity,
                    permissionsToBeRequired,
                    PERMISSIONS_MOCKING);
        } else {
            Log.d(TAG, "All permissions are granted.");
            checkDefaultMockLocationApp();
        }
    }

    @Override
    public @ServiceStatus int getState() {
        return mState;
    }

    private void startMockLocationService(Class<?> service) {
        if (mLocationItemCode == null) {
            return;
        }

        if (isServiceRunning(service)) {
            Log.d(TAG, service.getSimpleName() + " is already running");
            return;
        }


        mRunningServices.add(service);
        Log.d(TAG, "START: " + service.getSimpleName());
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
            Log.d(TAG, service.getSimpleName() + " not running");
            return;
        }

        Log.d(TAG, "STOP: " + service.getSimpleName());
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
        Log.d(TAG, "checkDefaultMockLocationApp");
        if (isMockLocationEnabled()) {
            Log.d(TAG, "MockLocations is enabled APP for Mocklation");
            startMockLocationService(MockLocationService.class);
        } else {
            // TODO: tutorial how to enable default permission app.
            try {
                mActivity.startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
            } catch (ActivityNotFoundException activityNotFound) {
                Toast.makeText(mContext, R.string.error_1019, Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isMockLocationEnabled() {
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
