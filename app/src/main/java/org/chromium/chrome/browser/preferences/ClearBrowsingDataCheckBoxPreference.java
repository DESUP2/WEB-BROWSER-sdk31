// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.core.view.ViewCompat;

import org.chromium.chrome.R;

/**
 * A preference representing one browsing data type in ClearBrowsingDataPreferences.
 */
public class ClearBrowsingDataCheckBoxPreference extends ChromeBaseCheckBoxPreference {
    private LinearLayout mView;

    /**
     * Constructor for inflating from XML.
     */
    public ClearBrowsingDataCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //@Override
    public View onCreateView(ViewGroup parent) {
        if (mView != null) return mView;

        //mView = (LinearLayout) super.onCreateView(parent);

        // Checkboxes in the Clear Browsing Data dialog will show and hide the results of
        // BrowsingDataCounter. It is important that they will not change height when doing so.
        // We will therefore set a fixed height.
        int height = getContext().getResources().getDimensionPixelSize(
                R.dimen.clear_browsing_data_checkbox_height);
        //mView.setMinimumHeight(height);

        // The title and summary are enclosed in a common RelativeLayout. We must remove
        // its vertical padding for it to be correctly vertically centered in the fixed-height view.
        View textLayout = (View) parent.findViewById(android.R.id.title).getParent();
        ViewCompat.setPaddingRelative(
                textLayout,
                ViewCompat.getPaddingStart(textLayout),
                0,
                ViewCompat.getPaddingEnd(textLayout),
                0);

        return parent;
    }

    public void announceForAccessibility(CharSequence announcement) {
        if (mView != null) mView.announceForAccessibility(announcement);
    }
}
