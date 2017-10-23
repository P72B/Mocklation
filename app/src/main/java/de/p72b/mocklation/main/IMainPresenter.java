package de.p72b.mocklation.main;


import de.p72b.mocklation.service.room.LocationItem;

public interface IMainPresenter {

    void onResume();

    void onDestroy();

    void locationItemPressed(LocationItem item);

    void onMockPermissionsResult(int[] grantedResults);

    void onPlayClicked();
}
