package de.p72b.mocklation.main;

public interface IMockServiceInteractor {

    void onMockPermissionsResult(int[] grantedResults);

    void pauseMockLocationService();

    void playMockLocationService();

    void stopMockLocationService();

    void startMockLocation(String locationItemCode);

    boolean isServiceRunning();

    @MockServiceInteractor.ServiceStatus
    int getState();
}
