package com.jevon.studentrollrecorder;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.jevon.studentrollrecorder.service.IdCheckService;
import com.jevon.studentrollrecorder.service.IdServiceBinder;
import com.jevon.studentrollrecorder.utils.Utils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_SCANNER = 1;
    private static final int PERMISSION_CAMERA = 2;
    private ListView lv_present_students;
    private ArrayList<String> students;
    private ArrayAdapter<String> adapter;
    private IdCheckService idCheckService;
    private boolean isBound;
    private static final String TAG = "Main";
    private Intent i;
    private IntentFilter intentFilter;
    private String studentIDScanned = null;

    /* Andrew Comment */
    private ServiceConnection idCheckServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            IdServiceBinder idServiceBinder = (IdServiceBinder) binder;
            idCheckService = idServiceBinder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setUpListView();
        intentFilter = new IntentFilter();
        intentFilter.addAction("SHOW.SNACKBAR.ADD.STUDENT");
        intentFilter.addAction("ADD.STUDENT.TO.LISTVIEW");
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (intent.getAction().equals("SHOW.SNACKBAR.ADD.STUDENT")) {
                if (studentIDScanned != null) {
                    Snackbar.make(findViewById(android.R.id.content), "Would you like to add this student to the system?", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Yes", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Bundle receivedData = intent.getExtras();
                                    Bundle bundle = new Bundle();
                                    bundle.putString(Utils.ID, studentIDScanned);
                                    bundle.putString(Utils.COURSE_CODE, receivedData.get("courseCode").toString());
                                    bundle.putString(Utils.SESSIONS, receivedData.get("sessionID").toString());
                                    Intent addStudentIntent = new Intent(MainActivity.this, AddStudentToSystem.class);
                                    addStudentIntent.putExtras(bundle);
                                    startActivity(addStudentIntent);
                                }
                            })
                            .show();
                }
            }
            else if(intent.getAction().equals("ADD.STUDENT.TO.LISTVIEW")){
                Bundle recievedData = intent.getExtras();
                String studentID = recievedData.get(Utils.ID).toString();
                String studentName = recievedData.get(Utils.NAME).toString();
                if(!students.contains(studentID + " - " + studentName) ) {
                    students.add(0, studentID + " - " + studentName);
                    adapter.notifyDataSetChanged();
                }

            }
        }
    };

    public void onResume(){
        super.onResume();
        registerReceiver(mReceiver,intentFilter);
    }

    public void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onStart() {
        isBound = true;
        i = new Intent(MainActivity.this,IdCheckService.class);
        bindService(i, idCheckServiceConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onStop() {
        isBound = false;
        unbindService(idCheckServiceConnection);
        super.onStop();
    }


    //This list views shows recently recorded students. The clear all icon in the actionbar lets you clear the list
    private void setUpListView(){
        lv_present_students = (ListView) findViewById(R.id.lv_present);
        students = new ArrayList<>();
        adapter= new ArrayAdapter<>(MainActivity.this,R.layout.layout_listview_item_med,students);
        lv_present_students.setAdapter(adapter);
    }

    //Receives results of scanning and communicated with the service
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SCANNER && resultCode == RESULT_OK) {
            String results = data.getStringExtra("results");
            studentIDScanned = results;
            Log.i(TAG,"scanned: "+results);
            if(!isBound)
                bindService(i, idCheckServiceConnection, Context.BIND_AUTO_CREATE);
            idCheckService.processStudent(results);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == PERMISSION_CAMERA){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                startActivityForResult(new Intent(MainActivity.this, Scanner.class), REQUEST_SCANNER);
             else
                Toast.makeText(this,"Camera permissions must be granted", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_view_courses) {
            startActivity(new Intent(MainActivity.this, ViewCoursesActivity.class));
            return true;
        }
        else if (id == R.id.action_add_course) {
            startActivity(new Intent(MainActivity.this, AddCourseActivity.class));
            return true;
        }
        else if (id == R.id.action_clear_list) {
            adapter.clear();
            return true;
        }
        else if (id == R.id.action_log_out) {
            SharedPreferences sp = getSharedPreferences(Utils.SHAREDPREF, MODE_PRIVATE);
            SharedPreferences.Editor spe = sp.edit();
            spe.putBoolean(Utils.LOGGED_IN, false);
            spe.remove(Utils.ID);
            spe.apply();
            finish();
            startActivity(new Intent(MainActivity.this,SignInActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void scanCode(View v){
        if(v == findViewById(R.id.fab_scan)){
            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                startActivityForResult(new Intent(MainActivity.this, Scanner.class), REQUEST_SCANNER);
            else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSION_CAMERA);
            }
        }
    }
}
