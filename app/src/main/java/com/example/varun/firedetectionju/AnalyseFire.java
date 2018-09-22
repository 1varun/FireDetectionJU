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

    String pathname = "/storage/emulated/0/Android/data/com.example.varun.firedetectionju/files/FIRE_SAMPLE.jpg";

    public int fireCheck() {
        Log.d("MyTAG", "Inside Analyze Fire Class");
        Mat imgYCbCr = new Mat();
        Mat imgRGB = Imgcodecs.imread(pathname);
        Imgproc.cvtColor(imgRGB, imgYCbCr, Imgproc.COLOR_BGR2YCrCb);
        int width = imgRGB.rows();
        int height = imgRGB.cols();
        Log.d("MyTAG", "Width: "+Integer.toString(width));
        Log.d("MyTAG", "Height: "+Integer.toString(height));

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

        Log.d("MyTAG", Double.toString(rSum.val[0]));
        Log.d("MyTAG", Double.toString(gSum.val[0]));
        Log.d("MyTAG", Double.toString(bSum.val[0]));
        Log.d("MyTAG", Double.toString(ySum.val[0]));
        Log.d("MyTAG", Double.toString(cbSum.val[0]));
        Log.d("MyTAG", Double.toString(crSum.val[0]));
        Log.d("MyTAG", Double.toString(rMean));
        Log.d("MyTAG", Double.toString(gMean));
        Log.d("MyTAG", Double.toString(bMean));
        Log.d("MyTAG", Double.toString(yMean));
        Log.d("MyTAG", Double.toString(cbMean));
        Log.d("MyTAG", Double.toString(crMean));


        double[] rgb;
        double[] ycbcr;
        boolean rgbFlag = false;
        boolean yCbCrFlag = false;


        Log.d("MyTAG", "Entering Loop");
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
                        (rValue > rMean && gValue > gMean && bValue < bMean))
                    rgbFlag = true;

                if((yValue >= cbValue) && (crValue >= cbValue)
                        && (yValue >= yMean && cbValue <= cbMean && crValue >= crMean)
                        && (crValue - cbValue >= 30.0)
                        && (cbValue <= 120.0 && crValue >= 150.0)) {
                    yCbCrFlag = true;
                }
            }
        }
        Log.d("MyTAG", "Loop Over");
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