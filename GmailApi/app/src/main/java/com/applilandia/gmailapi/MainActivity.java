package com.applilandia.gmailapi;

import android.accounts.AccountManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.applilandia.gmailapi.model.Label;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private enum UserStatus {
        connected,
        disconnected,
        unregistered
    }

    private static final int RC_SIGN_IN = 1;
    private static final int REQUEST_AUTHORIZATION = 2;

    /**
     * Determines if the Sign In button has been clicked on
     */
    private boolean mSignInButtonClicked = false;
    /**
     * Determines if the client is in a resolution state, and
     * waiting for resolution intent to return.
     */
    private boolean mIsInResolution;
    /**
     * Current status for the user
     */
    private UserStatus mStatus;
    /**
     * Buttons View
     */
    private Button mSignInButton;
    private Button mSignOutButton;
    private Button mRevokeAccessButton;
    /**
     * To show the current status
     */
    private TextView mTextStatus;

    /**
     * Google Api Client to connect to Play Services
     */
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inflateViews();
        setHandlers();
    }

    private void inflateViews() {
        mSignInButton = (Button) findViewById(R.id.signInButton);
        mSignOutButton = (Button) findViewById(R.id.signOutButton);
        mRevokeAccessButton = (Button) findViewById(R.id.revokeAccessButton);
        mTextStatus = (TextView) findViewById(R.id.signInStatusText);
    }

    private void setHandlers() {
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleApiClient == null) {
                    mGoogleApiClient = buildApiClient();
                }
                if (!mGoogleApiClient.isConnecting()) {
                    mTextStatus.setText(R.string.text_signing_in);
                    mSignInButtonClicked = true;
                    mIsInResolution = false;
                    mGoogleApiClient.connect();
                }
            }
        });

        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleApiClient.isConnected()) {
                    mStatus = UserStatus.disconnected;
                    setButtonStates();
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                }
            }
        });

        mRevokeAccessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleApiClient.isConnected()) {
                    mStatus = UserStatus.unregistered;
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            setButtonStates();
                            mGoogleApiClient.disconnect();
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode != RESULT_OK) {
                    mSignInButtonClicked = false;
                }
                retryConnecting();
                break;

            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    GmailApiAsync gmailApiAsync = new GmailApiAsync();
                    gmailApiAsync.execute(email);
                }
                break;
        }
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != RESULT_OK) {
                mSignInButtonClicked = false;
            }
            retryConnecting();
        }
    }

    /**
     * Set enabled status for the buttons according to the user state
     */
    private void setButtonStates() {
        if (mStatus == UserStatus.connected) {
            mSignInButton.setEnabled(false);
            mSignOutButton.setEnabled(true);
            mRevokeAccessButton.setEnabled(true);
        } else {
            mSignInButton.setEnabled(true);
            mSignOutButton.setEnabled(false);
            mRevokeAccessButton.setEnabled(false);
        }
    }

    /**
     * Build Google Api Client object to connect selecting the account with Email scope
     *
     * @return GoogleApiClient built object
     */
    private GoogleApiClient buildApiClient() {
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(new Scope(Scopes.EMAIL))
                .build();
    }

    /**
     * Try to connect to the Play Services if button is clicked, after a resolution fail
     */
    private void retryConnecting() {
        mIsInResolution = false;
        if (mSignInButtonClicked && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = buildApiClient();
        }
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mStatus = UserStatus.connected;
        mSignInButtonClicked = false;
        mIsInResolution = false;
        setButtonStates();
        String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        mTextStatus.setText(String.format("Signed In as %s", email));
        GmailApiAsync gmailApiAsync = new GmailApiAsync();
        gmailApiAsync.execute(email);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
        Log.i(LOG_TAG, "onConnectionSuspended:" + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (mSignInButtonClicked && !connectionResult.hasResolution()) {
            // Show a localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(
                    connectionResult.getErrorCode(), this, 0, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            retryConnecting();
                        }
                    }).show();
            return;
        }

        if (mIsInResolution) {
            return;
        }
        mIsInResolution = true;

        if (mSignInButtonClicked) {
            try {
                connectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                retryConnecting();
            }
        } else {
            mStatus = UserStatus.disconnected;
            setButtonStates();
        }

    }

    private class GmailApiAsync extends AsyncTask<String, Void, Void> {

        final String BASE_GMAIL_MESSAGES_API_URL = "https://www.googleapis.com/gmail/v1/users/%s/messages";
        final String BASE_GMAIL_LABEL_API_URL = "https://www.googleapis.com/gmail/v1/users/%s/labels";
        //String SCOPE = "oauth2:https://www.googleapis.com/auth/gmail.readonly";

        String EMAIL_SCOPE = "https://www.googleapis.com/auth/gmail.readonly";
        String LABELS_SCOPE = "https://www.googleapis.com/auth/gmail.labels";
        String SCOPE = "oauth2:" + EMAIL_SCOPE + " " + LABELS_SCOPE;

        private String authorize(String email) {
            try {
                String token = GoogleAuthUtil.getToken(MainActivity.this, email, SCOPE);
                return token;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Get the complete json message
         *
         * @param reader
         * @return
         */
        private String getJsonResponse(BufferedReader reader) {
            StringBuffer result = new StringBuffer();

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (result.length() == 0) {
                return null;
            } else {
                return result.toString();
            }
        }

        private ArrayList<Label> loadLabels(String jsonLabels) {
            try {
                JSONObject labelsJSON = new JSONObject(jsonLabels);
                JSONArray arrayLabel = labelsJSON.getJSONArray("labels");
                Log.i(LOG_TAG, (String) arrayLabel.getJSONObject(0).get("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected Void doInBackground(String... params) {

            if (params != null) {
                String email = params[0];

                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                String urlWithUserId = String.format(BASE_GMAIL_LABEL_API_URL, email);
                String token = authorize(email);

                if (token != null) {
                    try {
                        URL url = new URL(urlWithUserId);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setRequestProperty("Authorization", "Bearer " + token);
                        urlConnection.connect();

                        InputStream inputStream = urlConnection.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(inputStream));

                        String response = getJsonResponse(reader);
                        loadLabels(response);
                        Log.i(LOG_TAG, "Json: " + response);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);
        }
    }

}
