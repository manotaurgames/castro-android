/*
 * Copyright (C) 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.manotaurgames.simplecast;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;

import com.google.android.gms.common.api.GoogleApiClient;
import com.manotaurgames.simplecast.CastLogic.CastLogicCallbacks;

/**
 * Subclass this Activity to get easy access to Google Cast API's in your UI
 * This class handles the Menu Item, Connect/Reconnect, and Disconnect.
 *
 * If you need more specialized behavior, consider using {@link CastLogic} directly.
 */
public abstract class CastFragment extends Fragment implements CastLogicCallbacks {

    private static final String TAG = CastFragment.class.getSimpleName();

    private final Integer mCastAppIdRes;

    private CastLogic mCastLogic;

    /**
     * The default constructor will use the 'SimpleCastId' metadata field from AndroidManifest
     */
    public CastFragment() {
        this(null);
    }

    /**
     * Subclasses can also pass a custom cast app id in the constructor.
     * @param castAppId the resource id for our Cast app id.
     */
    protected CastFragment(Integer castAppId) {
        mCastAppIdRes = castAppId;
    }

    private String getCastAppId() {
        if (mCastAppIdRes != null) {
            return getString(mCastAppIdRes);
        }
        Activity a = getActivity();
        try {
            ApplicationInfo ai = a.getPackageManager().getApplicationInfo(a.getPackageName(), PackageManager.GET_META_DATA);
            return (String) ai.metaData.get("SimpleCastId");
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCastLogic = new CastLogic(activity, getCastAppId(), this);
    }

    @Override
    public void onDetach() {
        mCastLogic.onDestroy();
        mCastLogic = null;
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        mCastLogic.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mCastLogic.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
        mCastLogic.onCreateOptionsMenu(menu);
    }

    public final GoogleApiClient getCastApiClient() {
        return mCastLogic.getApiClient();
    }

    public final String getCastSessionId() {
        return mCastLogic.getSessionId();
    }

}
