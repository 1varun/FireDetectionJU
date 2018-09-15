package com.example.varun.firedetectionju;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Math;
import java.util.Scanner;

public class AnalyseFire {

    String pathname = "/storage/emulated/0/Android/data/com.example.varun.firedetectionju/files/FIRE_SAMPLE.jpg";

    public int fireCheck(){

        Mat imgRGB = new Mat(7,7, CvType.CV_8UC1);
        Log.d("MyTAG", "test1");
        //Mat imgRGB = new Mat();
        Mat inputFrame = Imgcodecs.imread(pathname, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        Log.d("MyTAG", "test2");
        Imgproc.cvtColor(inputFrame, imgRGB, Imgproc.COLOR_RGBA2RGB);
        Log.d("MyTAG", "test3");
        int width = imgRGB.rows();
        int height = imgRGB.cols();
        Log.d("MyTAG", Integer.toString(width));
        Log.d("MyTAG", Integer.toString(height));
        Mat imgYCbCr = new Mat();
        Imgproc.cvtColor(imgRGB, imgYCbCr, Imgproc.COLOR_RGB2YCrCb);
        double rMean,gMean,bMean;
        double rSum = 0.0,gSum = 0.0,bSum = 0.0;

        double[] rgb;
        double[] ycbcr;
        double yMean,cbMean,crMean;
        double ySum = 0.0,cbSum = 0.0,crSum = 0.0;

        for(int y = 0;y<height;y++) {
            for(int x = 0;x<width;x++) {
                rgb = imgRGB.get(x, y);
                double rValue = rgb[0];
                double gValue = rgb[1];
                double bValue = rgb[2];
                ycbcr =imgYCbCr.get(x, y);
                double yValue = ycbcr[0];
                double crValue = ycbcr[1];
                double cbValue = ycbcr[2];
                rSum += rValue;
                gSum += gValue;
                bSum += bValue;
                ySum += yValue;
                cbSum += cbValue;
                crSum += crValue;
            }
        }
        Log.d("MyTAG", "test4");

        int totalPixels = width*height;

        rMean = (double)(rSum)/totalPixels;
        gMean = (double)(gSum)/totalPixels;
        bMean = (double)(bSum)/totalPixels;

        yMean = (double)(ySum)/totalPixels;
        cbMean = (double)(cbSum)/totalPixels;
        crMean = (double)(crSum)/totalPixels;

        boolean rgbFlag = false;
        boolean yCbCrFlag = false;

        for(int y = 0;y<height;y++) {
            for(int x = 0;x<width;x++) {
                rgb = imgRGB.get(x, y);
                double rValue = rgb[0];
                double gValue = rgb[1];
                double bValue = rgb[2];
                ycbcr =imgYCbCr.get(x, y);
                double yValue = ycbcr[0];
                double crValue = ycbcr[1];
                double cbValue = ycbcr[2];
                if((rValue > 225.0 && gValue > 100.0 && bValue < 140.0) &&
                        (rValue > gValue && gValue > bValue)
                        && (rValue > rMean && gValue > gMean && bValue < bMean)) {
                    rgbFlag = true;
                }
                if((yValue >= cbValue) && (crValue >= cbValue)
                        && (yValue >= yMean && cbValue <= cbMean && crValue >= crMean)
                        && (crValue - cbValue >= 30.0)
                        && (cbValue <= 120.0 && crValue >= 150.0)) {
                    yCbCrFlag = true;
                }
            }
        }
        Log.d("MyTAG", "test5");
        if(rgbFlag && yCbCrFlag) {
            Log.d("MyTAG", "fire");
            return 1;
        }
        else {
            Log.d("MyTAG", "not fire");
            return 0;
        }
    }
}
