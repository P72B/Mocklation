package de.p72b.mocklation.imprint;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import de.p72b.mocklation.BuildConfig;
import de.p72b.mocklation.R;

public class ImprintActivity extends AppCompatActivity {

    private static final String REMOTE_CONFIG_KEY_PRODUCER_NAME = "imprint_producer_contact_name";
    private static final String REMOTE_CONFIG_KEY_PRODUCER_CITY = "imprint_procuder_contact_city";
    private static final String REMOTE_CONFIG_KEY_PRODUCER_COUNTRY = "imprint_producer_contact_country";
    private static final String REMOTE_CONFIG_KEY_PRODUCER = "imprint_producer";
    private static final String REMOTE_CONFIG_KEY_PRODUCER_MAIL = "imprint_producer_contact_mail";
    private static final String REMOTE_CONFIG_KEY_GITHUB_REPO = "imprint_github_project_repository";

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private TextView mProducer;
    private TextView mRepoLink;
    private TextView mProducerName;
    private TextView mProducerMail;
    private TextView mProducerAddress;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imprint);
        setupAppBar();
        initViews();
        setupRemoteConfig();
        setupDependencies();
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
    }

    private void setupRemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        fetchRemoteConfig();
    }

    private void fetchRemoteConfig() {
        setTextViewsFromRemoteConfig();

        long cacheExpiration = 3600; // 1 hour in seconds.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activateFetched();
                            setTextViewsFromRemoteConfig();
                        }
                    }
                });
    }

    private void setTextViewsFromRemoteConfig() {
        mProducer.setText(mFirebaseRemoteConfig.getString(REMOTE_CONFIG_KEY_PRODUCER));
        mRepoLink.setText(mFirebaseRemoteConfig.getString(REMOTE_CONFIG_KEY_GITHUB_REPO));
        mProducerName.setText(mFirebaseRemoteConfig.getString(REMOTE_CONFIG_KEY_PRODUCER_NAME));
        mProducerMail.setText(mFirebaseRemoteConfig.getString(REMOTE_CONFIG_KEY_PRODUCER_MAIL));
        String address = mFirebaseRemoteConfig.getString(REMOTE_CONFIG_KEY_PRODUCER_CITY) + " " +
                mFirebaseRemoteConfig.getString(REMOTE_CONFIG_KEY_PRODUCER_COUNTRY);
        mProducerAddress.setText(address);
    }

    private void setupDependencies() {
    }
}
