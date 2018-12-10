package de.p72b.mocklation.main.mode.fixed

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.google.maps.android.data.geojson.GeoJsonPoint
import de.p72b.mocklation.R
import de.p72b.mocklation.main.mode.BaseModeFragment
import de.p72b.mocklation.main.mode.BaseModePresenter
import de.p72b.mocklation.service.AppServices
import de.p72b.mocklation.service.room.LocationItem
import de.p72b.mocklation.service.setting.ISetting

class FixedFragment : BaseModeFragment() {

    private lateinit var root: View
    private lateinit var selectedLocationName: TextView
    private lateinit var selectedLocationLatitude: EditText
    private lateinit var selectedLocationLongitude: EditText

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanseState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_fixed, parent, false)
        return super.onCreateView(inflater, parent, savedInstanseState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun provideBaseFragmentView(inflater: LayoutInflater, parent: ViewGroup?,
                                         savedInstanceState: Bundle?): View {
        return root
    }

    override fun provideBaseModePresenter(): BaseModePresenter {
        val setting = AppServices.getService(AppServices.SETTINGS) as ISetting
        return FixedPresenter(this, setting)
    }

    override fun showSnackbar(message: Int, action: Int, listener: View.OnClickListener?, duration: Int) {
        val snackbar = Snackbar.make(root, message, duration)
        if (action != -1) {
            snackbar.setAction(action, listener)
        }
        snackbar.show()
    }

    override fun selectLocation(item: LocationItem) {
        super.selectLocation(item)
        selectedLocationName.text = LocationItem.getNameToBeDisplayed(item)
        item.geometry.let {
            when (it) {
                is GeoJsonPoint -> {
                    val latLng = it.coordinates
                    selectedLocationLatitude.setText(latLng.latitude.toString())
                    selectedLocationLongitude.setText(latLng.longitude.toString())
                }
                else -> { }
            }
        }
    }

    private fun initViews() {
        selectedLocationName = root.findViewById(R.id.card_view_selected_location_name)
        selectedLocationLatitude = root.findViewById(R.id.card_view_selected_location_latitude)
        selectedLocationLongitude = root.findViewById(R.id.card_view_selected_location_longitude)
    }
}