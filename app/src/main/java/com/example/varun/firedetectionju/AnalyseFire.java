package com.example.varun.firedetectionju;

import java.io.File;

public class AnalyseFire {

    public int fireCheck(){
        File imgFile = new  File("/storage/emulated/0/Android/data/com.example.varun.firedetectionju/files/FIRE_SAMPLE.jpg");
        if(imgFile.exists()){
            return 1;
        }
        else{
            return 0;
        }
    }
}
