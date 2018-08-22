package de.p72b.mocklation.map;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

import de.p72b.mocklation.R;
import de.p72b.mocklation.dialog.EditLocationItemDialog;
import de.p72b.mocklation.service.geocoder.Constants;
import de.p72b.mocklation.service.geocoder.GeocoderIntentService;
import de.p72b.mocklation.service.room.AppDatabase;
import de.p72b.mocklation.service.room.LocationItem;
import de.p72b.mocklation.util.AppUtil;
import de.p72b.mocklation.util.Logger;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MapsPresenter implements IMapsPresenter {

    private static final String TAG = MapsPresenter.class.getSimpleName();
    private IMapsView mView;
    private FragmentActivity mActivity;
    private Pair<String, LocationItem> mOnTheMapItemPair;
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private boolean mAddressRequested;
    private AppDatabase mDb;
    private Address mAddressOutput;
    private AddressResultReceiver mResultReceiver;
    private Disposable mDisposableGetAll;
    private LatLng mAddressRequestedLatLng;
    private String mAddressResult;

    MapsPresenter(FragmentActivity activity) {
        Logger.d(TAG, "new MapsPresenter");
        mActivity = activity;
        mView = (IMapsView) activity;
        mAddressRequested = false;
        mAddressOutput = null;
        mResultReceiver = new AddressResultReceiver(new Handler());
        mDb = Room.databaseBuilder(mActivity, AppDatabase.class, AppDatabase.DB_NAME_LOCATIONS).build();

        updateUIWidgets();
    }

    @Override
    public void onStart() {
        Logger.d(TAG, "onStart");
    }

    @Override
    public void onStop() {
        Logger.d(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG, "onDestroy");
        mDisposables.clear();
    }

    @Override
    public void onMapLongClicked(LatLng latLng) {
        LatLng roundedLatLng = AppUtil.roundLatLng(latLng);
        Logger.d(TAG, "onMapLongClicked LatLng: " + roundedLatLng.latitude + " / " + roundedLatLng.longitude);

        String code = AppUtil.createLocationItemCode(roundedLatLng);
        String geoJson = "{'type':'Feature','properties':{},'geometry':{'type':'Point','coordinates':[" + roundedLatLng.longitude + "," + roundedLatLng.latitude + "]}}";
        LocationItem item = new LocationItem(code, "", geoJson, 6, 0);
        mOnTheMapItemPair = new Pair<>(code, item);

        resolveAddressFromLocation(latLng);
        mView.addNewMarker(item);
    }

    @Override
    public void onMarkerClicked(Marker marker) {
        Logger.d(TAG, "onMarkerClicked marker id: " + marker.getId());

        LocationItem item = (LocationItem) marker.getTag();
        if (item != null && item.getDisplayedName().length() == 0) {
            resolveAddressFromLocation(marker.getPosition());
        }
        mView.showBottomSheet(item);
    }

    @Override
    public void setLastKnownLocation(Location location) {
        Logger.d(TAG, "setLastKnownLocation location:" + location.getProvider() + " "
                + location.getLatitude() + " / " + location.getLongitude() + " isMocked: "
                + location.isFromMockProvider());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save:
                if (mOnTheMapItemPair == null) {
                    mView.showSnackbar(R.string.error_1002, -1, null, Snackbar.LENGTH_LONG);
                    return;
                }

                showEditLocationItemDialog();
                break;
            case R.id.location:
                mView.showMyLocation(false);
                break;
            default:
                // do nothing;
        }
    }

    private void showEditLocationItemDialog() {
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        LocationItem item = mOnTheMapItemPair.second;
        if (mAddressOutput != null && item != null) {
            item.setDisplayedName(mAddressOutput.getLocality());
        }

        EditLocationItemDialog dialog = EditLocationItemDialog.newInstance(
                new EditLocationItemDialog.EditLocationItemDialogListener() {
                    @Override
                    public void onPositiveClick(LocationItem item) {
                        mActivity.finish();
                    }
                }, item
        );
        dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogFragmentTheme);
        dialog.show(fragmentManager, EditLocationItemDialog.TAG);
    }

    @Override
    public void onMapReady() {
        fetchAll();
    }

    @Override
    public void removeMarker() {
        mOnTheMapItemPair = null;
    }

    private void resolveAddressFromLocation(@Nullable LatLng latLng) {
        if (!Geocoder.isPresent()) {
            mView.showSnackbar(R.string.error_1007, -1, null, Snackbar.LENGTH_LONG);
            return;
        }

        if (mAddressRequested) {
            return;
        }

        if (latLng == null) {
            return;
        }

        if (mAddressRequestedLatLng != null && mAddressRequestedLatLng.latitude == latLng.latitude
            && mAddressRequestedLatLng.longitude == latLng.longitude) {
            // cached result
            mView.setAddress(mAddressResult, getMarkerText(mAddressOutput));
            return;
        }
        mAddressRequestedLatLng = latLng;

        Location location = new Location("");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        startGeocoderIntentService(location);
    }

    private @NonNull String getMarkerText(@Nullable Address mAddressOutput) {
        String markerText = "?";
        if (mAddressOutput != null) {
            markerText = mAddressOutput.getLocality();
            if (markerText == null) {
                markerText = mAddressOutput.getSubAdminArea();
            }
            if (markerText == null) {
                markerText = mAddressOutput.getAdminArea();
            }
            if (markerText == null) {
                markerText = mAddressOutput.getCountryName();
            }
        }
        return markerText;
    }

    private void startGeocoderIntentService(@NonNull Location location) {
        mAddressRequested = true;
        updateUIWidgets();

        Intent intent = new Intent(mActivity, GeocoderIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);

        mActivity.getApplication().startService(intent);
    }

    private void updateUIWidgets() {
        mView.setAddressProgressbarVisibility(mAddressRequested ? ProgressBar.VISIBLE : ProgressBar.GONE);
    }

    private void fetchAll() {
        mDisposableGetAll = mDb.locationItemDao().getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FetchAllLocationItemObserver());
        mDisposables.add(mDisposableGetAll);
    }

    private class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mAddressOutput = resultData.getParcelable(Constants.RESULT_DATA_KEY);
            String resultMessage;
            if (resultCode == Constants.FAILURE_RESULT) {
                resultMessage = resultData.getString(Constants.RESULT_DATA_MESSAGE);
            } else {
                resultMessage = getFormattedAddress(mAddressOutput);
            }
            mAddressResult = resultMessage;
            mView.setAddress(mAddressResult, getMarkerText(mAddressOutput));

            mAddressRequested = false;
            updateUIWidgets();
        }

        private String getFormattedAddress(Address address) {
            ArrayList<String> addressFragments = new ArrayList<>();

            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            return TextUtils.join(System.getProperty("line.separator"), addressFragments);
        }
    }

    private class FetchAllLocationItemObserver implements Consumer<List<LocationItem>> {
        @Override
        public void accept(List<LocationItem> locationItems) throws Exception {
            LatLngBounds bounds = AppUtil.getBounds(locationItems);
            mView.addMarkers(locationItems);
            if (bounds != null) {
                mView.showLatLngBounds(bounds, true);
            } else if (locationItems.size() == 1) {
                Object geometry = locationItems.get(0).getGeometry();
                if (geometry instanceof LatLng) {
                    LatLng point = (LatLng) geometry;
                    mView.showLocation(point, 8L, true);
                } else {
                    mView.tryToInitCameraPosition();
                }
            } else {
                mView.tryToInitCameraPosition();
            }
            mDisposables.remove(mDisposableGetAll);
        }
    }
}
