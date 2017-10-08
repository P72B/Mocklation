package de.p72b.mocklation.map;

import android.Manifest;
import java.util.Calendar;

import android.graphics.Point;
import android.location.Location;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.TextView;
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
import de.p72b.mocklation.service.AppServices;
import de.p72b.mocklation.service.location.ILocationService;
import de.p72b.mocklation.service.setting.ISetting;
import de.p72b.mocklation.util.AppUtil;

public class MapsActivity extends BaseActivity implements IMapsView, OnMapReadyCallback,
        View.OnClickListener, LocationSource, LocationSource.OnLocationChangedListener,
        ILocationService.OnLocationChanged {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private IMapsPresenter mPresenter;
    private OnLocationChangedListener mMapLocationListener = null;
    private GoogleMap mMap;
    private ILocationService mLocationService;
    private ISetting mSetting;
    private Marker mLocationMarker;
    private FloatingActionButton mFabActionSave;
    private BottomSheetBehavior<View> mBottomSheetBehavior;
    private TextView mCoordinates;
    private TextView mTstamp;
    private View mBottomSheet;

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

    @SuppressWarnings("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;

        mMap.setLocationSource(this);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (hasLocationPermission()) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                mPresenter.onMarkerClicked(marker);
                return true;
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mCoordinates.setText(AppUtil.getFormattedCoordinates(latLng));
                mTstamp.setText(AppUtil.getFormattedTimeStamp(Calendar.getInstance()));
                mPresenter.onMapLongClicked(latLng);

                if (BottomSheetBehavior.STATE_EXPANDED == mBottomSheetBehavior.getState()) {
                    return;
                }
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (BottomSheetBehavior.STATE_HIDDEN != mBottomSheetBehavior.getState()) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
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


        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getAdjustedLocation(latLng), getAdjustedMapZoom(zoom)));
    }

    private LatLng getAdjustedLocation(LatLng latLng) {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            return latLng;
        }

        int height = mBottomSheet.getLayoutParams().height;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        Log.d(TAG, "Display: " +  size.x + " x " + size.y + "\n" + "Height: " + height);

        LatLng adjustedLocation = mMap.getProjection().fromScreenLocation(new Point(size.x / 2, height));

        return adjustedLocation;
    }

    private float getAdjustedMapZoom(float zoom) {
        return zoom < 0 ? mMap.getCameraPosition().zoom : zoom;
    }

    private boolean hasLocationPermission() {
        return mPermissionService.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void initViews() {
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFabActionSave = (FloatingActionButton) findViewById(R.id.location);
        mFabActionSave.setOnClickListener(this);

        initBottomSheet();
    }

    private void initBottomSheet() {
        mBottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setPeekHeight(300);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mCoordinates = (TextView) findViewById(R.id.coordinates);
        mTstamp = (TextView) findViewById(R.id.tstamp);
    }
}
