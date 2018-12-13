package com.jevon.studentrollrecorder;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.jevon.studentrollrecorder.helpers.FirebaseHelper;
import com.jevon.studentrollrecorder.helpers.SortByDateHelper;
import com.jevon.studentrollrecorder.pojo.Attendee;
import com.jevon.studentrollrecorder.pojo.Session;
import com.jevon.studentrollrecorder.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;

/*This activity shows analytics for the course as a whole*/

public class ViewCourseAnalytics extends AppCompatActivity implements OnChartValueSelectedListener{

    private LineChart lineChart;
    private FirebaseHelper firebaseHelper;
    private Firebase ref;
    private Firebase studentRef;
    private Firebase sessionRef;
    private String courseCode;
    private String courseName;
    private int lateMarker=10;
    private long numStudents=0;
    private ArrayList<Session> sessions;
    private ArrayList<Entry> entriesAttendance;
    private ArrayList<Entry> entriesLateness;
    private ArrayList<String> labels;
    private EditText lateSetting;
    private LineData data;
    private LineDataSet dataset;            // will be used for attendance entries
    private LineDataSet dataset2;            // will be used for lateness entries

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lateSetting = (EditText)findViewById(R.id.late_setter);

        // create a new FirebaseHelper instance
        firebaseHelper = new FirebaseHelper();

        // set ref to point at the at this lecturers courses
        ref = firebaseHelper.getRef_id();

        // access data passed via bundle
        Bundle bundle = getIntent().getExtras();

        // pull out the course code and name

        if(bundle.containsKey(Utils.COURSE_CODE)){
            courseCode = bundle.getString(Utils.COURSE_CODE);
        }

        if(bundle.containsKey(Utils.COURSE_NAME)){
            courseName = bundle.getString(Utils.COURSE_NAME);
        }

        lineChart = (LineChart) findViewById(R.id.std_attend_line_chart);

