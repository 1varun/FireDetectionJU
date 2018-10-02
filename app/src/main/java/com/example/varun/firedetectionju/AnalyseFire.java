package com.example.varun.firedetectionju;

import android.util.Log;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;

public class AnalyseFire {

    String pathname_1 = "/storage/emulated/0/Android/data/com.example.varun.firedetectionju/files/FIRE_SAMPLE_1.jpg";
    String pathname_2 = "/storage/emulated/0/Android/data/com.example.varun.firedetectionju/files/FIRE_SAMPLE_2.jpg";

    public int fireCheck() {
        Log.d("MyTAG1", "Inside Analyze Fire Class");

        double[] firstCall = RGBnYCrCb(pathname_1, 1);
        Log.d("MyTAG1", "X1_Centroid: "+Double.toString(firstCall[1]));
        Log.d("MyTAG1", "Y1_Centroid: "+Double.toString(firstCall[2]));
        if (firstCall[0] == 1){
            double[] secondCall = RGBnYCrCb(pathname_2, 2);
            Log.d("MyTAG1", "X2_Centroid: "+Double.toString(secondCall[1]));
            Log.d("MyTAG1", "Y2_Centroid: "+Double.toString(secondCall[2]));
            if ((secondCall[0]==1) && (Math.abs(firstCall[1]-secondCall[1]) > 2) && (Math.abs(firstCall[2]-secondCall[2]) > 2))
                return 1;
            else return 0;
        }
        else
            return 0;
    }

