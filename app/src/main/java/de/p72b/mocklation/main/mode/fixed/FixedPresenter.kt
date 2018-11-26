package de.p72b.mocklation.main.mode.fixed

import de.p72b.mocklation.main.mode.BaseModePresenter
import de.p72b.mocklation.service.room.Mode
import de.p72b.mocklation.service.setting.ISetting

class FixedPresenter(fixedFragment: FixedFragment, setting: ISetting) :
        BaseModePresenter(fixedFragment.fragmentManager, fixedFragment, setting) {

    override fun getMode() : Mode {
        return Mode.FIXED
    }

}