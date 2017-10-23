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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
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
    private LocationItem mSelectedItem;
    private Disposable mDisposableGetAll;

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
    public void onMockPermissionsResult(int[] grantedResults) {
        mMockServiceInteractor.onMockPermissionsResult(grantedResults);
    }

    private class LocationItemObserver implements Consumer<List<LocationItem>> {
        @Override
        public void accept(List<LocationItem> locationItems) throws Exception {
            if (locationItems.size() == 0) {
                mView.showEmptyPlaceholder();
                return;
            }

            String lastSelectedItemCode = mSetting.getLastPressedLocationCode();
            mSelectedItem = null;
            if (lastSelectedItemCode != null) {
                for (LocationItem item : locationItems) {
                    if (lastSelectedItemCode.equals(item.getCode())) {
                        mSelectedItem = item;
                        break;
                    }
                }
            }

            if (mSelectedItem == null) {
                // preselect some item
                mSelectedItem = locationItems.get(0);
            }

            mView.showSavedLocations(locationItems);
            mView.selectLocation(mSelectedItem);

            mDisposables.remove(mDisposableGetAll);
        }
    }
}
