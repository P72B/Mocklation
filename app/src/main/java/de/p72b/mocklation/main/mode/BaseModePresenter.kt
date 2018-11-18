package de.p72b.mocklation.main.mode

import android.arch.persistence.room.Room
import android.support.design.widget.Snackbar
import android.view.View
import de.p72b.mocklation.R
import de.p72b.mocklation.dagger.MocklationApp
import de.p72b.mocklation.service.room.AppDatabase
import de.p72b.mocklation.service.room.LocationItem
import de.p72b.mocklation.service.setting.ISetting
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

open class BaseModePresenter(private val view: BaseModeFragment,
                             private val setting: ISetting) {

    private lateinit var locationItemList: List<LocationItem>
    private var selectedItem: LocationItem? = null
    private var db: AppDatabase = Room.databaseBuilder(MocklationApp.getInstance(),
            AppDatabase::class.java, AppDatabase.DB_NAME_LOCATIONS).build()
    private var disposableGetAll: Disposable? = null
    private val disposables = CompositeDisposable()


    fun onResume() {
        fetchAll()
    }

    fun onDestroy() {
        disposables.clear()
    }

    fun locationItemPressed(item: LocationItem) {
        selectedItem = item
        /*
        if (mockServiceInteractor.isServiceRunning() && item.code != setting.mockLocationItemCode) {
            view.showSnackbar(R.string.error_1001, R.string.stop, View.OnClickListener {
                mockServiceInteractor.stopMockLocationService()
                setting.saveLastPressedLocation(item.code)
                view.selectLocation(item)
            }, Snackbar.LENGTH_LONG)
            return
        }
        */

        setting.saveLastPressedLocation(item.code)
        view.selectLocation(item)
    }

    fun locationItemRemoved(item: LocationItem) {

    }

    private fun fetchAll() {
       val disposable = db.locationItemDao().all
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(FetchAllLocationItemObserver())
        disposableGetAll = disposable
        disposables.add(disposable)
    }

    private fun handleLocationItems(locationItems: List<LocationItem>) {
        locationItemList = locationItems
        if (locationItemList.isEmpty()) {
            view.showEmptyPlaceholder()
            return
        }

        val lastSelectedItemCode = setting.lastPressedLocationCode
        selectedItem = null
        if (lastSelectedItemCode != null) {
            for (item in locationItemList) {
                if (lastSelectedItemCode == item.code) {
                    selectedItem = item
                    break
                }
            }
        }

        if (selectedItem == null) {
            // preselect some item
            selectedItem = locationItemList[0]
        }

        view.showSavedLocations(locationItemList)
        view.selectLocation(selectedItem!!)
    }

    fun onClick(id: Int) {
        when(id) {
            R.id.vPlayStop -> {}
            R.id.vPause -> {}
            R.id.vFavorite -> onFavoriteClicked()
            R.id.vEdit -> {}
        }
    }

    private fun onFavoriteClicked() {
        if (selectedItem == null) {
            return
        }
        val item = selectedItem
        item!!.isIsFavorite = !item.isIsFavorite
        updateItem(item)
    }

    private fun updateItem(item: LocationItem) {
        Completable.fromAction { db.locationItemDao().updateLocationItems(item) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(UpdateLocationItemObserver(item))
    }

    private inner class FetchAllLocationItemObserver : Consumer<List<LocationItem>> {
        @Throws(Exception::class)
        override fun accept(locationItems: List<LocationItem>) {
            handleLocationItems(locationItems)
            disposableGetAll?.let { disposables.remove(it) }
        }
    }


    private inner class UpdateLocationItemObserver internal constructor(private val mItem: LocationItem) : CompletableObserver {
        private var disposableUpdateItem: Disposable? = null

        override fun onSubscribe(disposable: Disposable) {
            disposableUpdateItem = disposable
            disposables.add(disposableUpdateItem!!)
        }

        override fun onComplete() {
            disposables.remove(disposableUpdateItem!!)
            handleLocationItems(locationItemList)
        }

        override fun onError(e: Throwable) {
            view.showSnackbar(R.string.error_1012, R.string.snackbar_action_retry,
                    View.OnClickListener { updateItem(mItem) }, Snackbar.LENGTH_LONG)
        }
    }
}