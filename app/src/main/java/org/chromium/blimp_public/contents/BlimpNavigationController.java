// Decompiled by JEB v3.7.0.201909272058

package org.chromium.blimp_public.contents;

public interface BlimpNavigationController {
    boolean canGoBack();

    boolean canGoForward();

    String getTitle();

    String getUrl();

    void goBack();

    void goForward();

    void loadUrl(String arg1);

    void reload();
}

