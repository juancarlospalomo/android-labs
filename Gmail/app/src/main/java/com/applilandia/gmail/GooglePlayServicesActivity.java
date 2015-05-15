package com.applilandia.gmail;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;

public class GooglePlayServicesActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GooglePlayServicesActiv";

    private enum UserStatus {
        connected,
        disconnected,
        unregistered
    }


    private static final String KEY_IN_RESOLUTION = "is_in_resolution";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Determines if the client is in a resolution state, and
     * waiting for resolution intent to return.
     */
    private boolean mIsInResolution;

    private SignInButton mSignInButton;
    private Button mButtonSignOut;
    private Button mButtonUnregister;

    private boolean mSignInClicked = false;

    private UserStatus mStatus;

    /**
     * Called when the activity is starting. Restores the activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sing_in_activity);

        inflateViews();

        tryConnect();

        createButtonHandlers();

        if (savedInstanceState != null) {
            mIsInResolution = savedInstanceState.getBoolean(KEY_IN_RESOLUTION, false);
        }
    }

    private void inflateViews() {
        mSignInButton = (SignInButton) findViewById(R.id.buttonSignIn);
        mButtonSignOut = (Button) findViewById(R.id.buttonSignOut);
        mButtonUnregister = (Button) findViewById(R.id.buttonUnRegister);
    }

    private void createButtonHandlers() {
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleApiClient == null) {
                    mGoogleApiClient = new GoogleApiClient.Builder(GooglePlayServicesActivity.this)
                            .addApi(Plus.API)
                            .addScope(Plus.SCOPE_PLUS_LOGIN)
                                    // Optionally, add additional APIs and scopes if required.
                            .addConnectionCallbacks(GooglePlayServicesActivity.this)
                            .addOnConnectionFailedListener(GooglePlayServicesActivity.this)
                            .build();
                }
                Log.v(TAG, "Connecting: " + String.valueOf(mGoogleApiClient.isConnecting()));
                if (!mGoogleApiClient.isConnecting()) {
                    mSignInClicked = true;
                    mIsInResolution = false;
                    mGoogleApiClient.connect();
                }
            }
        });


        mButtonSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.clearDefaultAccountAndReconnect();
                    mGoogleApiClient.disconnect();
                    mStatus = UserStatus.disconnected;
                    setUIState();
                }
            }
        });


        mButtonUnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                mStatus = UserStatus.unregistered;
                                setUIState();
                                // mGoogleApiClient is now disconnected and access has been revoked.
                                // Trigger app logic to comply with the developer policies
                            }
                        });
            }
        });
    }

    /**
     * Try connect to see if the user is already signed-in
     */
    private void tryConnect() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(GooglePlayServicesActivity.this)
                    .addApi(Plus.API)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                            // Optionally, add additional APIs and scopes if required.
                    .addConnectionCallbacks(GooglePlayServicesActivity.this)
                    .addOnConnectionFailedListener(GooglePlayServicesActivity.this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    private void setUIState() {
        if (mStatus == UserStatus.connected) {
            mSignInButton.setVisibility(View.GONE);
            mButtonSignOut.setVisibility(View.VISIBLE);
            mButtonUnregister.setVisibility(View.VISIBLE);
        } else {
            mButtonSignOut.setVisibility(View.GONE);
            mButtonUnregister.setVisibility(View.GONE);
            mSignInButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Called when activity gets invisible. Connection to Play Services needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * Saves the resolution state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IN_RESOLUTION, mIsInResolution);
    }

    /**
     * Handles Google Play Services resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                Log.v(TAG, "Request Code Resolution");
                if (resultCode != RESULT_OK) {
                    mSignInClicked = false;
                }
                retryConnecting();
                break;
        }
    }

    private void retryConnecting() {
        mIsInResolution = false;
        if (mSignInClicked && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");
        mSignInClicked = false;
        mStatus = UserStatus.connected;
        setUIState();;
       // TODO: Start making API requests.
        Toast.makeText(this, Plus.AccountApi.getAccountName(mGoogleApiClient), Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when {@code mGoogleApiClient} connection is suspended.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
        retryConnecting();
    }

    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());

        if (mSignInClicked && !result.hasResolution()) {
            // Show a localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(
                    result.getErrorCode(), this, 0, new OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            retryConnecting();
                        }
                    }).show();
            return;
        }
        // If there is an existing resolution error being displayed or a resolution
        // activity has started before, do nothing and wait for resolution
        // progress to be completed.
        if (mIsInResolution) {
            return;
        }
        mIsInResolution = true;
        Log.v(TAG, "Clicked signed-in: " + mSignInClicked);
        if (mSignInClicked) {
            try {
                result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
            } catch (SendIntentException e) {
                Log.e(TAG, "Exception while starting resolution activity", e);
                retryConnecting();
            }
        } else {
            //If the connection fail was not triggered by the user click,
            //Then keep it as unconnected and don't start resolution
            mStatus = UserStatus.disconnected;
            setUIState();
        }
    }

    private void fetchEmails() {

    }
}
