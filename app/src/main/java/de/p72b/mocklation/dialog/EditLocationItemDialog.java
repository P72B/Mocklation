package de.p72b.mocklation.dialog;

import android.app.Dialog;
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

import java.util.List;

import de.p72b.mocklation.R;
import de.p72b.mocklation.service.room.AppDatabase;
import de.p72b.mocklation.service.room.LocationItem;
import de.p72b.mocklation.util.AppUtil;
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
    private TextInputLayout mDisplayedNameLayoutName;
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private Disposable mDisposableInsertAll;
    private AppDatabase mDb = null;
    private Disposable mDisposableFindByCode;
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
        toolbar.setTitle("Edit location");

        AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity != null) {

            activity.setSupportActionBar(toolbar);
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
                actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
            }
            setHasOptionsMenu(true);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mRootView = view;
        Bundle arguments = getArguments();
        if (arguments != null) {
            mLocationItem = arguments.getParcelable(EXTRA_LOCATION_ITEM);
        }
        if (getContext() != null) {
            mDb = Room.databaseBuilder(getContext(), AppDatabase.class, AppDatabase.DB_NAME_LOCATIONS).build();
        }
        mDisplayedName = mRootView.findViewById(R.id.input_displayed_name);
        mDisplayedName.addTextChangedListener(new SimpleTextWatcher(mDisplayedName));
        mDisplayedNameLayoutName = mRootView.findViewById(R.id.input_layout_pin);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        return dialog;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.menu_dialog_edit_location_item, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            if (!validateData()) {
                return true;
            }

            saveItem();
            return true;
        } else if (id == android.R.id.home) {
            dismiss();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveItem() {
        // new item was created, not restored from mSettings
        if (mDb == null) {
            // TODO: show error
            return;
        }

        // save the form data
        mLocationItem.setDisplayedName(mDisplayedName.getText().toString());

        requestLocationItem(mLocationItem.getDisplayedName());
    }

    private void wirteItem() {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                mDb.locationItemDao().insertAll(mLocationItem);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
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
                });
    }

    public void setListener(EditLocationItemDialogListener listener) {
        mListener = listener;
    }

    private boolean validateData() {
        if (mDisplayedName.getText().toString().trim().isEmpty()) {
            mDisplayedName.setError(getString(R.string.error_1010));
            return false;
        }

        mDisplayedNameLayoutName.setErrorEnabled(false);
        return true;
    }

    private void requestLocationItem(String displayedName) {
        Log.d(TAG, "requestLocationItem");
        mDisposableFindByCode = mDb.locationItemDao().findByDisplayedName(displayedName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LocationItemObserver());
        mDisposables.add(mDisposableFindByCode);
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
                case R.id.input_displayed_name:
                    validateData();
                    break;
            }
        }
    }

    private class LocationItemObserver implements Consumer<List<LocationItem>> {
        @Override
        public void accept(List<LocationItem> locationItems) throws Exception {
            mDisposables.remove(mDisposableFindByCode);

            if (locationItems == null) {
                return;
            }

            if (locationItems.size() == 0) {
                wirteItem();
                return;
            }

            showSnackbar(R.string.error_1011, R.string.action_save, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    wirteItem();
                }
            }, Snackbar.LENGTH_LONG);
        }
    }
}