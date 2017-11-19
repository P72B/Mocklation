package de.p72b.mocklation.main;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geojson.GeoJsonPoint;

import java.util.List;

import de.p72b.mocklation.R;
import de.p72b.mocklation.map.MapsActivity;
import de.p72b.mocklation.service.AppServices;
import de.p72b.mocklation.service.location.LocationItemFeature;
import de.p72b.mocklation.service.room.LocationItem;
import de.p72b.mocklation.service.setting.ISetting;
import de.p72b.mocklation.util.VisibilityAnimationListener;

public class MainActivity extends AppCompatActivity implements IMainView, View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private LocationListAdapter mAdapter = new LocationListAdapter(new AdapterListener());
    private IMainPresenter mPresenter;
    private TextView mSelectedLocationName;
    private EditText mSelectedLocationLatitude;
    private EditText mSelectedLocationLongitude;
    private ImageButton mButtonPlayStop;
    private View mDataView;
    private View mDataEmpty;
    private Animation mFadeInAnimation;
    private Animation mFadeOutAnimation;
    private VisibilityAnimationListener mFadeOutListener = new VisibilityAnimationListener();
    private VisibilityAnimationListener mFadeInListener = new VisibilityAnimationListener();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        ISetting setting = (ISetting) AppServices.getService(AppServices.SETTINGS);
        mPresenter = new MainPresenter(this, setting);
        mFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        mFadeInAnimation.setAnimationListener(mFadeInListener);
        mFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out_animation);
        mFadeOutAnimation.setAnimationListener(mFadeOutListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    protected void onDestroy() {
        mPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult requestCode: " + requestCode);
        switch (requestCode) {
            case MockServiceInteractor.PERMISSIONS_MOCKING: {
                mPresenter.onMockPermissionsResult(grantResults);
                return;
            }
            default:
                // do nothing
        }
    }

    @Override
    public void showSavedLocations(List<LocationItem> locationItems) {
        toggleDataViewTo(View.VISIBLE);
        mAdapter.setData(locationItems);
    }

    @Override
    public void selectLocation(LocationItem item) {
        mSelectedLocationName.setText(LocationItem.getNameToBeDisplayed(item));
        LocationItemFeature feature = item.deserialize();

        switch (feature.getGeoJsonFeature().getGeometry().getType()) {
            case "Point":
                GeoJsonPoint point = (GeoJsonPoint) feature.getGeoJsonFeature().getGeometry();
                LatLng latLng = point.getCoordinates();
                mSelectedLocationLatitude.setText(String.valueOf(latLng.latitude));
                mSelectedLocationLongitude.setText(String.valueOf(latLng.longitude));
                break;
            default:
                // do nothing
        }

        mAdapter.flagItem(item);
    }

    @Override
    public void showEmptyPlaceholder() {
        toggleDataViewTo(View.INVISIBLE);
    }

    private void toggleDataViewTo(int state) {
        if (View.INVISIBLE == state) {
            if (mDataEmpty.getVisibility() != View.VISIBLE) {
                mFadeInListener.setViewAndVisibility(mDataEmpty, View.VISIBLE);
                mDataEmpty.startAnimation(mFadeInAnimation);
            }
            if (mDataView.getVisibility() != View.INVISIBLE) {
                mFadeOutListener.setViewAndVisibility(mDataView, View.INVISIBLE);
                mDataView.startAnimation(mFadeOutAnimation);
            }
        } else {
            if (mDataEmpty.getVisibility() != View.INVISIBLE) {
                mFadeOutListener.setViewAndVisibility(mDataEmpty, View.INVISIBLE);
                mDataEmpty.startAnimation(mFadeOutAnimation);
            }
            if (mDataView.getVisibility() != View.VISIBLE) {
                mFadeInListener.setViewAndVisibility(mDataView, View.VISIBLE);
                mDataView.startAnimation(mFadeInAnimation);
            }
        }
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick");
        switch (view.getId()) {
            case R.id.fab:
                startActivity(new Intent(this, MapsActivity.class));
                break;
            default:
                mPresenter.onClick(view);
        }
    }

    @Override
    public void setPlayStopStatus(@MockServiceInteractor.ServiceStatus int state) {
        switch(state) {
            case MockServiceInteractor.SERVICE_STATE_RUNNING:
                mButtonPlayStop.setBackgroundResource(R.drawable.ic_stop_black_24dp);
                break;
            case MockServiceInteractor.SERVICE_STATE_STOP:
                mButtonPlayStop.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                break;
        }
    }

    @Override
    public void showSnackbar(int message, int action, View.OnClickListener listener, int duration) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.main_root), message, duration);
        if (action != -1) {
            snackbar.setAction(action, listener);
        }
        snackbar.show();
    }

    private void initViews() {
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDataView = findViewById(R.id.data_view);
        mDataEmpty = findViewById(R.id.data_empty);
        mDataView.setVisibility(View.INVISIBLE);
        mDataEmpty.setVisibility(View.INVISIBLE);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);

        mSelectedLocationName = findViewById(R.id.card_view_selected_location_name);
        mSelectedLocationLatitude = findViewById(R.id.card_view_selected_location_latitude);
        mSelectedLocationLongitude = findViewById(R.id.card_view_selected_location_longitude);

        mRecyclerView = findViewById(R.id.location_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper touchHelper = new ItemTouchHelper(new SwipeAndTouchHelper(mAdapter));
        touchHelper.attachToRecyclerView(mRecyclerView);

        mButtonPlayStop = findViewById(R.id.play_stop);
        mButtonPlayStop.setOnClickListener(this);

        findViewById(R.id.edit).setOnClickListener(this);

        final View root = findViewById(R.id.main_root);
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                root.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                Rect rectangle = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
                int statusBarHeight = rectangle.top;
                int rootHeight = root.getHeight();
                int selectedLocationCardHeight = findViewById(R.id.card_view_selected_location).getHeight();
                int toolbarHeight = toolbar.getHeight();
                int padding15dp = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        15, getResources().getDisplayMetrics()));

                int newHeight = rootHeight - statusBarHeight - toolbarHeight - selectedLocationCardHeight - 4 * padding15dp;

                ViewGroup.LayoutParams params = mRecyclerView.getLayoutParams();
                params.height = newHeight;
                mRecyclerView.setLayoutParams(params);
            }
        });
    }

    private class AdapterListener implements IAdapterListener {

        @Override
        public void onClick(View view) {
            int position = mRecyclerView.getChildLayoutPosition(view);
            LocationItem item = mAdapter.getItemAt(position);
            Log.d(TAG, "onClick item: " + item.getCode());
            mPresenter.locationItemPressed(item);
        }

        @Override
        public void onItemRemoved(LocationItem item) {
            Log.d(TAG, "onItemRemoved item: " + item.getCode());
            mPresenter.locationItemRemoved(item);
        }
    }

    public interface IAdapterListener extends View.OnClickListener {
        void onItemRemoved(LocationItem item);
    }
}
