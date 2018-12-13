package com.jevon.studentrollrecorder.pojo;

import java.util.HashMap;

/**
 * Created by jevon on 14-Apr-16.
 */

/*Not really a session object. More of an entry of a student for a session. Class necessary for FB
    to map data*/
public class Session{
    HashMap<String, Attendee> attendees;
    String date;


    public Session() {
    }

    public Session(HashMap<String, Attendee> attendees) {
//        attendees= new HashMap();
        this.attendees = attendees;
        this.date = "";

    }

    public HashMap<String, Attendee> getAttendees() {
        return attendees;
    }

    public void setAttendees(HashMap<String, Attendee> attendees) {
        this.attendees = attendees;
    }

    public String getDate(){
        return date;
    }
}