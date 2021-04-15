package de.p72b.mocklation.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.p72b.locator.location.LocationAwareAppCompatActivity;
import de.p72b.mocklation.BuildConfig;
import de.p72b.mocklation.R;
import de.p72b.mocklation.imprint.ImprintActivity;
import de.p72b.mocklation.service.AppServices;
import de.p72b.mocklation.service.analytics.IAnalyticsService;
import de.p72b.mocklation.service.room.LocationItem;
import de.p72b.mocklation.service.setting.ISetting;
import de.p72b.mocklation.util.Logger;
import de.p72b.mocklation.util.VisibilityAnimationListener;

public class MainActivity extends LocationAwareAppCompatActivity implements IMainView, View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener {

private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private LocationListAdapter mAdapter = new LocationListAdapter(new AdapterListener());
    private IMainPresenter mPresenter;
    private TextView mSelectedLocationName;
    private EditText mSelectedLocationLatitude;
    private EditText mSelectedLocationLongitude;
    private ImageButton mButtonPlayStop;
    private ImageButton mButtonPausePlay;
    private View mDataView;
    private View mDataEmpty;
    private Animation mFadeInAnimation;
    private Animation mFadeOutAnimation;
    private VisibilityAnimationListener mFadeOutListener = new VisibilityAnimationListener();
    private VisibilityAnimationListener mFadeInListener = new VisibilityAnimationListener();
    private ImageButton mFavorite;
    private DrawerLayout mDrawer;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        ISetting setting = (ISetting) AppServices.getService(AppServices.SETTINGS);
        IAnalyticsService analytics = (IAnalyticsService) AppServices.getService(AppServices.ANALYTICS);
        mPresenter = new MainPresenter(this, setting, analytics, getLocationManager(), (LocationManager) getSystemService(Context.LOCATION_SERVICE));
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case MockServiceInteractor.REQUEST_CODE_DEFAULT_MOCK_APP: {
                mPresenter.onDefaultMockAppRequest(resultCode);
            }
            case MockServiceInteractor.REQUEST_CODE_ENABLE_DEVELOPER_OPTIONS: {
                mPresenter.onDeveloperOptionsEnabledRequest(resultCode);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Logger.d(TAG, "onRequestPermissionsResult requestCode: " + requestCode);
        switch (requestCode) {
            case MockServiceInteractor.PERMISSIONS_MOCKING: {
                mPresenter.onMockPermissionsResult(grantResults);
                return;
            }
            default:
                // do nothing
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void showSavedLocations(List<LocationItem> locationItems) {
        toggleDataViewTo(View.VISIBLE);
        mAdapter.setData(locationItems);
    }

    @Override
    public void selectLocation(LocationItem item) {
        mSelectedLocationName.setText(LocationItem.getNameToBeDisplayed(item));

        if (item.isIsFavorite()) {
            mFavorite.setBackground(getDrawable(R.drawable.ic_favorite_black_24dp));
        } else {
            mFavorite.setBackground(getDrawable(R.drawable.ic_favorite_border_black_24dp));
        }

        Object geometry = item.getGeometry();
        if (geometry instanceof LatLng) {
            mSelectedLocationLatitude.setText(String.valueOf(((LatLng) geometry).latitude));
            mSelectedLocationLongitude.setText(String.valueOf(((LatLng) geometry).longitude));
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
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch(item.getItemId()) {
            case R.id.nav_fixed_mode:
                break;
            case R.id.nav_imprint:
                startActivity(new Intent(this, ImprintActivity.class));
                break;
            case R.id.nav_data_protection:
                mPresenter.showPrivacyUpdateDialog();
                break;
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        mPresenter.onClick(view.getId());
    }

    @Override
    public void setPlayPauseStopStatus(@MockServiceInteractor.ServiceStatus int state) {
        switch(state) {
            case MockServiceInteractor.SERVICE_STATE_RUNNING:
                mButtonPlayStop.setBackgroundResource(R.drawable.ic_stop_black_24dp);
                mButtonPausePlay.setBackgroundResource(R.drawable.ic_pause_black_24dp);
                mButtonPausePlay.setVisibility(View.VISIBLE);
                break;
            case MockServiceInteractor.SERVICE_STATE_STOP:
                mButtonPlayStop.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                mButtonPausePlay.setVisibility(View.INVISIBLE);
                break;
            case MockServiceInteractor.SERVICE_STATE_PAUSE:
                mButtonPausePlay.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
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

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_activity_fixed_mode));
        }
        mDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setCheckedItem(R.id.nav_fixed_mode);
        ((TextView) findViewById(R.id.nav_footer_item)).setText(BuildConfig.VERSION_NAME);

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
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper touchHelper = new ItemTouchHelper(new SwipeAndTouchHelper(mAdapter));
        touchHelper.attachToRecyclerView(mRecyclerView);

        mButtonPlayStop = findViewById(R.id.play_stop);
        mButtonPlayStop.setOnClickListener(this);

        mButtonPausePlay = findViewById(R.id.pause);
        mButtonPausePlay.setOnClickListener(this);

        mFavorite = findViewById(R.id.favorite);
        mFavorite.setOnClickListener(this);

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
            Logger.d(TAG, "onClick item: " + item.getCode());
            mPresenter.locationItemPressed(item);
        }

        @Override
        public void onItemRemoved(LocationItem item) {
            Logger.d(TAG, "onItemRemoved item: " + item.getCode());
            mPresenter.locationItemRemoved(item);
        }
    }

    public interface IAdapterListener extends View.OnClickListener {
        void onItemRemoved(LocationItem item);
    }
}
