package de.p72b.mocklation.map;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.data.Geometry;
import com.google.maps.android.data.geojson.GeoJsonPoint;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import de.p72b.locator.location.ILastLocationListener;
import de.p72b.locator.location.LocationManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.room.Room;
import de.p72b.mocklation.R;
import de.p72b.mocklation.dialog.EditLocationItemDialog;
import de.p72b.mocklation.service.geocoder.Constants;
import de.p72b.mocklation.service.geocoder.GeocoderIntentService;
import de.p72b.mocklation.service.room.AppDatabase;
import de.p72b.mocklation.service.room.LocationItem;
import de.p72b.mocklation.service.room.Mode;
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
    private LocationManager mLocationManager;

    MapsPresenter(FragmentActivity activity, @NonNull final LocationManager locationManager) {
        Logger.d(TAG, "new MapsPresenter");
        mActivity = activity;
        mView = (IMapsView) activity;
        mAddressRequested = false;
        mAddressOutput = null;
        mResultReceiver = new AddressResultReceiver(new Handler());
        mDb = AppDatabase.getLocationsDb().build();
        mLocationManager = locationManager;

        updateUIWidgets();
    }

    @Override
    public void onDestroy() {
        mDisposables.clear();
    }

    @Override
    public void onMapLongClicked(LatLng latLng) {
        final LatLng roundedLatLng = AppUtil.roundLatLng(latLng);
        final String code = AppUtil.createLocationItemCode(roundedLatLng);
        final String geometry = AppUtil.geometryToString(new GeoJsonPoint(roundedLatLng));
        if (geometry == null) {
            return;
        }
        final LocationItem item = new LocationItem(code, "", geometry, 6, 0, Mode.FIXED.name());
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
                mLocationManager.getLastLocation(new ILastLocationListener() {
                    @Override
                    public void onSuccess(@Nullable Location location) {
                        if (location != null) {
                            mView.showMyLocation(false, location);
                        } else {
                            mView.showSnackbar(R.string.error_1021, -1, null, Snackbar.LENGTH_LONG);
                        }
                    }

                    @Override
                    public void onError(int code, @Nullable String message) {
                        switch(code) {
                            case LocationManager.ERROR_LOCATION_UPDATES_RETRY_LIMIT:
                            case LocationManager.ERROR_SETTINGS_NOT_FULFILLED:
                            case LocationManager.ERROR_FUSED_LOCATION_ERROR:
                            case LocationManager.ERROR_MISSING_PERMISSION:
                            case LocationManager.ERROR_CANCELED_PERMISSION_CHANGE:
                            case LocationManager.ERROR_CANCELED_SETTINGS_CHANGE:
                                mView.showSnackbar(R.string.error_1021, -1, null, Snackbar.LENGTH_LONG);
                                break;
                            case LocationManager.ERROR_MISSING_PERMISSION_DO_NOT_ASK_AGAIN:
                                mView.showSnackbar(R.string.error_1022, R.string.action_settings, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        final Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
                                        final Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setData(uri);
                                        mActivity.startActivity(intent);
                                    }
                                }, Snackbar.LENGTH_LONG);
                                break;
                        }
                    }
                });
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
         * Receives data sent from FetchAddressIntentService and updates the UI in OldMainActivity.
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
        public void accept(List<LocationItem> locationItems) {
            LatLngBounds bounds = AppUtil.getBounds(locationItems);
            mView.addMarkers(locationItems);
            if (bounds != null) {
                mView.showLatLngBounds(bounds, true);
            } else if (locationItems.size() == 1) {
                Geometry geometry = locationItems.get(0).getGeometry();
                if (geometry instanceof GeoJsonPoint) {
                    final LatLng point = ((GeoJsonPoint) geometry).getCoordinates();
                    mView.showLocation(point, 8L, true);
                }
            }
            mDisposables.remove(mDisposableGetAll);
        }
    }
}
