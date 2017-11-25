package de.p72b.mocklation.service.location;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import de.p72b.mocklation.service.permission.IPermissionService;
import de.p72b.mocklation.service.permission.PermissionService;
import de.p72b.mocklation.service.setting.ISetting;

public interface ILocationService {

    /**
     * This method need to be called inside the lifecycle activity where this service should life
     * e.g. it will start the service.
     *
     * @param activity    Given FragmentActivity defines the lifecycle of this service.
     * @param permissions In order to get location updates permissions are needed.
     * @param settings    Setting service.
     */
    void onStartCommand(FragmentActivity activity, IPermissionService permissions,
                        ISetting settings);

    /**
     * Delivers the users location. It can not be null. In case user doesn't allow location
     * permission the country default location will be returned. See CountryResolver.
     *
     * @return Location holding lastKnownLocation or country default location
     */
    @Nullable
    Location getLastKnownLocation();

    /**
     * Delivers the restored location from shared preferences. This method should be used if a
     * location is needed, but without location permission check. Take in concern that this
     * location can be outdated or the country default location. Use this method carefully.
     *
     * @return Location holding last permanently saved location.
     */
    Location getRestoredLocation();

    /**
     * Stop the service and it's running tasks.
     */
    void onDestroyCommand();

    /**
     * Continue the service.
     */
    void onResume();

    /**
     * Add listener to location changes.
     *
     * @param listener Subscriber to be added.
     */
    void subscribeToLocationChanges(OnLocationChanged listener);

    /**
     * Remove listener.
     *
     * @param listener Subscriber which should be removed.
     */
    void unSubscribeToLocationChanges(OnLocationChanged listener);

    interface OnLocationChanged {

        /**
         * Emit the latest user location.
         *
         * @param location Location holding latitude and longitude of device location.
         */
        void onLocationChanged(Location location);

        /**
         * Emit when the first location of GoogleApiClient FusedLocation was found.
         *
         * @param location It Location
         */
        void onInitialLocationDetermined(Location location);

    }
}
