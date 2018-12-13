package com.jevon.studentrollrecorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.jevon.studentrollrecorder.helpers.FirebaseHelper;
import com.jevon.studentrollrecorder.pojo.Lecture;
import com.jevon.studentrollrecorder.utils.Utils;

import java.util.ArrayList;

/*This activity shows the current list of lecture times for a course */

public class ViewLecturesActivity extends AppCompatActivity {
    private ArrayList<Lecture> lectures;
    private ArrayAdapter<Lecture> adapter;
    private ListView lv_view_sessions;
    private String courseCode, courseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_lectures);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            courseCode = extras.getString(Utils.COURSE_CODE);
            courseName = extras.getString(Utils.COURSE_NAME);
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null)
                actionBar.setTitle(courseCode);
            setUpListView();
        }
    }

    private void setUpListView(){
        lv_view_sessions = (ListView) findViewById(R.id.lv_view_sessions);
        lectures = new ArrayList<>();
        adapter = new ArrayAdapter<>(ViewLecturesActivity.this,R.layout.layout_listview_item_lg, lectures);
        getLectures();
        lv_view_sessions.setAdapter(adapter);
    }

    public void getLectures(){
        FirebaseHelper fh = new FirebaseHelper();
        Firebase ref_ses = fh.getRef_id().child(courseCode).child(Utils.LECTURES);
        ref_ses.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                adapter.clear();
                Log.e("snapshot", snapshot.toString());
                for (DataSnapshot lectSnapshot: snapshot.getChildren()){
                    Lecture s = lectSnapshot.getValue(Lecture.class);
                    Log.e("Course received", s.toString());
                    adapter.add(s);
                }
            }
            @Override public void onCancelled(FirebaseError error) {
                System.out.println("The read failed: " + error.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_sessions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_sessions) {
            Intent i = new Intent(ViewLecturesActivity.this, AddLecturesActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(Utils.COURSE_CODE,courseCode);
            bundle.putString(Utils.COURSE_NAME,courseName);
            i.putExtras(bundle);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
