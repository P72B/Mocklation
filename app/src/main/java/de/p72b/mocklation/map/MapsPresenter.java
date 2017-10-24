package de.p72b.mocklation.map;

import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.os.ResultReceiver;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import de.p72b.mocklation.R;
import de.p72b.mocklation.service.geocoder.Constants;
import de.p72b.mocklation.service.geocoder.GeocoderIntentService;
import de.p72b.mocklation.service.permission.IPermissionService;
import de.p72b.mocklation.service.room.AppDatabase;
import de.p72b.mocklation.service.room.LocationItem;
import de.p72b.mocklation.service.setting.ISetting;
import de.p72b.mocklation.util.AppUtil;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class MapsPresenter implements IMapsPresenter {

    private static final String TAG = MapsPresenter.class.getSimpleName();
    private final IPermissionService mPermissionService;
    private final AppDatabase mDb;
    private IMapsView mView;
    private Activity mActivity;
    private Pair<String, LocationItem> mOnTheMapItemPair;
    private ISetting mSetting;
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private Disposable mDisposableInsertAll;
    private boolean mAddressRequested;
    private String mAddressOutput;
    private AddressResultReceiver mResultReceiver;

    MapsPresenter(Activity activity, IPermissionService permissionService, ISetting setting) {
        Log.d(TAG, "new MapsPresenter");
        mActivity = activity;
        mView = (IMapsView) activity;
        mPermissionService = permissionService;
        mSetting = setting;
        mDb = Room.databaseBuilder(mActivity, AppDatabase.class, AppDatabase.DB_NAME_LOCATIONS).build();
        mAddressRequested = false;
        mAddressOutput = "";
        mResultReceiver = new AddressResultReceiver(new Handler());

        updateUIWidgets();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mDisposables.clear();
    }

    @Override
    public void onMapLongClicked(LatLng latLng) {
        LatLng roundedLatLng = AppUtil.roundLatLng(latLng);
        Log.d(TAG, "onMapLongClicked LatLng: " + roundedLatLng.latitude + " / " + roundedLatLng.longitude);

        String code = AppUtil.createLocationItemCode(roundedLatLng);
        String geoJson = "{'type':'Feature','properties':{},'geometry':{'type':'Point','coordinates':[" + roundedLatLng.longitude + "," + roundedLatLng.latitude + "]}}";
        LocationItem item = new LocationItem(code, code, geoJson, 6, 0);
        mOnTheMapItemPair = new Pair<>(code, item);

        resolveAddressFromLocation(latLng);

        mView.selectLocation(roundedLatLng, code, -1);
    }

    @Override
    public void onMarkerClicked(Marker marker) {
        Log.d(TAG, "onMarkerClicked marker id: " + marker.getId());
    }

    @Override
    public void setLastKnownLocation(Location location) {
        Log.d(TAG, "setLastKnownLocation location:" + location.getProvider() + " "
                + location.getLatitude() + " / " + location.getLongitude() + " isMocked: "
                + location.isFromMockProvider());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save:
                if (mOnTheMapItemPair == null) {
                    mView.showSnackbar(R.string.error_1002, -1, null, Snackbar.LENGTH_LONG);
                    return;
                }

                // new item was created, not restored from mSettings
                Completable.fromAction(new Action() {
                    @Override
                    public void run() throws Exception {
                        mDb.locationItemDao().insertAll(mOnTheMapItemPair.second);
                    }
                })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable disposable) {
                                mDisposableInsertAll = disposable;
                                mDisposables.add(mDisposableInsertAll);
                            }

                            @Override
                            public void onComplete() {
                                mDisposables.remove(mDisposableInsertAll);
                                // TODO: finish map
                            }

                            @Override
                            public void onError(Throwable e) {
                                // TODO: show error
                            }
                        });
                break;
            case R.id.location:
                mView.showMyLocation();
                break;
            default:
                // do nothing;
        }
    }

    @Override
    public void onMapReady() {
    }

    @Override
    public void removeMarker() {
        mOnTheMapItemPair = null;
    }

    private void resolveAddressFromLocation(@Nullable LatLng latLng) {
        if (!Geocoder.isPresent()) {
            mView.showSnackbar(R.string.error_1007, -1, null, Snackbar.LENGTH_LONG);
            return;
        }

        if (mAddressRequested) {
            return;
        }

        if (latLng == null) {
            return;
        }

        Location location = new Location("");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        startGeocoderIntentService(location);
    }

    private void startGeocoderIntentService(@NonNull Location location) {
        mAddressRequested = true;
        updateUIWidgets();

        Intent intent = new Intent(mActivity, GeocoderIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);

        mActivity.getApplication().startService(intent);
    }

    private void updateUIWidgets() {
        mView.setAddressProgressbarVisibility(mAddressRequested ? ProgressBar.VISIBLE : ProgressBar.GONE);
    }

    private class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            mView.setAddress(mAddressOutput);

            mAddressRequested = false;
            updateUIWidgets();
        }
    }
}
