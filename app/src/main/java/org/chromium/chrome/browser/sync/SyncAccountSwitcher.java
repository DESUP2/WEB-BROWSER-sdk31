// Copyright 2016 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.sync;

import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentActivity;
import org.chromium.base.Callback;
import org.chromium.base.Promise;
import org.chromium.chrome.browser.preferences.SyncedAccountPreference;
import org.chromium.chrome.browser.signin.ConfirmImportSyncDataDialog;
import org.chromium.chrome.browser.signin.ConfirmImportSyncDataDialog.ImportSyncType;
import org.chromium.chrome.browser.signin.ConfirmSyncDataStateMachine;
import org.chromium.chrome.browser.signin.SigninManager;
import org.chromium.chrome.browser.signin.SigninManager.SignInCallback;

/**
 * A class that encapsulates the control flow of listeners and callbacks when switching sync
 * accounts.
 *
 * Control flows through the method in this order:
 *   {@link OnPreferenceChangeListener#onPreferenceChange}
 *   {@link ConfirmImportSyncDataDialog.Listener#onConfirm}
 *   {@link SignInCallback#onSignInComplete}
 */
public  class SyncAccountSwitcher extends AppCompatActivity
        implements Preference.OnPreferenceChangeListener, ConfirmImportSyncDataDialog.Listener,  SigninManager.SignInCallback  {
    private static final String TAG = "SyncAccountSwitcher";

    private final SyncedAccountPreference mSyncedAccountPreference;
    private  Activity mActivity;

    private String mNewAccountName;

    /**
     * Sets up a SyncAccountSwitcher to be ready to accept callbacks.
     * @param activity Activity used to get the context for signin and get the fragmentManager
     *                 for the data sync fragment.
     * @param syncedAccountPreference The preference to update once signin has been completed.
     */
    public SyncAccountSwitcher(Activity activity1, SyncedAccountPreference syncedAccountPreference) {
        this.mActivity = activity1;
        this.mSyncedAccountPreference = syncedAccountPreference;
    }
    FragmentActivity myContext = (FragmentActivity) mActivity;
    FragmentManager fragManager = myContext.getSupportFragmentManager();
    @Override
    public boolean onPreferenceChange(Preference p, Object newValue) {
        if (newValue == null) return false;

        this.mNewAccountName = (String) newValue;
        String currentAccount = this.mSyncedAccountPreference.getValue();

        if (!TextUtils.equals(mNewAccountName, currentAccount)) 
	{
        ConfirmSyncDataStateMachine.run(currentAccount, this.mNewAccountName, ConfirmImportSyncDataDialog.ImportSyncType.SWITCHING_SYNC_ACCOUNTS, fragManager, mActivity, this);
	} 
	//return false;

        // Don't update the selected account in the preference. It will be updated by
        // the call to mSyncAccountListPreference.update() if everything succeeds.
        return false;
    }

    @Override
    public void onConfirm(final boolean wipeData) {
        assert mNewAccountName != null;

        // Sign out first to ensure we don't wipe the data when sync is still on.
        SigninManager.get(mActivity).signOutPromise()
                .then(new Promise.AsyncFunction<Void, Void>(){
                    @Override
                    public Promise<Void> apply(Void argument) {
                        // Once signed out, clear the last signed in user and wipe data if needed.
                        SigninManager.get(mActivity).clearLastSignedInUser();
                        return SigninManager.wipeSyncUserDataIfRequired(wipeData);
                    }
                }).then(new Callback<Void>(){
                    @Override
                    public void onResult(Void result) {
                        // Once the data has been wiped (if needed), sign in to the next account.
                        SigninManager.get(mActivity)
                            .signIn(mNewAccountName, mActivity, SyncAccountSwitcher.this);
                    }
                });
    }

    @Override
    public void onCancel() {
        // The user aborted the 'merge data' dialog, there is nothing to do.
    }

    @Override
    public void onSignInComplete() {
        // Update the Preference so it displays the correct account name.
        mSyncedAccountPreference.update();
    }

    @Override
    public void onSignInAborted() {
        // If the user aborted signin, there is nothing to do.
    }
}