    public double[] RGBnYCrCb(String pathname, int countOfCall) {
        Log.d("MyTAG1", "Inside RGBnYCrCb method.");
        Mat imgYCbCr = new Mat();
        Mat imgRGB = Imgcodecs.imread(pathname);
        Imgproc.cvtColor(imgRGB, imgYCbCr, Imgproc.COLOR_BGR2YCrCb);
        int width = imgRGB.rows();
        int height = imgRGB.cols();
        if (countOfCall == 1) {
            Log.d("MyTAG1", "Width: " + Integer.toString(width));
            Log.d("MyTAG1", "Height: " + Integer.toString(height));
        }

        List<Mat> lRgb = new ArrayList<>(3);
        Core.split(imgRGB, lRgb);
        Mat rChannel = lRgb.get(2);
        Mat gChannel = lRgb.get(1);
        Mat bChannel = lRgb.get(0);
        List<Mat> lycbcr = new ArrayList<>(3);
        Core.split(imgYCbCr, lycbcr);
        Mat yChannel = lycbcr.get(0);
        Mat crChannel = lycbcr.get(1);
        Mat cbChannel = lycbcr.get(2);

        int totalPixels = width*height;

        double rMean,gMean,bMean;
        Scalar rSum = Core.sumElems(rChannel);
        Scalar gSum = Core.sumElems(gChannel);
        Scalar bSum = Core.sumElems(bChannel);
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

        if (countOfCall == 1) {
            Log.d("MyTAG1", "rSum: " + Double.toString(rSum.val[0]));
            Log.d("MyTAG1", "gSum: " + Double.toString(gSum.val[0]));
            Log.d("MyTAG1", "bSum: " + Double.toString(bSum.val[0]));
            Log.d("MyTAG1", "ySum: " + Double.toString(ySum.val[0]));
            Log.d("MyTAG1", "cbSum: " + Double.toString(cbSum.val[0]));
            Log.d("MyTAG1", "crSum: " + Double.toString(crSum.val[0]));
            Log.d("MyTAG1", "rMean: " + Double.toString(rMean));
            Log.d("MyTAG1", "gMean: " + Double.toString(gMean));
            Log.d("MyTAG1", "bMean: " + Double.toString(bMean));
            Log.d("MyTAG1", "yMean: " + Double.toString(yMean));
            Log.d("MyTAG1", "cbMean: " + Double.toString(cbMean));
            Log.d("MyTAG1", "crMean: " + Double.toString(crMean));
        }

        Core.subtract(bChannel, new Scalar(2), bChannel);
        Core.subtract(gChannel, new Scalar(1), gChannel);
        Mat r_mat = new Mat();
        Mat g_mat = new Mat();
        Mat b_mat = new Mat();
        Core.subtract(gChannel, bChannel, g_mat);
        Core.subtract(rChannel, gChannel, r_mat);
        Imgproc.threshold(g_mat, g_mat, 0, 1, Imgproc.THRESH_BINARY);
        Imgproc.threshold(r_mat, r_mat, 0, 1, Imgproc.THRESH_BINARY);
        Mat r_g_b_rule = new Mat();
        Core.bitwise_and(g_mat, r_mat, r_g_b_rule);
        Imgproc.threshold(rChannel, r_mat, 180, 1, Imgproc.THRESH_BINARY);
        Imgproc.threshold(gChannel, g_mat, 100, 1, Imgproc.THRESH_BINARY);
        Imgproc.threshold(bChannel, b_mat, 255, 1, Imgproc.THRESH_BINARY_INV);
        Core.add(r_mat, g_mat, r_mat);
        Core.add(r_mat, b_mat, r_mat);
        Imgproc.threshold(r_mat, r_mat, 2, 1, Imgproc.THRESH_BINARY);
        Core.bitwise_and(r_mat, r_g_b_rule, r_mat);

        Core.subtract(cbChannel, new Scalar(1), cbChannel);
        Mat y_mat = new Mat();
        Mat cr_mat = new Mat();
        Mat cb_mat = new Mat();
        Core.subtract(yChannel, cbChannel, y_mat);
        Core.add(cbChannel, new Scalar(30), cbChannel);
        Core.subtract(crChannel, cbChannel, cr_mat);
        Imgproc.threshold(y_mat, y_mat, 0, 1, Imgproc.THRESH_BINARY);
        Imgproc.threshold(cr_mat, cr_mat, 0, 1, Imgproc.THRESH_BINARY);
        Mat y_cr_cb_rule = new Mat();
        Core.bitwise_and(y_mat, cr_mat, y_cr_cb_rule);
        Imgproc.threshold(yChannel, y_mat, yMean, 1, Imgproc.THRESH_BINARY);
        Imgproc.threshold(crChannel, cr_mat, crMean, 1, Imgproc.THRESH_BINARY);
        Imgproc.threshold(cbChannel, cb_mat, cbMean, 1, Imgproc.THRESH_BINARY_INV);
        Core.add(y_mat, cr_mat, y_mat);
        Core.add(y_mat, cb_mat, y_mat);
        Imgproc.threshold(y_mat, y_mat, 2, 1, Imgproc.THRESH_BINARY);
        Core.bitwise_and(y_mat, y_cr_cb_rule, y_mat);
        Mat final_image = new Mat();
        Core.bitwise_and(y_mat, r_mat, final_image);
        Scalar pixelCount = Core.sumElems(final_image);
        Imgproc.threshold(final_image, final_image, 0, 255, Imgproc.THRESH_BINARY);

        Moments moments = Imgproc.moments(final_image);
        Point centroid = new Point();
        centroid.x = moments.get_m10() / moments.get_m00();
        centroid.y = moments.get_m01() / moments.get_m00();

        if (countOfCall == 1) {
            Imgproc.threshold(r_mat, r_mat, 0, 255, Imgproc.THRESH_BINARY);
            Imgproc.threshold(y_mat, y_mat, 0, 255, Imgproc.THRESH_BINARY);
            Imgcodecs.imwrite("/storage/emulated/0/Android/data/com.example.varun.firedetectionju/files/RGB_output.jpg", r_mat);
            Imgcodecs.imwrite("/storage/emulated/0/Android/data/com.example.varun.firedetectionju/files/YCrCb_output.jpg", y_mat);
            Imgcodecs.imwrite("/storage/emulated/0/Android/data/com.example.varun.firedetectionju/files/Final_Output.jpg", final_image);
        }

        if(pixelCount.val[0]>100) {
            if (countOfCall == 1)
                Log.d("MyTAG1", "Fire");
            double return_value[] = {1, centroid.x, centroid.y};
            return return_value;
        }
        else {
            if (countOfCall == 1)
                Log.d("MyTAG1", "No Fire");
            double return_value[] = {0, centroid.x, centroid.y};
            return return_value;
        }
    }
}