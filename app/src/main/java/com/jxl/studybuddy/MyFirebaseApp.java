package com.jxl.studybuddy;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Xavier on 26/09/2017.
 */

public class MyFirebaseApp extends android.app.Application{

    @Override
    public void onCreate(){
        super.onCreate();
        //Enables offline persistence.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
