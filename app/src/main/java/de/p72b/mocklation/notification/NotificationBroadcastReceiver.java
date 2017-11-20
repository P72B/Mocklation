package de.p72b.mocklation.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.p72b.mocklation.service.location.MockLocationService;
import de.p72b.mocklation.util.AppUtil;


public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = NotificationBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action == null) {
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
}
