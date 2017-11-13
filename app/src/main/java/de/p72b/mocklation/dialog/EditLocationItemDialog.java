package de.p72b.mocklation.dialog;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import de.p72b.mocklation.R;
import de.p72b.mocklation.service.room.LocationItem;

public class EditLocationItemDialog extends DialogFragment {

    public static final String TAG = EditLocationItemDialog.class.getSimpleName();
    private EditLocationItemDialogListener mListener;
    private LocationItem mLocationItem;
    private EditText mDisplayedName;
    private TextInputLayout mDisplayedNameLayoutName;

    public EditLocationItemDialog() {
    }

    public static EditLocationItemDialog newInstance(EditLocationItemDialogListener listener) {
        EditLocationItemDialog dialogFragment = new EditLocationItemDialog();
        Bundle args = new Bundle();
        dialogFragment.setArguments(args);
        dialogFragment.setListener(listener);
        return dialogFragment;
    }

    public static EditLocationItemDialog findOnStack(FragmentManager fragmentManager) {
        Fragment dialogFragment = fragmentManager.findFragmentByTag(TAG);

        if (dialogFragment instanceof EditLocationItemDialog) {
            return (EditLocationItemDialog) dialogFragment;
        }
        return null;
    }

    public interface EditLocationItemDialogListener {
        void onPositiveClick(LocationItem item);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_edit_item, container, false);

        Toolbar toolbar = rootView.findViewById(R.id.dialog_edit_item_toolbar);
        toolbar.setTitle("Edit location");

        AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity != null) {

            activity.setSupportActionBar(toolbar);
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
                actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
            }
            setHasOptionsMenu(true);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mDisplayedName = view.findViewById(R.id.input_displayed_name);
        mDisplayedName.addTextChangedListener(new SimpleTextWatcher(mDisplayedName));
        mDisplayedNameLayoutName = view.findViewById(R.id.input_layout_pin);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        return dialog;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.menu_dialog_edit_location_item, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            if (!validatePin()) {
                return true;
            }

            if (mListener != null) {
                mListener.onPositiveClick(mLocationItem);
            }
            dismiss();
            return true;
        } else if (id == android.R.id.home) {
            dismiss();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setListener(EditLocationItemDialogListener listener) {
        mListener = listener;
    }

    private boolean validatePin() {
        if (mDisplayedName.getText().toString().trim().isEmpty()) {
            mDisplayedName.setError(getString(R.string.error_1010));
            return false;
        }

        mDisplayedNameLayoutName.setErrorEnabled(false);
        return true;
    }

    private class SimpleTextWatcher implements TextWatcher {

        private View view;

        private SimpleTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_displayed_name:
                    validatePin();
                    break;
            }
        }
    }
}