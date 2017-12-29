package de.p72b.mocklation.map;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import de.p72b.mocklation.R;

@SuppressWarnings("unused") // It's used in R.layout.activity_maps
public class MapPaddingsBehavior extends CoordinatorLayout.Behavior<MapView> implements
        OnMapReadyCallback {

    private GoogleMap mMap;

    private int mTopComponentsPadding;
    private int mBottomDetailsPadding;

    public MapPaddingsBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTopComponentsPadding = (int) context.getResources().getDimension(R.dimen.map_top_padding);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, MapView child, int layoutDirection) {
        child.getMapAsync(this);
        return false;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, MapView child, View dependency) {
        return dependency.getId() == R.id.bottom_sheet;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, MapView child, View dependency) {
        if (dependency.getId() == R.id.bottom_sheet) {
            final int paddingExtra = child.getBottom() - dependency.getTop();
            if (paddingExtra != mBottomDetailsPadding) {
                mBottomDetailsPadding = paddingExtra;
                updatePadding();
                return true;
            }
        }
        return false;
    }

    private void updatePadding() {
        if (mMap == null) {
            return;
        }
        mMap.setPadding(0, mTopComponentsPadding, 0, mBottomDetailsPadding);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}