// Copyright 2015 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.firstrun;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.os.Bundle;
import androidx.fragment.app.FragmentStatePagerAdapter;
import org.chromium.chrome.browser.firstrun.FirstRunPage;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Adapter used to provide First Run pages to the FirstRunActivity ViewPager.
 */
public class FirstRunPagerAdapter extends FragmentStatePagerAdapter {
    public final List<Callable<FirstRunPage>> mPages;
    public final Bundle mFreProperties;

    public boolean mStopAtTheFirstPage;
/*
    public FirstRunPagerAdapter(androidx.fragment.app.FragmentManager fragmentManager,
            List<Callable<FirstRunPage>> pages, Bundle freProperties) {
        super(fragmentManager);
        assert pages != null;
        assert pages.size() > 0;
        assert freProperties != null;
        mPages = pages;
        mFreProperties = freProperties;
    }

    public FirstRunPagerAdapter(androidx.fragment.app.FragmentManager fragmentManager, List<Callable<FirstRunPage>> mPages, Bundle mFreProperties) {
        super();
    } */

public FirstRunPagerAdapter(FragmentManager fragmentManager, List list, Bundle bundle) {
        super(fragmentManager);
        this.mPages = list;
        this.mFreProperties = bundle;
    }

   /* public FirstRunPagerAdapter(androidx.fragment.app.FragmentManager fragmentManager, List<Callable<FirstRunPage>> mPages, Bundle mFreProperties) {
        super();
    } */

    /**
     * Controls progression beyond the first page.
     * @param stop True if no progression beyond the first page is allowed.
     */
    void setStopAtTheFirstPage(boolean stop) {
        if (stop != mStopAtTheFirstPage) {
            mStopAtTheFirstPage = stop;
            notifyDataSetChanged();
        }
    }
public final Fragment getItem(int i) {
        FirstRunPage firstRunPage;
        try {
            firstRunPage = (FirstRunPage) ((Callable) this.mPages.get(i)).call();
        } catch (Exception e) {
            firstRunPage = null;
        }
        if (firstRunPage == null) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putAll(this.mFreProperties);
        firstRunPage.setArguments(bundle);
        return firstRunPage;
    }

    public final int getCount() {
        if (this.mStopAtTheFirstPage) {
            return 1;
        }
        return this.mPages.size();
    }

    public final int getItemPosition(Object obj) {
        if (!(obj instanceof FirstRunPage) || ((FirstRunPage) obj).shouldRecreatePageOnDataChange()) {
            return -2;
        }
        return -1;
    }
}
/*
    @Override
    public Fragment getItem(int position) {
        assert position >= 0 && position < mPages.size();
        FirstRunPage result = null;
        try {
            result = mPages.get(position).call();
        } catch (Exception e) {
            // We can always return null and it will be properly handled at the caller level.
        }
        if (result == null) return null;

        Bundle props = new Bundle();
        props.putAll(mFreProperties);
        result.setArguments(props);

        return result;
    }

    @Override
    public int getCount() {
        if (mStopAtTheFirstPage) return 1;
        return mPages.size();
    }

    @Override
    public int getItemPosition(Object object) {
        // We do not keep track of constructed objects, but we want the pages to be recreated
        // on notifyDataSetChanged. Hence, tell the view that it needs to refresh the objects.
        return POSITION_NONE;
    }
}
*/
