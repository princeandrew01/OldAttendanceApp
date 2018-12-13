package com.jevon.studentrollrecorder.service;

import android.os.Binder;

/**
 * Binder class required to be able to bind the service to an activity
 */


public class IdServiceBinder extends Binder {
    private IdCheckService idCheckService;

    public IdServiceBinder(IdCheckService idCheckService){
        this.idCheckService = idCheckService;
    }

    public IdCheckService getService(){
        return this.idCheckService;
    }
}
