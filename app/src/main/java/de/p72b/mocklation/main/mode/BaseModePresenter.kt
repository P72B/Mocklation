package de.p72b.mocklation.main.mode

import android.arch.persistence.room.Room
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.view.View
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import de.p72b.mocklation.R
import de.p72b.mocklation.dagger.MocklationApp
import de.p72b.mocklation.dialog.PrivacyUpdateDialog
import de.p72b.mocklation.service.room.AppDatabase
import de.p72b.mocklation.service.room.LocationItem
import de.p72b.mocklation.service.setting.ISetting
import de.p72b.mocklation.util.Constants
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

open class BaseModePresenter(private val supportFragmentManager: FragmentManager?,
                             private val view: BaseModeFragment,
                             private val setting: ISetting) {

    private lateinit var locationItemList: MutableList<LocationItem>
    private lateinit var mockServiceInteractor: MockServiceInteractor
    private val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
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
        if (mockServiceInteractor.isServiceRunning && item.code != setting.mockLocationItemCode) {
            view.showSnackbar(R.string.error_1001, R.string.stop, View.OnClickListener {
                mockServiceInteractor.stopMockLocationService()
                setting.saveLastPressedLocation(item.code)
                view.selectLocation(item)
            }, Snackbar.LENGTH_LONG)
            return
        }

        setting.saveLastPressedLocation(item.code)
        view.selectLocation(item)
    }

    fun locationItemRemoved(item: LocationItem) {
        if (mockServiceInteractor.isServiceRunning && item.code == setting.mockLocationItemCode) {
            // don't remove the actual mocked location
            locationItemList.add(item)
            handleLocationItems(locationItemList)
            view.showSnackbar(R.string.error_1001, R.string.stop, View.OnClickListener {
                mockServiceInteractor.stopMockLocationService()
                setting.saveLastPressedLocation(item.code)
                view.selectLocation(item)
            }, Snackbar.LENGTH_LONG)
            return
        }

        Completable.fromAction { db.locationItemDao().delete(item) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(DeleteLocationItemObserver(item))
    }

    private fun fetchAll() {
        val disposable = db.locationItemDao().all
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(FetchAllLocationItemObserver())
        disposableGetAll = disposable
        disposables.add(disposable)
    }

    private fun handleLocationItems(locationItems: MutableList<LocationItem>) {
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
        when (id) {
            R.id.vPlayStop -> onPlayStopClicked()
            R.id.vPause -> onPauseClicked()
            R.id.vFavorite  -> onFavoriteClicked()
            R.id.vEdit -> {
            }
        }
    }

    private fun onPlayStopClicked() {
        if (!setting.isPrivacyStatementAccepted) {
            showPrivacyUpdateDialog()
            return
        }
        if (selectedItem == null) {
            // TODO show error missing location item to start mocking.
            return
        }

        if (setting.mockLocationItemCode != null && mockServiceInteractor.isServiceRunning) {
            mockServiceInteractor.stopMockLocationService()
        } else {
            mockServiceInteractor.startMockLocation(selectedItem!!.code)
        }
    }

    private fun onPauseClicked() {
        if (setting.mockLocationItemCode == null) {
            return
        }
        val state = mockServiceInteractor.state
        when (state) {
            MockServiceInteractor.SERVICE_STATE_RUNNING -> mockServiceInteractor.pauseMockLocationService()
            MockServiceInteractor.SERVICE_STATE_PAUSE -> mockServiceInteractor.playMockLocationService()
            MockServiceInteractor.SERVICE_STATE_STOP -> { }
        }
        view.setPlayPauseStopStatus(state)
    }

    private fun showPrivacyUpdateDialog() {
        val dialog = PrivacyUpdateDialog.newInstance(
                object : PrivacyUpdateDialog.PrivacyUpdateDialogListener {
                    override fun onAcceptClick() {
                        setting.acceptCurrentPrivacyStatement()
                        onPlayStopClicked()
                    }

                    override fun onDeclineClick() {
                        view.showSnackbar(R.string.error_1020, -1, null,
                                Snackbar.LENGTH_LONG)
                    }
                }, firebaseRemoteConfig.getString(Constants.REMOTE_CONFIG_KEY_URL_PRIVACY_POLICY))
        dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogFragmentTheme)
        dialog.show(supportFragmentManager, PrivacyUpdateDialog.TAG)
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

    private fun saveLocationItem(item: LocationItem) {
        Completable.fromAction { db.locationItemDao().insertAll(item) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(SaveLocationItemObserver(item))
    }

    fun setActivity(activity: FragmentActivity) {
        mockServiceInteractor = MockServiceInteractor(activity, setting, MockServiceListener())
    }

    private inner class FetchAllLocationItemObserver : Consumer<MutableList<LocationItem>> {
        @Throws(Exception::class)
        override fun accept(locationItems: MutableList<LocationItem>) {
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

    private inner class DeleteLocationItemObserver internal constructor(private val item: LocationItem) : CompletableObserver {
        private var disposableDeleteItem: Disposable? = null

        override fun onSubscribe(disposable: Disposable) {
            disposableDeleteItem = disposable
            disposables.add(disposableDeleteItem!!)
        }

        override fun onComplete() {
            disposables.remove(disposableDeleteItem!!)
            locationItemList.remove(item)
            handleLocationItems(locationItemList)
            view.showSnackbar(R.string.message_location_item_removed, R.string.snackbar_action_undo,
                    View.OnClickListener { saveLocationItem(item) }, Snackbar.LENGTH_LONG)
        }

        override fun onError(e: Throwable) {
            view.showSnackbar(R.string.error_1008, R.string.snackbar_action_retry,
                    View.OnClickListener { locationItemRemoved(item) }, Snackbar.LENGTH_LONG)
        }
    }

    private inner class SaveLocationItemObserver internal constructor(private val mItem: LocationItem) : CompletableObserver {
        private var disposableInsertAll: Disposable? = null

        override fun onSubscribe(disposable: Disposable) {
            disposableInsertAll = disposable
            disposables.add(disposableInsertAll!!)
        }

        override fun onComplete() {
            disposables.remove(disposableInsertAll!!)
            locationItemList.add(mItem)
            handleLocationItems(locationItemList)
        }

        override fun onError(e: Throwable) {
            view.showSnackbar(R.string.error_1009, R.string.snackbar_action_retry,
                    View.OnClickListener { saveLocationItem(mItem) }, Snackbar.LENGTH_LONG)
        }
    }

    private inner class MockServiceListener : MockServiceInteractor.MockServiceListener {
        override fun onStart() {
            view.setPlayPauseStopStatus(mockServiceInteractor.state)
        }

        override fun onStop() {
            view.setPlayPauseStopStatus(mockServiceInteractor.state)
        }

        override fun onUpdate() {
            view.setPlayPauseStopStatus(mockServiceInteractor.state)
        }
    }
}