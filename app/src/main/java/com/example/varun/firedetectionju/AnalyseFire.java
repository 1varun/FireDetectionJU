package com.example.varun.firedetectionju;

import android.util.Log;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

public class AnalyseFire {

    String pathname = "/storage/emulated/0/Android/data/com.example.varun.firedetectionju/files/FIRE_SAMPLE_1.jpg";

    public int fireCheck() {
        Log.d("MyTAG1", "Inside Analyze Fire Class");
        Mat imgYCbCr = new Mat();
        Mat imgRGB = Imgcodecs.imread(pathname);
        Imgproc.cvtColor(imgRGB, imgYCbCr, Imgproc.COLOR_BGR2YCrCb);
        int width = imgRGB.rows();
        int height = imgRGB.cols();
        Log.d("MyTAG1", "Width: "+Integer.toString(width));
        Log.d("MyTAG1", "Height: "+Integer.toString(height));

        List<Mat> lRgb = new ArrayList<>(3);
        Core.split(imgRGB, lRgb);
        Mat rChannel = lRgb.get(0);
        Mat gChannel = lRgb.get(1);
        Mat bChannel = lRgb.get(2);
        List<Mat> lycbcr = new ArrayList<>(3);
        Core.split(imgYCbCr, lycbcr);
        Mat yChannel = lycbcr.get(0);
        Mat cbChannel = lycbcr.get(1);
        Mat crChannel = lycbcr.get(2);

        int totalPixels = width*height;

        double rMean,gMean,bMean;
        Scalar bSum = Core.sumElems(rChannel);
        Scalar gSum = Core.sumElems(gChannel);
        Scalar rSum = Core.sumElems(bChannel);
        rMean = (rSum.val[0])/totalPixels;
        gMean = (gSum.val[0])/totalPixels;
        bMean = (bSum.val[0])/totalPixels;

        double yMean,cbMean,crMean;
        Scalar ySum = Core.sumElems(yChannel);
        Scalar cbSum = Core.sumElems(cbChannel);
        Scalar crSum = Core.sumElems(crChannel);
        yMean = (ySum.val[0])/totalPixels;
        cbMean =(cbSum.val[0])/totalPixels;
        crMean =(crSum.val[0])/totalPixels;

        Log.d("MyTAG1", "rSum: "+Double.toString(rSum.val[0]));
        Log.d("MyTAG1", "gSum: "+Double.toString(gSum.val[0]));
        Log.d("MyTAG1", "bSum: "+Double.toString(bSum.val[0]));
        Log.d("MyTAG1", "ySum: "+Double.toString(ySum.val[0]));
        Log.d("MyTAG1", "cbSum: "+Double.toString(cbSum.val[0]));
        Log.d("MyTAG1", "crSum: "+Double.toString(crSum.val[0]));
        Log.d("MyTAG1", "rMean: "+Double.toString(rMean));
        Log.d("MyTAG1", "gMean: "+Double.toString(gMean));
        Log.d("MyTAG1", "bMean: "+Double.toString(bMean));
        Log.d("MyTAG1", "yMean: "+Double.toString(yMean));
        Log.d("MyTAG1", "cbMean: "+Double.toString(cbMean));
        Log.d("MyTAG1", "crMean: "+Double.toString(crMean));


        double[] rgb;
        double[] ycbcr;
        boolean rgbFlag = false;
        boolean yCbCrFlag = false;
        int pixelCount = 0;


        Log.d("MyTAG1", "Entering Loop");
        for(int y = 0;y<height;y++) {
            //Log.d("MyTAG", Integer.toString(y));
            for(int x = 0;x<width;x++) {
                rgb = imgRGB.get(x, y);
                double rValue = rgb[2];
                double gValue = rgb[1];
                double bValue = rgb[0];
                ycbcr =imgYCbCr.get(x, y);
                double yValue = ycbcr[0];
                double crValue = ycbcr[1];
                double cbValue = ycbcr[2];

                if ((rValue > gValue && gValue > bValue) &&
                        (rValue > rMean && gValue > gMean && bValue < bMean)) {
                    rgbFlag = true;
                    pixelCount++;
                }

                if((yValue >= cbValue) && (crValue >= cbValue)
                        && (yValue >= yMean && cbValue <= cbMean && crValue >= crMean)
                        && (crValue - cbValue >= 30.0)
                        && (cbValue <= 120.0 && crValue >= 150.0)) {
                    yCbCrFlag = true;
                    pixelCount++;
                }
            }
        }
        Log.d("MyTAG1", "Loop Over");
        if(rgbFlag && yCbCrFlag && pixelCount > 10000) {
            Log.d("MyTAG1", "fire");
            return 1;
        }
        else {
            Log.d("MyTAG1", "not fire");
            return 0;
        }
    }
}