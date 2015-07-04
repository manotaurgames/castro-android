package com.manotaurgames.simplecast.sample;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.manotaurgames.simplecast.CastActivity;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Aaron Sarazan on 7/4/15
 * Copyright(c) 2015 Level, Inc.
 */
public class MainActivity extends CastActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 1;

    private HelloWorldChannel mHelloWorldChannel;

    public MainActivity() {
        super(R.string.app_id);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // When the user clicks on the button, use Android voice recognition to
        // get text
        Button voiceButton = (Button) findViewById(R.id.voiceButton);
        voiceButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceRecognitionActivity();
            }
        });
    }

    /**
     * Android voice recognition
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.message_to_cast));
        startActivityForResult(intent, REQUEST_CODE);
    }

    /*
     * Handle the voice recognition response
     *
     * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int,
     * android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            if (matches.size() > 0) {
                Log.d(TAG, matches.get(0));
                sendMessage(matches.get(0));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Send a text message to the receiver
     */
    private void sendMessage(String message) {
        GoogleApiClient apiClient = getCastApiClient();
        if (apiClient != null && mHelloWorldChannel != null) {
            try {
                Cast.CastApi.sendMessage(apiClient,
                        mHelloWorldChannel.getNamespace(), message).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status result) {
                                if (!result.isSuccess()) {
                                    Log.e(TAG, "Sending message failed");
                                }
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending message", e);
            }
        } else {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCastConnect(GoogleApiClient apiClient, boolean isReconnect) {
        if (mHelloWorldChannel == null) mHelloWorldChannel = new HelloWorldChannel();
        try {
            Cast.CastApi.setMessageReceivedCallbacks(
                    apiClient,
                    mHelloWorldChannel.getNamespace(),
                    mHelloWorldChannel);
            sendMessage(getString(R.string.instructions));
        } catch (IOException e) {
            Log.e(TAG, "Exception while creating channel", e);
        }
    }

    @Override
    public void onCastDisconnect(GoogleApiClient apiClient) {
        try {
            if (mHelloWorldChannel != null) {
                Cast.CastApi.removeMessageReceivedCallbacks(
                        apiClient,
                        mHelloWorldChannel.getNamespace());
                mHelloWorldChannel = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception while removing channel", e);
        }
    }

    /**
     * Custom message channel
     */
    class HelloWorldChannel implements MessageReceivedCallback {

        /**
         * @return custom namespace
         */
        public String getNamespace() {
            return getString(R.string.namespace);
        }

        /*
         * Receive message from the receiver app
         */
        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
            Log.d(TAG, "onMessageReceived: " + message);
        }

    }
}
