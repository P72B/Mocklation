package de.p72b.mocklation.main;

import android.app.Activity;
import android.arch.persistence.room.Room;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import java.util.List;

import de.p72b.mocklation.R;
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
    private Activity mActivity;
    private ISetting mSetting;
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private IMockServiceInteractor mMockServiceInteractor;
    private Disposable mDisposableDeleteItem;
    private Disposable mDisposableInsertAll;
    private LocationItem mSelectedItem;
    private Disposable mDisposableGetAll;
    private List<LocationItem> mLocationItems;

    MainPresenter(Activity activity, ISetting setting) {
        Log.d(TAG, "new MainPresenter");
        mActivity = activity;
        mView = (IMainView) activity;
        mSetting = setting;
        mDb = Room.databaseBuilder(mActivity, AppDatabase.class, AppDatabase.DB_NAME_LOCATIONS).build();
        mMockServiceInteractor = new MockServiceInteractor(mActivity, mSetting, new MockServiceInteractor.MockServiceListener() {
            @Override
            public void onStart() {
                Log.d(TAG, "MockServiceListener onStart()");
                mView.setPlayStopStatus(mMockServiceInteractor.getState());
            }

            @Override
            public void onStop() {
                Log.d(TAG, "MockServiceListener onStop()");
                mView.setPlayStopStatus(mMockServiceInteractor.getState());
            }

            @Override
            public void onError() {
                Log.d(TAG, "MockServiceListener onError()");

            }
        });

        mView.setPlayStopStatus(mMockServiceInteractor.getState());
    }

    @Override
    public void onResume() {
        mDisposableGetAll = mDb.locationItemDao().getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new LocationItemObserver());
        mDisposables.add(mDisposableGetAll);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mDisposables.clear();
    }

    @Override
    public void locationItemPressed(final LocationItem item) {
        mSelectedItem = item;
        if (mMockServiceInteractor.isServiceRunning()) {
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
    public void onPlayClicked() {
        if (mSelectedItem == null) {
            // TODO show error missing location item to start mocking.
            return;
        }

        if (mSetting.getMockLocationItemCode() != null) {
            mMockServiceInteractor.stopMockLocationService();
        } else {
            mMockServiceInteractor.startMockLocation(mSelectedItem.getCode());
        }
    }

    @Override
    public void locationItemRemoved(final LocationItem item) {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                mDb.locationItemDao().delete(item);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        mDisposableDeleteItem = disposable;
                        mDisposables.add(mDisposableDeleteItem);
                    }

                    @Override
                    public void onComplete() {
                        mDisposables.remove(mDisposableDeleteItem);
                        mLocationItems.remove(item);
                        handleLocationItems(mLocationItems);
                        mView.showSnackbar(R.string.message_location_item_removed,
                                R.string.snackbar_action_undo, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        saveLocationItem(item);
                                    }
                                }, Snackbar.LENGTH_LONG);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.showSnackbar(R.string.error_1008,
                                R.string.snackbar_action_retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        locationItemRemoved(item);
                                    }
                                }, Snackbar.LENGTH_LONG);
                    }
                });
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
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable disposable) {
                        mDisposableInsertAll = disposable;
                        mDisposables.add(mDisposableInsertAll);
                    }

                    @Override
                    public void onComplete() {
                        mDisposables.remove(mDisposableInsertAll);
                        mLocationItems.add(item);
                        handleLocationItems(mLocationItems);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.showSnackbar(R.string.error_1009,
                                R.string.snackbar_action_retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        saveLocationItem(item);
                                    }
                                }, Snackbar.LENGTH_LONG);
                    }
                });
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

    private class LocationItemObserver implements Consumer<List<LocationItem>> {
        @Override
        public void accept(List<LocationItem> locationItems) throws Exception {
            handleLocationItems(locationItems);
            mDisposables.remove(mDisposableGetAll);
        }
    }
}
