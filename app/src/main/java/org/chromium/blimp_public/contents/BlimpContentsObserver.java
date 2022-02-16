// Decompiled by JEB v3.7.0.201909272058

package org.chromium.blimp_public.contents;

public interface BlimpContentsObserver {
    void onLoadingStateChanged(boolean arg1);

    void onNavigationStateChanged();

    void onPageLoadingStateChanged(boolean arg1);
}

