package com.protaskinate.protaskinate;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

/**
 * Created by michael on 10/12/16.
 * This class has static methods to help us get up and running
 * with fake Event instances until we get the Calendar API giving us
 * real instances.
 **/

public class EventCheater
{
    public static Event makeEvent(String description, DateTime start, DateTime end)
    {
        Event e = new Event();
        e.setDescription(description);

        // I don't understand the difference between EventDateTime and DateTime,
        // so this hack makes DateTime usable for now
        EventDateTime startEDT = new EventDateTime();
        startEDT.setDateTime(start);
        e.setStart(startEDT);

        EventDateTime endEDT = new EventDateTime();
        endEDT.setDateTime(end);
        e.setEnd(endEDT);

        return e;
    }

    static String pad(Integer i) {
        if (i < 10)
            return "0" + i.toString();
        else
            return i.toString();
    }

    public static DateTime makeDateTime(Integer year, Integer month, Integer day,
                                        Integer hour, Integer minute, Integer second)
    {
        // "2016-10-12T12:58:58.875-06:00" is what
        // came from new DateTime().toStringRfc3339()
        // so it's a valid format.
        // return DateTime.parseRfc3339(String.format("{0}-{1}-{2}T{3}:{4}:{5}.875-06:00", year, month, day, hour, minute, second));
        String Rfc3339 =
                year + "-" + pad(month) + "-" + pad(day) + "T" + pad(hour) + ":" + pad(minute) + ":" + pad(second) + "-06:00";
        return DateTime.parseRfc3339(Rfc3339);
    }
}
