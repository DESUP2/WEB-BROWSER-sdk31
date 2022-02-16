// Decompiled by JEB v3.7.0.201909272058

package org.chromium.blimp_public;

import androidx.preference.PreferenceFragmentCompat;
import java.util.Map;
import org.chromium.blimp_public.contents.BlimpContents;
import org.chromium.ui.base.WindowAndroid;

public interface BlimpClientContext {
    void attachBlimpPreferences(PreferenceFragmentCompat arg1);

    void connect();

    BlimpContents createBlimpContents(WindowAndroid arg1);

    Map getFeedbackMap();

    boolean isBlimpEnabled();

    boolean isBlimpSupported();

    void setDelegate(BlimpClientContextDelegate arg1);
}