        numStudentInCourse();
    }

    // obtain the number of students in this particular course so that chart can be set up properly
    private void numStudentInCourse(){
        // move directly to a reference to the students for this particular course
        studentRef = ref.child(courseCode).child(Utils.STUDENTS);

        // add a listener to student location in firebase that will be triggered only once when data there changes (initially). when triggered the value of the data will be accessible
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                numStudents = dataSnapshot.getChildrenCount();

                // we set up the line chart after we have received the number of students in the course
                setUpLineChart();
                getSessions();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void getSessions(){

        // move directly to a reference to the sessions data for this particular course
        sessionRef = ref.child(courseCode).child(Utils.SESSIONS);
        sessions = new ArrayList<>();

        // add a listener to session location in firebase that will be triggered when data there changes. when triggered the value of the data will be accessible
        sessionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // empty the currently stored session data to be replaced by updated data
                sessions.clear();
                for(DataSnapshot sesSnapshot: dataSnapshot.getChildren()) {
                    Session temp = sesSnapshot.getValue(Session.class);
                    sessions.add(temp);
                }
                populateLineChart();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    // this method will create the data points on the line chart from the sessions data
    private void createEntries(){
        int xCord=0;

        for(Session s : sessions){
            // add label of string version of date of this session
            labels.add(shortenDate(s.getDate()));

            // add entry indicating number of students present at each session
            entriesAttendance.add(new Entry((float)s.getAttendees().size(), xCord, toDate(s.getDate())));

            // add entry indicating number of students late at each session
            // default late time is 10 mins after scheduled start
            entriesLateness.add(new Entry((float)findNumLate(s.getAttendees(), lateMarker, s.getDate()), xCord++, toDate(s.getDate())));
        }
    }

    private Date toDate(String dateStr){
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd-MM-yy H", Locale.ENGLISH);
        Date sessionDate = new Date();

        try{
            sessionDate = dateFormat.parse(dateStr);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return sessionDate;
    }

    private String shortenDate(String longDate){
        String date = "";
        String token=null;
        int tokenNum=1;

        StringTokenizer stringTokenizer = new StringTokenizer(longDate,"-, ");

        while(stringTokenizer.hasMoreTokens()){
            token = stringTokenizer.nextToken();

            if(tokenNum == 2){
                date += token + "-";
            }

            else if(tokenNum == 3){
                date += token + " ";
            }

            else if(tokenNum == 5) {
                date += token;
            }

            tokenNum++;
        }
        return date;
    }

    // determine the number of students that arrived late for a particular session
    private int findNumLate(HashMap<String, Attendee> map, int lateMarker, String date){
        int numLate=0;

        int startHr = getStartHour(date);

        Collection collection = map.values();
        Iterator iterator = collection.iterator();

        while(iterator.hasNext()){
            Attendee temp = (Attendee)iterator.next();
            int arriveHr = temp.getHr();
            int arriveMin = temp.getMin();
            int lateBy=0;

            if(arriveHr - startHr < 0){
                // teacher recorded student before official start of time
                lateBy = 0;
            }

            else{
                lateBy += ((arriveHr-startHr) * 60) + arriveMin;
            }

            if(lateBy > lateMarker)
                numLate++;
        }

        return numLate;
    }

    public int getStartHour(String date){
        // consider replacing with String.split
        StringTokenizer stringTokenizer = new StringTokenizer(date);
        String token=null;

        while(stringTokenizer.hasMoreTokens()){
            token = stringTokenizer.nextToken();
        }

        return Integer.valueOf(token);
    }

    public void onLateUpdate(View view){
        String valueEntered = lateSetting.getText().toString();

        // get the late time entered in mins
        try{
            lateMarker = Integer.parseInt(valueEntered);
        }
        catch(Exception e){

        }

        // update chart
        populateLineChart();
    }

    private void populateLineChart(){

        entriesAttendance = new ArrayList<>();
        entriesLateness = new ArrayList<>();
        labels = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Collections.sort(sessions, new SortByDateHelper());

                createEntries();

                dataset = new LineDataSet(entriesAttendance, "# of Students Present");
                dataset.setDrawCircles(true);
                dataset.setDrawCubic(true);
                dataset.setDrawValues(false);
                dataset.setColor(Color.BLUE);
                dataset.setCircleColor(Color.GREEN);

                dataset2 = new LineDataSet(entriesLateness, "# of Students Late");
                dataset2.setDrawCircles(true);
                dataset2.setDrawCubic(true);
                dataset2.setDrawValues(false);
                dataset2.setColor(Color.RED);
                dataset2.setDrawFilled(true);
                dataset2.setFillColor(Color.RED);
                dataset2.setCircleColor(Color.GREEN);

                data = new LineData(labels, dataset);
                data.setValueTextColor(Color.BLUE);

                data.addDataSet(dataset2);
                data.notifyDataChanged();

                //add data
                lineChart.setData(data);

                //let the chart know its data has changed
                lineChart.notifyDataSetChanged();
            }
        }).start();

        // implicitly refreshes the chart
        lineChart.animateXY(2000,3000);
    }

    private void setUpLineChart(){

        lineChart.setOnChartValueSelectedListener(this);

        // descriptive text to explain to the user why there is no chart available
        lineChart.setNoDataTextDescription("No Student Attendance Data Available.");
        // descriptive text that appears in the bottom right corner of the chart
        lineChart.setDescription("Student Attendance");
        lineChart.setDescriptionColor(Color.BLACK);

        // enable touch gestures
        lineChart.setTouchEnabled(true);

        // enable scaling and dragging
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

        lineChart.setDrawGridBackground(true);

        // both axes can be scaled simultaneously on zoom
        lineChart.setPinchZoom(true);

        // set an alternative background color
        lineChart.setBackgroundColor(Color.BLACK);

        data = new LineData();

        // add empty data
        lineChart.setData(data);

        // get the legend
        Legend legend = lineChart.getLegend();

        // modify the legend
        legend.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextColor(Color.WHITE);

        // set up Y axis

        YAxis leftAxis = lineChart.getAxisLeft();
        // set the max value on Y axis to the number of students in the course
        leftAxis.setAxisMaxValue((float)numStudents);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setTextColor(Color.WHITE);

        // indicate the number of students registered for course as a line on the y axis
        LimitLine maxStudents = new LimitLine((float)(numStudents), "# registered");
        maxStudents.setLineColor(Color.GREEN);
        maxStudents.setTextColor(Color.BLACK);
        maxStudents.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
        maxStudents.setTextSize(5);
        leftAxis.addLimitLine(maxStudents);

        YAxis rightAxis = lineChart.getAxisRight();
        // do not draw this
        rightAxis.setEnabled(false);

        // set up X axis
        XAxis xl = lineChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setSpaceBetweenLabels(5);
        xl.setEnabled(true);

    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Date dateSelected = (Date)e.getData();

        if(dataSetIndex == data.getIndexOfDataSet(dataset)){
            String message = "Attendance is: " + e.getVal() + " on " + dateSelected.toString();
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        }

        else{
            String message = "Late arrivals are: " + e.getVal() + " on " + dateSelected.toString();
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onNothingSelected() {

    }
}