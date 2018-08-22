package de.p72b.mocklation.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.arch.persistence.room.EmptyResultSetException;
import android.arch.persistence.room.Room;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import de.p72b.mocklation.R;
import de.p72b.mocklation.service.room.AppDatabase;
import de.p72b.mocklation.service.room.LocationItem;
import de.p72b.mocklation.util.AppUtil;
import de.p72b.mocklation.util.Logger;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class EditLocationItemDialog extends DialogFragment {

    public static final String TAG = EditLocationItemDialog.class.getSimpleName();
    public static final String EXTRA_LOCATION_ITEM = "EXTRA_LOCATION_ITEM";
    private EditLocationItemDialogListener mListener;
    private LocationItem mLocationItem;
    private EditText mDisplayedName;
    private EditText mLatitude;
    private EditText mLongitude;
    private TextInputLayout mDisplayedNameLayoutName;
    private TextInputLayout mLatitudeLayoutName;
    private TextInputLayout mLongitudeLayoutName;
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private Disposable mDisposableInsertAll;
    private Disposable mDisposableFindByCode;
    private Disposable mDisposableFindByDisplayedName;
    private Disposable mDisposableUpdateItem;
    private AppDatabase mDb = null;
    private View mRootView;

    public EditLocationItemDialog() {
    }

    public static EditLocationItemDialog newInstance(EditLocationItemDialogListener listener,
                                                     final LocationItem locationItem) {
        EditLocationItemDialog dialogFragment = new EditLocationItemDialog();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_LOCATION_ITEM, locationItem);
        dialogFragment.setArguments(args);
        dialogFragment.setListener(listener);
        return dialogFragment;
    }

    public static EditLocationItemDialog findOnStack(FragmentManager fragmentManager) {
        Fragment dialogFragment = fragmentManager.findFragmentByTag(TAG);

        if (dialogFragment instanceof EditLocationItemDialog) {
            return (EditLocationItemDialog) dialogFragment;
        }
        return null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_edit_item, container, false);

        Toolbar toolbar = rootView.findViewById(R.id.dialog_edit_item_toolbar);
        toolbar.setTitle(R.string.dialog_edit_title);

        AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity != null) {

            activity.setSupportActionBar(toolbar);
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
            }
            setHasOptionsMenu(true);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRootView = view;
        Bundle arguments = getArguments();
        if (arguments != null) {
            mLocationItem = arguments.getParcelable(EXTRA_LOCATION_ITEM);
        } else {
            return;
        }
        if (getContext() != null) {
            mDb = Room.databaseBuilder(getContext(), AppDatabase.class, AppDatabase.DB_NAME_LOCATIONS).build();
        }
        mDisplayedName = mRootView.findViewById(R.id.input_displayed_name);
        mLatitude = mRootView.findViewById(R.id.input_latitude);
        mLongitude = mRootView.findViewById(R.id.input_longitude);

        mDisplayedNameLayoutName = mRootView.findViewById(R.id.input_layout_pin);
        mLatitudeLayoutName = mRootView.findViewById(R.id.input_layout_latitude);
        mLongitudeLayoutName = mRootView.findViewById(R.id.input_layout_longitude);

        Object geometry = mLocationItem.getGeometry();
        if ((geometry instanceof LatLng)) {
            mLatitudeLayoutName.setVisibility(View.VISIBLE);
            mLongitudeLayoutName.setVisibility(View.VISIBLE);
            mLatitude.setText(String.valueOf(((LatLng) geometry).latitude));
            mLongitude.setText(String.valueOf(((LatLng) geometry).longitude));
        }
        mDisplayedName.setText(mLocationItem.getDisplayedName());

        mDisplayedName.addTextChangedListener(new SimpleTextWatcher(mDisplayedName));
        mLatitude.addTextChangedListener(new SimpleTextWatcher(mLatitude));
        mLongitude.addTextChangedListener(new SimpleTextWatcher(mLongitude));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        Window window = dialog.getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setBackgroundDrawable(new ColorDrawable(0));
        }
        return dialog;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        Activity activity = getActivity();
        if (activity != null) {
            activity.getMenuInflater().inflate(R.menu.menu_dialog_edit_location_item, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            if (!validateData()) {
                return true;
            }

            checkUpdateOrCreateMode();
            return true;
        } else if (id == android.R.id.home) {
            dismiss();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkUpdateOrCreateMode() {
        checkDbConnection();

        Logger.d(TAG, "requestLocationItem");
        mDisposableFindByCode = mDb.locationItemDao().findByCode(mLocationItem.getCode())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LocationItemCodeObserver(), new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (throwable instanceof EmptyResultSetException) {
                            // create NEW item
                            saveItem();
                            return;
                        }
                        Logger.d(TAG, "Error on getting DEvents - " + Log.getStackTraceString(throwable));
                        retry();
                    }
                });
        mDisposables.add(mDisposableFindByCode);
    }

    private void checkDbConnection() {
        if (mDb == null) {
            retry();
        }
    }

    private void retry() {
        showSnackbar(R.string.error_1012, R.string.snackbar_action_retry, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUpdateOrCreateMode();
            }
        }, Snackbar.LENGTH_LONG);
    }

    private void saveItem() {
        checkDbConnection();
        saveFormData();
        requestLocationItem(mLocationItem.getDisplayedName());
    }

    private void saveFormData() {
        // save the form data
        mLocationItem.setDisplayedName(mDisplayedName.getText().toString());
        if (mLatitudeLayoutName.getVisibility() == View.VISIBLE) {
            String geoJson = "{'type':'Feature','properties':{},'geometry':{'type':'Point','coordinates':[" + mLongitude.getText() + "," + mLatitude.getText() + "]}}";
            mLocationItem.setGeoJson(geoJson);
        }
    }

    private void writeItem() {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                mDb.locationItemDao().insertAll(mLocationItem);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SaveLocationItemObserver());
    }

    public void setListener(EditLocationItemDialogListener listener) {
        mListener = listener;
    }

    private boolean validateData() {
        if (mDisplayedName.getText().toString().length() == 0) {
            mDisplayedName.setError(getString(R.string.error_1010));
            return false;
        }
        float latitude;
        float longitude;
        try {
            longitude = Float.valueOf(mLongitude.getText().toString());
        } catch (NumberFormatException exception){
            mLongitude.setError(getString(R.string.error_1017));
            return false;
        }
        try {
            latitude = Float.valueOf(mLatitude.getText().toString());
        } catch (NumberFormatException exception){
            mLatitude.setError(getString(R.string.error_1017));
            return false;
        }
        if (mLongitude.getText().toString().length() == 0 ||
                mLatitude.getText().toString().length() == 0) {
            mLongitude.setError(getString(R.string.error_1013));
            return false;
        }
        if (latitude > 90 || latitude < -90) {
            mLatitude.setError(getString(R.string.error_1014));
            return false;
        }
        if (longitude > 180 || longitude < -180) {
            mLongitude.setError(getString(R.string.error_1015));
            return false;
        }

        mDisplayedNameLayoutName.setErrorEnabled(false);
        mLatitudeLayoutName.setErrorEnabled(false);
        mLongitudeLayoutName.setErrorEnabled(false);
        return true;
    }

    private void requestLocationItem(String displayedName) {
        Logger.d(TAG, "requestLocationItem");
        mDisposableFindByDisplayedName = mDb.locationItemDao().findByDisplayedName(displayedName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LocationItemDisplayedNameObserver());
        mDisposables.add(mDisposableFindByDisplayedName);
    }

    private void showSnackbar(int message, int action, View.OnClickListener listener, int duration) {
        AppUtil.hideKeyboard(getContext(), mRootView);

        Snackbar snackbar = Snackbar.make(mRootView, message, duration);
        View view = snackbar.getView();
        view.setElevation(50);
        if (action != -1) {
            snackbar.setAction(action, listener);
        }
        snackbar.show();
    }

    private void updateItem() {
        saveFormData();
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                mDb.locationItemDao().updateLocationItems(mLocationItem);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new UpdateLocationItemObserver());
    }

    public interface EditLocationItemDialogListener {
        void onPositiveClick(LocationItem item);
    }

    private class SimpleTextWatcher implements TextWatcher {

        private View view;

        private SimpleTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_latitude:
                case R.id.input_longitude:
                case R.id.input_displayed_name:
                    // Fallthrough expected
                    validateData();
                    break;
            }
        }
    }

    private class LocationItemDisplayedNameObserver implements Consumer<List<LocationItem>> {
        @Override
        public void accept(List<LocationItem> locationItems) throws Exception {
            mDisposables.remove(mDisposableFindByDisplayedName);

            if (locationItems == null) {
                return;
            }

            if (locationItems.size() == 0) {
                writeItem();
                return;
            }

            showSnackbar(R.string.error_1011, R.string.action_save, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    writeItem();
                }
            }, Snackbar.LENGTH_LONG);
        }
    }

    private class LocationItemCodeObserver implements Consumer<LocationItem> {
        @Override
        public void accept(LocationItem locationItem) throws Exception {
            mDisposables.remove(mDisposableFindByCode);
            updateItem();
        }
    }

    private class SaveLocationItemObserver implements CompletableObserver {
        @Override
        public void onSubscribe(Disposable disposable) {
            mDisposableInsertAll = disposable;
            mDisposables.add(mDisposableInsertAll);
        }

        @Override
        public void onComplete() {
            mDisposables.remove(mDisposableInsertAll);
            if (mListener != null) {
                mListener.onPositiveClick(mLocationItem);
            }
            dismiss();
        }

        @Override
        public void onError(Throwable e) {
            showSnackbar(R.string.error_1012, R.string.snackbar_action_retry, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveItem();
                }
            }, Snackbar.LENGTH_LONG);
        }
    }

    private class UpdateLocationItemObserver implements CompletableObserver {
        @Override
        public void onSubscribe(Disposable disposable) {
            mDisposableUpdateItem = disposable;
            mDisposables.add(mDisposableUpdateItem);
        }

        @Override
        public void onComplete() {
            mDisposables.remove(mDisposableUpdateItem);
            if (mListener != null) {
                mListener.onPositiveClick(mLocationItem);
            }
            dismiss();
        }

        @Override
        public void onError(Throwable e) {
            showSnackbar(R.string.error_1012, R.string.snackbar_action_retry, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateItem();
                }
            }, Snackbar.LENGTH_LONG);
        }
    }
}