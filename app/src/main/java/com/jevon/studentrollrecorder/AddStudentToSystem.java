package com.jevon.studentrollrecorder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jevon.studentrollrecorder.helpers.FirebaseHelper;
import com.jevon.studentrollrecorder.utils.Utils;

/*When an id is scanned and a course is on the way and the student isn't registered for the course
* the lecturer is given the option to add the student to the class listing
 * If he chooses yes this activity is launched and he is prompted to enter the Students name*/

public class AddStudentToSystem extends AppCompatActivity {
    private EditText userNameET;
    private TextView userIDTV;
    private String studentID, courseID, sessionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student_to_system);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loadStudentInfo();
        setupViews();
    }

    public void loadStudentInfo(){
        Bundle studentData = getIntent().getExtras();
        studentID = studentData.getString(Utils.ID);
        courseID = studentData.getString(Utils.COURSE_CODE);
        sessionID = studentData.getString(Utils.SESSIONS);
    }

    public void setupViews(){
        userIDTV = (TextView)findViewById(R.id.tv_studentIDField);
        if(userIDTV != null) userIDTV.setText(studentID);
        userNameET = (EditText)findViewById(R.id.et_StudentName);
    }

    public void addStudentToSystem(View view){
        String userName = userNameET.getText().toString();

        FirebaseHelper fh = new FirebaseHelper();
        fh.markAsPresent(courseID, sessionID, studentID, userName);
        fh.addStudentToClass(courseID,userName,studentID);
    }

}