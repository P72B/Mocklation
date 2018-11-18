package de.p72b.mocklation.main.mode.fixed

import android.support.v4.app.FragmentActivity
import de.p72b.mocklation.main.mode.BaseModePresenter
import de.p72b.mocklation.service.setting.ISetting

class FixedPresenter(activity: FragmentActivity?, fixedFragment: FixedFragment, setting: ISetting) :
        BaseModePresenter(activity, fixedFragment.fragmentManager, fixedFragment, setting) {

}