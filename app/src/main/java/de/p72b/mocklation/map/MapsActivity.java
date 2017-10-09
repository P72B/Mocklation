package de.p72b.mocklation.map;

import android.Manifest;

import java.util.Calendar;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Point;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
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
    private FloatingActionButton mFabActionLocation;
    private BottomSheetBehavior<View> mBottomSheetBehavior;
    private TextView mTstamp;
    private View mBottomSheet;
    private View mBottomSheetHeader;
    private boolean mIsDark;
    private TextView mBottomSheetTitleText;
    private boolean mInitCameraPositionSet = false;
    private boolean mOnInitialLocationDetermined = false;
    private int mMyLocatioNotCenterColor;
    private int mMyLocatioCenterColor;

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
                mBottomSheetTitleText.setText(AppUtil.getFormattedCoordinates(latLng));
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

        if (mOnInitialLocationDetermined) {
            tryToInitCameraPostion();
        }

        mPresenter.onMapReady();
    }

    private void tryToInitCameraPostion() {
        Log.d(TAG, "tryToInitCameraPostion mInitCameraPositionSet: " + mInitCameraPositionSet);
        if (mInitCameraPositionSet || mMap == null) {
            return;
        }

        Location location = mLocationService.getLastKnownLocation();
        if (location != null) {
            Log.d(TAG, "SET initial map location:" + location.getLatitude() + "/" + location.getLongitude());
            LatLng startLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 15.0f));
            mInitCameraPositionSet = true;
        }
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
    public void onInitialLocationDetermined(Location location) {
        Log.d(TAG, "onInitialLocationDetermined:" + location.getLatitude() + "/" + location.getLongitude());
        mOnInitialLocationDetermined = true;
        tryToInitCameraPostion();
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
    }

    @Override
    public void showMyLocation() {
        Location location = mLocationService.getLastKnownLocation();
        if (mMap == null || location == null) {
            return;
        }
        LatLng lastKnowLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                lastKnowLocation, mMap.getCameraPosition().zoom));
        setFollowGps(true);
    }

    private void setFollowGps(boolean followGps) {
        int color = followGps ? mMyLocatioCenterColor : mMyLocatioNotCenterColor;
        DrawableCompat.setTint(mFabActionLocation.getDrawable(), color);
    }

    private LatLng getAdjustedLocation(LatLng latLng) {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            return latLng;
        }

        int height = mBottomSheet.getLayoutParams().height;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        Log.d(TAG, "Display: " + size.x + " x " + size.y + "\n" + "Height: " + height);

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
        mFabActionSave = (FloatingActionButton) findViewById(R.id.save);
        mFabActionSave.setOnClickListener(this);
        mFabActionLocation = (FloatingActionButton) findViewById(R.id.location);
        mFabActionLocation.setOnClickListener(this);

        mMyLocatioNotCenterColor = ContextCompat.getColor(this, R.color.eye);
        mMyLocatioCenterColor = ContextCompat.getColor(this, R.color.colorAccent);

        findViewById(R.id.touch_overlay).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                setFollowGps(false);
                return false;
            }
        });

        initBottomSheet();
    }

    private void initBottomSheet() {
        mBottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetHeader = findViewById(R.id.bottom_sheet_header);
        mBottomSheetTitleText = (TextView) findViewById(R.id.bottom_sheet_header_title);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setPeekHeight(300);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mTstamp = (TextView) findViewById(R.id.tstamp);

        initBottomSheetColorAnimations();
    }

    private void initBottomSheetColorAnimations() {
        int primaryColor;
        int faceColor;
        int eyeColor;
        int duration = 150;
        mIsDark = false;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            primaryColor = getColor(R.color.colorPrimary);
            faceColor = getColor(R.color.face);
            eyeColor = getColor(R.color.eye);
        } else {
            primaryColor = getResources().getColor(R.color.colorPrimary);
            faceColor = getResources().getColor(R.color.face);
            eyeColor = getResources().getColor(R.color.eye);
        }

        final ValueAnimator colorAnimationFaceToPrimary = ValueAnimator.ofObject(new ArgbEvaluator(), faceColor, primaryColor);
        colorAnimationFaceToPrimary.setDuration(duration);
        colorAnimationFaceToPrimary.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mBottomSheetHeader.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });
        final ValueAnimator colorAnimationPrimaryToFace = ValueAnimator.ofObject(new ArgbEvaluator(), primaryColor, faceColor);
        colorAnimationPrimaryToFace.setDuration(duration);
        colorAnimationPrimaryToFace.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mBottomSheetHeader.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });
        final ValueAnimator colorAnimationEyeToFace = ValueAnimator.ofObject(new ArgbEvaluator(), eyeColor, faceColor);
        colorAnimationEyeToFace.setDuration(duration);
        colorAnimationEyeToFace.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mBottomSheetTitleText.setTextColor((int) animator.getAnimatedValue());
            }

        });
        final ValueAnimator colorAnimationFaceToEye = ValueAnimator.ofObject(new ArgbEvaluator(), faceColor, eyeColor);
        colorAnimationFaceToEye.setDuration(duration);
        colorAnimationFaceToEye.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mBottomSheetTitleText.setTextColor((int) animator.getAnimatedValue());
            }

        });

        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {}

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset > 0) {
                    if (!mIsDark) {
                        mIsDark = true;
                        colorAnimationFaceToPrimary.start();
                        colorAnimationEyeToFace.start();
                    }
                } else {
                    if (mIsDark) {
                        mIsDark = false;
                        colorAnimationPrimaryToFace.start();
                        colorAnimationFaceToEye.start();
                    }
                }
            }
        });
    }

}
