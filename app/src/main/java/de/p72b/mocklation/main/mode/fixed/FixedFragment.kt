package de.p72b.mocklation.main.mode.fixed

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.p72b.mocklation.R
import de.p72b.mocklation.main.mode.BaseModeFragment
import de.p72b.mocklation.main.mode.BaseModePresenter
import de.p72b.mocklation.service.AppServices
import de.p72b.mocklation.service.setting.ISetting

class FixedFragment : BaseModeFragment() {

    override fun provideBaseFragmentView(inflater: LayoutInflater, parent: ViewGroup?,
                                         savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_fixed, parent, false)
    }

    override fun provideBaseModePresenter(): BaseModePresenter {
        val setting = AppServices.getService(AppServices.SETTINGS) as ISetting
        return FixedPresenter(this, setting)
    }
}