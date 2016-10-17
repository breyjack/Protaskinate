package com.protaskinate.protaskinate;

import pub.devrel.easypermissions.AfterPermissionGranted;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.calendar.*;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends CalendarAPIActivity {

    private TextView outputTextView;

    @Override
    protected void showErrorMessage(String msg){
        outputTextView.setText(msg);
    }

    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout activityLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        activityLayout.setLayoutParams(lp);
        activityLayout.setOrientation(LinearLayout.VERTICAL);
        activityLayout.setPadding(16, 16, 16, 16);

        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        /*** Michael's button */

        final Button changeThingButton = new Button(this);
        changeThingButton.setLayoutParams(tlp);
        changeThingButton.setText("Click to break things");
        changeThingButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                changeThingButton.setEnabled(false);
                testTheAPI();
                changeThingButton.setEnabled(true);
            }
        });

        activityLayout.addView(changeThingButton);

        /*** End Michael's button */

        outputTextView = new TextView(this);
        outputTextView.setLayoutParams(tlp);
        outputTextView.setPadding(16, 16, 16, 16);
        outputTextView.setVerticalScrollBarEnabled(true);
        outputTextView.setMovementMethod(new ScrollingMovementMethod());
        outputTextView.setText(
                "Click the magic button to test the API.");
        activityLayout.addView(outputTextView);

        setContentView(activityLayout);
    }

    public void cancelCurrentTask(String reason){
        outputTextView.setText(reason);
    }

    private void testTheAPI() {

        // These are if-else instead of if, because acquiring play
        // services and selecting an account are both done in
        // separate threads.

        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
            showErrorMessage("Acquired play services");
        }
        else if (googleCredential.getSelectedAccountName() == null) {
            chooseAccount();
            showErrorMessage("Chose account");
        }

        if (!isDeviceOnline())
            showErrorMessage("No network connection available.");
        else
        {
            // Set up the little spinny waity guy
            progressDialog.setMessage("Doing thing");
            progressDialog.show();

            // Create a new task to be run
            CalendarTask ct = new CalendarTask() {
                @Override
                public String execute(Calendar service) throws Exception {
                    List<Event> events =
                            service
                            .events()
                            .list("primary")
                            .setTimeMin(new DateTime(9000L))
                            .setMaxResults(3)
                            .execute()
                            .getItems();
                    String r = "Events:";
                    for (Event e : events)
                        r += "\n"
                            + e.getSummary()
                            + "\n\tfrom " + e.getStart().toString()
                            + "\n\tto " + e.getEnd().toString();
                    return r;
                }

                @Override
                public void onPostExecute(Calendar c, String result){
                    showErrorMessage(result);
                    progressDialog.hide();
                }

                @Override
                public void onCancelled(Calendar c, Exception causeOrNull) throws Exception {
                    super.onCancelled(c, causeOrNull);
                    progressDialog.hide();
                    showErrorMessage("Thing cancelled");
                }

                @Override
                public void setResultMessage(String message) {
                    showErrorMessage(message);
                }
            };

            // Set the task
            setCurrentTask(ct);
            // Run the task
            executeCurrentTask();
        }
    }
}