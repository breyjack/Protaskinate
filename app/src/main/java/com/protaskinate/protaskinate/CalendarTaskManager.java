package com.protaskinate.protaskinate;

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

/**
 * Created by michael on 10/16/16.
 */

class CalendarTaskManager {

    private com.google.api.services.calendar.Calendar calendarService;

    private CalendarAPIActivity.CalendarTask currentTask;
    private Exception failureCause = null;

    @SuppressWarnings("UnusedDeclaration")
    public void doTask(CalendarAPIActivity.CalendarTask task) {
        if (isBusy()) {
            wrapper.cancel(true);
        }

        wrapper = new AsyncWrapper();

        currentTask = task;
        failureCause = null;
        wrapper.execute();
    }

    private AsyncWrapper wrapper = null;
    // The task manager shouldn't be used or have its task changed if it
    // is asynchronously doing another task right now.
    @SuppressWarnings("UnusedDeclaration")
    public boolean isBusy() {
        return wrapper != null && wrapper.getStatus() == AsyncTask.Status.RUNNING;
    }

    CalendarTaskManager(GoogleAccountCredential credential) {

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        calendarService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Protaskinate Calender Tester")
                .build();
    }

    class AsyncWrapper extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            if (failureCause != null)
                return failureCause.getMessage();
            else
                try {
                    if (currentTask == null)
                        throw new Exception("CalendarTaskManager cannot execute without being given a task. Use the doTask method.");
                    else
                        return currentTask.execute(calendarService);
                } catch (Exception ex) {
                    failureCause = ex;
                    cancel(true);
                    return ex.getMessage();
                }
        }

        @Override
        protected void onPreExecute() {
            if (failureCause == null)
                try {
                    currentTask.onPreExecute(calendarService);
                } catch (Exception ex) {
                    failureCause = ex;
                }
        }

        @Override
        protected void onPostExecute(String output) {
            if (failureCause == null)
                try {
                    currentTask.onPostExecute(calendarService, output);
                } catch (Exception ex) {
                    failureCause = ex;
                }
            currentTask = null;
        }

        @Override
        protected void onCancelled() {
            try {
                currentTask.onCancelled(calendarService, failureCause);
            } catch (Exception ex) {
                failureCause = ex;
            }
            currentTask = null;
        }
    }
}