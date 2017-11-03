package de.p72b.mocklation.util;

import android.view.View;
import android.view.animation.Animation;

public class VisibilityAnimationListener implements Animation.AnimationListener {
    private View mView;
    private int mVisibility;

    public void setViewAndVisibility(View view, int visibility) {
        mView = view;
        mVisibility = visibility;
    }
    public void onAnimationEnd(Animation animation) {
        if (mView != null) {
            mView.setVisibility(mVisibility);
        }
    }
    public void onAnimationRepeat(Animation animation) {
    }
    public void onAnimationStart(Animation animation) {
    }
}
