package de.p72b.mocklation.dialog

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import de.p72b.mocklation.R
import de.p72b.mocklation.util.AppUtil

class PrivacyUpdateDialog : DialogFragment() {

    companion object {
        val TAG: String = PrivacyUpdateDialog::class.java.simpleName
        fun newInstance(listener: PrivacyUpdateDialogListener,
                        url: String): PrivacyUpdateDialog {
            val dialogFragment = PrivacyUpdateDialog()
            dialogFragment.listener = listener
            dialogFragment.url = url
            return dialogFragment
        }
    }

    private lateinit var checkBox: CheckBox
    private lateinit var url: String
    private lateinit var listener: PrivacyUpdateDialogListener
    private var isAccepted = false

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
        checkBox = view.findViewById(R.id.privacyPolicyCheckBox)
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            setCheckBoxHintVisibility(false)
            isAccepted = isChecked
        }
        val message = view.findViewById<TextView>(R.id.privacyPolicyMessage)
        val con = context
        if (con != null) {
            message.text = AppUtil.underline(con, R.string.dialog_privacy_update_message, R.string.dialog_privacy_update_title)
        }

        message.findViewById<View>(R.id.privacyPolicyMessage).setOnClickListener(View.OnClickListener {
            val context = context?: return@OnClickListener
            AppUtil.openInCustomTab(context, url, false)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        activity?.menuInflater?.inflate(R.menu.menu_dialog_edit_location_item, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                if (isAccepted) {
                    listener.onAcceptClick()
                    dismiss()
                } else {
                    setCheckBoxHintVisibility(true)
                }
                return true
            }
            android.R.id.home -> {
                if (isAccepted) {
                    listener.onAcceptClick()
                } else {
                    listener.onDeclineClick()
                }
                dismiss()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun setCheckBoxHintVisibility(checkBoxHintVisibility: Boolean) {
        checkBox.setTextColor(ContextCompat.getColor(requireContext(), if (checkBoxHintVisibility)
            R.color.colorAccent
        else
            R.color.eye))
    }

    interface PrivacyUpdateDialogListener {
        fun onAcceptClick()

        fun onDeclineClick()
    }
}