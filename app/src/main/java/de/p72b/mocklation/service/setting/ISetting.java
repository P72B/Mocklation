package de.p72b.mocklation.service.setting;

import android.support.annotation.Nullable;

public interface ISetting {

    void saveLastPressedLocation(String code);

    String getLastPressedLocationCode();

    String getMockLocationItemCode();

    void setMockLocationItemCode(@Nullable final String code);

    boolean isPrivacyStatementAccepted();

    void acceptCurrentPrivacyStatement();
}
