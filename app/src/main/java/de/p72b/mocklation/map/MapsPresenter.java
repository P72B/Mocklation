package de.p72b.mocklation.map;

import android.app.Activity;
import android.arch.persistence.room.Room;
import android.location.Location;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.Display;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import de.p72b.mocklation.R;
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

    MapsPresenter(Activity activity, IPermissionService permissionService, ISetting setting) {
        Log.d(TAG, "new MapsPresenter");
        mActivity = activity;
        mView = (IMapsView) activity;
        mPermissionService = permissionService;
        mSetting = setting;
        mDb = Room.databaseBuilder(mActivity, AppDatabase.class, AppDatabase.DB_NAME_LOCATIONS).build();
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
}
