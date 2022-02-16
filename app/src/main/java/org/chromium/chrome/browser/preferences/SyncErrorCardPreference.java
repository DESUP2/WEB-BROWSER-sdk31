// Copyright 2016 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.preferences;

import android.content.Context;
//import androidx.core.content.ContextCompat;
import android.graphics.Typeface;
//import androidx.core.graphics.TypefaceCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
//import androidx.core.util.AtomicFile;
import android.util.AttributeSet;
import android.util.TypedValue;
//import androidx.core.view.ViewCompat;
//import androidx.viewpager.widget.ViewPager;
//import androidx.cardview.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
//import androidx.core.widget.TextViewCompat;
//import androidx.
import org.chromium.base.ApiCompatibilityUtils;
import org.chromium.chrome.R;

/**
 * A preference that displays hint message to resolve sync error. Click of it navigates user to
 * appropriate place to resolve error.
 */
public class SyncErrorCardPreference extends Preference {
    /**
     * Constructor for inflating from XML.
     */
    public SyncErrorCardPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    protected View onCreateView(ViewGroup parent) {
        //View view = super.onCreateView(parent);
        TextView title = (TextView) parent.findViewById(android.R.id.title);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setTextColor(ApiCompatibilityUtils.getColor(
                getContext().getResources(), R.color.input_underline_error_color));
        return parent;
    }
}
