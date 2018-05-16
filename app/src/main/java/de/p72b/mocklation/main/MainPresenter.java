package de.p72b.mocklation.main;

import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

import de.p72b.mocklation.R;
import de.p72b.mocklation.dialog.EditLocationItemDialog;
import de.p72b.mocklation.service.analytics.AnalyticsService;
import de.p72b.mocklation.service.analytics.IAnalyticsService;
import de.p72b.mocklation.service.room.AppDatabase;
import de.p72b.mocklation.service.room.LocationItem;
import de.p72b.mocklation.service.setting.ISetting;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainPresenter implements IMainPresenter {

    private static final String TAG = MainPresenter.class.getSimpleName();
    private IMainView mView;
    private AppDatabase mDb;
    private FragmentActivity mActivity;
    private ISetting mSetting;
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private IMockServiceInteractor mMockServiceInteractor;
    private Disposable mDisposableDeleteItem;
    private Disposable mDisposableInsertAll;
    private LocationItem mSelectedItem;
    private Disposable mDisposableGetAll;
    private Disposable mDisposableUpdateItem;
    private IAnalyticsService mAnalyticsService;
    private List<LocationItem> mLocationItems;

    MainPresenter(FragmentActivity activity, ISetting setting, IAnalyticsService analytics) {
        Log.d(TAG, "new MainPresenter");
        mActivity = activity;
        mView = (IMainView) activity;
        mSetting = setting;
        mAnalyticsService = analytics;
        mDb = Room.databaseBuilder(mActivity, AppDatabase.class, AppDatabase.DB_NAME_LOCATIONS).build();
        mMockServiceInteractor = new MockServiceInteractor(mActivity, mSetting,
                new MockServiceListener());

        mView.setPlayPauseStopStatus(mMockServiceInteractor.getState());
    }

    @Override
    public void onResume() {
        fetchAll();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mDisposables.clear();
    }

    @Override
    public void locationItemPressed(final LocationItem item) {
        mSelectedItem = item;
        if (mMockServiceInteractor.isServiceRunning() && !item.getCode().equals(
                mSetting.getMockLocationItemCode())) {
            mView.showSnackbar(R.string.error_1001, R.string.stop, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mMockServiceInteractor.stopMockLocationService();
                    mSetting.saveLastPressedLocation(item.getCode());
                    mView.selectLocation(item);
                }
            }, Snackbar.LENGTH_LONG);
            return;
        }

        mSetting.saveLastPressedLocation(item.getCode());
        mView.selectLocation(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_stop:
                onPlayStopClicked();
                break;
            case R.id.pause:
                onPauseClicked();
                break;
            case R.id.edit:
                showEditLocationItemDialog();
                break;
            case R.id.favorite:
                onFavoriteClicked();
                break;
        }
    }

    @Override
    public void locationItemRemoved(final LocationItem item) {
        if (mMockServiceInteractor.isServiceRunning() && item.getCode().equals(
                mSetting.getMockLocationItemCode())) {
            // don't remove the actual mocked location
            mLocationItems.add(item);
            handleLocationItems(mLocationItems);
            mView.showSnackbar(R.string.error_1001, R.string.stop, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mMockServiceInteractor.stopMockLocationService();
                    mSetting.saveLastPressedLocation(item.getCode());
                    mView.selectLocation(item);
                }
            }, Snackbar.LENGTH_LONG);
            return;
        }

        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                mDb.locationItemDao().delete(item);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new DeleteLocationItemObserver(item));
    }

    @Override
    public void onMockPermissionsResult(int[] grantedResults) {
        mMockServiceInteractor.onMockPermissionsResult(grantedResults);
    }

    private void saveLocationItem(final LocationItem item) {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                mDb.locationItemDao().insertAll(item);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SaveLocationItemObserver(item));
    }

    private void handleLocationItems(List<LocationItem> locationItems) {
        mLocationItems = locationItems;
        if (mLocationItems.size() == 0) {
            mView.showEmptyPlaceholder();
            return;
        }

        String lastSelectedItemCode = mSetting.getLastPressedLocationCode();
        mSelectedItem = null;
        if (lastSelectedItemCode != null) {
            for (LocationItem item : mLocationItems) {
                if (lastSelectedItemCode.equals(item.getCode())) {
                    mSelectedItem = item;
                    break;
                }
            }
        }

        if (mSelectedItem == null) {
            // preselect some item
            mSelectedItem = mLocationItems.get(0);
        }

        mView.showSavedLocations(mLocationItems);
        mView.selectLocation(mSelectedItem);
    }

    private void onPauseClicked() {
        if (mSetting.getMockLocationItemCode() != null) {
            switch (mMockServiceInteractor.getState()) {
                case MockServiceInteractor.SERVICE_STATE_RUNNING:
                    mMockServiceInteractor.pauseMockLocationService();
                    break;
                case MockServiceInteractor.SERVICE_STATE_PAUSE:
                    mMockServiceInteractor.playMockLocationService();
                    break;
                case MockServiceInteractor.SERVICE_STATE_STOP:
                    // nothing to do here
                    break;
            }
            mView.setPlayPauseStopStatus(mMockServiceInteractor.getState());
        }
    }

    private void onPlayStopClicked() {
        if (mSelectedItem == null) {
            // TODO show error missing location item to start mocking.
            return;
        }

        if (mSetting.getMockLocationItemCode() != null && mMockServiceInteractor.isServiceRunning()) {
            mMockServiceInteractor.stopMockLocationService();
        } else {
            mMockServiceInteractor.startMockLocation(mSelectedItem.getCode());
        }
    }

    private void showEditLocationItemDialog() {
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        EditLocationItemDialog dialog = EditLocationItemDialog.newInstance(
                new EditLocationItemDialog.EditLocationItemDialogListener() {
                    @Override
                    public void onPositiveClick(LocationItem item) {
                        fetchAll();
                    }
                }, mSelectedItem
        );
        dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogFragmentTheme);
        dialog.show(fragmentManager, EditLocationItemDialog.TAG);
    }

    private void fetchAll() {
        mDisposableGetAll = mDb.locationItemDao().getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FetchAllLocationItemObserver());
        mDisposables.add(mDisposableGetAll);
    }

    private void onFavoriteClicked() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, mSelectedItem.getCode());
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, mSelectedItem.getDisplayedName());
        if (mSelectedItem.isIsFavorite()) {
            mAnalyticsService.trackEvent(AnalyticsService.Event.REMOVE_FAVORITE, bundle);
        } else {
            mAnalyticsService.trackEvent(AnalyticsService.Event.ADD_FAVORITE, bundle);
        }

        mSelectedItem.setIsFavorite(!mSelectedItem.isIsFavorite());
        updateItem(mSelectedItem);
    }

    private void updateItem(final LocationItem item) {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                mDb.locationItemDao().updateLocationItems(item);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new UpdateLocationItemObserver(item));
    }

    private class FetchAllLocationItemObserver implements Consumer<List<LocationItem>> {
        @Override
        public void accept(List<LocationItem> locationItems) throws Exception {
            handleLocationItems(locationItems);
            mDisposables.remove(mDisposableGetAll);
        }
    }

    private class UpdateLocationItemObserver implements CompletableObserver {
        private final LocationItem mItem;

        UpdateLocationItemObserver(LocationItem item) {
            mItem = item;
        }

        @Override
        public void onSubscribe(Disposable disposable) {
            mDisposableUpdateItem = disposable;
            mDisposables.add(mDisposableUpdateItem);
        }

        @Override
        public void onComplete() {
            mDisposables.remove(mDisposableUpdateItem);
            handleLocationItems(mLocationItems);
        }

        @Override
        public void onError(Throwable e) {
            mView.showSnackbar(R.string.error_1012, R.string.snackbar_action_retry,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            updateItem(mItem);
                        }
                    }, Snackbar.LENGTH_LONG);
        }
    }

    private class SaveLocationItemObserver implements CompletableObserver {
        private final LocationItem mItem;

        SaveLocationItemObserver(LocationItem item) {
            mItem = item;
        }

        @Override
        public void onSubscribe(Disposable disposable) {
            mDisposableInsertAll = disposable;
            mDisposables.add(mDisposableInsertAll);
        }

        @Override
        public void onComplete() {
            mDisposables.remove(mDisposableInsertAll);
            mLocationItems.add(mItem);
            handleLocationItems(mLocationItems);
        }

        @Override
        public void onError(Throwable e) {
            mView.showSnackbar(R.string.error_1009,
                    R.string.snackbar_action_retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            saveLocationItem(mItem);
                        }
                    }, Snackbar.LENGTH_LONG);
        }
    }

    private class DeleteLocationItemObserver implements CompletableObserver {
        private final LocationItem mItem;

        DeleteLocationItemObserver(LocationItem item) {
            mItem = item;
        }
        @Override
        public void onSubscribe(Disposable disposable) {
            mDisposableDeleteItem = disposable;
            mDisposables.add(mDisposableDeleteItem);
        }

        @Override
        public void onComplete() {
            mDisposables.remove(mDisposableDeleteItem);
            mLocationItems.remove(mItem);
            handleLocationItems(mLocationItems);
            mView.showSnackbar(R.string.message_location_item_removed,
                    R.string.snackbar_action_undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            saveLocationItem(mItem);
                        }
                    }, Snackbar.LENGTH_LONG);
        }

        @Override
        public void onError(Throwable e) {
            mView.showSnackbar(R.string.error_1008,
                    R.string.snackbar_action_retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            locationItemRemoved(mItem);
                        }
                    }, Snackbar.LENGTH_LONG);
        }
    }

    private class MockServiceListener implements MockServiceInteractor.MockServiceListener {
        @Override
        public void onStart() {
            Log.d(TAG, "MockServiceListener onStart()");
            mView.setPlayPauseStopStatus(mMockServiceInteractor.getState());
        }

        @Override
        public void onStop() {
            Log.d(TAG, "MockServiceListener onStop()");
            mView.setPlayPauseStopStatus(mMockServiceInteractor.getState());
        }

        @Override
        public void onUpdate() {
            Log.d(TAG, "MockServiceListener onUpdate()");
            mView.setPlayPauseStopStatus(mMockServiceInteractor.getState());
        }
    }
}
