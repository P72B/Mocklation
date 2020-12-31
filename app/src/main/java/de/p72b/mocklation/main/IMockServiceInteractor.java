package de.p72b.mocklation.main;

public interface IMockServiceInteractor {

    void onMockPermissionsResult(int[] grantedResults);

    void onDefaultMockAppRequest(int results);

    void pauseMockLocationService();

    void playMockLocationService();

    void stopMockLocationService();

    void startMockLocation(String locationItemCode);

    boolean isServiceRunning();

    boolean hasRequiredPermissions();

    @MockServiceInteractor.ServiceStatus
    int getState();

    void requestRequiredPermissions();

    void setLocationItem(String code);

    boolean isDefaultAppForMockLocations();

    boolean areDeveloperOptionsEnabled();

    void requestEnableDeveloperOptions();

    void requestSetMockLocationApp();
}
