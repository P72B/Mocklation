package de.p72b.mocklation.main;


import java.util.List;

import de.p72b.mocklation.service.database.LocationItem;

public interface IMainView {

    void showSavedLocations(List<LocationItem> locationItems);

    void selectLocation(LocationItem item);

    void showEmptyPlaceholder();
}
