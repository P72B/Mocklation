package de.p72b.mocklation.service.location;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import de.p72b.mocklation.service.permission.IPermissionService;
import de.p72b.mocklation.service.setting.ISetting;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class LocationService implements ILocationService, LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, IPermissionService.OnPermissionChanged {

    private static final String TAG = LocationService.class.getSimpleName();
    private static final long INTERVAL = 1_000 * 5; // 5 seconds
    private static final long FASTEST_INTERVAL = 1_000 * 2; // 2 seconds
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters
    private static final int DELAY_FOR_RETRY_LAST_KNOWN_LOCATION = 1; // 1 second
    public static final int PERMISSION_REQUEST_CODE = 97;

    @Nullable
    private GoogleApiClient mGoogleApiClient;
    private Location mLastKnownLocation;
    private LocationRequest mLocationRequest;
    private IPermissionService mPermissions;
    private FragmentActivity mFragmentActivity;
    private ISetting mSettings;
    private boolean mInitLocationRetry = true;
    private CopyOnWriteArrayList<ILocationService.OnLocationChanged> mSubscribers = new CopyOnWriteArrayList<>();
    private CompositeDisposable mDisposables = new CompositeDisposable();

    public LocationService() {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");

        if (mPermissions.hasPermission(mFragmentActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            checkIfLocationSniffingShouldStart();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged:" + location.getLatitude() + "/" + location.getLongitude());
        mLastKnownLocation = location;
        mSettings.saveLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());

        // inform listeners
        dispatch(location);
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
            checkIfLocationSniffingShouldStart();
        }
    }

    @Override
    public void onPermissionsChanged(int requestCode, String[] permissions, int[] grantResults) {

    }

    @Override
    @Nullable
    public Location getLastKnownLocation() {
        final boolean hasPermission = mPermissions.hasPermission(mFragmentActivity, Manifest.permission.ACCESS_FINE_LOCATION);

        if (!hasPermission) {
            mPermissions.requestPermission(mFragmentActivity, Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_CODE);
        }
        return mLastKnownLocation;
    }

    @NonNull
    @Override
    public Location getRestoredLocation() {
        return null;
    }

    @Override
    public void subscribeToLocationChanges(OnLocationChanged listener) {
        if (mGoogleApiClient != null
                && mGoogleApiClient.isConnected()
                && mPermissions.hasPermission(mFragmentActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                && mSubscribers.size() == 0) {
            // We have at least one subscriber please start location updates.
            initLocationUpdateSniffing();
        }
        mSubscribers.add(listener);
    }

    @Override
    public void unSubscribeToLocationChanges(OnLocationChanged listener) {
        if (mGoogleApiClient != null
                && mGoogleApiClient.isConnected()
                && mSubscribers.size() == 1) {
            Log.d(TAG, "stop location sniffing");
            // Nobody is listening anymore. Stop getting updates to save battery.
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        mSubscribers.remove(listener);
    }

    @Override
    public void onStartCommand(FragmentActivity activity, IPermissionService permissions,
                               ISetting setting) {
        Log.d(TAG, "onStartCommand");

        mFragmentActivity = activity;
        mPermissions = permissions;
        mSettings = setting;
        mPermissions.subscribeToPermissionChanges(this);

        createLocationRequest();

        mGoogleApiClient = new GoogleApiClient.Builder(mFragmentActivity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onDestroyCommand() {
        Log.d(TAG, "onDestroyCommand");

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d(TAG, "do mGoogleApiClient disconnect");
            mGoogleApiClient.disconnect();
        }
        mPermissions.unSubscribeToPermissionChanges(this);
        mDisposables.clear();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");

        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        } else if (mPermissions.hasPermission(mFragmentActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                && mSubscribers.size() > 0) {
            // In case permissions where enabled again we restart the location updates:
            requestLocationUpdates();
        }
    }

    @SuppressWarnings("MissingPermission")
    private void requestLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void initLocationUpdateSniffing() {
        Log.d(TAG, "initLocationUpdateSniffing");
        // We can start directly to listen on location changes.
        requestLocationUpdates();

        // But we want also to know the last known location (immediately) from API without any
        // location change
        getInitialLocationWithoutChange();
    }

    @SuppressWarnings("MissingPermission")
    private void getInitialLocationWithoutChange() {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location != null) {
            Log.d(TAG, "initial last known location set");
            mLastKnownLocation = location;
            mSettings.saveLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());

            mInitLocationRetry = true;
            dispatchInitialLocation(mLastKnownLocation);
        } else {
            // from time to time on second retry a last known location can be found
            Log.d(TAG, "retry initial last known location set");
            if (mInitLocationRetry) {
                // automated retry
                mInitLocationRetry = false;

                mDisposables.add(Observable.empty()
                        .delay(DELAY_FOR_RETRY_LAST_KNOWN_LOCATION, TimeUnit.SECONDS)
                        // Run on a background thread
                        .subscribeOn(Schedulers.io())
                        // Be notified on the main thread
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(getObserver()));
            }
        }
    }

    private DisposableObserver<Object> getObserver() {
        return new DisposableObserver<Object>() {

            @Override
            public void onNext(Object value) {
                Log.d(TAG, " onNext : value : " + value);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, " onError : " + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, " onComplete");
                // Ok it can happen that during DELAY_FOR_RETRY_LAST_KNOWN_LOCATION
                // the location permission is removed (very unlikely)
                if (mPermissions.hasPermission(mFragmentActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    getInitialLocationWithoutChange();
                }
            }
        };
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(MIN_DISTANCE_CHANGE_FOR_UPDATES);
    }

    private void dispatch(Location location) {
        for (ILocationService.OnLocationChanged subscriber : mSubscribers) {
            subscriber.onLocationChanged(location);
        }
    }

    private void dispatchInitialLocation(Location location) {
        for (ILocationService.OnLocationChanged subscriber : mSubscribers) {
            subscriber.onInitialLocationDetermined(location);
        }
    }

    private void checkIfLocationSniffingShouldStart() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return;
        }

        if (mSubscribers.size() > 0) {
            initLocationUpdateSniffing();
        } else {
            getInitialLocationWithoutChange();
        }
    }
}

