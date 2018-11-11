package de.p72b.mocklation.service.location;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geojson.GeoJsonPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.p72b.mocklation.R;
import de.p72b.mocklation.main.MainActivity;
import de.p72b.mocklation.notification.NotificationBroadcastReceiver;
import de.p72b.mocklation.service.AppServices;
import de.p72b.mocklation.service.analytics.AnalyticsService;
import de.p72b.mocklation.service.analytics.IAnalyticsService;
import de.p72b.mocklation.service.permission.IPermissionService;
import de.p72b.mocklation.service.room.AppDatabase;
import de.p72b.mocklation.service.room.LocationItem;
import de.p72b.mocklation.service.setting.ISetting;
import de.p72b.mocklation.service.setting.Setting;
import de.p72b.mocklation.util.AppUtil;
import de.p72b.mocklation.util.Logger;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MockLocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, IPermissionService.OnPermissionChanged {

    private static final String TAG = MockLocationService.class.getSimpleName();
    public static final int PERMISSION_REQUEST_CODE = 97;
    public static final int NOTIFICATION_ID = 909;
    public static final String NOTIFICATION_ACTION_PAUSE = "NOTIFICATION_ACTION_PAUSE";
    public static final String NOTIFICATION_ACTION_PLAY = "NOTIFICATION_ACTION_PLAY";
    public static final String NOTIFICATION_ACTION_STOP = "NOTIFICATION_ACTION_STOP";
    public static final String EVENT_PAUSE = "EVENT_PAUSE";
    public static final String EVENT_PLAY = "EVENT_PLAY";
    public static final String EVENT_STOP = "EVENT_STOP";
    private static final String NOTIFICATION_CHANNEL_ID = "MOCK_LOCATION_NOTIFICATION";
    private static final String MOCKLOCATION_PROVIDER_NAME = "gps";

    @Nullable
    private GoogleApiClient mGoogleApiClient;
    private IPermissionService mPermissions;
    private ISetting mSetting;
    private GpsLocationListener mGpsLocationListener;
    private LocationManager mLocationManager;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotifyBuilder;
    private int mNumMessages;
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private List<LatLng> mLatLngList = new ArrayList<>();
    private AppDatabase mDb;
    private Disposable mDisposableFindByCode;
    private LocationUpdateInterval mMockLocationUpdateInterval;
    private LocationItem mLocationItem;
    private IAnalyticsService mAnalyticsService;
    private String mCachedLocationId = null;
    private final BroadcastReceiver mLocalAppBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Logger.d(TAG, "Received local broadcast: " + AppUtil.toString(intent));
            final String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (action) {
                case EVENT_PAUSE:
                    Logger.d(TAG, "Pause service");
                    mNotifyBuilder.mActions.clear();
                    mNotifyBuilder.setColor(getResources().getColor(R.color.mouth));
                    mNotifyBuilder.addAction(getPlayAction());
                    mNotifyBuilder.addAction(getStopAction());
                    pause();
                    mNotificationManager.notify(
                            NOTIFICATION_ID,
                            mNotifyBuilder.build());
                    break;
                case EVENT_PLAY:
                    Logger.d(TAG, "Play service");
                    mNotifyBuilder.mActions.clear();
                    mNotifyBuilder.setColor(getResources().getColor(R.color.dark_green));
                    mNotifyBuilder.addAction(getPauseAction());
                    mNotifyBuilder.addAction(getStopAction());
                    play();
                    mNotificationManager.notify(
                            NOTIFICATION_ID,
                            mNotifyBuilder.build());
                    break;
                case EVENT_STOP:
                    Logger.d(TAG, "Stop service");
                    stopService(new Intent(getApplication(), MockLocationService.class));
                    break;
            }
        }
    };

    public MockLocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPermissions = (IPermissionService) AppServices.getService(AppServices.PERMISSIONS);
        mAnalyticsService = (IAnalyticsService) AppServices.getService(AppServices.ANALYTICS);
        mPermissions.subscribeToPermissionChanges(this);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mSetting = (Setting) AppServices.getService(AppServices.SETTINGS);
        mDb = Room.databaseBuilder(this, AppDatabase.class, AppDatabase.DB_NAME_LOCATIONS).build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Logger.d(TAG, "onConnected");
        checkPermissionAndStart();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Logger.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Logger.d(TAG, "onConnectionFailed");
    }

    @Override
    public void onPermissionChanged(String permission, boolean granted, int code) {
        Logger.d(TAG, "onPermissionChanged permission: " + permission + " granted: " + granted);
        if (!Manifest.permission.ACCESS_FINE_LOCATION.equals(permission) || PERMISSION_REQUEST_CODE != code) {
            return;
        }
        if (granted) {
            // lets start listening on location changes again using fused location

        }
    }

    @Override
    public void onPermissionsChanged(int requestCode, String[] permissions, int[] grantResults) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(TAG, "onStartCommand");

        if (mGoogleApiClient.isConnected()) {
            checkPermissionAndStart();
        } else {
            mGoogleApiClient.connect();
        }
        AppUtil.registerLocalBroadcastReceiver(
                getApplicationContext(),
                mLocalAppBroadcastReceiver,
                EVENT_PAUSE,
                EVENT_PLAY,
                EVENT_STOP);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG, "onDestroy");
        mAnalyticsService.trackEvent(AnalyticsService.Event.STOP_MOCK_LOCATION_SERVICE);
        mSetting.setMockLocationItemCode(null);
        dismissNotification();

        reset();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Logger.d(TAG, "do mGoogleApiClient disconnect");
            mGoogleApiClient.disconnect();
        }
        mPermissions.unSubscribeToPermissionChanges(this);

        mDisposables.clear();

        AppUtil.unregisterLocalBroadcastReceiver(getApplicationContext(),
                mLocalAppBroadcastReceiver);

        super.onDestroy();
    }

    @SuppressWarnings("MissingPermission")
    private void checkPermissionAndStart() {
        final String code = mSetting.getMockLocationItemCode();
        Logger.d(TAG, "code: " + code + " hasPermission: " + mPermissions.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION));
        if (mPermissions.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                && code != null) {
            requestLocationItem(code);
        }
    }

    private void requestLocationItem(String code) {
        Logger.d(TAG, "requestLocationItem");
        mDisposableFindByCode = mDb.locationItemDao().findByCode(code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LocationItemObserver());
        mDisposables.add(mDisposableFindByCode);
    }

    private void reset() {
        Logger.d(TAG, "reset()");

        mDisposables.clear();

        if (mGpsLocationListener != null) {
            mLocationManager.removeUpdates(mGpsLocationListener);
        }
        mGpsLocationListener = null;
    }

    private void processLocationItem() {
        Logger.d(TAG, "processLocationItem");
        reset();

        play();

        createNotification();
    }

    private void createNotification() {
        String channelId = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channelId = "mocklation_channel";
            CharSequence channelName = "Mocklation Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(false);
            notificationChannel.setSound(null, null);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        mNumMessages = 0;
        mNotifyBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_location_on_black_24dp)
                .setContentTitle(mLocationItem.getDisplayedName());

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mNotifyBuilder.setContentIntent(resultPendingIntent);
        mNotifyBuilder.setColor(getResources().getColor(R.color.dark_green));
        mNotifyBuilder.addAction(getPauseAction());
        mNotifyBuilder.addAction(getStopAction());
        mNotifyBuilder.setOngoing(true);
        mNotifyBuilder.setVibrate(new long[]{0L});
        if (channelId != null) {
            mNotifyBuilder.setChannelId(channelId);
        } else {
            mNotifyBuilder.setAutoCancel(false);
        }
        final Notification notification = mNotifyBuilder.build();
        notification.defaults = 0;

        notify(notification);
    }

    private NotificationCompat.Action getPauseAction() {
        Intent intent = new Intent(getApplicationContext(), NotificationBroadcastReceiver.class);
        intent.setAction(NOTIFICATION_ACTION_PAUSE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return new NotificationCompat.Action.Builder(R.drawable.ic_pause_black_24dp,
                getString(R.string.action_pause), pendingIntent).build();
    }

    private NotificationCompat.Action getPlayAction() {
        Intent intent = new Intent(getApplicationContext(), NotificationBroadcastReceiver.class);
        intent.setAction(NOTIFICATION_ACTION_PLAY);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return new NotificationCompat.Action.Builder(R.drawable.ic_play_arrow_black_24dp,
                getString(R.string.action_play), pendingIntent).build();
    }

    private NotificationCompat.Action getStopAction() {
        Intent intent = new Intent(getApplicationContext(), NotificationBroadcastReceiver.class);
        intent.setAction(NOTIFICATION_ACTION_STOP);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return new NotificationCompat.Action.Builder(R.drawable.ic_stop_black_24dp,
                getString(R.string.action_stop), pendingIntent).build();
    }

    private void updateNotification(Location location) {
        mNotifyBuilder.setContentText(location.getLatitude() + " / " + location.getLongitude())
                .setNumber(++mNumMessages);
        notify(mNotifyBuilder.build());
    }

    private void notify(@NonNull final Notification notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, notification);
        } else {
            mNotificationManager.notify(
                    NOTIFICATION_ID,
                    notification);
        }
    }

    private void dismissNotification() {
        if (mNotificationManager != null) {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }

    public LocationUpdateInterval getUpdateInterval() {
        return Observable.interval(0, 1, TimeUnit.SECONDS)
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new LocationUpdateInterval());
    }

    @SuppressWarnings("MissingPermission")
    private void play() {
        mAnalyticsService.trackEvent(AnalyticsService.Event.START_MOCK_LOCATION_SERVICE);
        LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true);

        mGpsLocationListener = new GpsLocationListener();

        mLocationManager.requestLocationUpdates(MOCKLOCATION_PROVIDER_NAME, 0, 0, mGpsLocationListener);
        mLocationManager.addTestProvider(MOCKLOCATION_PROVIDER_NAME, false, false,
                false, false, true, true, true, 0, 5);

        mMockLocationUpdateInterval = getUpdateInterval();
        mDisposables.add(mMockLocationUpdateInterval);
    }

    @SuppressWarnings("MissingPermission")
    private void pause() {
        mAnalyticsService.trackEvent(AnalyticsService.Event.PAUSE_MOCK_LOCATION_SERVICE);
        LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, false);

        mLocationManager.removeUpdates(mGpsLocationListener);
        mLocationManager.removeTestProvider(MOCKLOCATION_PROVIDER_NAME);

        if (mMockLocationUpdateInterval != null && !mMockLocationUpdateInterval.isDisposed()) {
            mMockLocationUpdateInterval.dispose();
            mDisposables.remove(mMockLocationUpdateInterval);
        }
    }

    private class GpsLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Logger.d(TAG, "GpsLocationListener onLocationChanged Location: " + location.getLatitude() + " / " + location.getLongitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Logger.d(TAG, "GpsLocationListener onStatusChanged provider: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Logger.d(TAG, "GpsLocationListener onProviderEnabled provider: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Logger.d(TAG, "GpsLocationListener onProviderDisabled provider: " + provider);
        }
    }

    private class LocationItemObserver implements Consumer<LocationItem> {
        @Override
        public void accept(LocationItem locationItem) throws Exception {
            mDisposables.remove(mDisposableFindByCode);
            mLocationItem = locationItem;
            if (mLocationItem == null) {
                return;
            }

            Logger.d(TAG, " item: " + mLocationItem.getCode() + "\n" + mLocationItem.getGeoJson());

            // parse geojson feature
            LocationItemFeature feature = mLocationItem.deserialize();

            switch (feature.getGeoJsonFeature().getGeometry().getType()) {
                case "Point":
                    GeoJsonPoint point = (GeoJsonPoint) feature.getGeoJsonFeature().getGeometry();
                    mLatLngList.add(point.getCoordinates());
                    break;
                default:
                    // do nothing
            }

            if (mLatLngList.size() > 0) {
                processLocationItem();
            } else {
                // TODO: no locations found
            }
        }
    }

    private class LocationUpdateInterval extends DisposableObserver<Long> {
        @SuppressWarnings("MissingPermission")
        @Override
        public void onNext(Long value) {
            Logger.d(TAG, " onNext : value : " + value);
            LatLng nextLatLng = mLatLngList.get(0);
            final Location location = new Location(MOCKLOCATION_PROVIDER_NAME);
            location.setLatitude(nextLatLng.latitude);
            location.setLongitude(nextLatLng.longitude);
            location.setAccuracy(6);
            location.setTime(Calendar.getInstance().getTimeInMillis());
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

            final String latestMockedLocationId = location.getLatitude() + "_" + location.getLongitude() + "_"
                    + location.getAltitude() + "_" + location.getAccuracy();
            if (!latestMockedLocationId.equals(mCachedLocationId)) {
                mCachedLocationId = latestMockedLocationId;
                updateNotification(location);
            }

            mLocationManager.setTestProviderEnabled(MOCKLOCATION_PROVIDER_NAME, true);
            mLocationManager.setTestProviderStatus(MOCKLOCATION_PROVIDER_NAME, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
            mLocationManager.setTestProviderLocation(MOCKLOCATION_PROVIDER_NAME, location);
            LocationServices.FusedLocationApi.setMockLocation(mGoogleApiClient, location);
        }

        @Override
        public void onError(Throwable e) {
            Logger.d(TAG, " onError : " + e.getMessage());
        }

        @Override
        public void onComplete() {
            Logger.d(TAG, " onComplete");
        }
    }
}

