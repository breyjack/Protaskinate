package com.protaskinate.protaskinate;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.sql.Date;

/**
 * Created by michael on 10/12/16.
 * This class defines an object that normalizes a schedule.
 * In our meeting, we talked about this being a static class, but
 * it might make sense to let the user select from different
 * normalizers that behave differently. For now,
 * the static Normalize method will just instantiate and use a default
 * working model.
 */

public class Normalizer {
    public Normalizer() {
        super();
    }

    // Singleton instance
    private static Normalizer __instance;

    public String Normalize(Event[] events) {

        // Right now, this just shows the earliest and latest events

            DateTime minTime = DateTime.parseRfc3339("2050-10-12T12:58:58.875-06:00");
            DateTime maxTime = DateTime.parseRfc3339("2001-10-12T12:58:58.875-06:00");

            for (Event e :
                    events) {
                if (e.getStart().getDateTime().getValue() < minTime.getValue())
                    minTime = e.getStart().getDateTime();

                if (e.getEnd().getDateTime().getValue() > maxTime.getValue())
                    maxTime = e.getEnd().getDateTime();
            }

            return "Min: " + minTime.getValue() + "\nMax: " + maxTime.getValue();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    // TODO: Interning to avoid loading duplicate normalizers
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}