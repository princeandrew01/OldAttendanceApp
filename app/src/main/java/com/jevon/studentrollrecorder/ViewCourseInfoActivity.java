package com.jevon.studentrollrecorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jevon.studentrollrecorder.utils.Utils;

/*This activity allows the lecturer to choose from various details about the course to be viewed */

public class ViewCourseInfoActivity extends AppCompatActivity {

    private Bundle bundle;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_course_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bundle = getIntent().getExtras();
        String code = bundle.getString(Utils.COURSE_CODE);
        ActionBar bar = getSupportActionBar();
        bar.setTitle(code);

        setUpListView();
    }

    private void setUpListView(){
        String[] entries = getResources().getStringArray(R.array.course_info_list);
        ArrayAdapter<String> adapter = new ArrayAdapter(this, R.layout.layout_listview_item_lg, entries);
        listView = (ListView) findViewById(R.id.course_info_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    start(ViewLecturesActivity.class);
                }
                else if(position == 1){
                    start(ViewCourseAnalytics.class);
                }
                else if(position == 2){
                    start(ViewStudentAnalyticsActivity.class);
                }
            }
        });
    }

    public void start(Class className){
        Intent intent = new Intent(ViewCourseInfoActivity.this, className);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
