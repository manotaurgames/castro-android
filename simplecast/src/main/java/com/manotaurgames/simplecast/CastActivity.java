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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.google.android.gms.common.api.GoogleApiClient;
import com.manotaurgames.simplecast.CastLogic.CastLogicCallbacks;

/**
 * Main activity to send messages to the receiver.
 */
public abstract class CastActivity extends AppCompatActivity implements CastLogicCallbacks {

    private static final String TAG = CastActivity.class.getSimpleName();

    private final Integer mCastAppIdRes;
    private final String mCastAppId;

    private CastLogic mCastLogic;

    private CastActivity() {
        throw new UnsupportedOperationException("Subclass must use specialized constructor");
    }

    protected CastActivity(int castAppId) {
        mCastAppIdRes = castAppId;
        mCastAppId = null;
    }

    protected CastActivity(String castAppId) {
        mCastAppIdRes = null;
        mCastAppId = castAppId;
    }

    private String getCastAppId() {
        return mCastAppId != null ? mCastAppId : getString(mCastAppIdRes);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCastLogic = new CastLogic(this, getCastAppId(), this);
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
