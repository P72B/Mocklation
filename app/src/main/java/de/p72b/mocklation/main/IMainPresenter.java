package de.p72b.mocklation.main;


import android.view.View;

import de.p72b.mocklation.service.room.LocationItem;

public interface IMainPresenter {

    void onResume();

    void onDestroy();

    void locationItemPressed(LocationItem item);

    void onMockPermissionsResult(int[] grantedResults);

    void onClick(View view);

    void locationItemRemoved(LocationItem item);
}
