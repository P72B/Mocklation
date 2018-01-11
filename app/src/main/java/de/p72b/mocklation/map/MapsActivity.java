package de.p72b.mocklation.map;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import de.p72b.mocklation.BaseActivity;
import de.p72b.mocklation.R;
import de.p72b.mocklation.service.AppServices;
import de.p72b.mocklation.service.location.ILocationService;
import de.p72b.mocklation.service.room.LocationItem;
import de.p72b.mocklation.service.setting.ISetting;
import de.p72b.mocklation.util.AppUtil;

public class MapsActivity extends BaseActivity implements IMapsView, OnMapReadyCallback,
        View.OnClickListener, LocationSource, LocationSource.OnLocationChangedListener,
        ILocationService.OnLocationChanged {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final long MARKER_REMOVE_MILLISECONDS = 300;
    private static final float DEFAULT_ZOOM_LEVEL = 15L;
    private static final String KEY_MAP_STATE = "mapViewSaveState";

    private IMapsPresenter mPresenter;
    private OnLocationChangedListener mMapLocationListener = null;
    private GoogleMap mMap;
    private ILocationService mLocationService;
    private ISetting mSetting;
    private Marker mLocationMarker;
    private FloatingActionButton mFabActionLocation;
    private BottomSheetBehavior<View> mBottomSheetBehavior;
    private TextView mTstamp;
    private View mBottomSheet;
    private View mBottomSheetHeader;
    private boolean mIsDark;
    private boolean mShouldMarkerDisappearOnHideBottomSheet;
    private TextView mBottomSheetSubTitleText;
    private TextView mBottomSheetTitleText;
    private boolean mInitCameraPositionSet = false;
    private boolean mOnInitialLocationDetermined = false;
    private int mMyLocationNotCenterColor;
    private int mMyLocationCenterColor;
    private View mBottomSheetTitleTextProgressBar;
    private Animation mFadeInAnimation;
    private Animation mFadeOutAnimation;
    private View mCardViewAutocompleteWrapper;
    private int mDeltaMapSearchBar;
    private int mStateMapItems = View.VISIBLE;
    private int mDeltaFabs;
    private View mFabWrapper;
    private MapView mMapView;
    private Bitmap mEmptyMarkerBitmap;
    private float mColoredMarkerFontSize;
    private int mColoredCenterX;
    private int mColoredCenterY;
    private int mColoredRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        initViews(savedInstanceState);

        mLocationService = (ILocationService) AppServices.getService(AppServices.LOCATION);
        mSetting = (ISetting) AppServices.getService(AppServices.SETTINGS);

        mFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        mFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out_animation);

        mPresenter = new MapsPresenter(this);

        mEmptyMarkerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_new_location);
        mColoredMarkerFontSize = 18 * getResources().getDisplayMetrics().density;
        int width = mEmptyMarkerBitmap.getWidth();
        mColoredCenterY = (int) Math.round(40.27777 * width / 100);
        mColoredRadius = (int) (Math.round(65.972222 * width / 100) / 2);
        mColoredCenterX = width / 2;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        mLocationService.onStartCommand(this, mPermissionService, mSetting);
        mPresenter.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        mLocationService.onResume();
        mLocationService.subscribeToLocationChanges(this);
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        mLocationService.unSubscribeToLocationChanges(this);
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        mPresenter.onStop();
        mMapView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        mPresenter.onDestroy();
        mMapView.onDestroy();
        mLocationService.onDestroyCommand();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //This MUST be done before saving any of your own or your base class's variables
        final Bundle mapViewSaveState = new Bundle(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(mapViewSaveState);
        }
        outState.putBundle(KEY_MAP_STATE, mapViewSaveState);
        //Add any other variables here.
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        mMapView.onLowMemory();
        super.onLowMemory();
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
                //mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                mPresenter.onMarkerClicked(marker);
                return true;
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (mStateMapItems == View.INVISIBLE) {
                    toggleAnimationOverlayItems();
                }
                mPresenter.onMapLongClicked(latLng);
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (BottomSheetBehavior.STATE_EXPANDED == mBottomSheetBehavior.getState()) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    return;
                }
                if (BottomSheetBehavior.STATE_HIDDEN == mBottomSheetBehavior.getState()) {
                    toggleAnimationOverlayItems();
                } else {
                    hideComponents();
                }
            }
        });

        if (mOnInitialLocationDetermined) {
            tryToInitCameraPostion();
        }

        mPresenter.onMapReady();
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick");
        switch (view.getId()) {
            default:
                mPresenter.onClick(view);
        }
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
    public void showBottomSheet(LocationItem item) {
        Object geometry = item.getGeometry();
        if (!(geometry instanceof LatLng)) {
            return;
        }
        if (!item.getDisplayedName().isEmpty()) {
            mBottomSheetTitleText.setText(item.getDisplayedName());
        }
        mBottomSheetSubTitleText.setText(AppUtil.getFormattedCoordinates((LatLng) geometry));
        mTstamp.setText(AppUtil.getFormattedTimeStamp(Calendar.getInstance()));
        mBottomSheet.setVisibility(View.VISIBLE);
        if (BottomSheetBehavior.STATE_EXPANDED == mBottomSheetBehavior.getState()) {
            return;
        }
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void addNewMarker(LocationItem item) {
        Object geometry = item.getGeometry();
        if (!(geometry instanceof LatLng)) {
            return;
        }
        if (mLocationMarker != null) {
            mLocationMarker.remove();
        }

        if (item.getColor() == 0) {
            item.setColor(getRandomColor());
        }
        mLocationMarker = mMap.addMarker(
                new MarkerOptions()
                        .position((LatLng) geometry)
                        .icon(getColoredMarker(item.getDisplayedName(), item.getColor()))
        );
        mLocationMarker.setTag(item);
        showBottomSheet(item);
    }

    private BitmapDescriptor getColoredMarker(@NonNull String text, int fillColor) {
        Bitmap mutableBitmap = mEmptyMarkerBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint;
        paint = new Paint();
        paint.setColor(fillColor);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        canvas.drawCircle(mColoredCenterX, mColoredCenterY, mColoredRadius, paint);

        paint.setColor(Color.BLACK);

        if (!text.isEmpty()) {
            TextPaint textPaint = new TextPaint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(mColoredMarkerFontSize);
            textPaint.setTextAlign(Paint.Align.CENTER);
            float textHeight = textPaint.descent() - textPaint.ascent();
            float textOffset = (textHeight / 2) - textPaint.descent();

            canvas.drawText(String.valueOf(text.charAt(0)), mColoredCenterX, mColoredCenterY + textOffset, textPaint);
        }

        Paint p = new Paint();
        canvas.drawBitmap(mutableBitmap, 0, 0, p);

        return BitmapDescriptorFactory.fromBitmap(mutableBitmap);
    }

    private int getRandomColor() {
        int randomNum = ThreadLocalRandom.current().nextInt(1, 17 + 1);
        int colorResId = getResources().getIdentifier("r" + randomNum, "color", getPackageName());
        return ContextCompat.getColor(this, colorResId);
    }

    @Override
    public void addMarkers(List<LocationItem> items) {
        for (LocationItem item : items) {
            Object geometry = item.getGeometry();
            if (!(geometry instanceof LatLng)) {
                continue;
            }

            if (item.getColor() == 0) {
                item.setColor(getRandomColor());
            }
            Marker marker = mMap.addMarker(
                    new MarkerOptions()
                            .position((LatLng) geometry)
                            .icon(getColoredMarker(item.getDisplayedName(), item.getColor()))
            );
            marker.setTag(item);
        }
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
        View view = snackbar.getView();
        view.setElevation(50);
        if (action != -1) {
            snackbar.setAction(action, listener);
        }
        snackbar.show();
    }

    @Override
    public void setAddress(String formattedAddress, String title) {
        mBottomSheetTitleText.setText(formattedAddress);
        if (mLocationMarker != null) {
            LocationItem item = (LocationItem) mLocationMarker.getTag();
            mLocationMarker.remove();
            if (item == null) {
                return;
            }
            mLocationMarker = mMap.addMarker(
                    new MarkerOptions()
                            .position(mLocationMarker.getPosition())
                            .icon(getColoredMarker(title, item.getColor()))
            );
            mLocationMarker.setTag(item);
        }
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks whether a hardware keyboard is available
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            Toast.makeText(this, "keyboard visible", Toast.LENGTH_SHORT).show();
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            Toast.makeText(this, "keyboard hidden", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideComponents() {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        removeMarker();
    }

    private void removeMarker() {
        mPresenter.removeMarker();
        if (mLocationMarker != null) {
            AppUtil.removeMarkerAnimated(mLocationMarker, MARKER_REMOVE_MILLISECONDS);
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

    private void setFollowGps(boolean followGps) {
        int color = followGps ? mMyLocationCenterColor : mMyLocationNotCenterColor;
        DrawableCompat.setTint(mFabActionLocation.getDrawable(), color);
    }

    private boolean hasLocationPermission() {
        return mPermissionService.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void toggleAnimationOverlayItems() {
        if (mStateMapItems == View.VISIBLE) {
            mStateMapItems = View.INVISIBLE;
        } else {
            mStateMapItems = View.VISIBLE;
        }

        int finalPositionMapSeachBar = mDeltaMapSearchBar;
        int finalPositionFabs = mDeltaFabs;
        if (mStateMapItems == View.VISIBLE) {
            finalPositionMapSeachBar = 0;
            finalPositionFabs = 0;
        }

        SpringAnimation springAnimationMapSearch = new SpringAnimation(mCardViewAutocompleteWrapper,
                DynamicAnimation.TRANSLATION_Y, finalPositionMapSeachBar);
        springAnimationMapSearch.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY);
        springAnimationMapSearch.start();

        SpringAnimation springAnimationFabs = new SpringAnimation(mFabWrapper,
                DynamicAnimation.TRANSLATION_Y, finalPositionFabs);
        springAnimationFabs.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY);
        springAnimationFabs.start();
    }

    private void initViews(Bundle savedInstanceState) {
        setContentView(R.layout.activity_maps);

        mMapView = findViewById(R.id.map);

        final Bundle mapViewSavedInstanceState = savedInstanceState != null ?
                savedInstanceState.getBundle(KEY_MAP_STATE) : null;
        mMapView.onCreate(mapViewSavedInstanceState);
        mMapView.getMapAsync(this);

        mFabWrapper = findViewById(R.id.fabs_wrapper);
        findViewById(R.id.save).setOnClickListener(this);
        mFabActionLocation = findViewById(R.id.location);
        mFabActionLocation.setOnClickListener(this);

        mMyLocationNotCenterColor = ContextCompat.getColor(this, R.color.eye);
        mMyLocationCenterColor = ContextCompat.getColor(this, R.color.colorAccent);

        findViewById(R.id.touch_overlay).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                setFollowGps(false);
                return false;
            }
        });

        initBottomSheet();
        initMapSearchBar();
    }

    private void initMapSearchBar() {
        mCardViewAutocompleteWrapper = findViewById(R.id.card_view_selected_location);
        mCardViewAutocompleteWrapper.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mCardViewAutocompleteWrapper.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                        calculateViewElementDimension();
                    }
                }
        );

        PlaceAutocompleteFragment mAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        mAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        place.getLatLng(), DEFAULT_ZOOM_LEVEL));
                mPresenter.onMapLongClicked(place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        View view = mAutocompleteFragment.getView();
        if (view != null) {
            EditText editText = view.findViewById(R.id.place_autocomplete_search_input);
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(
                    R.dimen.text_size_middle));
        }
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();
        mAutocompleteFragment.setFilter(typeFilter);
    }

    private void calculateViewElementDimension() {
        int mapSearchBarHeight = mCardViewAutocompleteWrapper.getHeight();
        int fabsHeight = mFabWrapper.getHeight();
        int[] location = new int[2];
        mCardViewAutocompleteWrapper.getLocationOnScreen(location);
        mDeltaMapSearchBar = (mapSearchBarHeight + location[1]) * -1;

        mFabWrapper.getLocationOnScreen(location);
        int fabMarginBottom = (int) getResources().getDimension(R.dimen.fab_margin);
        mDeltaFabs = fabsHeight - fabMarginBottom;

        mBottomSheetBehavior.setPeekHeight(mBottomSheetHeader.getHeight());
    }

    private void initBottomSheet() {
        mBottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetHeader = findViewById(R.id.bottom_sheet_header);
        mBottomSheetTitleText = findViewById(R.id.bottom_sheet_header_title);
        mBottomSheetTitleTextProgressBar = findViewById(R.id.bottom_sheet_header_title_progress_bar);
        mBottomSheetSubTitleText = findViewById(R.id.bottom_sheet_subheader_title);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setHideable(true);
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
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

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
                    mShouldMarkerDisappearOnHideBottomSheet = true;
                }

                if (slideOffset == -1) {
                    if (mShouldMarkerDisappearOnHideBottomSheet) {
                        mBottomSheet.setVisibility(View.GONE);
                        removeMarker();
                        mShouldMarkerDisappearOnHideBottomSheet = false;
                    }
                }
            }
        });
    }
}
