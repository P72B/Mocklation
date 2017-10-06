package de.p72b.mocklation.map;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.geojson.GeoJsonPoint;
import com.squareup.sqlbrite2.BriteDatabase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.p72b.mocklation.BuildConfig;
import de.p72b.mocklation.R;
import de.p72b.mocklation.dagger.MocklationApp;
import de.p72b.mocklation.service.database.LocationItem;
import de.p72b.mocklation.service.location.LocationItemFeature;
import de.p72b.mocklation.service.location.MockLocationService;
import de.p72b.mocklation.service.permission.IPermissionService;
import de.p72b.mocklation.service.setting.ISetting;
import de.p72b.mocklation.util.AppUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class MapsPresenter implements IMapsPresenter {

    private static final String TAG = MapsPresenter.class.getSimpleName();
    private final IPermissionService mPermissionService;
    private IMapsView mView;
    private Context mContext;
    private Activity mActivity;
    private List<Class<?>> mRunningServices;
    @Inject
    BriteDatabase db;
    private Pair<String, ContentValues> mOnTheMapItemPair;
    private ISetting mSetting;
    private CompositeDisposable mDisposables = new CompositeDisposable();

    MapsPresenter(Activity activity, IPermissionService permissionService, ISetting setting) {
        Log.d(TAG, "new MapsPresenter");
        mActivity = activity;
        mView = (IMapsView) activity;
        mContext = activity.getApplicationContext();
        mPermissionService = permissionService;
        mRunningServices = new ArrayList<>();
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
            case R.id.location:
                if (mSetting.getMockLocationItemCode() != null) {
                    mSetting.setMockLocationItemCode(null);
                    if (isServiceRunning(MockLocationService.class)) {
                        stopMockLocationService(MockLocationService.class);
                    }
                } else {
                    startMockLocation();
                }
                break;
            default:
                // do nothing;
        }
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
    public void onMapReady() {
        if (mSetting.getMockLocationItemCode() != null) {
            mDisposables.add(db.createQuery(LocationItem.TABLE, AppUtil.LOCATION_ITEM_QUERY,
                    mSetting.getMockLocationItemCode())
                    .mapToList(LocationItem.MAPPER)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new LocationItemObserver())
            );
        }
    }

    private void startMockLocationService(Class<?> service) {
        if (mOnTheMapItemPair == null) {
            mView.showMessage(mContext.getString(R.string.error_missing_location_to_start_service));
            return;
        }

        if (isServiceRunning(service)) {
            Log.d(TAG, service.getSimpleName() + " is already running");
            return;
        }

        // new item was created, not restored from mSettings
        db.insert(LocationItem.TABLE, mOnTheMapItemPair.second);

        mRunningServices.add(service);
        Log.d(TAG, "START: " + service.getSimpleName());
        Intent intent = new Intent(mContext, service);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mSetting.setMockLocationItemCode(mOnTheMapItemPair.first);
        mActivity.startService(intent);
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
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
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
            Log.d(TAG, "MockLocations is enabled APP for Mockloctaion");
            startMockLocationService(MockLocationService.class);
        } else {
            Log.d(TAG, "Change settings and set Mocklation as APP for MockLocations!");
            // TODO: tutorial how to enable default permission app.
            mActivity.startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
        }
    }

    private boolean isMockLocationEnabled() {
        boolean isMockLocation = false;
        try {
            //if marshmallow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AppOpsManager opsManager = (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
                isMockLocation = (opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), BuildConfig.APPLICATION_ID) == AppOpsManager.MODE_ALLOWED);
            } else {
                // in marshmallow this will always return true
                isMockLocation = !android.provider.Settings.Secure.getString(mContext.getContentResolver(), "mock_location").equals("0");
            }
        } catch (Exception e) {
            return isMockLocation;
        }
        return isMockLocation;
    }

    private void startMockLocation() {
        Log.d(TAG, "checkPermissions");
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
                    MapsActivity.PERMISSIONS_MOCKING);
        } else {
            Log.d(TAG, "All permissions are granted.");
            checkDefaultMockLocationApp();
        }
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private class LocationItemObserver extends DisposableObserver<List<LocationItem>> {
        @Override
        public void onNext(@io.reactivex.annotations.NonNull List<LocationItem> locationItems) {
            Log.d(TAG, "onNext : value list size: " + locationItems.size());
            if (locationItems.size() > 0) {
                LocationItem item = locationItems.get(0);
                Log.d(TAG, " item[0]: " + item.code() + "\n" + item.geojosn());

                // parse geojson feature
                LocationItemFeature feature = item.deserialize();

                switch (feature.getGeoJsonFeature().getGeometry().getType()) {
                    case "Point":
                        GeoJsonPoint point = (GeoJsonPoint) feature.getGeoJsonFeature().getGeometry();
                        mView.selectLocation(point.getCoordinates(), item.code(), 16);
                        break;
                    default:
                        // do nothing
                }
            }
        }

        @Override
        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
            Log.d(TAG, " onError : " + e.getMessage());
        }

        @Override
        public void onComplete() {
            Log.d(TAG, " onComplete");
            mDisposables.remove(this);
        }
    }
}
