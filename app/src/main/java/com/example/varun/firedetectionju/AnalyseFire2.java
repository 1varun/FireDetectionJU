package com.example.varun.firedetectionju;

import android.util.Log;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnalyseFire2 {

    String pathname = "/storage/emulated/0/Android/data/com.example.varun.firedetectionju/files/fireImage10.jpg";

    public int fireCheck(){
        //Mat imgHSV = inputFrame.rgba();
        Mat imgHSV = Imgcodecs.imread(pathname);
        int x1 = imgHSV.rows();
        int y1 = imgHSV.cols();
        imgHSV.convertTo(imgHSV, CvType.CV_32FC3, 1.0/255.0);
        Imgproc.cvtColor(imgHSV, imgHSV, Imgproc.COLOR_BGR2HSV);

        List<Mat> lHsv = new ArrayList<Mat>(3);
        Core.split(imgHSV, lHsv);
        Mat hChannel = lHsv.get(0);
        Mat sChannel = lHsv.get(1);
        Mat vChannel = lHsv.get(2);
        //double h_col_sum1 = sum_h(hChannel);
        //double s_col_sum1 = sum_rgb(sChannel);
        //double v_col_sum1 = sum_rgb(vChannel);
        //System.out.println("h_col_sum : "+h_col_sum1);
        //System.out.println("s_col_sum : "+s_col_sum1);
        //System.out.println("v_col_sum : "+v_col_sum1);


        Core.multiply(sChannel, new Scalar(2), sChannel);
        Mat newS = sChannel.clone();

        Scalar meanV = Core.mean(vChannel);
        Mat newV = vChannel_correction(vChannel, meanV.val[0]);


        //double h_col_sum = sum_h(hChannel);
        //double s_col_sum = sum_rgb(newS);
        //double v_col_sum = sum_rgb(newV);
        //double Y = h_col_sum/(x1*y1);
        //double C = s_col_sum/(x1*y1);
        //double B = v_col_sum/(x1*y1);

        //System.out.println("Rows : "+x1);
        //System.out.println("Cols : "+y1);
        //System.out.println("h_col_sum : "+h_col_sum);
        //System.out.println("s_col_sum : "+s_col_sum);
        //System.out.println("v_col_sum : "+v_col_sum);
        //System.out.println("Y : "+Y);
        //System.out.println("C : "+C);
        //System.out.println("B : "+B);

        List<Mat> listMat = Arrays.asList(hChannel, newS, newV);
        Core.merge(listMat, imgHSV);
        Imgproc.cvtColor(imgHSV, imgHSV, Imgproc.COLOR_HSV2RGB);

        List<Mat> klm = new ArrayList<Mat>(3);
        Core.split(imgHSV, klm);
        Mat k = klm.get(0);
        Mat l = klm.get(1);
        Mat m = klm.get(2);

        Scalar L01 = Core.sumElems(k);
        Scalar a01 = Core.sumElems(l);
        Scalar b01 = Core.sumElems(m);
        double RED = L01.val[0]/(x1*y1);
        double GREEN = a01.val[0]/(x1*y1);
        double BLUE = b01.val[0]/(x1*y1);

        //System.out.println("L01 : "+L01);
        //System.out.println("a01 : "+a01);
        //System.out.println("b01 : "+b01);
        //System.out.println("RED : "+RED);
        //System.out.println("GREEN : "+GREEN);
        //System.out.println("BLUE : "+BLUE);
        Log.d("MyTAG", "L01 : "+L01);
        Log.d("MyTAG", "a01 : "+a01);
        Log.d("MyTAG", "b01 : "+b01);
        Log.d("MyTAG", "RED : "+RED);
        Log.d("MyTAG", "GREEN : "+GREEN);
        Log.d("MyTAG", "BLUE : "+BLUE);

        Mat allBlack = Mat.zeros(x1, y1, CvType.CV_32FC1);
        List<Mat> temp_r = Arrays.asList(k, allBlack, allBlack);
        List<Mat> temp_g = Arrays.asList(allBlack, l, allBlack);
        List<Mat> temp_b = Arrays.asList(allBlack, allBlack, m);
        Mat red = new Mat();
        Mat green = new Mat();
        Mat blue = new Mat();
        Core.merge(temp_r, red);
        Core.merge(temp_g, green);
        Core.merge(temp_b, blue);

        //red.convertTo(red, CvType.CV_8UC3);
        //green.convertTo(green, CvType.CV_8UC3);
        //blue.convertTo(blue, CvType.CV_8UC3);

        Mat a1 = Mat.zeros(x1, y1, CvType.CV_8UC1);
        Mat b1 = Mat.zeros(x1, y1, CvType.CV_8UC1);
        Mat g = Mat.zeros(x1, y1, CvType.CV_8UC1);
        Mat a2 = Mat.zeros(x1, y1, CvType.CV_8UC1);
        //Mat b2 = Mat.zeros(x1, y1, CvType.CV_8UC1);
        Mat h = Mat.zeros(x1, y1, CvType.CV_8UC1);

        for(int row=0; row<x1; row++){
            for(int col=0; col<y1; col++) {
                if(k.get(row, col)[0] >= l.get(row, col)[0])
                    a1.put(row, col, 1);
                else
                    a1.put(row, col, 0);

                if((k.get(row, col)[0] >= RED) && (l.get(row, col)[0] >= GREEN) && (m.get(row, col)[0] >= BLUE))
                    b1.put(row, col, 1);
                else
                    b1.put(row, col, 0);

                if((newS.get(row, col)[0] <= 0.38) && (newV.get(row, col)[0] >= 1))
                    g.put(row, col, 1);
                else
                    g.put(row, col, 0);

                if((g.get(row, col)[0]!=0.0) && (a1.get(row, col)[0]!=0.0) && (b1.get(row, col)[0]==1)) {
                    h.put(row, col, 1);
                    //newV.put(row, col, 1);
                    a2.put(row, col, row);
                    //b2.put(row, col, col);
                }
                else {
                    h.put(row, col, 0);
                    //newV.put(row, col, 0);
                }

            }
        }
        //System.out.println("test3");
        Log.d("MyTAG", "test3");

//        Mat final_img = morph(imFill(newV));
//        Mat x0 = Mat.zeros(x1, y1, CvType.CV_8UC1);
//        Mat y0 = Mat.zeros(x1, y1, CvType.CV_8UC1);
//        Mat h1 = Mat.zeros(x1, y1, CvType.CV_8UC1);
//        for(int row=0; row<x1; row++){
//            for(int col=0; col<y1; col++) {
//                if(final_img.get(row, col)[0]==1){
//                    h1.put(row, col, 1);
//                    x0.put(row, col, row);
//                    y0.put(row, col, col);
//                }
//                else
//                    h1.put(row, col, 0);
//            }
//        }


        Scalar h_value = Core.sumElems(h);
        Scalar a2_value = Core.sumElems(a2);
        //System.out.println(h_value);
        //System.out.println(a2_value);
        Log.d("MyTAG", "h_value :"+h_value);
        Log.d("MyTAG", "a2_value :"+a2_value);


//        double h1_value = sum_rgb(h1);
//        double x_c0 = a2_value/h_value;
//        double b2_value = sum_rgb(b2);
//        double y_co = b2_value/h_value;
//        double x0_value = sum_rgb(x0);
//        double x_c1 = x0_value/h1_value;
//        double y0_value = sum_rgb(y0);
//        double y_c1 = y0_value/h1_value;
//        double ch=(h - a3)/100;


        if (h_value.val[0] > 300) {
            //count = count + 1;
            Log.d("MyTAG", "FIRE");
            //System.out.println("FIRE");

            //if(a2_value.val[0] < h_value.val[0]) {
                //System.out.println("FIRE GROWING");
                //Log.d("MyTAG", "FIRE GROWING");
                //count1=count1+1;
                //return 10;
            //}
            return 1;
        }
        else {
            Log.d("MyTAG", "FIRE NOT DETECTED");
            //System.out.println("FIRE NOT DETECTED");
            return 0;
        }
    }

    private static double sum_rgb (Mat matArray) {
        double sum_x = 0.0;
        double sum[] = new double[matArray.cols()];
        for(int col=0; col<matArray.cols(); col++){
            for(int row=0; row<matArray.rows(); row++) {
                sum_x += matArray.get(row, col)[0];
            }
            sum[col] = sum_x;
            sum_x=0;
        }

        for(int col=0; col<matArray.cols(); col++)
            sum_x += sum[col];
        return sum_x;
    }

    private static double sum_h (Mat matArray) {
        double sum_x = 0.0;
        double sum[] = new double[matArray.cols()];
        for(int col=0; col<matArray.cols(); col++){
            for(int row=0; row<matArray.rows(); row++) {
                sum_x += matArray.get(row, col)[0]/360;
            }
            sum[col] = sum_x;
            sum_x=0;
        }

        for(int col=0; col<matArray.cols(); col++)
            sum_x += sum[col];
        return sum_x;
    }

    private static Mat vChannel_correction (Mat matArray, double mean) {
        Mat return_mat = matArray.clone();
        for(int col=0; col<matArray.cols(); col++){
            for(int row=0; row<matArray.rows(); row++) {
                double[] data = matArray.get(row, col);
                data[0] = 1.5*data[0] - 0.5*mean;
                return_mat.put(row, col, data);
            }
        }
        return return_mat;
    }

    private Mat imFill(Mat input_mat) {
        Mat im_th = new Mat();
        Imgproc.threshold(input_mat, im_th, 220 , 255 , Imgproc.THRESH_BINARY_INV);
        Mat im_floodfill = im_th.clone();
        Point flood=new Point(0,0);
        Mat mask = Mat.zeros(new Size(input_mat.width() + 2, input_mat.height() + 2), CvType.CV_8UC1);
        Imgproc.floodFill(im_floodfill, mask, flood, Scalar.all(255));
        Mat im_floodfill_inv = new Mat();
        Core.bitwise_not(im_floodfill, im_floodfill_inv);
        Mat im_out = new Mat();
        List<Mat> imfill = Arrays.asList(im_th, im_floodfill_inv);
        Core.merge(imfill, im_out);
        return im_out;
    }

    private Mat morph(Mat input_mat) {
        double erosion_size = 0.1;
        //double dilation_size = 0.1;
        Mat final_img = new Mat();
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(2*erosion_size + 1, 2*erosion_size+1));
        Imgproc.erode(input_mat, final_img, element);
        Core.bitwise_not(final_img, final_img);
        return final_img;
    }
}
