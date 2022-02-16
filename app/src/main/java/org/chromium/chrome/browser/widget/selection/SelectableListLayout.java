// Copyright 2016 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.widget.selection;

import android.content.Context;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerViewAccessibilityDelegate;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver;
//import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.appcompat.widget.ActionMenuView.OnMenuItemClickListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.chromium.base.ApiCompatibilityUtils;
import org.chromium.chrome.R;
import org.chromium.chrome.browser.download.ui.DownloadHistoryItemWrapper;
import org.chromium.chrome.browser.download.ui.DownloadManagerUi;
import org.chromium.chrome.browser.widget.FadingShadow;
import org.chromium.chrome.browser.widget.FadingShadowView;
import org.chromium.chrome.browser.widget.LoadingView;
import org.chromium.ui.base.DeviceFormFactor;
import org.chromium.chrome.browser.widget.selection.SelectionToolbar;
import org.chromium.chrome.browser.util.FeatureUtilities;

import javax.annotation.Nullable;

/**
 * Contains UI elements common to selectable list views: a loading view, empty view, selection
 * toolbar, shadow, and recycler view.
 *
 * After the SelectableListLayout is inflated, it should be initialized through calls to
 * #initializeRecyclerView(), #initializeToolbar(), and #initializeEmptyView().
 */
public class SelectableListLayout extends RelativeLayout {
    private Adapter<RecyclerView.ViewHolder> mAdapter;
    SelectionToolbar mToolbar;
    private ViewStub mToolbarStub;
    private TextView mEmptyView;
    private LoadingView mLoadingView;
    private RecyclerView mRecyclerView;

    private final AdapterDataObserver mAdapterObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            if (SelectableListLayout.this.mAdapter.getItemCount() == 0) {
                SelectableListLayout.this.mEmptyView.setVisibility(View.VISIBLE);
                SelectableListLayout.this.mRecyclerView.setVisibility(View.GONE);
            } else {
                SelectableListLayout.this.mEmptyView.setVisibility(View.GONE);
                SelectableListLayout.this.mRecyclerView.setVisibility(View.VISIBLE);
            }
            // At inflation, the RecyclerView is set to gone, and the loading view is visible. As
            // long as the adapter data changes, we show the recycler view, and hide loading view.
            SelectableListLayout.this.mLoadingView.hideLoadingUI();
            //SelectableListLayout.this.mToolbar.onDataChanged(SelectableListLayout.this.mAdapter.getItemCount());
            //onDataChanged(SelectableListLayout.this.mAdapter.getItemCount());

        }
    };

    public SelectableListLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.selectable_list_layout, this);

        this.mEmptyView = (TextView) findViewById(R.id.empty_view);
        this.mLoadingView = (LoadingView) findViewById(R.id.loading_view);
        this.mLoadingView.showLoadingUI();

        this.mToolbarStub = (ViewStub) findViewById(R.id.action_bar_stub);
	setFocusable(true);
        setFocusableInTouchMode(true);
        FadingShadowView shadow = (FadingShadowView) findViewById(R.id.shadow);
        if (DeviceFormFactor.isLargeTablet(getContext())) {
            shadow.setVisibility(View.GONE);
        } else {
            shadow.init(ApiCompatibilityUtils.getColor(getResources(),
                    R.color.toolbar_shadow_color), FadingShadow.POSITION_TOP);
        }
    }

    /**
     * Initializes the RecyclerView.
     *
     * @param adapter The adapter that provides a binding from an app-specific data set to views
     *                that are displayed within the RecyclerView.
     */
    public final RecyclerView initializeRecyclerView(Adapter<RecyclerView.ViewHolder> adapter) {
        this.mAdapter = adapter;
        this.mAdapter.registerAdapterDataObserver(mAdapterObserver);

        this.mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        this.mRecyclerView.setAdapter(mAdapter);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
	return this.mRecyclerView;
    }

    /**
     * Initializes the SelectionToolbar.
     *
     * @param toolbarLayoutId The resource id of the toolbar layout. This will be inflated into
     *                        a ViewStub.
     * @param delegate The SelectionDelegate that will inform the toolbar of selection changes.
     * @param titleResId The resource id of the title string. May be 0 if this class shouldn't set
     *                   set a title when the selection is cleared.
     * @param drawerLayout The DrawerLayout whose navigation icon is displayed in this toolbar.
     * @param normalGroupResId The resource id of the menu group to show when a selection isn't
     *                         established.
     * @param selectedGroupResId The resource id of the menu item to show when a selection is
     *                           established.
     * @param listener The OnMenuItemClickListener to set on the toolbar.
     * @return The initialized SelectionToolbar.
     */
    //<E>
    public  final SelectionToolbar initializeToolbar(int toolbarLayoutId, SelectionDelegate<DownloadHistoryItemWrapper> delegate, int titleResId, @Nullable DrawerLayout drawerLayout, int normalGroupResId, int selectedGroupResId, DownloadManagerUi listener) {
        mToolbarStub.setLayoutResource(toolbarLayoutId);
        @SuppressWarnings("unchecked")
                //<E>
        SelectionToolbar toolbar = (SelectionToolbar) mToolbarStub.inflate();
        toolbar.initialize(delegate, titleResId, drawerLayout, normalGroupResId, selectedGroupResId);
       // toolbar.setOnMenuItemClickListener(listener);
        return toolbar;

	/*
	FadingShadowView fadingShadowView = (FadingShadowView) findViewById(R.id.shadow);
        if (!z || !DeviceFormFactor.isLargeTablet(getContext())) {
            fadingShadowView.init$514KIAAM(ApiCompatibilityUtils.getColor(getResources(), R.color.toolbar_shadow_color));
        } else {
            fadingShadowView.setVisibility(8);
        }
        this.mToolbarStub.setLayoutResource(i);
        this.mToolbar = (SelectionToolbar) this.mToolbarStub.inflate();
        this.mToolbar.initialize(selectionDelegate, i2, drawerLayout, i3, i4, num);
        this.mToolbar.mOnMenuItemClickListener = listener;
        return this.mToolbar;
	*/
    }

    /**
     * Initializes the view shown when the selectable list is empty.
     *
     * @param emptyIconResId The icon to show when the selectable list is empty.
     * @param emptyStringResId The string to show when the selectable list is empty.
	 public final TextView initializeEmptyView(Drawable drawable, int i) {
        this.mEmptyView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
        this.mEmptyView.setText(i);
        return this.mEmptyView;
    }

    public final void setEmptyViewText(int i) {
        this.mEmptyView.setText(i);
    }
     */
    public void initializeEmptyView(int emptyIconResId, int emptyStringResId) {
        this.mEmptyView.setCompoundDrawablesWithIntrinsicBounds(null, VectorDrawableCompat.create(getResources(), emptyIconResId,
                        getContext().getTheme()), null, null);
        this.mEmptyView.setText(emptyStringResId);
    }

    /**
     * Called when the view that owns the SelectableListLayout is destroyed.
     */
    public void onDestroyed() {
        this.mAdapter.unregisterAdapterDataObserver(this.mAdapterObserver);
    }
}
