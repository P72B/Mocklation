package de.p72b.mocklation.main;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geojson.GeoJsonPoint;

import java.util.List;

import de.p72b.mocklation.R;
import de.p72b.mocklation.map.MapsActivity;
import de.p72b.mocklation.service.AppServices;
import de.p72b.mocklation.service.database.LocationItem;
import de.p72b.mocklation.service.location.LocationItemFeature;
import de.p72b.mocklation.service.setting.ISetting;

public class MainActivity extends AppCompatActivity implements IMainView{

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private LocationListAdapter mAdapter = new LocationListAdapter(new ItemOnClickListener());
    private IMainPresenter mPresenter;
    private TextView mSelectedLocationName;
    private EditText mSelectedLocationLatidude;
    private EditText mSelectedLocationLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        ISetting setting = (ISetting) AppServices.getService(AppServices.SETTINGS);
        mPresenter = new MainPresenter(this, setting);
    }

    @Override
    protected void onDestroy() {
        mPresenter.onDestroy();
        super.onDestroy();
    }

    private void initViews() {
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final Intent intent = new Intent(this, MapsActivity.class);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent);
            }
        });

        mSelectedLocationName = (TextView) findViewById(R.id.card_view_selected_location_name);
        mSelectedLocationLatidude = (EditText) findViewById(R.id.card_view_selected_location_latitude);
        mSelectedLocationLongitude = (EditText) findViewById(R.id.card_view_selected_location_longitude);

        mRecyclerView = (RecyclerView) findViewById(R.id.location_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);


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

    @Override
    public void showSavedLocations(List<LocationItem> locationItems) {
        mAdapter.setData(locationItems);
    }

    @Override
    public void selectLocation(LocationItem item) {
        mSelectedLocationName.setText(item.code());
        LocationItemFeature feature = item.deserialize();

        switch (feature.getGeoJsonFeature().getGeometry().getType()) {
            case "Point":
                GeoJsonPoint point = (GeoJsonPoint) feature.getGeoJsonFeature().getGeometry();
                LatLng latLng = point.getCoordinates();
                mSelectedLocationLatidude.setText(String.valueOf(latLng.latitude));
                mSelectedLocationLongitude.setText(String.valueOf(latLng.longitude));
                break;
            default:
                // do nothing
        }

        mAdapter.flagItem(item);
    }

    @Override
    public void showEmptyPlaceholder() {
        // TODO
    }

    private class ItemOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int position = mRecyclerView.getChildLayoutPosition(view);
            LocationItem item = mAdapter.getItemAt(position);
            Log.d(TAG, "onClick item: " + item.code());
            mPresenter.locationItemPressed(item);
        }
    }
}
