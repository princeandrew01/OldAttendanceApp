package com.jevon.studentrollrecorder.adapter;

/**
 * Created by jevon on 19-Apr-16.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.jevon.studentrollrecorder.R;
import com.jevon.studentrollrecorder.ViewIndividualStudentAnalytics;
import com.jevon.studentrollrecorder.pojo.Student;
import com.jevon.studentrollrecorder.utils.Utils;

import java.util.ArrayList;

/**
 * Created by jevon on 19-Apr-16.
 */
public class StudentsAdapter extends RecyclerView.Adapter<StudentsAdapter.StudentViewHolder> {
    private static ArrayList<Student> students;
    private static String courseCode, courseName;
    private static Context context;



    public StudentsAdapter(Context context, ArrayList<Student> students, String courseCode, String courseName){
        this.students = students;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.context = context;
    }

    @Override
    public StudentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_student, parent, false);
        StudentViewHolder svh = new StudentViewHolder(v);
        return svh;
    }

    @Override
    public void onBindViewHolder(StudentViewHolder holder, int position) {
        String firstLetter = String.valueOf(students.get(position).getName().charAt(0)).toUpperCase();
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(position);
        TextDrawable drawable = TextDrawable.builder()
                .buildRound(firstLetter, color);

        holder.iv_letter.setImageDrawable(drawable);
        holder.tv_Name.setText(students.get(position).getName());
        holder.tv_Id.setText(students.get(position).getId());
        holder.postion = position;
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        ImageView iv_letter;
        TextView tv_Name;
        TextView tv_Id;
        int postion;


        StudentViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv_item);
            iv_letter = (ImageView) itemView.findViewById(R.id.iv_letter);
            tv_Name = (TextView)itemView.findViewById(R.id.tv_name);
            tv_Id = (TextView)itemView.findViewById(R.id.tv_id);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString(Utils.NAME, students.get(postion).getName());
                    bundle.putString(Utils.ID, students.get(postion).getId());
                    bundle.putString(Utils.COURSE_CODE, courseCode);
                    bundle.putString(Utils.COURSE_NAME, courseName);

                    Intent intent = new Intent(context, ViewIndividualStudentAnalytics.class);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });

        }
    }
}
