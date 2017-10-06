package de.p72b.mocklation.main;

import android.app.Activity;
import android.util.Log;

import com.squareup.sqlbrite2.BriteDatabase;

import java.util.List;

import javax.inject.Inject;

import de.p72b.mocklation.dagger.MocklationApp;
import de.p72b.mocklation.service.database.LocationItem;
import de.p72b.mocklation.util.AppUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class MainPresenter implements IMainPresenter {

    private static final String TAG = MainPresenter.class.getSimpleName();
    private final IMainView mView;
    private Activity mActivity;
    @Inject
    BriteDatabase db;
    private CompositeDisposable mDisposables = new CompositeDisposable();

    MainPresenter(Activity activity) {
        Log.d(TAG, "new MainPresenter");
        mActivity = activity;
        mView = (IMainView) activity;
        MocklationApp.getComponent(mActivity).inject(this);

        mDisposables.add(db.createQuery(LocationItem.TABLE, AppUtil.LOCATION_ITEMS_QUERY)
                .mapToList(LocationItem.MAPPER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new MainPresenter.LocationItemObserver())
        );
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mDisposables.clear();
    }

    private class LocationItemObserver extends DisposableObserver<List<LocationItem>> {
        @Override
        public void onNext(@io.reactivex.annotations.NonNull List<LocationItem> locationItems) {
            mView.showSavedLocations(locationItems);
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
