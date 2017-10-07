package de.p72b.mocklation.main;

import de.p72b.mocklation.service.database.LocationItem;

public interface IMainPresenter {

    void onDestroy();

    void locationItemPressed(LocationItem item);
}
