package de.p72b.mocklation.dialog

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import de.p72b.mocklation.R


class BackgroundLocationDialog : DialogFragment() {

    companion object {
        val TAG: String = BackgroundLocationDialog::class.java.simpleName
        fun newInstance(listener: DialogListener?): BackgroundLocationDialog {
            val dialogFragment = BackgroundLocationDialog()
            dialogFragment.listener = listener
            return dialogFragment
        }
    }

    private var listener: DialogListener? = null
    private var isAccepted = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_background_location, container, false)
        val toolbar = rootView.findViewById<Toolbar>(R.id.dialog_edit_item_toolbar)
        toolbar.setTitle(R.string.dialog_background_location_title)

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

        val message = view.findViewById<TextView>(R.id.privacyPolicyMessage)
        val unformattedText = String.format(
            getString(R.string.dialog_background_location_message),
            activity?.packageManager?.backgroundPermissionOptionLabel
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            message.text = Html.fromHtml(unformattedText, Html.FROM_HTML_MODE_COMPACT)
        } else {
            message.text = Html.fromHtml(unformattedText)
        }



        view.findViewById<Button>(R.id.ctaUnderstood).setOnClickListener {
            isAccepted = true
            listener?.onAcceptClick()
            dismiss()
        }
        view.findViewById<Button>(R.id.ctaSettings).setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri: Uri = Uri.fromParts("package", activity?.packageName, null)
            intent.data = uri
            startActivity(intent)

            dismiss()
        }
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
        when (item.itemId) {
            android.R.id.home -> {
                if (isAccepted) {
                    listener?.onAcceptClick()
                } else {
                    listener?.onDeclineClick()
                }
                dismiss()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}