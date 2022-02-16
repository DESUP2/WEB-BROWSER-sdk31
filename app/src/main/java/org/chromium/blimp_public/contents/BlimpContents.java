// Decompiled by JEB v3.7.0.201909272058

package org.chromium.blimp_public.contents;

import android.view.ViewGroup;

public interface BlimpContents {
    void addObserver(BlimpContentsObserver arg1);

    void destroy();

    BlimpNavigationController getNavigationController();

    int getThemeColor();

    ViewGroup getView();

    void hide();

    void removeObserver(BlimpContentsObserver arg1);

    void show();
}

