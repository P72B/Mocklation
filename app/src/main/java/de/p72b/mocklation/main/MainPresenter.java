package de.p72b.mocklation.main;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.squareup.sqlbrite2.BriteDatabase;

import java.util.List;

import javax.inject.Inject;

import de.p72b.mocklation.R;
import de.p72b.mocklation.dagger.MocklationApp;
import de.p72b.mocklation.service.database.LocationItem;
import de.p72b.mocklation.service.setting.ISetting;
import de.p72b.mocklation.util.AppUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class MainPresenter implements IMainPresenter {

    private static final String TAG = MainPresenter.class.getSimpleName();
    private final IMainView mView;
    private Activity mActivity;
    private ISetting mSetting;
    @Inject
    BriteDatabase db;
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private IMockServiceInteractor mMockServiceInteractor;
    private LocationItem mSelectedItem;

    MainPresenter(Activity activity, ISetting setting) {
        Log.d(TAG, "new MainPresenter");
        mActivity = activity;
        mView = (IMainView) activity;
        mSetting = setting;
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
        MocklationApp.getComponent(mActivity).inject(this);

        mDisposables.add(db.createQuery(LocationItem.TABLE, AppUtil.LOCATION_ITEMS_QUERY)
                .mapToList(LocationItem.MAPPER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new MainPresenter.LocationItemObserver())
        );

        mView.setPlayStopStatus(mMockServiceInteractor.getState());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mDisposables.clear();
    }

    @Override
    public void locationItemPressed(final LocationItem item) {
        if (mMockServiceInteractor.isServiceRunning()) {
            mView.showSnackbar(R.string.error_1001, R.string.stop, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mMockServiceInteractor.stopMockLocationService();
                    mSetting.saveLastPressedLocation(item.code());
                    mView.selectLocation(item);
                }
            }, Snackbar.LENGTH_LONG);
            return;
        }

        mSetting.saveLastPressedLocation(item.code());
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
            mMockServiceInteractor.startMockLocation(mSelectedItem.code());
        }
    }

    @Override
    public void onMockPermissionsResult(int[] grantedResults) {
        mMockServiceInteractor.onMockPermissionsResult(grantedResults);
    }

    private class LocationItemObserver extends DisposableObserver<List<LocationItem>> {
        @Override
        public void onNext(@io.reactivex.annotations.NonNull List<LocationItem> locationItems) {
            if (locationItems.size() == 0) {
                mView.showEmptyPlaceholder();
                return;
            }

            String lastSelectedItemCode = mSetting.getLastPressedLocationCode();
            mSelectedItem = null;
            if (lastSelectedItemCode != null) {
                for (LocationItem item : locationItems) {
                    if (lastSelectedItemCode.equals(item.code())) {
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
        }

        @Override
        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
            Log.d(TAG, " onError : " + e.getMessage());
        }

        @Override
        public void onComplete() {
            Log.d(TAG, " onComplete");
            mDisposables.remove(this);
        }
    }
}
