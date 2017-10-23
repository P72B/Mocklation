package de.p72b.mocklation.service.location;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
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
import de.p72b.mocklation.service.AppServices;
import de.p72b.mocklation.service.permission.IPermissionService;
import de.p72b.mocklation.service.room.AppDatabase;
import de.p72b.mocklation.service.room.LocationItem;
import de.p72b.mocklation.service.setting.ISetting;
import de.p72b.mocklation.service.setting.Setting;
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
    private static final int NOTIFICATION_ID = 909;
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

    public MockLocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPermissions = (IPermissionService) AppServices.getService(AppServices.PERMISSIONS);
        mPermissions.subscribeToPermissionChanges(this);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mSetting = (Setting) AppServices.getService(AppServices.SETTINGS);
        mDb = Room.databaseBuilder(this, AppDatabase.class, AppDatabase.DB_NAME_LOCATIONS).build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
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
        Log.d(TAG, "onConnected");
        checkPermissionAndStart();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }

    @Override
    public void onPermissionChanged(String permission, boolean granted, int code) {
        Log.d(TAG, "onPermissionChanged permission: " + permission + " granted: " + granted);
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
        Log.d(TAG, "onStartCommand");

        if (mGoogleApiClient.isConnected()) {
            checkPermissionAndStart();
        } else {
            mGoogleApiClient.connect();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        dismissNotification();

        reset();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.d(TAG, "do mGoogleApiClient disconnect");
            mGoogleApiClient.disconnect();
        }
        mPermissions.unSubscribeToPermissionChanges(this);

        mDisposables.clear();

        super.onDestroy();
    }

    @SuppressWarnings("MissingPermission")
    private void checkPermissionAndStart() {
        final String code = mSetting.getMockLocationItemCode();
        Log.d(TAG, "code: " + code + " hasPermission: " + mPermissions.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION));
        if (mPermissions.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                && code != null) {
            LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true);
            requestLocationItem(code);
        }
    }

    private void requestLocationItem(String code) {
        Log.d(TAG, "requestLocationItem");
        mDisposableFindByCode = mDb.locationItemDao().findByCode(code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LocationItemObserver());
        mDisposables.add(mDisposableFindByCode);
    }

    private void reset() {
        Log.d(TAG, "reset()");

        mDisposables.clear();

        if (mGpsLocationListener != null) {
            mLocationManager.removeUpdates(mGpsLocationListener);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void processLocationItem() {
        Log.d(TAG, "processLocationItem");
        reset();

        mGpsLocationListener = new GpsLocationListener();
        mLocationManager.requestLocationUpdates(MOCKLOCATION_PROVIDER_NAME, 0, 0, mGpsLocationListener);

        mLocationManager.addTestProvider(MOCKLOCATION_PROVIDER_NAME, false, false,
                false, false, true, true, true, 0, 5);

        mDisposables.add(Observable.interval(0, 1, TimeUnit.SECONDS)
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new LocationUpdateInterval()));

        createNotification();
    }

    private void createNotification() {
        mNumMessages = 0;
        mNotifyBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_location_on_black_24dp)
                .setContentTitle(getApplicationName());

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
        mNotifyBuilder.setOngoing(true);
        mNotifyBuilder.setAutoCancel(false);
        mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
    }

    public String getApplicationName() {
        ApplicationInfo applicationInfo = getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : getString(stringId);
    }

    private void updateNotification(Location location) {
        mNotifyBuilder.setContentText(location.getLatitude() + " / " + location.getLongitude())
                .setNumber(++mNumMessages);
        mNotificationManager.notify(
                NOTIFICATION_ID,
                mNotifyBuilder.build());
    }

    private void dismissNotification() {
        if (mNotificationManager != null) {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }

    private class GpsLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "GpsLocationListener onLocationChanged Location: " + location.getLatitude() + " / " + location.getLongitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "GpsLocationListener onStatusChanged provider: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "GpsLocationListener onProviderEnabled provider: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "GpsLocationListener onProviderDisabled provider: " + provider);
        }
    }

    private class LocationItemObserver implements Consumer<LocationItem> {
        @Override
        public void accept(LocationItem locationItem) throws Exception {
            mDisposables.remove(mDisposableFindByCode);

            if (locationItem == null) {
                return;
            }

            Log.d(TAG, " item: " + locationItem.getCode() + "\n" + locationItem.getGeoJson());

            // parse geojson feature
            LocationItemFeature feature = locationItem.deserialize();

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
            Log.d(TAG, " onNext : value : " + value);

            LatLng nextLatLng = mLatLngList.get(0);
            Location location = new Location(MOCKLOCATION_PROVIDER_NAME);
            location.setLatitude(nextLatLng.latitude);
            location.setLongitude(nextLatLng.longitude);
            location.setAccuracy(6);
            location.setTime(Calendar.getInstance().getTimeInMillis());
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

            updateNotification(location);

            mLocationManager.setTestProviderEnabled(MOCKLOCATION_PROVIDER_NAME, true);
            mLocationManager.setTestProviderStatus(MOCKLOCATION_PROVIDER_NAME, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
            mLocationManager.setTestProviderLocation(MOCKLOCATION_PROVIDER_NAME, location);
            LocationServices.FusedLocationApi.setMockLocation(mGoogleApiClient, location);
        }

        @Override
        public void onError(Throwable e) {
            Log.d(TAG, " onError : " + e.getMessage());
        }

        @Override
        public void onComplete() {
            Log.d(TAG, " onComplete");
        }
    }
}

