package de.p72b.mocklation.map;

import android.Manifest;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import de.p72b.mocklation.BaseActivity;
import de.p72b.mocklation.R;
import de.p72b.mocklation.dagger.MocklationApp;
import de.p72b.mocklation.service.AppServices;
import de.p72b.mocklation.service.location.ILocationService;
import de.p72b.mocklation.service.setting.ISetting;

public class MapsActivity extends BaseActivity implements IMapsView, OnMapReadyCallback,
        View.OnClickListener, LocationSource, LocationSource.OnLocationChangedListener,
        ILocationService.OnLocationChanged {

    private static final String TAG = MapsActivity.class.getSimpleName();
    public static final int PERMISSIONS_MOCKING = 115;

    private IMapsPresenter mPresenter;
    private OnLocationChangedListener mMapLocationListener = null;
    private GoogleMap mMap;
    private ILocationService mLocationService;
    private ISetting mSetting;
    private Marker mLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        initViews();

        mLocationService = (ILocationService) AppServices.getService(AppServices.LOCATION);
        mSetting = (ISetting) AppServices.getService(AppServices.SETTINGS);

        mPresenter = new MapsPresenter(this, mPermissionService, mSetting);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        mLocationService.onStartCommand(this, mPermissionService, mSetting);
        mPresenter.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        mLocationService.onResume();
        mLocationService.subscribeToLocationChanges(this);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        mLocationService.unSubscribeToLocationChanges(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        mPresenter.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        mPresenter.onDestroy();
        mLocationService.onDestroyCommand();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult requestCode: " + requestCode);
        switch (requestCode) {
            case PERMISSIONS_MOCKING: {
                mPresenter.onMockPermissionsResult(grantResults);
                return;
            }
            default:
                // do nothing
        }
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;

        mMap.setLocationSource(this);
        if (hasLocationPermission()) {
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mPresenter.onMarkerClicked(marker);
                return true;
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mPresenter.onMapLongClicked(latLng);
            }
        });

        // Add a marker in Sydney and move the camera
        LatLng berlin = new LatLng(52.4, 13.5);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(berlin, 12.0f));

        mPresenter.onMapReady();
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick");
        mPresenter.onClick(view);
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mMapLocationListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mMapLocationListener = null;
    }

    private boolean hasLocationPermission() {
        return mPermissionService.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void initViews() {
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        FloatingActionButton mMock = (FloatingActionButton) findViewById(R.id.location);
        mMock.setOnClickListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged:" + location.getLatitude() + "/" + location.getLongitude());

        if (mMapLocationListener != null) {
            mMapLocationListener.onLocationChanged(location);
        }

        mPresenter.setLastKnownLocation(location);
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void selectLocation(LatLng latLng, String id, float zoom) {
        if (mLocationMarker != null) {
            mMap.clear();
        }
        mLocationMarker = mMap.addMarker(new MarkerOptions().position(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom < 0 ? mMap.getCameraPosition().zoom : zoom));
    }
}
