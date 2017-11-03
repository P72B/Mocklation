package de.p72b.mocklation.map;

import android.Manifest;

import java.util.Calendar;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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
    private static final long MARKER_REMOVE_MILLISECONDS = 300;
    private static final float DEFAULT_ZOOM_LEVEL = 15L;

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
    private boolean mShouldMarkerDisapperOnHideBottomSheet;
    private TextView mBottomSheetSubTitleText;
    private TextView mBottomSheetTitleText;
    private boolean mInitCameraPositionSet = false;
    private boolean mOnInitialLocationDetermined = false;
    private int mMyLocatioNotCenterColor;
    private int mMyLocatioCenterColor;
    private View mBottomSheetTitleTextProgressBar;
    private Animation mFadeInAnimation;
    private Animation mFadeOutAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        initViews();

        mLocationService = (ILocationService) AppServices.getService(AppServices.LOCATION);
        mSetting = (ISetting) AppServices.getService(AppServices.SETTINGS);

        mFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        mFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out_animation);

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
                mBottomSheetSubTitleText.setText(AppUtil.getFormattedCoordinates(latLng));
                mTstamp.setText(AppUtil.getFormattedTimeStamp(Calendar.getInstance()));
                mPresenter.onMapLongClicked(latLng);
                mBottomSheet.setVisibility(View.VISIBLE);
                if (BottomSheetBehavior.STATE_EXPANDED == mBottomSheetBehavior.getState()) {
                    return;
                }
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (BottomSheetBehavior.STATE_EXPANDED == mBottomSheetBehavior.getState()) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    return;
                }

                hideComponents();
            }
        });

        if (mOnInitialLocationDetermined) {
            tryToInitCameraPostion();
        }

        mPresenter.onMapReady();
    }

    private void hideComponents() {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        removeMarker();
    }

    private void removeMarker() {
        mPresenter.removeMarker();
        if (mLocationMarker != null) {
            AppUtil.removeMarkerAnimated(mLocationMarker, MARKER_REMOVE_MILLISECONDS, 0);
            mLocationMarker = null;
        }
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
    public void selectLocation(LatLng latLng, String id, float zoom) {
        if (mLocationMarker != null) {
            mLocationMarker.remove();
        }

        mLocationMarker = mMap.addMarker(
                new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(
                                R.drawable.ic_new_location))
        );
    }

    @Override
    public void showMyLocation() {
        Location location = mLocationService.getLastKnownLocation();
        if (mMap == null || location == null) {
            return;
        }
        LatLng lastKnowLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                lastKnowLocation, DEFAULT_ZOOM_LEVEL));
        setFollowGps(true);
    }

    @Override
    public void showSnackbar(int message, int action, View.OnClickListener listener, int duration) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.touch_overlay), message, duration);
        if (action != -1) {
            snackbar.setAction(action, listener);
        }
        snackbar.show();
    }

    @Override
    public void setAddress(String formattedAddress) {
        mBottomSheetTitleText.setText(formattedAddress);
    }

    @Override
    public void setAddressProgressbarVisibility(int visibility) {
        if (View.VISIBLE == visibility) {
            mBottomSheetTitleTextProgressBar.setVisibility(View.VISIBLE);
            mBottomSheetTitleText.setVisibility(View.GONE);

            mBottomSheetTitleTextProgressBar.startAnimation(mFadeInAnimation);
            mBottomSheetTitleText.startAnimation(mFadeOutAnimation);

        } else {
            mBottomSheetTitleTextProgressBar.setVisibility(View.GONE);
            mBottomSheetTitleText.setVisibility(View.VISIBLE);

            mBottomSheetTitleTextProgressBar.startAnimation(mFadeOutAnimation);
            mBottomSheetTitleText.startAnimation(mFadeInAnimation);
        }
    }

    private void setFollowGps(boolean followGps) {
        int color = followGps ? mMyLocatioCenterColor : mMyLocatioNotCenterColor;
        DrawableCompat.setTint(mFabActionLocation.getDrawable(), color);
    }

    private boolean hasLocationPermission() {
        return mPermissionService.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void initViews() {
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFabActionSave = findViewById(R.id.save);
        mFabActionSave.setOnClickListener(this);
        mFabActionLocation = findViewById(R.id.location);
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
        mBottomSheetTitleText = findViewById(R.id.bottom_sheet_header_title);
        mBottomSheetTitleTextProgressBar = findViewById(R.id.bottom_sheet_header_title_progress_bar);
        mBottomSheetSubTitleText = findViewById(R.id.bottom_sheet_subheader_title);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setPeekHeight(300);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mTstamp = findViewById(R.id.tstamp);

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
                mBottomSheetSubTitleText.setTextColor((int) animator.getAnimatedValue());
            }

        });
        final ValueAnimator colorAnimationFaceToEye = ValueAnimator.ofObject(new ArgbEvaluator(), faceColor, eyeColor);
        colorAnimationFaceToEye.setDuration(duration);
        colorAnimationFaceToEye.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mBottomSheetTitleText.setTextColor((int) animator.getAnimatedValue());
                mBottomSheetSubTitleText.setTextColor((int) animator.getAnimatedValue());
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

                if (slideOffset >= 0) {
                    mShouldMarkerDisapperOnHideBottomSheet = true;
                }

                if (slideOffset == -1) {
                    if (mShouldMarkerDisapperOnHideBottomSheet) {
                        mBottomSheet.setVisibility(View.GONE);
                        removeMarker();
                        mShouldMarkerDisapperOnHideBottomSheet = false;
                    }
                }
            }
        });
    }

}
