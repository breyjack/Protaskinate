package com.protaskinate.protaskinate;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by michael on 10/16/16.
 * Extends android.app.Activity to provide child classes with everything they need to use
 * the Calendar API.
 */

public abstract class CalendarAPIActivity extends Activity implements EasyPermissions.PermissionCallbacks {

    GoogleAccountCredential googleCredential;
    ProgressDialog progressDialog;

    private CalendarTaskManager taskManager;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };

    /**
     * Gives text feedback to the user about a Google API error message
     * @param message The message to be shown to the user, however the
     *                implementing class sees fit.
     */
    protected abstract void showErrorMessage(String message);

    public abstract class CalendarTask{
        public abstract String execute(Calendar service) throws Exception;
        public void onPreExecute(Calendar service)  throws Exception { }
        public abstract void onPostExecute(Calendar service, String output)  throws Exception;

        public abstract void setResultMessage(String message) throws Exception;
        public void setErrorMessage(String message) throws Exception {
            setResultMessage(message);
        }

        public void onCancelled(Calendar service, Exception failureCauseOrNull) throws Exception
        {
            if (failureCauseOrNull != null) {
                if (failureCauseOrNull instanceof GooglePlayServicesAvailabilityIOException
                        || failureCauseOrNull instanceof UserRecoverableAuthIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) failureCauseOrNull)
                                    .getConnectionStatusCode());
                } else if (failureCauseOrNull instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) failureCauseOrNull).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    setErrorMessage("The following error occurred:\n"
                                    + failureCauseOrNull.getMessage()
                                    /* + "\n\nat\n" + TextUtils.join("\n", mLastError.getStackTrace())*/);
                }
            } else {
                setResultMessage("Request cancelled.");
            }
        }
    }


    /**
     * Initializes the activity, and sets up the Google credentials
     * @param savedInstance
     */
    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        // Initialize credentials and service object.
        googleCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Calling Google Calendar API ...");
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     *
     * This doesn't return anything, because the result might be to show an account
     * selection dialog. In that case, the onActivityResult method handles the action.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    protected void chooseAccount() {

        if (EasyPermissions.hasPermissions(this, android.Manifest.permission.GET_ACCOUNTS)){

            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);

            if (accountName != null) {

                googleCredential.setSelectedAccountName(accountName);

            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        googleCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }

        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    android.Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        showErrorMessage("Request code: " + requestCode + "\nResultCode: " + resultCode
        + "\nIntent data:\n" + data.getData());

        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    showErrorMessage(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                }
                executeCurrentTask();

                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK
                        && data != null
                        && data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        googleCredential.setSelectedAccountName(accountName);

                        executeCurrentTask();
                    }
                    else {
                        cancelCurrentTask("Cannot get Google Account name.");
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    executeCurrentTask();
                }
                else {
                    showErrorMessage("Task cancelled due to unexpected result code from Google API: " + requestCode + ".");
                }
                break;
            default:
                showErrorMessage(
                        "Task cancelled due to unexpected request code from Google API: " + resultCode
                                + ". Request code: " + requestCode + ".");
                break;
        }
    }


    private CalendarTask curTask;
    protected CalendarTask getCurrentTask(){
        return curTask;
    }
    protected void setCurrentTask(CalendarTask newTask){
        curTask = newTask;
    }

    public abstract void cancelCurrentTask(String why);

    // Start the result of getCurrentTask(), if it's not null.
    protected void executeCurrentTask() {
        CalendarTask ct = getCurrentTask();
        if (ct != null) {
            if (taskManager == null)
                taskManager = new CalendarTaskManager(googleCredential);
            taskManager.doTask(ct);
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    protected boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    protected boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    protected void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }
}