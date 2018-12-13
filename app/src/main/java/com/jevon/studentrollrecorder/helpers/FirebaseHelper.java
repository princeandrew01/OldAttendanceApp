package com.jevon.studentrollrecorder.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.Firebase;
import com.jevon.studentrollrecorder.pojo.Attendee;
import com.jevon.studentrollrecorder.pojo.Course;
import com.jevon.studentrollrecorder.pojo.Lecture;
import com.jevon.studentrollrecorder.pojo.Student;
import com.jevon.studentrollrecorder.utils.MyApplication;
import com.jevon.studentrollrecorder.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jevon on 11-Apr-16.
 */

/*Somewhat of a wrapper for firebase
* All operations could not be abstracted to this class due to the callbacks that firebase query methods use*/
public class FirebaseHelper {
    private String uid;
    private Firebase ref, ref_id;
    private MyApplication myApplication;

    public FirebaseHelper(){
        myApplication = MyApplication.getInstance();
        ref = myApplication.getRef();
        uid = myApplication.getSharedPreferences(Utils.SHAREDPREF, Context.MODE_PRIVATE).getString(Utils.ID,null);
        /*keep a reference to the json tree from the point of the lecturer ID
        since all DB access for a given lecturer would occur from this point*/
        ref_id = ref.child(Utils.LECTURERS).child(uid);
    }

    public Firebase getRef(){ return ref;}

    public Firebase getRef_id() {
        return ref_id;
    }

    public void addCourse(Course course){
        ref_id.child(course.getCourseCode()).setValue(course);
    }

    //Adds a list of lectures to the DB
    public void addLectures(String courseCode, ArrayList<Lecture> lectures){
        ObjectMapper m = new ObjectMapper();
        Map<String,Object> sesMap = new HashMap<>();
        for (Lecture l : lectures) {
                try{
                    sesMap = m.convertValue(l, HashMap.class);
                    ref_id.child(courseCode).child(Utils.LECTURES).child(l.getDay().substring(0,3)+" "+l.getStartHr()).setValue(sesMap);
                    Log.i("FBH","added lecture");
                }catch (IllegalArgumentException e){e.printStackTrace();}
        }
    }

    //records a student as present in the DB
    public  void markAsPresent(String courseCode, String session_id, String student_id, String student_name){
        int hour = TimeHelper.getCurrentHour();
        int minute = TimeHelper.getCurrentMinute();

        Attendee a = new Attendee(hour,minute);
        ObjectMapper m = new ObjectMapper();
        Map<String, Object> attendeeMap = new HashMap<>();

        try {
            attendeeMap = m.convertValue(a, HashMap.class);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        ref_id.child(courseCode).child(Utils.SESSIONS).child(session_id).child(Utils.DATE).setValue(session_id);
        ref_id.child(courseCode).child(Utils.SESSIONS).child(session_id).child(Utils.ATTENDEES).child(student_id).setValue(attendeeMap);
        broadcastAddStudent(student_id, student_name);
    }


    //broadcasts that a student was recorded as present so the list in the main activity can be updated
    public void addStudentToClass(String courseCode, String studentName, String studentID){
        Student student = new Student(studentID,studentName);
        ObjectMapper m = new ObjectMapper();
        Map<String, Object> studentMap = new HashMap<>();
        try{
            studentMap = m.convertValue(student, HashMap.class);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        ref_id.child(courseCode).child(Utils.STUDENTS).child(studentID).setValue(studentMap);
    }

    public String getUID(){
        return this.uid;
    }


    public void broadcastAddStudent(String studentId, String studentName){
        Intent i = new Intent();
        i.setAction("ADD.STUDENT.TO.LISTVIEW");
        Bundle b = new Bundle();
        b.putString(Utils.ID, studentId);
        b.putString(Utils.NAME, studentName);
        i.putExtras(b);
        Context c = myApplication.getApplicationContext();
        c.sendBroadcast(i);
    }
}
