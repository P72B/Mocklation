package de.p72b.mocklation.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import de.p72b.mocklation.R;
import de.p72b.mocklation.util.AppUtil;

public class PrivacyUpdateDialog extends DialogFragment {

    public static final String TAG = PrivacyUpdateDialog.class.getSimpleName();
    private View mRootView;
    private PrivacyUpdateDialogListener mListener;
    private boolean mIsAccepted = false;
    private CheckBox mCheckBox;
    private String mUrl;

    public PrivacyUpdateDialog() {
    }

    public static PrivacyUpdateDialog newInstance(PrivacyUpdateDialogListener listener,
                                                  @NonNull final String url) {
        PrivacyUpdateDialog dialogFragment = new PrivacyUpdateDialog();
        dialogFragment.setListener(listener);
        dialogFragment.setUrl(url);
        return dialogFragment;
    }

    public static PrivacyUpdateDialog findOnStack(FragmentManager fragmentManager) {
        Fragment dialogFragment = fragmentManager.findFragmentByTag(TAG);

        if (dialogFragment instanceof PrivacyUpdateDialog) {
            return (PrivacyUpdateDialog) dialogFragment;
        }
        return null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_privacy_update, container, false);

        Toolbar toolbar = rootView.findViewById(R.id.dialog_edit_item_toolbar);
        toolbar.setTitle(R.string.dialog_privacy_update_title);

        AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity != null) {

            activity.setSupportActionBar(toolbar);
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_black_24dp);
            }
            setHasOptionsMenu(true);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRootView = view;
        mCheckBox = mRootView.findViewById(R.id.privacyPolicyCheckBox);
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                @Override
                                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                    setCheckBoxHintVisibility(false);
                                                    mIsAccepted = isChecked;
                                                }
                                            }
                );
        final TextView message = mRootView.findViewById(R.id.privacyPolicyMessage);
        final Context context = getContext();
        if (context != null) {
            message.setText(AppUtil.underline(context, R.string.dialog_privacy_update_message, R.string.dialog_privacy_update_title));
        }
        message.findViewById(R.id.privacyPolicyMessage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context context = getContext();
                if (context == null) {
                    return;
                }
                AppUtil.openInCustomTab(context, mUrl, false);
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        Window window = dialog.getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setBackgroundDrawable(new ColorDrawable(0));
        }
        return dialog;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        Activity activity = getActivity();
        if (activity != null) {
            activity.getMenuInflater().inflate(R.menu.menu_dialog_edit_location_item, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (mIsAccepted) {
                    mListener.onAcceptClick();
                    dismiss();
                } else {
                    setCheckBoxHintVisibility(true);
                }
                return true;
            case android.R.id.home:
                if (mIsAccepted) {
                    mListener.onAcceptClick();
                } else {
                    mListener.onDeclineClick();
                }
                dismiss();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setListener(PrivacyUpdateDialogListener listener) {
        mListener = listener;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public void setCheckBoxHintVisibility(boolean checkBoxHintVisibility) {
        mCheckBox.setTextColor(ContextCompat.getColor(getContext(), checkBoxHintVisibility
                ? R.color.colorPrimary : R.color.eye));
    }

    public interface PrivacyUpdateDialogListener {
        void onAcceptClick();

        void onDeclineClick();
    }
}