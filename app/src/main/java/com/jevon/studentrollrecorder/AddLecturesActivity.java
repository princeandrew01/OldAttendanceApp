package com.jevon.studentrollrecorder;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.jevon.studentrollrecorder.helpers.FirebaseHelper;
import com.jevon.studentrollrecorder.helpers.TimeHelper;
import com.jevon.studentrollrecorder.pojo.Course;
import com.jevon.studentrollrecorder.pojo.Lecture;
import com.jevon.studentrollrecorder.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/*This activity allows the lecturer to add lectures to a course*/

public class AddLecturesActivity extends AppCompatActivity {
    private String name;
    private String code;
    private Spinner spinner_day;
    private TextView tv_start, tv_end;
    private String day;
    private ListView lv_sessions;
    private ArrayAdapter<Lecture> adapter;
    private ArrayList<Lecture> lectures;
    private Context context;
    private ArrayList<Course> courses;
    private static final String TAG = "Add lecture activity";
    private static final String NONE = "none";
    private int startHr = -1, startMin = -1, endHr = -1, endMin = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lectures);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;
        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            code = extras.getString(Utils.COURSE_CODE);
            name = extras.getString(Utils.COURSE_NAME);
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null)
                actionBar.setTitle(code);
        }
        getCoursesFromFB();
        setUpSpinner();
        setUpTextViews();
        setUpListView();

    }
    
    //get the list of the user's courses from the DB. Needed to check for clashes
    public void getCoursesFromFB(){
        FirebaseHelper fh = new FirebaseHelper();
        Firebase ref_id = fh.getRef_id();
        ref_id.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                courses = new ArrayList<>();
                for (DataSnapshot postSnapshot: snapshot.getChildren()){
                    Course c = postSnapshot.getValue(Course.class);
                    Log.i("Course received", c.toString());
                    courses.add(c);
                }
            }
            @Override public void onCancelled(FirebaseError error) {
                Log.e(TAG,"The read failed: " + error.getMessage());
            }
        });
    }

    /*When the floating action button is clicked
    * If all fields are filled out and there is no clash
    *   then the course is added to a list to be pushed when u user clicked the save icon*/
    public void onClickAddLect(View v) {
        if (v.getId() == R.id.fab_save){
            if(day != null && startHr != -1 && startMin != -1 && endHr != -1 && endMin != -1){
                if(TimeHelper.getMilitaryTime(endHr,endMin) - TimeHelper.getMilitaryTime(startHr,startMin) >= 100){
                    String clashedWith = isClashing(startHr, endHr, startMin, endMin);
                    if(clashedWith.equals(NONE)){
                        lectures.add(0,new Lecture(startHr,startMin,endHr,endMin,day));
                        adapter.notifyDataSetChanged();
                        resetFields();
                    }
                    else Snackbar.make(lv_sessions, "Clash: " + clashedWith, Snackbar.LENGTH_LONG).show();
                }
                else Snackbar.make(lv_sessions,"Ensure that start time precede end time by at least 1 hour",Snackbar.LENGTH_LONG).show();
            }
            else Snackbar.make(lv_sessions,"Select a day, start time and end time",Snackbar.LENGTH_LONG).show();
        }
    }
    
    //check for clashes with other courses
    private String isClashing(int startHr, int endHr, int startMin, int endMin){
        if(courses.size() <= 0){
            Toast.makeText(this, "Could not check for clashes",Toast.LENGTH_LONG).show();
        }
        else{
            int proposed_starttime = TimeHelper.getMilitaryTime(startHr, startMin);
            int proposed_endtime = TimeHelper.getMilitaryTime(endHr, endMin);
            for(Course c: courses){
                HashMap<String,Lecture> lectures = c.getLecturess();
                if(lectures!=null)
                    for(Lecture l: lectures.values()){
                        int lect_start = TimeHelper.getMilitaryTime(l.getStartHr(), l.getStartMin());
                        int lect_end = TimeHelper.getMilitaryTime(l.getEndHr(), l.getEndMin());
                        if(lect_start <= proposed_starttime && proposed_starttime < lect_end && l.getDay().equals(day))
                            return c.getCourseCode() + "[" + TimeHelper.formatTime(l.getStartHr(),l.getStartMin()) + " - " + TimeHelper.formatTime(l.getEndHr(),l.getEndMin()) +"]";
                        if(proposed_starttime <= lect_start && lect_start < proposed_endtime && l.getDay().equals(day))
                            return c.getCourseCode() + "[" + TimeHelper.formatTime(l.getStartHr(),l.getStartMin()) + " - " + TimeHelper.formatTime(l.getEndHr(),l.getEndMin()) +"]";
                    }
            }
        }
        return NONE;
    }

    private void setUpListView(){
        lv_sessions = (ListView) findViewById(R.id.lv_sessions);
        lectures = new ArrayList<>();
        adapter = new ArrayAdapter<>(context,R.layout.layout_listview_item_med, lectures);
        lv_sessions.setAdapter(adapter);
    }

    private void setUpTextViews(){
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int which;
                if(v.getId() == R.id.tv_start) which = 1;
                else which = 2;
                showTimePicker(which);
            }
        };
        tv_start = (TextView) findViewById(R.id.tv_start);
        tv_end = (TextView) findViewById(R.id.tv_end);
        tv_start.setOnClickListener(listener);
        tv_end.setOnClickListener(listener);
    }

    private void setUpSpinner(){
        spinner_day = (Spinner) findViewById(R.id.spinner_day);
        final String[] days = new String[]{"Select a Day","Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.layout_spinner_item, days);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_day.setAdapter(adapter);

        spinner_day.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position > 0) {
                    day = days[position];
                }
                else
                    day = null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showTimePicker(final int which){
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        Dialog d = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if(which == 1){
                    startHr = hourOfDay;
                    startMin = minute;
                    tv_start.setText(TimeHelper.formatTime(hourOfDay,minute));
                }
                else if(which == 2) {
                    endHr = hourOfDay;
                    endMin = minute;
                    tv_end.setText(TimeHelper.formatTime(hourOfDay, minute));
                }
            }
        }, hour, minute, false);
        d.setCancelable(true);
        d.show();
    }

    private void resetFields() {
        tv_end.setText("End");
        tv_start.setText("Start");
        spinner_day.setSelection(0);
        startHr =  startMin =  endHr =  endMin = -1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_lectures, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save_all) {
            saveAllLectures();
            return true;
        }
        else if (id == R.id.action_clear_list) {
            adapter.clear();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //saves the list of lectures to the DB
    private void saveAllLectures(){
        if(lectures.size() > 0){
            FirebaseHelper fh = new FirebaseHelper();
            fh.addLectures(code, lectures);
            Snackbar.make(lv_sessions, "Successfully add "+ lectures.size()+" lectures", Snackbar.LENGTH_LONG).show();
            adapter.clear();
        }
        else {
            Snackbar.make(lv_sessions, "No lectures to be added", Snackbar.LENGTH_LONG).show();
        }

    }
}
