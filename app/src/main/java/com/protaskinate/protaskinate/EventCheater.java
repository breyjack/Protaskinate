package com.protaskinate.protaskinate;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.util.Random;

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

    // Overload for if caller wants to use EventCheater.makeEventDateTime
    public static Event makeEvent(String description, EventDateTime start, EventDateTime end)
    {
        Event e = new Event();
        e.setDescription(description);
        e.setStart(start);
        e.setEnd(end);

        return e;
    }

    private static String pad(Integer i){
        return pad(i, 2);
    }

    private static String pad(Integer i, int places) {
        if (i.toString().length() < places) {
            String r = "0";
            for (int z = i.toString().length() + 1; z < places; z++)
                r += "0";
            r += i;
            return r;
        }
        else
            return i.toString();
    }

    public static EventDateTime makeEventDateTime(Integer year, Integer month, Integer day,
                                        Integer hour, Integer minute, Integer second) {
        // "2016-10-12T12:58:58.875-06:00" is what
        // came from new DateTime().toStringRfc3339()
        // so it's a valid format. Rfc3339 is messy.
        String Rfc3339 =
                pad(year, 4) + "-" + pad(month) + "-" + pad(day) + "T" + pad(hour) + ":" + pad(minute) + ":" + pad(second) + "-06:00";
        DateTime parsed = DateTime.parseRfc3339(Rfc3339);
        EventDateTime edt = new EventDateTime();
        edt.setDateTime(parsed);
        return edt;
    }

    private static Random r;
    public static EventDateTime randomEventDateTime(){

        if (r == null)
            r = new Random();

        // Pick a random time in October 2016
        // The random +1s here are needed because Random.nextInt returns a number
        // between 0 (inclusive) and the number its passed (exclusive).
        return EventCheater.makeEventDateTime(2016, 10, r.nextInt(31) + 1,
                r.nextInt(24), r.nextInt(60), r.nextInt(60));
    }


    public static Event[] makeRandomEvents(int howMany){
        Event[] events = new Event[howMany];

        for (int i = 0; i < howMany; i++) {
            EventDateTime d1 = EventCheater.randomEventDateTime();
            EventDateTime d2 = EventCheater.randomEventDateTime();
            events[i] = new Event();
            if (d1.getDateTime().getValue() < d2.getDateTime().getValue()) {
                events[i].setStart(d1);
                events[i].setEnd(d2);
            }
            else {
                events[i].setStart(d2);
                events[i].setEnd(d1);
            }
        }
        return events;
    }
}
