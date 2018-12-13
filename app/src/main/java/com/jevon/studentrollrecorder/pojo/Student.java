package com.jevon.studentrollrecorder.pojo;

/**
 * Created by jevon on 09-Apr-16.
 */
public class Student {
    private String id;
    private String name;

    public Student(){}

    public Student(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name + " - " + this.id;
    }
}
