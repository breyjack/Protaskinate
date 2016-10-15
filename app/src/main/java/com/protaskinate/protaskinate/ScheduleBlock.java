package com.protaskinate.protaskinate;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

/**
 * Created by michael on 10/13/16.
 */

public class ScheduleBlock {

    DateTime start;
    DateTime end;

    public DateTime getStart(){
        return start;
    }

    public DateTime getEnd(){
        return end;
    }

    public void setStart(DateTime newStart){
        if (end.getValue() < newStart.getValue()) {
            start = end;
            end = newStart;
        }
        else {
            start = newStart;
        }
    }

    public void setEnd(DateTime newEnd){
        if (start.getValue() > newEnd.getValue()){
            end = start;
            start = newEnd;
        }
        else{
            end = newEnd;
        }
    }


    public DateTime getLength(){
        return new DateTime(end.getValue() - start.getValue());
    }


    public ScheduleBlock(EventDateTime edt){

    }
}
