package com.jevon.studentrollrecorder.pojo;

/**
 * Created by Shiva on 4/16/2016.
 */
public class Attendee {
    int hr, min;

    public Attendee() {
    }

    public Attendee(int hr, int min) {
        this.hr = hr;
        this.min = min;
    }

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }
}
