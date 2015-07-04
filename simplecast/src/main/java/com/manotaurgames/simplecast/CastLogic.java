package com.manotaurgames.simplecast;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.Callback;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

/**
 * Created by Aaron Sarazan on 7/4/15
 * Copyright(c) 2015 Level, Inc.
 */
public class CastLogic {

    private static final String TAG = CastLogic.class.getSimpleName();

    /**
     * Primary interface for getting things done with cast. Pass this to the {@link CastLogic} constructor.
     */
    public interface CastLogicCallbacks {

        /**
         * Gets called every time the API is connected, and provides you with the client.
         * @param apiClient the ApiClient you'll want to operate on.
         * @param isReconnect whether this is a reconnect of a dropped connection.
         */
        void onCastConnect(GoogleApiClient apiClient, boolean isReconnect);

        /**
         * Gets called when the API is disconnected. You'll probably want to remove message handlers etc.
         * @param apiClient the ApiClient you'll want to use for cleanup.
         */
        void onCastDisconnect(GoogleApiClient apiClient);
    }

    private final Activity mActivity;
    private final String mCastAppId;
    private final CastLogicCallbacks mCallbacks;

    private final State mState;

    private static class State {
        public final MediaRouter mediaRouter;
        public final MediaRouteSelector mediaRouteSelector;
        public final MediaRouter.Callback mediaRouterCallback;

        public CastDevice selectedDevice;
        public GoogleApiClient apiClient;
        public boolean applicationStarted;
        public boolean waitingForReconnect;
        public String sessionId;

        public State(MediaRouter mediaRouter, MediaRouteSelector mediaRouteSelector, Callback mediaRouterCallback) {
            this.mediaRouter = mediaRouter;
            this.mediaRouteSelector = mediaRouteSelector;
            this.mediaRouterCallback = mediaRouterCallback;
        }
    }

    public CastLogic(Activity activity, String castAppId, CastLogicCallbacks callbacks) {
        mActivity = activity;
        mCastAppId = castAppId;
        mCallbacks = callbacks;
        mState = new State(
                MediaRouter.getInstance(mActivity),
                new MediaRouteSelector.Builder()
                        .addControlCategory(CastMediaControlIntent.categoryForCast(mCastAppId))
                        .build(),
                new MyMediaRouterCallback());
    }

    public final void onStart() {
        mState.mediaRouter.addCallback(mState.mediaRouteSelector, mState.mediaRouterCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    public final void onStop() {
        mState.mediaRouter.removeCallback(mState.mediaRouterCallback);
    }

    public final void onDestroy() {
        Log.d(TAG, "onDestroy");
        teardown(true);
    }

    public final void onCreateOptionsMenu(Menu menu) {
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat
                        .getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mState.mediaRouteSelector);
    }

    public final GoogleApiClient getApiClient() {
        return mState.apiClient;
    }

    public final String getSessionId() {
        return mState.sessionId;
    }

    /**
     * Start the receiver app
     */
    private void launchReceiver() {
        try {
            Cast.Listener castListener = new Cast.Listener() {

                @Override
                public void onApplicationDisconnected(int errorCode) {
                    Log.d(TAG, "application has stopped");
                    teardown(true);
                }

            };
            // Connect to Google Play services
            ConnectionCallbacks connectionCallbacks = new ConnectionCallbacks();
            ConnectionFailedListener connectionFailedListener = new ConnectionFailedListener();
            Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                    .builder(mState.selectedDevice, castListener);
            mState.apiClient = new GoogleApiClient.Builder(mActivity)
                    .addApi(Cast.API, apiOptionsBuilder.build())
                    .addConnectionCallbacks(connectionCallbacks)
                    .addOnConnectionFailedListener(connectionFailedListener)
                    .build();
            mState.apiClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "Failed launchReceiver", e);
        }
    }

    /**
     * Tear down the connection to the receiver
     */
    private void teardown(boolean selectDefaultRoute) {
        Log.d(TAG, "teardown");
        if (mState.apiClient != null) {
            if (mState.applicationStarted) {
                if (mState.apiClient.isConnected() || mState.apiClient.isConnecting()) {
                    Cast.CastApi.stopApplication(mState.apiClient, mState.sessionId);
                    if (mCallbacks != null) mCallbacks.onCastDisconnect(mState.apiClient);
                    mState.apiClient.disconnect();
                }
                mState.applicationStarted = false;
            }
            mState.apiClient = null;
        }
        if (selectDefaultRoute) {
            mState.mediaRouter.selectRoute(mState.mediaRouter.getDefaultRoute());
        }
        mState.selectedDevice = null;
        mState.waitingForReconnect = false;
        mState.sessionId = null;
    }

    /**
     * Callback for MediaRouter events
     */
    private class MyMediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, RouteInfo info) {
            Log.d(TAG, "onRouteSelected");
            // Handle the user route selection.
            mState.selectedDevice = CastDevice.getFromBundle(info.getExtras());
            launchReceiver();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, RouteInfo info) {
            Log.d(TAG, "onRouteUnselected: info=" + info);
            teardown(false);
            mState.selectedDevice = null;
        }
    }

    /**
     * Google Play services callbacks
     */
    private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected(Bundle connectionHint) {
            Log.d(TAG, "onConnected");

            if (mState.apiClient == null) {
                // We got disconnected while this runnable was pending
                // execution.
                return;
            }

            try {
                if (mState.waitingForReconnect) {
                    mState.waitingForReconnect = false;

                    // Check if the receiver app is still running
                    if ((connectionHint != null)
                            && connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
                        Log.d(TAG, "App  is no longer running");
                        teardown(true);
                    } else {
                        if (mCallbacks != null) mCallbacks.onCastConnect(mState.apiClient, true);
                    }
                } else {
                    // Launch the receiver app
                    Cast.CastApi.launchApplication(mState.apiClient, mCastAppId, false)
                            .setResultCallback(
                                    new ResultCallback<ApplicationConnectionResult>() {
                                        @Override
                                        public void onResult(
                                                ApplicationConnectionResult result) {
                                            Status status = result.getStatus();
                                            Log.d(TAG,
                                                    "ApplicationConnectionResultCallback.onResult:"
                                                            + status.getStatusCode());
                                            if (status.isSuccess()) {
                                                ApplicationMetadata applicationMetadata = result
                                                        .getApplicationMetadata();
                                                mState.sessionId = result.getSessionId();
                                                String applicationStatus = result
                                                        .getApplicationStatus();
                                                boolean wasLaunched = result.getWasLaunched();
                                                Log.d(TAG, "application name: "
                                                        + applicationMetadata.getName()
                                                        + ", status: " + applicationStatus
                                                        + ", sessionId: " + mState.sessionId
                                                        + ", wasLaunched: " + wasLaunched);
                                                mState.applicationStarted = true;
                                                if (mCallbacks != null) mCallbacks.onCastConnect(mState.apiClient, false);
                                            } else {
                                                Log.e(TAG, "application could not launch");
                                                teardown(true);
                                            }
                                        }
                                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to launch application", e);
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.d(TAG, "onConnectionSuspended");
            mState.waitingForReconnect = true;
        }
    }

    /**
     * Google Play services callbacks
     */
    private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.e(TAG, "onConnectionFailed ");
            teardown(false);
        }
    }
}
