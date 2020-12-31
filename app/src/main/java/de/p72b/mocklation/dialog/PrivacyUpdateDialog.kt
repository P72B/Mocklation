package de.p72b.mocklation.dialog

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import de.p72b.mocklation.R
import de.p72b.mocklation.service.AppServices
import de.p72b.mocklation.service.analytics.IAnalyticsService
import de.p72b.mocklation.service.setting.ISetting
import de.p72b.mocklation.util.AppUtil

class PrivacyUpdateDialog : DialogFragment() {

    companion object {
        val TAG: String = PrivacyUpdateDialog::class.java.simpleName
        fun newInstance(listener: DialogListener?): PrivacyUpdateDialog {
            val dialogFragment = PrivacyUpdateDialog()
            dialogFragment.listener = listener
            return dialogFragment
        }
    }

    private lateinit var checkBox: CheckBox
    private lateinit var checkBoxAnalytics: CheckBox
    private var listener: DialogListener? = null
    private var isAccepted = false
    private lateinit var analytics: IAnalyticsService
    private lateinit var setting: ISetting

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_privacy_update, container, false)
        val toolbar = rootView.findViewById<Toolbar>(R.id.dialog_edit_item_toolbar)
        toolbar.setTitle(R.string.dialog_privacy_update_title)

        val activity = activity as AppCompatActivity?
        if (activity != null) {
            activity.setSupportActionBar(toolbar)
            val actionBar = activity.supportActionBar
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true)
                actionBar.setHomeButtonEnabled(true)
                actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp)
            }
            setHasOptionsMenu(true)
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setting = AppServices.getService(AppServices.SETTINGS) as ISetting
        analytics = AppServices.getService(AppServices.ANALYTICS) as IAnalyticsService

        checkBox = view.findViewById(R.id.privacyPolicyCheckBox)
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            setCheckBoxHintVisibility(false)
            setting.acceptCurrentPrivacyStatement(isChecked)
            isAccepted = isChecked
        }
        checkBox.isChecked = setting.isPrivacyStatementAccepted

        checkBoxAnalytics = view.findViewById(R.id.analyticsCheckBox)
        checkBoxAnalytics.setOnCheckedChangeListener { _, isChecked ->
            analytics.setAnalyticsCollectionEnabled(isChecked)
            setting.setAnalyticsCollectionEnabled(isChecked)
        }
        checkBoxAnalytics.isChecked = setting.isAnalyticsCollectionEnabled


        view.findViewById<Button>(R.id.ctaSave).setOnClickListener {
            if (isAccepted) {
                listener?.onAcceptClick()
                dismiss()
            } else {
                setCheckBoxHintVisibility(true)
            }
        }
        view.findViewById<Button>(R.id.discard).setOnClickListener {
            setting.acceptCurrentPrivacyStatement(false)
            analytics.setAnalyticsCollectionEnabled(false)
            setting.setAnalyticsCollectionEnabled(false)
            listener?.onDeclineClick()
            dismiss()
        }

        val message = view.findViewById<TextView>(R.id.privacyPolicyMessage)
        val con = context
        if (con != null) {
            message.text = AppUtil.underline(
                con,
                R.string.dialog_privacy_update_message,
                R.string.dialog_privacy_update_link
            )
        }

        message.findViewById<View>(R.id.privacyPolicyMessage).setOnClickListener(View.OnClickListener {
            val context = context ?: return@OnClickListener
            AppUtil.openInCustomTab(context, getString(R.string.privacy_policy_url), false)
        })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        val window = dialog.window
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE)
            window.setBackgroundDrawable(ColorDrawable(0))
        }
        return dialog
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (isAccepted) {
                    listener?.onAcceptClick()
                } else {
                    listener?.onDeclineClick()
                }
                dismiss()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setCheckBoxHintVisibility(checkBoxHintVisibility: Boolean) {
        checkBox.setTextColor(
            ContextCompat.getColor(
                requireContext(), if (checkBoxHintVisibility)
                    R.color.colorAccent
                else
                    R.color.eye
            )
        )
    }
}