package de.p72b.mocklation.main;

import androidx.annotation.Nullable;
import de.p72b.mocklation.dialog.DialogListener;
import de.p72b.mocklation.service.room.LocationItem;

public interface IMainPresenter {

    void onResume();

    void onDestroy();

    void locationItemPressed(LocationItem item);

    void onMockPermissionsResult(int[] grantedResults);

    void onDefaultMockAppRequest(int result);

    void onClick(int viewId);

    void locationItemRemoved(LocationItem item);

    void showPrivacyUpdateDialog();

    void onDeveloperOptionsEnabledRequest(int result);
}
