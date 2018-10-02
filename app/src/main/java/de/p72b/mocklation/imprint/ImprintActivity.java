package de.p72b.mocklation.imprint;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.List;

import de.p72b.mocklation.BuildConfig;
import de.p72b.mocklation.R;
import de.p72b.mocklation.util.AppUtil;

public class ImprintActivity extends AppCompatActivity {

    private static final String REMOTE_CONFIG_KEY_PRODUCER_NAME = "imprint_producer_contact_name";
    private static final String REMOTE_CONFIG_KEY_PRODUCER_CITY = "imprint_procuder_contact_city";
    private static final String REMOTE_CONFIG_KEY_PRODUCER_COUNTRY = "imprint_producer_contact_country";
    private static final String REMOTE_CONFIG_KEY_PRODUCER = "imprint_producer";
    private static final String REMOTE_CONFIG_KEY_PRODUCER_MAIL = "imprint_producer_contact_mail";
    private static final String REMOTE_CONFIG_KEY_GITHUB_REPO = "imprint_github_project_repository";

    private TextView mProducer;
    private TextView mRepoLink;
    private TextView mProducerName;
    private TextView mProducerMail;
    private TextView mProducerAddress;
    private LinearLayout mWrapperDependencies;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imprint);
        setupAppBar();
        initViews();
        setupDependencies();
        setTextViewsFromRemoteConfig();
    }

    /**
     * Setup the material toolbar.
     */
    private void setupAppBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_activity_imprint);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        mProducer = findViewById(R.id.imprint_producer);
        mRepoLink = findViewById(R.id.imprint_github_project_repository);
        mProducerName = findViewById(R.id.imprint_producer_contact_name);
        mProducerMail = findViewById(R.id.imprint_producer_contact_mail);
        mProducerAddress = findViewById(R.id.imprint_producer_address);
        mWrapperDependencies = findViewById(R.id.wrapper_dependencies);
    }

    private void setTextViewsFromRemoteConfig() {
        final FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        mProducer.setText(config.getString(REMOTE_CONFIG_KEY_PRODUCER));
        mRepoLink.setText(config.getString(REMOTE_CONFIG_KEY_GITHUB_REPO));
        mProducerName.setText(config.getString(REMOTE_CONFIG_KEY_PRODUCER_NAME));
        mProducerMail.setText(config.getString(REMOTE_CONFIG_KEY_PRODUCER_MAIL));
        String address = config.getString(REMOTE_CONFIG_KEY_PRODUCER_CITY) + " " +
                config.getString(REMOTE_CONFIG_KEY_PRODUCER_COUNTRY);
        mProducerAddress.setText(address);
    }

    @SuppressLint("StringFormatInvalid")
    private void setupDependencies() {
        List<DependencyItem> items = new ArrayList<>();
        items.add(new DependencyItem(
                R.string.v7_appcompat_library,
                R.string.v7_appcompat_library_link,
                String.format(getString(R.string.imprint_dependencies_version),
                        BuildConfig.SUPPORT_LIB_VERSION),
                String.format(getString(R.string.imprint_dependencies_license),
                        getString(R.string.apache_license)))
        );
        items.add(new DependencyItem(
                R.string.v7_cardview_library,
                R.string.v7_cardview_library_link,
                String.format(getString(R.string.imprint_dependencies_version),
                        BuildConfig.SUPPORT_LIB_VERSION),
                String.format(getString(R.string.imprint_dependencies_license),
                        getString(R.string.apache_license)))
        );
        items.add(new DependencyItem(
                R.string.android_design_library,
                R.string.android_design_library_link,
                String.format(getString(R.string.imprint_dependencies_version),
                        BuildConfig.SUPPORT_LIB_VERSION),
                String.format(getString(R.string.imprint_dependencies_license),
                        getString(R.string.apache_license)))
        );
        items.add(new DependencyItem(
                R.string.v7_support_dynamic_animation_library,
                R.string.v7_support_dynamic_animation_library_link,
                String.format(getString(R.string.imprint_dependencies_version),
                        BuildConfig.SUPPORT_LIB_VERSION),
                String.format(getString(R.string.imprint_dependencies_license),
                        getString(R.string.apache_license)))
        );
        items.add(new DependencyItem(
                R.string.v7_support_custom_tabs_library,
                R.string.v7_support_custom_tabs_library_link,
                String.format(getString(R.string.imprint_dependencies_version),
                        BuildConfig.SUPPORT_LIB_VERSION),
                String.format(getString(R.string.imprint_dependencies_license),
                        getString(R.string.apache_license)))
        );
        items.add(new DependencyItem(
                R.string.room_library,
                R.string.room_library_link,
                String.format(getString(R.string.imprint_dependencies_version),
                        BuildConfig.ROOM_VERSION),
                String.format(getString(R.string.imprint_dependencies_license),
                        getString(R.string.apache_license)))
        );
        items.add(new DependencyItem(
                R.string.room_rxjava2_library,
                R.string.room_rxjava2_library_link,
                String.format(getString(R.string.imprint_dependencies_version),
                        BuildConfig.ROOM_VERSION),
                String.format(getString(R.string.imprint_dependencies_license),
                        getString(R.string.apache_license)))
        );
        items.add(new DependencyItem(
                R.string.maps_api,
                R.string.maps_api_link,
                String.format(getString(R.string.imprint_dependencies_version),
                        BuildConfig.PLAY_SERVICES_VERSION),
                String.format(getString(R.string.imprint_dependencies_license),
                        getString(R.string.apache_license)))
        );
        items.add(new DependencyItem(
                R.string.google_places_api,
                R.string.google_places_api_link,
                String.format(getString(R.string.imprint_dependencies_version),
                        BuildConfig.PLAY_SERVICES_VERSION),
                String.format(getString(R.string.imprint_dependencies_license),
                        getString(R.string.apache_license)))
        );
        items.add(new DependencyItem(
                R.string.google_location,
                R.string.google_location_link,
                String.format(getString(R.string.imprint_dependencies_version),
                        BuildConfig.PLAY_SERVICES_VERSION),
                String.format(getString(R.string.imprint_dependencies_license),
                        getString(R.string.apache_license)))
        );
        items.add(new DependencyItem(
                R.string.maps_utils_api,
                R.string.maps_utils_api_link,
                String.format(getString(R.string.imprint_dependencies_version),
                        BuildConfig.MAP_UTILITIES_VERSION),
                String.format(getString(R.string.imprint_dependencies_license),
                        getString(R.string.apache_license)))
        );
        items.add(new DependencyItem(
                R.string.google_gson,
                R.string.google_gson_link,
                String.format(getString(R.string.imprint_dependencies_version),
                        BuildConfig.GSON_VERSION),
                String.format(getString(R.string.imprint_dependencies_license),
                        getString(R.string.apache_license)))
        );
        items.add(new DependencyItem(
                R.string.dagger_2,
                R.string.dagger_2_link,
                String.format(getString(R.string.imprint_dependencies_version),
                        BuildConfig.DAGGER_VERSION),
                String.format(getString(R.string.imprint_dependencies_license),
                        getString(R.string.apache_license)))
        );
        items.add(new DependencyItem(
                R.string.firebase_core,
                R.string.firebase_core_link,
                String.format(getString(R.string.imprint_dependencies_version),
                        BuildConfig.FIREBASE_VERSION),
                String.format(getString(R.string.imprint_dependencies_license),
                        getString(R.string.apache_license)))
        );
        items.add(new DependencyItem(
                R.string.firebase_config,
                R.string.firebase_config_link,
                String.format(getString(R.string.imprint_dependencies_version),
                        BuildConfig.FIREBASE_VERSION),
                String.format(getString(R.string.imprint_dependencies_license),
                        getString(R.string.apache_license)))
        );
        items.add(new DependencyItem(
                R.string.rxjava_2,
                R.string.rxjava_2_link,
                String.format(getString(R.string.imprint_dependencies_version),
                        BuildConfig.RX_JAVA_VERSION),
                String.format(getString(R.string.imprint_dependencies_license),
                        getString(R.string.apache_license)))
        );
        items.add(new DependencyItem(
                R.string.rxjava_2_android,
                R.string.rxjava_2_android_link,
                String.format(getString(R.string.imprint_dependencies_version),
                        BuildConfig.RX_ANDROID_VERSION),
                String.format(getString(R.string.imprint_dependencies_license),
                        getString(R.string.apache_license)))
        );


        int valueInPixel = (int) getResources().getDimension(
                R.dimen.guideline_screen_edge_top_bottom_distance_half);
        int color = ContextCompat.getColor(this, R.color.eye);
        for (int i = 0; i < items.size(); i++) {
            DependencyItem item = items.get(i);

            LinearLayout itemWrapper = new LinearLayout(this);
            itemWrapper.setOrientation(LinearLayout.VERTICAL);

            TextView title = getTextView(item.getTitle());
            title.setTypeface(null, Typeface.BOLD);
            title.setTextColor(color);

            TextView link = new TextView(this);
            link.setText(Html.fromHtml("<a href=\'" + item.getLink() + "\'>"
                    + getString(R.string.imprint_dependencies_homepage)+ "</a>"));
            link.setMovementMethod(LinkMovementMethod.getInstance());
            link = AppUtil.stripUnderlines(link);

            itemWrapper.addView(title);
            itemWrapper.addView(link);
            itemWrapper.addView(getTextView(item.getVersion()));
            itemWrapper.addView(getTextView(item.getLicense()));
            itemWrapper.setPadding(0,0,0,valueInPixel);
            mWrapperDependencies.addView(itemWrapper);
        }
    }

    private TextView getTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        return textView;
    }

    private class DependencyItem {
        private String mTitle;
        private String mLink;
        private String mVersion;

        private String mLicense;

        DependencyItem(int title, int link, String version, String license) {
            mTitle = getString(title);
            mLink = getString(link);
            mVersion = version;
            mLicense = license;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getLink() {
            return mLink;
        }

        public String getVersion() {
            return mVersion;
        }

        public String getLicense() {
            return mLicense;
        }
    }
}
