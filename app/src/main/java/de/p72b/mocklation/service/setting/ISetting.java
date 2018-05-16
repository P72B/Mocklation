package de.p72b.mocklation.service.setting;

public interface ISetting {

    /**
     * Saves given location.
     *
     * @param latitude  - latitude coordinate.
     * @param longitude - longitude coordinate.
     */
    void saveLocation(double latitude, double longitude);

    /**
     * Saves the user decision on given permission.
     *
     * @param permission Name of the dangerous permission.
     * @param decision   Boolean true indicates a granted permission. False vice versa.
     */
    void setPermissionDecision(String permission, boolean decision);

    boolean getPermissionDecision(String permission);

    void saveLastPressedLocation(String code);

    String getLastPressedLocationCode();

    String getMockLocationItemCode();

    void setMockLocationItemCode(String code);
}
