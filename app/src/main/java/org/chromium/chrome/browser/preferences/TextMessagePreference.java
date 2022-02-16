// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.preferences;

import android.content.Context;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.Fragment;
import android.widget.TextView;

/**
 * A preference that displays informational text.
 */
public class TextMessagePreference extends Preference {

    //ViewGroup parent = View.getParent();
    /**
     * Constructor for inflating from XML.
     * public class TextMessagePreference extends Preference{}
     */
    public TextMessagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSelectable(false);
    }

    //@Override
    protected View onCreateView(ViewGroup parent) {
        //View view =parent.findViewById();super.onCreateView(parent);

        TextView textView = (TextView) parent.findViewById(android.R.id.title);
        textView.setSingleLine(false);
        textView.setMaxLines(Integer.MAX_VALUE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        return parent;
    }
}
