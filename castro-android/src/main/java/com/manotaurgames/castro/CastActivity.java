/*
 * Copyright (C) 2015 Manotaur LLC. All Rights Reserved.
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

package com.manotaurgames.castro;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.google.android.gms.common.api.GoogleApiClient;
import com.manotaurgames.castro.CastLogic.CastLogicCallbacks;

/**
 * Subclass this Activity to get easy access to Google Cast API's in your UI
 * This class handles the Menu Item, Connect/Reconnect, and Disconnect.
 *
 * If you need more specialized behavior, consider using {@link CastLogic} directly.
 */
public abstract class CastActivity extends AppCompatActivity implements CastLogicCallbacks {

    private static final String TAG = CastActivity.class.getSimpleName();

    private final Integer mCastAppIdRes;

    private CastLogic mCastLogic;

    /**
     * The default constructor will use the 'GoogleCastId' metadata field from AndroidManifest
     */
    public CastActivity() {
        this(null);
    }

    /**
     * Subclasses can also pass a custom cast app id in the constructor.
     * @param castAppId the resource id for our Cast app id.
     */
    protected CastActivity(Integer castAppId) {
        mCastAppIdRes = castAppId;
    }

    private String getCastAppId() {
        if (mCastAppIdRes != null) {
            return getString(mCastAppIdRes);
        }
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            return (String) ai.metaData.get("GoogleCastId");
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCastLogic = new CastLogic(this, getCastAppId(), this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCastLogic.onDestroy();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        mCastLogic.onCreateOptionsMenu(menu);
        return true;
    }

    public final GoogleApiClient getCastApiClient() {
        return mCastLogic.getApiClient();
    }

    public final String getCastSessionId() {
        return mCastLogic.getSessionId();
    }

}
