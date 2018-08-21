package com.example.varun.firedetectionju;

import java.io.File;

public class AnalyseFire {

    String pathname = "/storage/emulated/0/Android/data/com.example.varun.firedetectionju/files/FIRE_SAMPLE.jpg";

    public int fireCheck(){
        File imgFile = new  File(pathname);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(imgFile.exists()){
            return 1;
        }
        else{
            return 0;
        }
    }
}
