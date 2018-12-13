package com.jevon.studentrollrecorder;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.jevon.studentrollrecorder.helpers.FirebaseHelper;
import com.jevon.studentrollrecorder.pojo.Course;
import com.jevon.studentrollrecorder.utils.Utils;
/*
* This activity allows the lecturer to add a new course to his profile then gives him/her the option
* to set lecture times for that course*/

public class AddCourseActivity extends AppCompatActivity {
    private EditText et_code, et_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        et_code = (EditText) findViewById(R.id.et_course_code);
        et_name = (EditText) findViewById(R.id.et_course_name);
    }

    private void saveCourse() {
        final String name = et_name.getText().toString().toUpperCase();
        final String code = et_code.getText().toString().toUpperCase();
        if (!name.matches(".*[a-zA-Z]+.*") || !code.matches(".*[a-zA-Z]+.*")) {
            Snackbar.make(et_code, "You must enter a course name and course code.", Snackbar.LENGTH_LONG).show();
        } else {
            Course c = new Course(code, name);
            FirebaseHelper fh = new FirebaseHelper();
            fh.addCourse(c);
            Snackbar.make(et_code, "Created " + c.toString(), Snackbar.LENGTH_INDEFINITE)
                    .setAction("Add Lectures", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle b = new Bundle();
                            b.putString(Utils.COURSE_CODE, code);
                            b.putString(Utils.COURSE_NAME, name);
                            startActivity((new Intent(AddCourseActivity.this, AddLecturesActivity.class)).putExtras(b));
                        }
                    }).show();
            et_code.setText("");
            et_name.setText("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_course, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            saveCourse();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}