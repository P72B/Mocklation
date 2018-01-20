package de.p72b.mocklation.notification;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import de.p72b.mocklation.R;
import de.p72b.mocklation.service.AppServices;
import de.p72b.mocklation.service.location.MockLocationService;
import de.p72b.mocklation.service.setting.Setting;
import de.p72b.mocklation.util.AppUtil;


public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = NotificationBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (!isServiceRunning(context, MockLocationService.class)) {
            closeNotification(context);
            ((Setting) AppServices.getService(AppServices.SETTINGS)).setMockLocationItemCode(null);
            Toast.makeText(context, R.string.error_1018, Toast.LENGTH_LONG).show();
            return;
        }
        switch (action) {
            case MockLocationService.NOTIFICATION_ACTION_PAUSE:
                Log.d(TAG, "Pause service");
                AppUtil.sendLocalBroadcast(context, new Intent(MockLocationService.EVENT_PAUSE));
                break;
            case MockLocationService.NOTIFICATION_ACTION_PLAY:
                Log.d(TAG, "Play service");
                AppUtil.sendLocalBroadcast(context, new Intent(MockLocationService.EVENT_PLAY));
                break;
            case MockLocationService.NOTIFICATION_ACTION_STOP:
                Log.d(TAG, "Stop service");
                AppUtil.sendLocalBroadcast(context, new Intent(MockLocationService.EVENT_STOP));
                break;
        }
    }

    private void closeNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(MockLocationService.NOTIFICATION_ID);
        }
    }

    private boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
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
}
