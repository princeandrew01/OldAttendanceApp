package com.jevon.studentrollrecorder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.jevon.studentrollrecorder.helpers.FirebaseHelper;
import com.jevon.studentrollrecorder.pojo.Attendee;
import com.jevon.studentrollrecorder.pojo.Course;
import com.jevon.studentrollrecorder.pojo.Lecture;
import com.jevon.studentrollrecorder.pojo.Session;
import com.jevon.studentrollrecorder.utils.Utils;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;

/*This activity shows analytics for a selected student*/

public class ViewIndividualStudentAnalytics extends AppCompatActivity {

    private String courseCode;
    private String courseName;
    private ArrayList<Session> sessions;
    private String studentId;
    private String studentName;

    private Course course;

    /* variables for analytics. */
    private int totalSessionsAttended = 0;
    private int totalSessionsNotAttended = 0;
    private int totalSessions = 0;

    private int earlySessions;
    private int lateSessions;

    /* set default late time for second graph as 15 mins. */
    private int lateTime = 15;

    PieChart attendancePieChart = null;
    PieChart punctualityPieChart = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_individual_student_analytics);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* retrieve data from the intent. */
        Bundle bundle = getIntent().getExtras();

        if(bundle != null){
            courseCode = bundle.getString(Utils.COURSE_CODE);
            courseName = bundle.getString(Utils.COURSE_NAME);
            studentId = bundle.getString(Utils.ID);
            studentName = bundle.getString(Utils.NAME);
        }

        /* Draw charts. */
        drawAttendancePieChart();
        drawPunctualityPieChart();

        /* get the course from firebase. */
        getCourse();

        /* display basic student and course information. */
        studentInfo();
        courseInfo();

    }

    public void studentInfo(){
        TextView student = (TextView) findViewById(R.id.txt_student);
        student.setText(studentName + " (" + studentId + ")");
    }

    public void courseInfo(){
        TextView course = (TextView) findViewById(R.id.txt_course);
        course.setText(courseName + " (" + courseCode + ")");
    }

    public void attendanceCalculations(){

        /* initialize variables.  */
        this.totalSessions  = 0;
        this.totalSessionsNotAttended = 0;
        this.totalSessionsAttended = 0;

        /* get the sessions from the course. */
        HashMap<String, Session> sessions = course.getSessions();

        /* checks the total number of sessions stored in the hashmap. */
        this.totalSessions =  sessions.size();

        /* iterate over each session. */
        for(HashMap.Entry<String, Session> currentSession : sessions.entrySet()){

            /* check if the student we are interested in attended this session. */
            if(currentSession.getValue().getAttendees().get(studentId) != null){
                totalSessionsAttended++;
            }
            else{
                totalSessionsNotAttended++;
            }
        }

        updateAttendancePieChart();
    }

    public void updateAttendancePieChart(){
        /* create an arraylist of entries to enter. these are values on piechart. */
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(totalSessionsAttended, 0));
        entries.add(new Entry(totalSessionsNotAttended,1));

        /* convert to pieDataSet. */
        PieDataSet dataset = new PieDataSet(entries, "Sessions");
        dataset.setColors(ColorTemplate.COLORFUL_COLORS);
        /* create an arraylist of labels for piechart. */
        ArrayList<String> labels = new ArrayList<String>();
        labels.add("Attended");
        labels.add("Missed");

        PieData data = new PieData(labels, dataset);

        this.attendancePieChart.setData(data);
        this.attendancePieChart.notifyDataSetChanged();
        this.attendancePieChart.invalidate();
        this.attendancePieChart.animateY(1500);
    }

    public void drawAttendancePieChart(){

        /* text in the center of graph. */
        SpannableString centerText = new SpannableString("Percentage Attendance");

        /* get pie chart view from ui. */
        this.attendancePieChart = (PieChart) findViewById(R.id.attendance_graph);

        /* create an arraylist of entries to enter. these are values on piechart. */
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(totalSessionsAttended, 0));
        entries.add(new Entry(totalSessionsNotAttended,1));

        /* convert to pieDataSet. */
        PieDataSet dataset = new PieDataSet(entries, "Sessions");
        dataset.setColors(ColorTemplate.COLORFUL_COLORS);
        /* create an arraylist of labels for piechart. */
        ArrayList<String> labels = new ArrayList<String>();
        labels.add("Attended");
        labels.add("Missed");

        PieData data = new PieData(labels, dataset);
        this.attendancePieChart.setCenterText(centerText);
        this.attendancePieChart.setUsePercentValues(true);
        this.attendancePieChart.setData(data);

        this.attendancePieChart.setDescription("Breakdown of Student Attendance.");
    }

    public void updatePunctualityPieChart(){
        /* create an arraylist of entries to enter. these are values on piechart. */
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(earlySessions, 0));
        entries.add(new Entry(lateSessions,1));

        /* convert to pieDataSet. */
        PieDataSet dataset = new PieDataSet(entries, "Sessions");
        dataset.setColors(ColorTemplate.COLORFUL_COLORS);
        /* create an arraylist of labels for piechart. */
        ArrayList<String> labels = new ArrayList<String>();
        labels.add("Early");
        labels.add("Late");

        PieData data = new PieData(labels, dataset);

        this.punctualityPieChart.setData(data);
        this.punctualityPieChart.notifyDataSetChanged();
        this.punctualityPieChart.invalidate();
        this.punctualityPieChart.animateY(1500);
    }



    public void drawPunctualityPieChart(){

        /* text in the center of graph. */
        SpannableString centerText = new SpannableString("Punctuality Percentage");

        /* get pie chart view from ui. */
        this.punctualityPieChart = (PieChart) findViewById(R.id.punctuality_graph);

        /* create an arraylist of entries to enter. these are values on piechart. */
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(earlySessions, 0));
        entries.add(new Entry(lateSessions,1));

        /* convert to pieDataSet. */
        PieDataSet dataset = new PieDataSet(entries, "Sessions");
        dataset.setColors(ColorTemplate.COLORFUL_COLORS);
        /* create an arraylist of labels for piechart. */
        ArrayList<String> labels = new ArrayList<String>();
        labels.add("Early");
        labels.add("Late");

        PieData data = new PieData(labels, dataset);
        this.punctualityPieChart.setCenterText(centerText);
        this.punctualityPieChart.setUsePercentValues(true);
        this.punctualityPieChart.setData(data);

        this.punctualityPieChart.setDescription("Breakdown of Student Punctuality");
    }

    public void getCourse(){
        /* get a firebase helper. */
        FirebaseHelper firebaseHelper = new FirebaseHelper();

        /* get a ref to the lecturer courses. */
        Firebase ref = firebaseHelper.getRef_id();

        /* narrow down to the course we are interested in . */
        Firebase courseRef = ref.child(courseCode);

        /* get the course from firebase and store it locally. */
        courseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /* load in the course. */
                course = dataSnapshot.getValue(Course.class);

                if(course.getSessions() != null && course.getLecturess() != null){
                    attendanceCalculations();
                    punctualityCalculations();
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void punctualityCalculations(){

        /* initialize data. */
        lateSessions = 0;
        earlySessions = 0;

        /* get the hashmap of sessions from the course. */
        HashMap<String, Session> sessions = course.getSessions();

        /* iterate over each session. */
        for(HashMap.Entry<String, Session> session : sessions.entrySet()){

            /* check if this student attended the session. */
            Attendee attendee = session.getValue().getAttendees().get(studentId);

            /* the student attended. */
            if(attendee != null){
                /* when the student arrived */
                int studentHrArrival = attendee.getHr();
                int studentMinsArrival = attendee.getMin();

                /* using outdated function, look for an update.  */
                Time studentArrivalTime = new Time(studentHrArrival, studentMinsArrival, 0);

                /* we need to figure out when the session started, we need the session key to find the lecture. */
                String sessionKey = (String) session.getKey();

                /* trim the spaces */
                sessionKey = sessionKey.trim();

                /* divide key into parts to identify lecture. */
                String[] keyParts = sessionKey.split("\\s+");

                /* build string to indentify the lecture. */
                String lectureKey = keyParts[0].substring(0,3) + " " + keyParts[2];

                /* get the lecture object from lecturekey. */
                Lecture lecture = course.getLecturess().get(lectureKey);

                /* make sure the lecture exist. */
                if(lecture != null){


                    /* get lecture start time. */
                    Time lectureStartTime = new Time(lecture.getStartHr(), lecture.getStartMin(), 0);

                    /* if the student comes before the lecture, early.  */
                    if(studentArrivalTime.compareTo(lectureStartTime) < 0){
                        earlySessions++;
                    }
                    else{

                        int lateLectureHr = lecture.getStartHr();
                        int lateLectureMin = lecture.getStartMin();

                        int lateHours = 0;
                        int lateMins = 0;
                        lateHours = lateTime / 60;
                        lateMins = lateTime % 60;

                        if(lateLectureMin + lateMins < 60){
                            lateLectureMin = lateLectureMin + lateMins;
                        }
                        else{
                            lateLectureMin = lateLectureMin + lateMins - 60;
                            lateLectureHr++;
                        }

                        lateLectureHr += lateHours;

                        Time lateLectureTime = new Time(lateLectureHr, lateLectureMin, 0);
                        /* check if student comes before. */
                        if(studentArrivalTime.compareTo(lateLectureTime) < 0){
                            earlySessions++;
                        }
                        else{
                            lateSessions++;
                        }
                    }
                }
            }
        }

        updatePunctualityPieChart();
    }

    public void updateLateGraph(View view){

        /* get edit text view. */
        EditText late_edit = (EditText) findViewById(R.id.edit_txt_late_time);

        /* get the late time entered in mins. */
        try{
            this.lateTime = Integer.parseInt(late_edit.getText().toString());
        }
        catch(Exception e){
            /* toast for invalid number.*/
            Toast.makeText(this, "You did not enter a valid number", Toast.LENGTH_LONG).show();
        }

        /* update chart. */
        punctualityCalculations();
    }
}
