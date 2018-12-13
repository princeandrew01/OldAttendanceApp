package com.jevon.studentrollrecorder.helpers;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by jevon on 20-Apr-16.
 *
 * Helper class that provides some useful time related functions
 */
public class TimeHelper {

    //formats 24hr time to am/pm
    public static String formatTime(int hour, int minute){
        String am_pm = "am";
        if(hour>=12 && hour < 24){
            am_pm = "pm";
            hour = hour % 12;
        }
        if(hour == 0) hour = 12;
        DecimalFormat df = new DecimalFormat("00");
        return df.format(hour)+":"+df.format(minute)+" "+am_pm;
    }

    //generate an ID for a session by concatenating the current date and the time the session is scheduled to start
    public static String getIDTimeStamp(int startHr){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("EE dd-MM-yy", Locale.ENGLISH);
        return df.format(c.getTime())+" "+startHr;
    }

    public static int getCurrentHour(){
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.HOUR_OF_DAY);
    }

    public static int getCurrentMinute(){
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.MINUTE);
    }

    public static String getCurrDay(){
        Calendar c = Calendar.getInstance();
        return c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
    }

    public static int getMilitaryTime(int hour, int min){
        return hour*100 + min;
    }
}
