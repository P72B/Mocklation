package de.p72b.mocklation.map;

import android.app.Activity;
import android.content.ContentValues;
import android.location.Location;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.squareup.sqlbrite2.BriteDatabase;

import javax.inject.Inject;

import de.p72b.mocklation.R;
import de.p72b.mocklation.dagger.MocklationApp;
import de.p72b.mocklation.service.database.LocationItem;
import de.p72b.mocklation.service.permission.IPermissionService;
import de.p72b.mocklation.service.setting.ISetting;
import de.p72b.mocklation.util.AppUtil;
import io.reactivex.disposables.CompositeDisposable;

public class MapsPresenter implements IMapsPresenter {

    private static final String TAG = MapsPresenter.class.getSimpleName();
    private final IPermissionService mPermissionService;
    private IMapsView mView;
    private Activity mActivity;
    @Inject
    BriteDatabase db;
    private Pair<String, ContentValues> mOnTheMapItemPair;
    private ISetting mSetting;
    private CompositeDisposable mDisposables = new CompositeDisposable();

    MapsPresenter(Activity activity, IPermissionService permissionService, ISetting setting) {
        Log.d(TAG, "new MapsPresenter");
        mActivity = activity;
        mView = (IMapsView) activity;
        mPermissionService = permissionService;
        mSetting = setting;
        MocklationApp.getComponent(mActivity).inject(this);
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
        mOnTheMapItemPair = new Pair<>(code, new LocationItem.Builder()
                .code(code)
                .geojson("{'type':'Feature','properties':{},'geometry':{'type':'Point','coordinates':[" + roundedLatLng.longitude + "," + roundedLatLng.latitude + "]}}")
                .accuracy(6)
                .speed(0)
                .build()
        );

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
                db.insert(LocationItem.TABLE, mOnTheMapItemPair.second);
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
}
